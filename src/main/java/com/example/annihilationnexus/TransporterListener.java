package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.UUID;

public class TransporterListener implements Listener {

    private final AnnihilationNexus plugin;

    public TransporterListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String clazz = plugin.getPlayerClassManager().getPlayerClass(player.getUniqueId());
        if (clazz == null || !clazz.equalsIgnoreCase("transporter")) return;

        TransporterAbility ability = plugin.getPlayerClassManager().getTransporterAbility(player.getUniqueId());
        if (ability == null) return;

        if (plugin.isTransporterItem(player.getInventory().getItemInMainHand())) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block clicked = event.getClickedBlock();
                if (clicked != null) {
                    ability.handlePortalCreation(player, clicked);
                    event.setCancelled(true);
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clicked = event.getClickedBlock();
            if (clicked != null && TransporterAbility.isPortalBlock(clicked)) {
                UUID owner = ability.getPortalOwner(clicked);
                if (owner != null && owner.equals(player.getUniqueId())) {
                    ability.destroyPortal(clicked);
                    ability.resetPortalMakerItemName(player); // Reset item name
                    player.sendMessage(ChatColor.GREEN + "Your Transporter portal has been destroyed.");
                } else {
                    player.sendMessage(ChatColor.RED + "You can only destroy your own Transporter portals!");
                }
                event.setCancelled(true);
            }
        }
    }

    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        Block below = player.getLocation().getBlock().getRelative(0, -1, 0);
        if (!TransporterAbility.isPortalBlock(below)) return;

        UUID ownerUUID = UUID.fromString(below.getMetadata("TransporterPortalOwner").get(0).asString());
        TransporterAbility ability = plugin.getPlayerClassManager().getTransporterAbility(ownerUUID);

        if (ability == null) {
            // Destroy both portals if the ability instance is gone
            org.bukkit.Location linkedLocation = TransporterAbility.getLinkedPortalLocation(plugin, below);
            if (linkedLocation != null) {
                TransporterAbility.forceDestroyPortal(plugin, linkedLocation.getBlock());
            }
            TransporterAbility.forceDestroyPortal(plugin, below);
            return;
        }

        if (event.isSneaking()) {
            ability.startTeleportTask(player, below);
        } else {
            ability.cancelTeleportTask(player);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!TransporterAbility.isPortalBlock(block)) return;

        UUID owner = UUID.fromString(block.getMetadata("TransporterPortalOwner").get(0).asString());
        TransporterAbility ability = plugin.getPlayerClassManager().getTransporterAbility(owner);

        if (ability == null) {
            // Destroy both portals if the ability instance is gone
            org.bukkit.Location linkedLocation = TransporterAbility.getLinkedPortalLocation(plugin, block);
            if (linkedLocation != null) {
                TransporterAbility.forceDestroyPortal(plugin, linkedLocation.getBlock());
            }
            TransporterAbility.forceDestroyPortal(plugin, block);
            plugin.getServer().broadcastMessage(ChatColor.GRAY + "An old Transporter portal was destroyed.");
            return;
        }

        // Allow anyone to break the portal, but only the owner gets a specific message.
        ability.destroyPortal(block);
        ability.resetPortalMakerItemName(event.getPlayer()); // Reset item name
        if (owner.equals(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "Your Transporter portal has been destroyed.");
        } else {
            event.getPlayer().sendMessage(ChatColor.YELLOW + "You destroyed someone else's Transporter portal.");
        }
        event.setCancelled(true); // Prevent the block from dropping
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String clazz = plugin.getPlayerClassManager().getPlayerClass(player.getUniqueId());
        if (clazz == null || !clazz.equalsIgnoreCase("transporter")) return;

        // ★ 修正：死後にポータルを削除しない
        // plugin.getPlayerClassManager().addPlayerToPostDeathPortalCooldown(player.getUniqueId());
    }
}
