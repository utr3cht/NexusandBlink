package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProtectedCropManager {

    private final AnnihilationNexus plugin;
    private final Map<Location, ProtectedCropInfo> protectedCrops = new ConcurrentHashMap<>();
    private final File cropsFile;
    private final FileConfiguration cropsConfig;

    public ProtectedCropManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.cropsFile = new File(plugin.getDataFolder(), "protected_crops.yml");
        this.cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
    }

    public void loadCrops() {
        if (!cropsFile.exists()) {
            return;
        }
        // Clear current map before loading
        protectedCrops.clear();

        if (cropsConfig.isConfigurationSection("crops")) {
            for (String key : cropsConfig.getConfigurationSection("crops").getKeys(false)) {
                Location loc = Location.deserialize(cropsConfig.getConfigurationSection("crops").getConfigurationSection(key).getValues(true));
                ProtectedCropInfo info = (ProtectedCropInfo) cropsConfig.get("crops." + key + ".info");
                if (loc != null && info != null) {
                    protectedCrops.put(loc, info);
                }
            }
        }
        plugin.getLogger().info("Loaded " + protectedCrops.size() + " protected crops from file.");
    }

    public void saveCrops() {
        // Clear existing config
        for (String key : cropsConfig.getKeys(false)) {
            cropsConfig.set(key, null);
        }

        int index = 0;
        for (Map.Entry<Location, ProtectedCropInfo> entry : protectedCrops.entrySet()) {
            String key = "crop_" + index;
            cropsConfig.set("crops." + key, entry.getKey().serialize());
            cropsConfig.set("crops." + key + ".info", entry.getValue());
            index++;
        }

        try {
            cropsConfig.save(cropsFile);
            plugin.getLogger().info("Saved " + protectedCrops.size() + " protected crops to file.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save protected crops to file: " + e.getMessage());
        }
    }

    public void addCrop(Location location, UUID planter) {
        protectedCrops.put(location, new ProtectedCropInfo(System.currentTimeMillis(), planter));
        // For performance, you might want to save periodically or on disable, not on every add.
        // For now, we will save on disable.
    }

    public void removeCrop(Location location) {
        protectedCrops.remove(location);
    }

    public boolean isProtected(Location location) {
        return protectedCrops.containsKey(location);
    }

    public ProtectedCropInfo getCropInfo(Location location) {
        return protectedCrops.get(location);
    }

    public Map<Location, ProtectedCropInfo> getProtectedCrops() {
        return protectedCrops;
    }
}
