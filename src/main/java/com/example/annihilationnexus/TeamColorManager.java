package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TeamColorManager {

    private final AnnihilationNexus plugin;
    private final File colorsFile;
    private FileConfiguration colorsConfig;
    private final Map<String, ChatColor> teamColors = new ConcurrentHashMap<>();

    public TeamColorManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.colorsFile = new File(plugin.getDataFolder(), "team_colors.yml");
    }

    public void loadColors() {
        if (!colorsFile.exists()) {
            return;
        }
        colorsConfig = YamlConfiguration.loadConfiguration(colorsFile);
        if (colorsConfig.isConfigurationSection("teams")) {
            for (String teamName : colorsConfig.getConfigurationSection("teams").getKeys(false)) {
                String colorName = colorsConfig.getString("teams." + teamName);
                try {
                    ChatColor color = ChatColor.valueOf(colorName.toUpperCase());
                    teamColors.put(teamName.toUpperCase(), color);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid color '" + colorName + "' for team '" + teamName + "' in team_colors.yml.");
                }
            }
        }
    }

    public void saveColors() {
        colorsConfig = new YamlConfiguration();
        for (Map.Entry<String, ChatColor> entry : teamColors.entrySet()) {
            colorsConfig.set("teams." + entry.getKey(), entry.getValue().name());
        }
        try {
            colorsConfig.save(colorsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save team_colors.yml: " + e.getMessage());
        }
    }

    public void setTeamColor(String teamName, ChatColor color) {
        teamColors.put(teamName.toUpperCase(), color);
        saveColors();
    }

    public ChatColor getTeamColor(String teamName) {
        return teamColors.get(teamName.toUpperCase());
    }
}