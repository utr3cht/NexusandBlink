package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final AnnihilationNexus plugin;
    private final ScoreboardManager scoreboardManager;

    public PlayerJoinListener(AnnihilationNexus plugin, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scoreboardManager.updateScoreboard(player);

        // Check for and activate persistent class
        PlayerClassManager classManager = plugin.getPlayerClassManager();
        String className = classManager.getPlayerClass(player.getUniqueId());
        if (className != null) {
            // Re-set the class to activate any abilities (like Dasher)
            classManager.setPlayerClass(player, className);
        }
    }
}