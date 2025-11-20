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
    private final TeamColorManager teamColorManager;
    private final PlayerTeamManager playerTeamManager;
    private final Map<UUID, Boolean> scoreboardVisibility = new HashMap<>(); // Player UUID -> is visible?

    public ScoreboardManager(AnnihilationNexus plugin, NexusManager nexusManager, TeamColorManager teamColorManager, PlayerTeamManager playerTeamManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
        this.teamColorManager = teamColorManager;
        this.playerTeamManager = playerTeamManager;
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
        // First, check for a custom color
        ChatColor customColor = teamColorManager.getTeamColor(teamName);
        if (customColor != null) {
            return customColor;
        }
        // Fallback to default colors
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
        team.setAllowFriendlyFire(ffEnabled); // Standard for Annihilation
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    private void addPlayerToTeamOnScoreboard(Scoreboard scoreboard, Player player, String teamName) {
        if (scoreboard == null) return;
        String playerName = player.getName();

        Team newTeam = scoreboard.getTeam(teamName);
        if (newTeam == null) {
            newTeam = scoreboard.registerNewTeam(teamName);
            configureTeam(newTeam, teamName);
        }

        // Remove player from their old team on this specific scoreboard
        Team currentTeam = scoreboard.getEntryTeam(playerName);
        if (currentTeam != null && !currentTeam.equals(newTeam)) {
            currentTeam.removeEntry(playerName);
        }

        // Add player to the new team if they aren't already on it
        if (!newTeam.hasEntry(playerName)) {
            newTeam.addEntry(playerName);
        }
    }

    public void setPlayerTeam(Player player, String teamName) {
        // Update the team for the player on every online player's scoreboard
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = onlinePlayer.getScoreboard();
            // Ensure we are not modifying the main scoreboard if the player has a custom one
            if (playerScoreboard != null && playerScoreboard != Bukkit.getScoreboardManager().getMainScoreboard()) {
                addPlayerToTeamOnScoreboard(playerScoreboard, player, teamName);
            }
        }

        // Also, always update the main scoreboard for server-wide compatibility and for new players.
        addPlayerToTeamOnScoreboard(Bukkit.getScoreboardManager().getMainScoreboard(), player, teamName);

        // Finally, set the display name for the player who changed teams. This affects chat.
        player.setDisplayName(getTeamColor(teamName) + player.getName() + ChatColor.RESET);
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

    public void updateTeamConfiguration(String teamName) {
        String upperTeamName = teamName.toUpperCase();

        // Update team visuals on all scoreboards
        for (Player p : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = p.getScoreboard();
            if (sb != null) {
                Team team = sb.getTeam(upperTeamName);
                if (team != null) {
                    configureTeam(team, upperTeamName);
                }
            }
        }
        // Also update the main scoreboard
        Scoreboard mainSb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team mainTeam = mainSb.getTeam(upperTeamName);
        if (mainTeam != null) {
            configureTeam(mainTeam, upperTeamName);
        }

        // Update display name for all players on the affected team
        ChatColor newColor = getTeamColor(upperTeamName);
        for (Player p : Bukkit.getOnlinePlayers()) {
            String playerTeam = playerTeamManager.getPlayerTeam(p.getUniqueId());
            if (playerTeam != null && playerTeam.equalsIgnoreCase(upperTeamName)) {
                p.setDisplayName(newColor + p.getName() + ChatColor.RESET);
            }
        }

        // Refresh all scoreboards to show changes (e.g., nexus status color)
        updateForAllPlayers();
    }
}
