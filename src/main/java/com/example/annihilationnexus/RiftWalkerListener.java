package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class RiftWalkerListener implements Listener {

    private final AnnihilationNexus plugin;
    private final PlayerClassManager playerClassManager;

    public RiftWalkerListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item == null || item.getType() != Material.BLAZE_ROD) {
                return;
            }

            String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
            if (playerClass != null && playerClass.equalsIgnoreCase("riftwalker")) {
                RiftWalkerAbility ability = playerClassManager.getRiftWalkerAbility(player.getUniqueId());
                if (ability != null) {
                    ability.useAbility(player);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_PURPLE + "RiftWalker: Select Target")) {
            return;
        }

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.PLAYER_HEAD) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();

        if (meta != null && meta.getOwningPlayer() != null && meta.getOwningPlayer().getPlayer() != null) {
            Player target = meta.getOwningPlayer().getPlayer();

            String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
            if (playerClass != null && playerClass.equalsIgnoreCase("riftwalker")) {
                RiftWalkerAbility ability = playerClassManager.getRiftWalkerAbility(player.getUniqueId());
                if (ability != null) {
                    player.closeInventory();
                    ability.startRift(player, target);
                }
            }
        }
    }
}
