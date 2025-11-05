package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerToggleSneakListener implements Listener {

    private final AnnihilationNexus plugin;

    public PlayerToggleSneakListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String playerClass = plugin.getPlayerClassManager().getPlayerClass(player.getUniqueId());

        if (playerClass != null && playerClass.equalsIgnoreCase("dasher")) {
            DasherAbility ability = plugin.getPlayerClassManager().getDasherAbility(player.getUniqueId());
            if (ability != null) {
                if (event.isSneaking()) {
                    ability.startVisualizer();
                } else {
                    ability.stopVisualizer();
                }
            }
        }
    }
}
