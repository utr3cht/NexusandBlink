package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ClassRegionManager {
    private final AnnihilationNexus plugin;
    private final List<ClassRegion> regions;
    private final File regionsFile;
    private FileConfiguration regionsConfig;

    public ClassRegionManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.regions = new ArrayList<>();
        this.regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        saveDefaultRegionsConfig();
    }

    public void addRegion(ClassRegion region) {
        regions.add(region);
        saveRegions();
    }

    public boolean removeRegion(String name) {
        boolean removed = regions.removeIf(region -> region.getName().equalsIgnoreCase(name));
        if (removed) {
            saveRegions();
        }
        return removed;
    }

    public List<ClassRegion> getAllRegions() {
        return new ArrayList<>(regions);
    }

    public boolean isLocationInRestrictedRegion(Location loc) {
        for (ClassRegion region : regions) {
            if (region.contains(loc)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerInRestrictedRegion(Player player) {
        return isLocationInRestrictedRegion(player.getLocation());
    }

    public boolean isLocationInAllowedRegion(Location loc) {
        // If no regions are defined, all locations are allowed
        if (regions.isEmpty()) {
            return true;
        }
        // If regions are defined, a location is allowed only if it's NOT in any restricted region
        return !isLocationInRestrictedRegion(loc);
    }


    public void loadRegions() {
        regions.clear();
        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);

        if (regionsConfig.isConfigurationSection("regions")) {
            for (String key : regionsConfig.getConfigurationSection("regions").getKeys(false)) {
                String serializedRegion = regionsConfig.getString("regions." + key);
                if (serializedRegion != null) {
                    try {
                        // Assuming all regions are in the same world for simplicity, or world name is part of serialization
                        // For now, let's assume the main world or get world from serialized string
                        String[] parts = serializedRegion.split(";");
                        if (parts.length > 1) {
                            World world = Bukkit.getWorld(parts[1]);
                            if (world != null) {
                                regions.add(ClassRegion.deserialize(serializedRegion, world));
                            } else {
                                plugin.getLogger().warning("World '" + parts[1] + "' not found for region '" + parts[0] + "'. Skipping region.");
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().log(Level.WARNING, "Failed to deserialize region: " + serializedRegion, e);
                    }
                }
            }
        }
        plugin.getLogger().info("Loaded " + regions.size() + " class regions.");
    }

    public void saveRegions() {
        regionsConfig = new YamlConfiguration(); // Clear previous config
        for (ClassRegion region : regions) {
            regionsConfig.set("regions." + region.getName(), region.serialize());
        }
        try {
            regionsConfig.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save regions to " + regionsFile, e);
        }
    }

    private void saveDefaultRegionsConfig() {
        if (!regionsFile.exists()) {
            plugin.saveResource("regions.yml", false);
        }
    }
}
