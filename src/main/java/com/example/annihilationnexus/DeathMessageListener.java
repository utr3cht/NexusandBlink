package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathMessageListener implements Listener {

    private final PlayerClassManager playerClassManager;
    private final PlayerTeamManager playerTeamManager;
    private final ScoreboardManager scoreboardManager;
    private final XpManager xpManager;
    private final NexusManager nexusManager;

    public DeathMessageListener(PlayerClassManager playerClassManager, PlayerTeamManager playerTeamManager,
            ScoreboardManager scoreboardManager, XpManager xpManager, NexusManager nexusManager) {
        this.playerClassManager = playerClassManager;
        this.playerTeamManager = playerTeamManager;
        this.scoreboardManager = scoreboardManager;
        this.xpManager = xpManager;
        this.nexusManager = nexusManager;
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

        String formattedVictimName = String.format("%s%s %s(%s)%s", victimTeamColor, victimDisplayName, victimTeamColor,
                victimClassAbbr,
                ChatColor.GRAY);

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

            String formattedKillerName = String.format("%s%s %s(%s)%s", killerTeamColor, killerDisplayName,
                    killerTeamColor,
                    killerClassAbbr, ChatColor.GRAY);

            // Determine kill type (Melee or Bow)
            boolean isBowKill = false;
            if (victim.getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
                if (damageEvent.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                    isBowKill = true;
                }
            }

            String action = isBowKill ? "shot" : "killed";
            deathMessage = String.format("%s %s %s", formattedKillerName, action, formattedVictimName);

            // XP Logic
            int xp = 1; // Default minimum

            AnnihilationNexus plugin = AnnihilationNexus.getPlugin(AnnihilationNexus.class);
            int baseXp = plugin.getPlayerKillXp();
            xp = baseXp;
            String reason = "Kill";

            // Check for bonuses and suffixes
            if (killerTeamName != null) {
                Nexus ownNexus = nexusManager.getNexus(killerTeamName);

                // 1. Honour (Destroyed Team)
                if (ownNexus != null && ownNexus.isDestroyed()) {
                    int avengerBonus = plugin.getAvengerBonusXp();
                    xp += avengerBonus;
                    reason += " + Avenger";
                    if (!isBowKill) {
                        deathMessage += " in honour of " + killerTeamColor + killerTeamName + ChatColor.GRAY;
                    }
                }
                // 2. Defending (Near Own Nexus)
                else if (ownNexus != null && ownNexus.getLocation().getWorld().equals(killer.getWorld())
                        && ownNexus.getLocation().distance(killer.getLocation()) <= 10) {
                    int defenseBonus = plugin.getDefenseBonusXp();
                    xp += defenseBonus;
                    reason += " + Defense";
                    if (!isBowKill) {
                        deathMessage += " defending " + killerTeamColor + killerTeamName + "'s Nexus" + ChatColor.GRAY;
                    }
                }
                // 3. Attacking (Near Enemy Nexus)
                else if (victimTeamName != null) {
                    Nexus enemyNexus = nexusManager.getNexus(victimTeamName);
                    if (enemyNexus != null && enemyNexus.getLocation().getWorld().equals(killer.getWorld())
                            && enemyNexus.getLocation().distance(killer.getLocation()) <= 10) {
                        int offenseBonus = plugin.getOffenseBonusXp();
                        xp += offenseBonus;
                        reason += " + Offense";
                        if (!isBowKill) {
                            deathMessage += " attacking " + victimTeamColor + victimTeamName + "'s Nexus"
                                    + ChatColor.GRAY;
                        }
                    }
                }
            }

            xpManager.giveXp(killer, xp, reason);
        } else {
            // Handle deaths not caused by another player (e.g., fall, lava)
            String originalMessage = event.getDeathMessage();
            if (originalMessage != null) {
                deathMessage = originalMessage.replace(victim.getName(), formattedVictimName);
            } else {
                deathMessage = String.format("%s died", formattedVictimName);
            }
        }

        event.setDeathMessage(deathMessage);
    }

    private String getClassAbbreviation(String className) {
        if (className == null || className.isEmpty()) {
            return "CIV";
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
