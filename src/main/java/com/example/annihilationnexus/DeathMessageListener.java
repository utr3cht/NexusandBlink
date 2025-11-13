package com.example.annihilationnexus;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageListener implements Listener {

    private final PlayerClassManager playerClassManager;

    public DeathMessageListener(PlayerClassManager playerClassManager) {
        this.playerClassManager = playerClassManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        String victimClass = playerClassManager.getPlayerClass(victim.getUniqueId());
        String victimClassAbbr = getClassAbbreviation(victimClass);
        String victimDisplayName = victim.getDisplayName(); // Use display name to respect team colors

        String deathMessage;

        if (killer != null) {
            String killerClass = playerClassManager.getPlayerClass(killer.getUniqueId());
            String killerClassAbbr = getClassAbbreviation(killerClass);
            String killerDisplayName = killer.getDisplayName(); // Use display name to respect team colors

            deathMessage = String.format("%s(%s) was killed by %s(%s)",
                    victimDisplayName, victimClassAbbr, killerDisplayName, killerClassAbbr);
        } else {
            // Handle deaths not caused by another player (e.g., fall, lava)
            deathMessage = String.format("%s(%s) died", victimDisplayName, victimClassAbbr);
        }

        event.setDeathMessage(deathMessage);
    }

    private String getClassAbbreviation(String className) {
        if (className == null || className.isEmpty()) {
            return "N/A";
        }
        if (className.length() <= 3) {
            return className.toUpperCase();
        }
        return className.substring(0, 3).toUpperCase();
    }
}
