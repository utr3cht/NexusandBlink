package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final AnnihilationNexus plugin;
    private final ScoreboardManager scoreboardManager;

    public PlayerQuitListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.scoreboardManager = plugin.getScoreboardManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if we should remove portals on logout
        if (plugin.isRemoveTransporterPortalOnLogout()) {
            PlayerClassManager classManager = plugin.getPlayerClassManager();
            TransporterAbility transporterAbility = classManager.getTransporterAbility(player.getUniqueId());

            if (transporterAbility != null) {
                transporterAbility.destroyAllPortalsForPlayer(player.getUniqueId());
            }
        }

        // Also remove the player from the class manager's active abilities maps
        plugin.getPlayerClassManager().removePlayer(player.getUniqueId());

        // Update scoreboard for everyone to reflect new player count
        // Delay slightly to ensure player is actually removed from online players list
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            scoreboardManager.updateForAllPlayers();
        }, 1L);
    }
}
