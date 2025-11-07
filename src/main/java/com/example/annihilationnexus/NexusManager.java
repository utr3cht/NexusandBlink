package com.example.annihilationnexus;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NexusManager {

    private final AnnihilationNexus plugin;
    private final Map<String, Nexus> nexuses = new HashMap<>();
    private final File nexusesFile;
    private final FileConfiguration nexusesConfig;

    public NexusManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.nexusesFile = new File(plugin.getDataFolder(), "nexuses.yml");
        this.nexusesConfig = YamlConfiguration.loadConfiguration(nexusesFile);
    }

    public void createNexus(String teamName, Location location) {
        Nexus nexus = new Nexus(teamName, location, plugin.getNexusHealth());
        nexuses.put(teamName, nexus);
        location.getBlock().setType(plugin.getNexusMaterial()); // Place the block in the world
        saveNexuses();
    }

    public void removeNexus(String teamName) {
        nexuses.remove(teamName);
        nexusesConfig.set("nexuses." + teamName, null); // Remove from config
        try {
            nexusesConfig.save(nexusesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save nexuses.yml!");
            e.printStackTrace();
        }
    }

    public Nexus getNexus(String teamName) {
        return nexuses.get(teamName);
    }

    public Nexus getNexusAt(Location location) {
        for (Nexus nexus : nexuses.values()) {
            // Compare block locations, ignoring pitch/yaw
            if (nexus.getLocation().getWorld().equals(location.getWorld()) &&
                nexus.getLocation().getBlockX() == location.getBlockX() &&
                nexus.getLocation().getBlockY() == location.getBlockY() &&
                nexus.getLocation().getBlockZ() == location.getBlockZ()) {
                return nexus;
            }
        }
        return null;
    }

    public Map<String, Nexus> getAllNexuses() {
        return nexuses;
    }

    public void loadNexuses() {
        if (!nexusesFile.exists()) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(nexusesFile);
        ConfigurationSection nexusSection = config.getConfigurationSection("nexuses");
        if (nexusSection != null) {
            for (String teamName : nexusSection.getKeys(false)) {
                Location loc = nexusSection.getLocation(teamName + ".location");
                int health = nexusSection.getInt(teamName + ".health");
                if (loc != null) {
                    nexuses.put(teamName, new Nexus(teamName, loc, health));
                }
            }
        }
    }

    public void saveNexuses() {
        // Clear the old nexuses section before saving to ensure a clean slate
        nexusesConfig.set("nexuses", null);
        ConfigurationSection nexusSection = nexusesConfig.createSection("nexuses");
        for (Map.Entry<String, Nexus> entry : nexuses.entrySet()) {
            String teamName = entry.getKey();
            Nexus nexus = entry.getValue();
            nexusSection.set(teamName + ".location", nexus.getLocation());
            nexusSection.set(teamName + ".health", nexus.getHealth());
        }
        try {
            nexusesConfig.save(nexusesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save nexuses.yml!");
            e.printStackTrace();
        }
    }
}