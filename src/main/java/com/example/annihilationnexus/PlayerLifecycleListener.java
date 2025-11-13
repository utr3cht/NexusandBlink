package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.ChatColor;

public class PlayerLifecycleListener implements Listener {

    private final AnnihilationNexus plugin;
    private final ScoreboardManager scoreboardManager;
    private final PlayerClassManager playerClassManager;
    private final PlayerTeamManager playerTeamManager;

    public PlayerLifecycleListener(AnnihilationNexus plugin, ScoreboardManager scoreboardManager, PlayerClassManager playerClassManager, PlayerTeamManager playerTeamManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
        this.playerClassManager = playerClassManager;
        this.playerTeamManager = playerTeamManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scoreboardManager.updateScoreboard(player);
        playerClassManager.addPlayer(player);

        // Re-join team if previously selected
        String teamName = playerTeamManager.getPlayerTeam(player.getUniqueId());
        if (teamName != null && plugin.getNexusManager().getNexus(teamName) != null) {
            scoreboardManager.setPlayerTeam(player, teamName);
            player.sendMessage(ChatColor.GREEN + "You have automatically rejoined team " + teamName + ".");
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Remove player from post-death portal cooldown
        playerClassManager.removePlayerFromPostDeathPortalCooldown(player.getUniqueId());

        // A short delay to ensure the player is ready
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            String className = playerClassManager.getPlayerClass(player.getUniqueId());
            if (className != null) {
                playerClassManager.addPlayer(player); // This method handles giving items
            }
        }, 1L);
    }
}
