package com.example.annihilationnexus;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SpyListener implements Listener {

    private final AnnihilationNexus plugin;
    private final PlayerClassManager playerClassManager;

    public SpyListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
    }

    private boolean isSpy(Player player) {
        String className = playerClassManager.getPlayerClass(player.getUniqueId());
        return className != null && className.equalsIgnoreCase("spy");
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!isSpy(player)) return;

        SpyAbility spyAbility = playerClassManager.getSpyAbility(player.getUniqueId());
        if (spyAbility == null) return;

        if (event.isSneaking()) {
            spyAbility.startVanishTimer();
        } else {
            spyAbility.cancelVanishTimer();
            spyAbility.unVanish();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isSpy(player)) return;

        SpyAbility spyAbility = playerClassManager.getSpyAbility(player.getUniqueId());
        if (spyAbility == null || !spyAbility.isVanished()) return;

        Location vanishLocation = spyAbility.getVanishLocation();

        if (vanishLocation != null) {
            if (vanishLocation.getBlockX() != event.getTo().getBlockX() || 
                vanishLocation.getBlockY() != event.getTo().getBlockY() || 
                vanishLocation.getBlockZ() != event.getTo().getBlockZ()) {
                spyAbility.unVanish();
                if (player.isSneaking()) {
                    spyAbility.startVanishTimer();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isSpy(player)) return;

        SpyAbility spyAbility = playerClassManager.getSpyAbility(player.getUniqueId());
        if (spyAbility == null) return;

        if (plugin.isSpyItem(event.getItem()) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            spyAbility.flee();
            event.setCancelled(true);
            return;
        }

        if (spyAbility.isVanished()) {
            spyAbility.unVanish();
            if (player.isSneaking()) {
                spyAbility.startVanishTimer();
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player attacker = (Player) event.getDamager();
        Entity victim = event.getEntity();

        if (isSpy(attacker) && victim instanceof Player) {
            SpyAbility spyAbility = playerClassManager.getSpyAbility(attacker.getUniqueId());
            if (spyAbility != null && spyAbility.isVanished()) {
                spyAbility.unVanish();
                if (attacker.isSneaking()) {
                    spyAbility.startVanishTimer();
                }
            }

            Vector attackerDirection = attacker.getLocation().getDirection().normalize();
            Vector victimDirection = victim.getLocation().getDirection().normalize();

            if (attackerDirection.dot(victimDirection) > 0.5) {
                event.setDamage(event.getDamage() + 1.0);
            }
        }

        if (victim instanceof Player && isSpy((Player) victim)) {
            SpyAbility victimAbility = playerClassManager.getSpyAbility(victim.getUniqueId());
            if (victimAbility != null && victimAbility.isVanished()) {
                victimAbility.unVanish();
                if (((Player) victim).isSneaking()) {
                    victimAbility.startVanishTimer();
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(org.bukkit.event.entity.EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            // Vanish logic (only for Spy players)
            if (isSpy(player)) {
                SpyAbility spyAbility = playerClassManager.getSpyAbility(player.getUniqueId());
                if (spyAbility != null && spyAbility.isVanished()) {
                    spyAbility.unVanish();
                    if (player.isSneaking()) {
                        spyAbility.startVanishTimer();
                    }
                }
            }
            // Invisibility potion logic (for all players)
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        Player player = event.getPlayer();
        // Vanish logic (only for Spy players)
        if (isSpy(player)) {
            SpyAbility spyAbility = playerClassManager.getSpyAbility(player.getUniqueId());
            if (spyAbility != null && spyAbility.isVanished()) {
                spyAbility.unVanish();
                if (player.isSneaking()) {
                    spyAbility.startVanishTimer();
                }
            }
        }
        // Invisibility potion logic (for all players)
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (isSpy(onlinePlayer)) {
                SpyAbility spyAbility = playerClassManager.getSpyAbility(onlinePlayer.getUniqueId());
                if (spyAbility != null && spyAbility.isVanished()) {
                    joinedPlayer.hidePlayer(plugin, onlinePlayer);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isSpy(player)) {
            SpyAbility spyAbility = playerClassManager.getSpyAbility(player.getUniqueId());
            if (spyAbility != null) {
                spyAbility.unVanish();
            }
        }
    }
}
