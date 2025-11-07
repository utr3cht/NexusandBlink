package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ScorpioListener implements Listener {

    private final AnnihilationNexus plugin;
    private final PlayerClassManager playerClassManager;

    public ScorpioListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());

        if (playerClass == null || !playerClass.equalsIgnoreCase("scorpio")) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !plugin.isScorpioItem(item)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            playerClassManager.getScorpioAbility(player.getUniqueId()).use(true);
            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            playerClassManager.getScorpioAbility(player.getUniqueId()).use(false);
            event.setCancelled(true);
        }
    }


}
