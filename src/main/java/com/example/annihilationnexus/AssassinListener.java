package com.example.annihilationnexus;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AssassinListener implements Listener {

    private final PlayerClassManager playerClassManager;
    private final AnnihilationNexus plugin;

    public AssassinListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());

        if (playerClass == null || !playerClass.equalsIgnoreCase("assassin")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !plugin.isAssassinItem(item)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            playerClassManager.getAssassinAbility(player.getUniqueId()).leap();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());

        if (playerClass == null || !playerClass.equalsIgnoreCase("assassin")) {
            return;
        }

        if (playerClassManager.getAssassinAbility(player.getUniqueId()).isLeaping()) {
            if (event.getDamager() instanceof Arrow) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        String killerClass = playerClassManager.getPlayerClass(killer.getUniqueId());
        if (killerClass == null || !killerClass.equalsIgnoreCase("assassin")) {
            return;
        }

        playerClassManager.getAssassinAbility(killer.getUniqueId()).reduceCooldown();
        killer.sendMessage("Your Leap cooldown has been reduced by 8 seconds.");
    }
}
