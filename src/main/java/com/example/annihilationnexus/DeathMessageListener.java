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

    public DeathMessageListener(PlayerClassManager playerClassManager, PlayerTeamManager playerTeamManager, ScoreboardManager scoreboardManager) {
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
                    String victimDisplayName = victim.getDisplayName(); // Use display name to respect team colors
                    String victimTeamName = playerTeamManager.getPlayerTeam(victim.getUniqueId());
                    ChatColor victimTeamColor = ChatColor.WHITE; // Default color
                    if (victimTeamName != null) {
                        victimTeamColor = scoreboardManager.getTeamColor(victimTeamName);
                    }
        
                    String deathMessage;
        
                    if (killer != null) {
                        String killerClass = playerClassManager.getPlayerClass(killer.getUniqueId());
                        String killerClassAbbr = getClassAbbreviation(killerClass);
                        String killerDisplayName = killer.getDisplayName(); // Use display name to respect team colors
                        String killerTeamName = playerTeamManager.getPlayerTeam(killer.getUniqueId());
                        ChatColor killerTeamColor = ChatColor.WHITE; // Default color
                        if (killerTeamName != null) {
                            killerTeamColor = scoreboardManager.getTeamColor(killerTeamName);
                        }
        
                                    deathMessage = String.format("%s%s(%s)%s was killed by %s%s(%s)%s",
        
                                            victimDisplayName,
        
                                            victimTeamColor, victimClassAbbr, ChatColor.RESET,
        
                                            killerDisplayName,
        
                                            killerTeamColor, killerClassAbbr, ChatColor.RESET);        } else {
            // Handle deaths not caused by another player (e.g., fall, lava)
            deathMessage = String.format("%s (%s) died", victimDisplayName, victimClassAbbr);
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
