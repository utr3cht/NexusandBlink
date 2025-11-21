package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathMessageListener implements Listener {

    private final PlayerClassManager playerClassManager;
    private final PlayerTeamManager playerTeamManager;
    private final ScoreboardManager scoreboardManager;

    public DeathMessageListener(PlayerClassManager playerClassManager, PlayerTeamManager playerTeamManager,
            ScoreboardManager scoreboardManager) {
        this.playerClassManager = playerClassManager;
        this.playerTeamManager = playerTeamManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        String victimClass = playerClassManager.getPlayerClass(victim.getUniqueId());
        String victimClassAbbr = getClassAbbreviation(victimClass);
        String victimDisplayName = victim.getDisplayName();
        String victimTeamName = playerTeamManager.getPlayerTeam(victim.getUniqueId());
        ChatColor victimTeamColor = ChatColor.WHITE;
        if (victimTeamName != null) {
            victimTeamColor = scoreboardManager.getTeamColor(victimTeamName);
        }

        String formattedVictimName = String.format("%s%s (%s)%s", victimTeamColor, victimDisplayName, victimClassAbbr,
                ChatColor.RESET);

        String deathMessage;

        if (killer != null) {
            String killerClass = playerClassManager.getPlayerClass(killer.getUniqueId());
            String killerClassAbbr = getClassAbbreviation(killerClass);
            String killerDisplayName = killer.getDisplayName();
            String killerTeamName = playerTeamManager.getPlayerTeam(killer.getUniqueId());
            ChatColor killerTeamColor = ChatColor.WHITE;
            if (killerTeamName != null) {
                killerTeamColor = scoreboardManager.getTeamColor(killerTeamName);
            }

            String formattedKillerName = String.format("%s%s (%s)%s", killerTeamColor, killerDisplayName,
                    killerClassAbbr, ChatColor.RESET);

            deathMessage = String.format("%s was killed by %s", formattedVictimName, formattedKillerName);
        } else {
            // Handle deaths not caused by another player (e.g., fall, lava)
            // Use the original death message but replace the player's name with the
            // formatted one
            String originalMessage = event.getDeathMessage();
            if (originalMessage != null) {
                deathMessage = originalMessage.replace(victim.getName(), formattedVictimName);
            } else {
                // Fallback if message is null
                deathMessage = String.format("%s died", formattedVictimName);
            }
        }

        event.setDeathMessage(deathMessage);
    }

    private String getClassAbbreviation(String className) {
        if (className == null || className.isEmpty()) {
            return "N/A";
        }
        if (className.equalsIgnoreCase("Assassin")) {
            return "ASN";
        }
        if (className.length() <= 3) {
            return className.toUpperCase();
        }
        return className.substring(0, 3).toUpperCase();
    }
}
