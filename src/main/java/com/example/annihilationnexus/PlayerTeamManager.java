package com.example.annihilationnexus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTeamManager {

    private final AnnihilationNexus plugin;
    private final Map<UUID, String> playerTeams = new ConcurrentHashMap<>();
    private final File teamsFile;
    private FileConfiguration teamsConfig;

    public PlayerTeamManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.teamsFile = new File(plugin.getDataFolder(), "player_teams.yml");
    }

    public void loadTeams() {
        if (!teamsFile.exists()) {
            return;
        }
        teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
        if (teamsConfig.isConfigurationSection("teams")) {
            for (String uuidString : teamsConfig.getConfigurationSection("teams").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    String teamName = teamsConfig.getString("teams." + uuidString);
                    if (teamName != null) {
                        playerTeams.put(uuid, teamName);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in player_teams.yml: " + uuidString);
                }
            }
        }
    }

    public void saveTeams() {
        teamsConfig = new YamlConfiguration(); // Create a new config to save
        for (Map.Entry<UUID, String> entry : playerTeams.entrySet()) {
            teamsConfig.set("teams." + entry.getKey().toString(), entry.getValue());
        }
        try {
            teamsConfig.save(teamsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player_teams.yml: " + e.getMessage());
        }
    }

    public void setPlayerTeam(UUID playerUUID, String teamName) {
        if (teamName == null) {
            playerTeams.remove(playerUUID);
        } else {
            playerTeams.put(playerUUID, teamName);
        }
    }

    public String getPlayerTeam(UUID playerUUID) {
        return playerTeams.get(playerUUID);
    }

    public java.util.Set<UUID> getTeamPlayers(String teamName) {
        java.util.Set<UUID> teamMembers = new java.util.HashSet<>();
        for (Map.Entry<UUID, String> entry : playerTeams.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(teamName)) {
                teamMembers.add(entry.getKey());
            }
        }
        return teamMembers;
    }
}