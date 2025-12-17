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
    private final RankManager rankManager;
    private final Map<UUID, Boolean> scoreboardVisibility = new HashMap<>(); // Player UUID -> is visible?

    public ScoreboardManager(AnnihilationNexus plugin, NexusManager nexusManager, TeamColorManager teamColorManager,
            PlayerTeamManager playerTeamManager, RankManager rankManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
        this.teamColorManager = teamColorManager;
        this.playerTeamManager = playerTeamManager;
        this.rankManager = rankManager;
        loadScoreboardVisibility();
    }

    public void updatePlayerPrefix(Player player) {
        Rank rank = rankManager.getDisplayRank(player);
        String prefix = (rank != null) ? rank.getPrefix() : "";
        String teamName = playerTeamManager.getPlayerTeam(player.getUniqueId());
        ChatColor teamColor = (teamName != null) ? getTeamColor(teamName) : ChatColor.WHITE;

        // Update Tab List Name
        player.setPlayerListName(prefix + teamColor + player.getName());

        // Update Scoreboard Team Prefix (if needed, though Tab List Name usually
        // overrides for list)
        // But for name tag above head, we need to update the team prefix/suffix
        // However, we are using teams for coloring, so we might not want to mess with
        // prefix too much if it breaks coloring.
        // Actually, team prefix is used for color usually.
        // Let's just stick to setPlayerListName for now as per previous requirements.

        // Wait, if we want the prefix to show above head, we need to add it to the team
        // prefix.
        // But the team prefix is currently set to the color.
        // Let's append the rank prefix to the team prefix.

        Scoreboard mainSb = Bukkit.getScoreboardManager().getMainScoreboard();
        if (teamName != null) {
            Team team = mainSb.getTeam(teamName);
            if (team != null) {
                // We can't easily change the team prefix for just one player if they share the
                // team.
                // So we can't show rank prefix in name tag above head unless we have per-player
                // teams or per-rank teams.
                // Given the constraints, we will only update the Tab List Name and Chat format
                // (handled in ChatListener).
            }
        }
    }

    public void updateScoreboard(Player player) {
        if (!scoreboardVisibility.getOrDefault(player.getUniqueId(), true)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()); // Clear scoreboard
            return;
        }

        // Use Main Scoreboard to allow tab list (deaths) and other plugins to work
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private void updateSidebar() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection scoreboardConfig = config.getConfigurationSection("scoreboard");
        if (scoreboardConfig == null) {
            return; // Scoreboard not configured
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("annihilation");

        if (objective == null) {
            String title = ChatColor.translateAlternateColorCodes('&',
                    scoreboardConfig.getString("title", "&e&lAnnihilation"));
            objective = scoreboard.registerNewObjective("annihilation", "dummy", title);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        List<String> lines = scoreboardConfig.getStringList("lines");
        // We need to clear existing scores to prevent duplicates if lines change
        // dynamically
        // However, clearing all scores causes flickering.
        // A common strategy is to use unique entries (colors) and update their scores.
        // For simplicity and to fix the immediate lag caused by unregistering, we will
        // just reset scores for entries that are no longer in the list?
        // Actually, the simplest fix for "unregistering causes lag" is to NOT
        // unregister.
        // But if we don't unregister, old lines remain.
        // So we must remove old entries.

        // Better approach:
        // 1. Get all current entries in the objective.
        // 2. Calculate new entries.
        // 3. Remove entries that are in (1) but not in (2).
        // 4. Set scores for (2).

        Set<String> newEntries = new HashSet<>();

        int score = lines.size();
        for (String line : lines) {
            line = ChatColor.translateAlternateColorCodes('&', line);
            line = line.replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()));

            if (line.equals("NEXUS_STATUS")) {
                List<Map.Entry<String, Nexus>> sortedNexuses = new java.util.ArrayList<>(
                        nexusManager.getAllNexuses().entrySet());
                sortedNexuses.sort((e1, e2) -> {
                    int healthCompare = Integer.compare(e2.getValue().getHealth(), e1.getValue().getHealth()); // Descending
                                                                                                               // health
                    if (healthCompare != 0) {
                        return healthCompare;
                    }
                    return e1.getKey().compareTo(e2.getKey()); // Alphabetical team name as tie-breaker
                });

                for (Map.Entry<String, Nexus> entry : sortedNexuses) {
                    String teamName = entry.getKey();
                    Nexus nexus = entry.getValue();
                    ChatColor teamColor = getTeamColor(teamName);
                    String status = nexus.isDestroyed() ? ChatColor.RED + "DESTROYED"
                            : ChatColor.GREEN + "" + nexus.getHealth();
                    String entryName = teamColor + teamName + ": " + status;

                    // Handle duplicates if any (unlikely for nexus status)
                    while (newEntries.contains(entryName)) {
                        entryName += ChatColor.RESET;
                    }

                    objective.getScore(entryName).setScore(score--);
                    newEntries.add(entryName);
                }
            } else {
                // To handle duplicate lines, add invisible color codes
                while (newEntries.contains(line)
                        || scoreboard.getEntries().contains(line) && !newEntries.contains(line)) {
                    // Wait, checking scoreboard.getEntries() is global. We only care about this
                    // objective?
                    // Actually, getScore(line) creates an entry.
                    // We just need to ensure unique strings for this update cycle.
                    if (newEntries.contains(line)) {
                        line += ChatColor.RESET;
                    } else {
                        break;
                    }
                }
                objective.getScore(line).setScore(score--);
                newEntries.add(line);
            }
        }

        // Remove entries that are no longer present
        for (String entry : scoreboard.getEntries()) {
            if (objective.getScore(entry).isScoreSet() && !newEntries.contains(entry)) {
                scoreboard.resetScores(entry);
            }
        }
    }

    public void updateForAllPlayers() {
        // Update the sidebar content on the main scoreboard once
        updateSidebar();

        // Ensure all players are viewing the main scoreboard (if visibility is on)
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
        player.sendMessage(
                ChatColor.GREEN + "Scoreboard visibility toggled " + (!currentVisibility ? "on" : "off") + ".");
    }

    public ChatColor getTeamColor(String teamName) {
        // First, check for a custom color
        ChatColor customColor = teamColorManager.getTeamColor(teamName);
        if (customColor != null) {
            return customColor;
        }
        // Fallback to default colors
        switch (teamName.toLowerCase()) {
            case "red":
                return ChatColor.RED;
            case "blue":
                return ChatColor.BLUE;
            case "green":
                return ChatColor.GREEN;
            case "yellow":
                return ChatColor.YELLOW;
            default:
                return ChatColor.WHITE;
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

        // Capitalize team name for display
        String initial = teamName.substring(0, 1).toUpperCase();
        team.setPrefix(ChatColor.WHITE + "[" + color + initial + ChatColor.WHITE + "] " + color);

        boolean ffEnabled = plugin.isFriendlyFireEnabled();
        team.setAllowFriendlyFire(ffEnabled); // Standard for Annihilation
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
    }

    private void addPlayerToTeamOnScoreboard(Scoreboard scoreboard, Player player, String teamName) {
        if (scoreboard == null)
            return;
        String playerName = player.getName();

        Team newTeam = scoreboard.getTeam(teamName);
        if (newTeam == null) {
            newTeam = scoreboard.registerNewTeam(teamName);
            configureTeam(newTeam, teamName);
        }

        // Check if player is already on this team
        if (newTeam.hasEntry(playerName)) {
            return; // Already on this team, no need to do anything
        }

        // Remove player from their old team on this specific scoreboard
        Team currentTeam = scoreboard.getEntryTeam(playerName);
        if (currentTeam != null && currentTeam.hasEntry(playerName)) {
            try {
                currentTeam.removeEntry(playerName);
            } catch (IllegalStateException e) {
                // Player might not be on the team due to desync - log and continue
                plugin.getLogger().warning("Failed to remove " + playerName + " from team " + currentTeam.getName()
                        + ": " + e.getMessage());
            }
        }

        // Add player to the new team
        try {
            newTeam.addEntry(playerName);
        } catch (IllegalStateException e) {
            plugin.getLogger().warning("Failed to add " + playerName + " to team " + teamName + ": " + e.getMessage());
        }
    }

    public void removePlayerFromTeam(Player player) {
        String playerName = player.getName();

        // Remove from all online players' scoreboards
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Scoreboard sb = onlinePlayer.getScoreboard();
            if (sb != null) {
                Team team = sb.getEntryTeam(playerName);
                if (team != null) {
                    team.removeEntry(playerName);
                }
            }
        }

        // Remove from main scoreboard
        Scoreboard mainSb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team mainTeam = mainSb.getEntryTeam(playerName);
        if (mainTeam != null) {
            mainTeam.removeEntry(playerName);
        }

        // Reset display name
        player.setDisplayName(player.getName());
    }

    public void setPlayerTeam(Player player, String teamName) {
        // Update the team for the player on every online player's scoreboard
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Scoreboard playerScoreboard = onlinePlayer.getScoreboard();
            // Ensure we are not modifying the main scoreboard if the player has a custom
            // one
            if (playerScoreboard != null && playerScoreboard != Bukkit.getScoreboardManager().getMainScoreboard()) {
                addPlayerToTeamOnScoreboard(playerScoreboard, player, teamName);
            }
        }

        // Also, always update the main scoreboard for server-wide compatibility and for
        // new players.
        addPlayerToTeamOnScoreboard(Bukkit.getScoreboardManager().getMainScoreboard(), player, teamName);

        // Finally, set the display name for the player who changed teams. This affects
        // chat.
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

    private boolean updatePending = false;

    public void requestUpdate() {
        if (updatePending) {
            return;
        }
        updatePending = true;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            updateForAllPlayers();
            updatePending = false;
        });
    }

    public void initializeTeams() {
        Scoreboard mainSb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (String teamName : nexusManager.getAllNexuses().keySet()) {
            Team team = mainSb.getTeam(teamName);
            if (team == null) {
                team = mainSb.registerNewTeam(teamName);
            }
            configureTeam(team, teamName);
        }
    }
}
