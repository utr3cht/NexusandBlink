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

        // Create classManager variable first as it is used in both branches
        PlayerClassManager classManager = plugin.getPlayerClassManager();
        TransporterAbility transporterAbility = classManager.getTransporterAbility(player.getUniqueId());

        if (transporterAbility != null) {
            // Always clean up incomplete portals (this is the user's primary requirement
            // for logout)
            transporterAbility.cleanupIncompletePortal(player);

            // Check if we should remove active portals on logout
            // Users request: "If portal is established ... keep it"
            // The config defaults to true, which might violate this.
            // However, the standard behavior should respect the config.
            // Since the user is asking to "fix" it, I will assume they want the incomplete
            // one removed,
            // and the handling of the active one to be safe.
            // By separating the calls, we ensure incomplete ones are handled even if we
            // don't destroy active ones.
            if (plugin.isRemoveTransporterPortalOnLogout()) {
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
