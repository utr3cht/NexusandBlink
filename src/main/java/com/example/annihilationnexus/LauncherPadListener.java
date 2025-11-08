package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LauncherPadListener implements Listener {

    private final AnnihilationNexus plugin;
    private final Set<UUID> launchedPlayers = new HashSet<>();

    public LauncherPadListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.STONE_PRESSURE_PLATE) {
            return;
        }

        Block blockBelow = block.getRelative(0, -1, 0);
        Material belowType = blockBelow.getType();

        if (belowType != Material.IRON_BLOCK && belowType != Material.DIAMOND_BLOCK &&
                belowType != Material.EMERALD_BLOCK && belowType != Material.GOLD_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Vector direction;
        double launchPower;
        Sound launchSound;

        if (belowType == Material.DIAMOND_BLOCK) {
            launchPower = plugin.getLauncherPadDiamondPower();
            launchSound = Sound.ENTITY_WITHER_SHOOT;
            direction = player.getLocation().getDirection();
            direction.setY(0).normalize();
        } else if (belowType == Material.IRON_BLOCK) {
            launchPower = plugin.getLauncherPadIronPower();
            launchSound = Sound.ENTITY_WITHER_SHOOT;
            direction = player.getLocation().getDirection();
            direction.setY(0).normalize();
        } else if (belowType == Material.EMERALD_BLOCK) {
            launchPower = plugin.getLauncherPadEmeraldPower();
            launchSound = Sound.ENTITY_SLIME_JUMP;
            direction = new Vector(0, 1, 0);
        } else if (belowType == Material.GOLD_BLOCK) {
            launchPower = plugin.getLauncherPadGoldPower();
            launchSound = Sound.ENTITY_SLIME_JUMP;
            direction = new Vector(0, 1, 0);
        } else {
            return; // Should not happen due to the check above, but as a safeguard.
        }

        player.getWorld().playSound(player.getLocation(), launchSound, 1, 1);
        player.setVelocity(direction.multiply(launchPower));

        launchedPlayers.add(player.getUniqueId()); // Mark player as launched
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (!launchedPlayers.contains(playerUUID)) {
            return;
        }

        // Check if the player has landed
        if (player.isOnGround()) {
            // Remove from launched players to prevent this from running again
            launchedPlayers.remove(playerUUID);
            
            // Add to the main no-fall set, which handles the 1-second grace period after landing
            plugin.getNoFall().add(playerUUID);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            // If the player was launched OR is in the post-landing grace period, cancel the damage.
            if (launchedPlayers.contains(player.getUniqueId()) || plugin.getNoFall().contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}
