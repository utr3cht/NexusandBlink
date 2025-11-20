package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ProtectedCropManager {

    private final AnnihilationNexus plugin;
    private final Map<Location, ProtectedCropInfo> protectedCrops = new ConcurrentHashMap<>();
    private final Map<Location, BukkitTask> growthTasks = new ConcurrentHashMap<>();
    private final File cropsFile;
    private final FileConfiguration cropsConfig;

    public ProtectedCropManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.cropsFile = new File(plugin.getDataFolder(), "protected_crops.yml");
        this.cropsConfig = YamlConfiguration.loadConfiguration(cropsFile);
    }

    public void loadCropsAndStartGrowth() {
        if (!cropsFile.exists()) {
            return;
        }
        protectedCrops.clear();
        growthTasks.values().forEach(BukkitTask::cancel);
        growthTasks.clear();

        if (cropsConfig.isConfigurationSection("crops")) {
            for (String key : cropsConfig.getConfigurationSection("crops").getKeys(false)) {
                Location loc = cropsConfig.getSerializable("crops." + key + ".location", Location.class);
                ProtectedCropInfo info = (ProtectedCropInfo) cropsConfig.get("crops." + key + ".info");

                if (loc != null && info != null) {
                    // This check is crucial because the world might not be loaded when the location is deserialized.
                    if (loc.getWorld() == null) {
                        plugin.getLogger().warning("Could not load protected crop at " + loc.getX() + "," + loc.getY() + "," + loc.getZ() + " because its world is not loaded. Skipping.");
                        continue;
                    }
                    protectedCrops.put(loc, info);
                    startGrowthTask(loc.getBlock());
                }
            }
        }
    }

    public void saveCrops() {
        // Clear the entire config to prevent old data from remaining
        cropsConfig.set("crops", null);

        if (protectedCrops.isEmpty()) {
            try {
                cropsConfig.save(cropsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save empty protected crops file: " + e.getMessage());
            }
            return;
        }

        int index = 0;
        for (Map.Entry<Location, ProtectedCropInfo> entry : protectedCrops.entrySet()) {
            String key = "crop_" + index;
            // Store location and info under the same key for cleaner structure
            cropsConfig.set("crops." + key + ".location", entry.getKey());
            cropsConfig.set("crops." + key + ".info", entry.getValue());
            index++;
        }

        try {
            cropsConfig.save(cropsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save protected crops to file: " + e.getMessage());
        }
    }

    public void addCrop(Location location, UUID planter) {
        protectedCrops.put(location, new ProtectedCropInfo(System.currentTimeMillis(), planter));
        startGrowthTask(location.getBlock());
    }

    public void removeCrop(Location location) {
        protectedCrops.remove(location);
        if (growthTasks.containsKey(location)) {
            growthTasks.get(location).cancel();
            growthTasks.remove(location);
        }
    }

    private void startGrowthTask(Block block) {
        Location location = block.getLocation();
        if (growthTasks.containsKey(location)) {
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!location.isWorldLoaded()) {
                    removeCrop(location);
                    return;
                }
                Block currentBlock = location.getBlock();
                if (!(currentBlock.getBlockData() instanceof Ageable)) {
                    removeCrop(location);
                    return;
                }

                Ageable ageable = (Ageable) currentBlock.getBlockData();
                if (ageable.getAge() >= ageable.getMaximumAge()) {
                    removeCrop(location);
                    return;
                }

                ageable.setAge(ageable.getAge() + 1);
                currentBlock.setBlockData(ageable);
            }
        }.runTaskTimer(plugin, 143L, 143L);

        growthTasks.put(location, task);
    }

    public boolean isProtected(Location location) {
        return protectedCrops.containsKey(location);
    }

    public ProtectedCropInfo getCropInfo(Location location) {
        return protectedCrops.get(location);
    }
}
