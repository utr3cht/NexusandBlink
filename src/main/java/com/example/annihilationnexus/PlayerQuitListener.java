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

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getLogger().info("PlayerQuitEvent triggered for player: " + player.getName());
        PlayerClassManager classManager = plugin.getPlayerClassManager();
        TransporterAbility transporterAbility = classManager.getTransporterAbility(player.getUniqueId());

        if (transporterAbility != null) {
            plugin.getLogger().info("Player " + player.getName() + " is quitting. TransporterAbility found. Destroying their Transporter portals.");
            transporterAbility.destroyAllPortalsForPlayer(player.getUniqueId());
        } else {
            plugin.getLogger().info("Player " + player.getName() + " is quitting. No TransporterAbility found.");
        }
        // Also remove the player from the class manager's active abilities maps
        classManager.removePlayer(player.getUniqueId());
    }
}