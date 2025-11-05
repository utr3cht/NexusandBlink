package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final AnnihilationNexus plugin;

    public PlayerQuitListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerClassManager().removePlayer(player.getUniqueId());
    }
}
