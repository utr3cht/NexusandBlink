package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Set;
import java.util.HashSet;

public class ScoreboardManager {

    private final AnnihilationNexus plugin;
    private final NexusManager nexusManager;
    private final Map<UUID, Boolean> scoreboardVisibility = new HashMap<>(); // Player UUID -> is visible?

    public ScoreboardManager(AnnihilationNexus plugin, NexusManager nexusManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
        loadScoreboardVisibility();
    }

    public void updateScoreboard(Player player) {
        if (!scoreboardVisibility.getOrDefault(player.getUniqueId(), true)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()); // Clear scoreboard
            return;
        }

        FileConfiguration config = plugin.getConfig();
        ConfigurationSection scoreboardConfig = config.getConfigurationSection("scoreboard");
        if (scoreboardConfig == null) {
            return; // Scoreboard not configured
        }

        Scoreboard scoreboard = player.getScoreboard();

        // If a new scoreboard is needed, back up existing team info
        Map<String, Set<String>> teamsBackup = null;
        if (scoreboard == null || scoreboard.getObjective("annihilation") == null) {
            if (scoreboard != null) {
                teamsBackup = new HashMap<>();
                for (Team team : scoreboard.getTeams()) {
                    teamsBackup.put(team.getName(), new HashSet<>(team.getEntries()));
                }
            }
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective = scoreboard.getObjective("annihilation");
        String title = ChatColor.translateAlternateColorCodes('&', scoreboardConfig.getString("title", "&e&lAnnihilation"));
        if (objective == null) {
            objective = scoreboard.registerNewObjective("annihilation", "dummy", title);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        } else {
            objective.setDisplayName(title);
        }

        // Restore teams from backup if a new scoreboard was created
        if (teamsBackup != null) {
            for (Map.Entry<String, Set<String>> entry : teamsBackup.entrySet()) {
                String teamName = entry.getKey();
                Set<String> members = entry.getValue();
                Team newTeam = scoreboard.registerNewTeam(teamName);
                configureTeam(newTeam, teamName); // This applies prefix, friendly fire settings, etc.
                for (String member : members) {
                    newTeam.addEntry(member);
                }
            }
        }
        
        // Clear old scores to prevent duplicates
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = scoreboardConfig.getStringList("lines");
        int score = lines.size();
        for (String line : lines) {
            line = ChatColor.translateAlternateColorCodes('&', line);
            line = line.replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()));

            if (line.equals("NEXUS_STATUS")) {
                for (Map.Entry<String, Nexus> entry : nexusManager.getAllNexuses().entrySet()) {
                    String teamName = entry.getKey();
                    Nexus nexus = entry.getValue();
                    ChatColor teamColor = getTeamColor(teamName);
                    String status = nexus.isDestroyed() ? ChatColor.RED + "DESTROYED" : ChatColor.GREEN + "" + nexus.getHealth();
                    objective.getScore(teamColor + teamName + ": " + status).setScore(score--);
                }
            } else {
                // To handle duplicate lines, add invisible color codes
                while (scoreboard.getEntries().contains(line)) {
                    line += ChatColor.RESET;
                }
                objective.getScore(line).setScore(score--);
            }
        }
        player.setScoreboard(scoreboard);
    }

    public void updateForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }

    public void toggleScoreboard(Player player) {
        UUID playerUUID = player.getUniqueId();
        boolean currentVisibility = scoreboardVisibility.getOrDefault(playerUUID, true);
        scoreboardVisibility.put(playerUUID, !currentVisibility);
        saveScoreboardVisibility();
        updateScoreboard(player); // Update immediately after toggle
        player.sendMessage(ChatColor.GREEN + "Scoreboard visibility toggled " + (!currentVisibility ? "on" : "off") + ".");
    }

    public ChatColor getTeamColor(String teamName) {
        // Placeholder: Implement actual team color logic here
        // For example, read from config or a team manager
        switch (teamName.toLowerCase()) {
            case "red": return ChatColor.RED;
            case "blue": return ChatColor.BLUE;
            case "green": return ChatColor.GREEN;
            case "yellow": return ChatColor.YELLOW;
            default: return ChatColor.WHITE;
        }
    }

    public void loadScoreboardVisibility() {
        File config = new File(plugin.getDataFolder(), "scoreboard_visibility.yml");
        if (!config.exists()) {
            return;
        }
        FileConfiguration visibilityConfig = YamlConfiguration.loadConfiguration(config);
        for (String uuidString : visibilityConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                scoreboardVisibility.put(uuid, visibilityConfig.getBoolean(uuidString));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID found in scoreboard_visibility.yml: " + uuidString);
            }
        }
    }

    public void saveScoreboardVisibility() {
        File config = new File(plugin.getDataFolder(), "scoreboard_visibility.yml");
        FileConfiguration visibilityConfig = new YamlConfiguration();
        for (Map.Entry<UUID, Boolean> entry : scoreboardVisibility.entrySet()) {
            visibilityConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            visibilityConfig.save(config);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save scoreboard_visibility.yml!");
            e.printStackTrace();
        }
    }

    private void configureTeam(Team team, String teamName) {
        ChatColor color = getTeamColor(teamName);
        team.setColor(color);
        team.setPrefix(color + "");
        boolean ffEnabled = plugin.isFriendlyFireEnabled();
        plugin.getLogger().info("[DEBUG] configureTeam for '" + teamName + "'. Setting friendly fire to: " + ffEnabled);
        team.setAllowFriendlyFire(ffEnabled); // Standard for Annihilation
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
    }

    private void updateTeamOnScoreboard(Scoreboard scoreboard, Player player, String teamName) {
        if (scoreboard == null) return;
        String playerName = player.getName();

        Team newTeam = scoreboard.getTeam(teamName);
        if (newTeam == null) {
            newTeam = scoreboard.registerNewTeam(teamName);
            configureTeam(newTeam, teamName);
        }

        Team currentTeam = scoreboard.getEntryTeam(playerName);
        if (currentTeam != null && !currentTeam.equals(newTeam)) {
            currentTeam.removeEntry(playerName);
        }

        if (!newTeam.hasEntry(playerName)) {
            newTeam.addEntry(playerName);
        }

        player.setDisplayName(getTeamColor(teamName) + playerName + ChatColor.RESET);
    }

    public void setPlayerTeam(Player player, String teamName) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Scoreboard playerSB = player.getScoreboard();

        // Update the main scoreboard for server-wide compatibility
        updateTeamOnScoreboard(mainScoreboard, player, teamName);

        // If the player has a different scoreboard (for the sidebar), update it as well.
        if (playerSB != null && playerSB != mainScoreboard) {
            updateTeamOnScoreboard(playerSB, player, teamName);
        }
    }

    public String getPlayerTeamName(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard != null) {
            Team team = scoreboard.getEntryTeam(player.getName());
            if (team != null) {
                return team.getName();
            }
        }
        return null;
    }
}
