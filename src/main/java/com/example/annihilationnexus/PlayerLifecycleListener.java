package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerLifecycleListener implements Listener {

    private final AnnihilationNexus plugin;
    private final ScoreboardManager scoreboardManager;

    public PlayerLifecycleListener(AnnihilationNexus plugin, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scoreboardManager.updateScoreboard(player);
        plugin.getPlayerClassManager().addPlayer(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Remove player from post-death portal cooldown
        plugin.getPlayerClassManager().removePlayerFromPostDeathPortalCooldown(player.getUniqueId());

        // A short delay to ensure the player is ready
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            String className = plugin.getPlayerClassManager().getPlayerClass(player.getUniqueId());
            if (className != null) {
                plugin.getPlayerClassManager().addPlayer(player); // This method handles giving items
            }
        }, 1L);
    }
}
