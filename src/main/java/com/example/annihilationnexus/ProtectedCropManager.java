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
                Location loc = Location.deserialize(cropsConfig.getConfigurationSection("crops").getConfigurationSection(key).getValues(true));
                ProtectedCropInfo info = (ProtectedCropInfo) cropsConfig.get("crops." + key + ".info");
                if (loc != null && info != null) {
                    protectedCrops.put(loc, info);
                    startGrowthTask(loc.getBlock()); // Start growth task for loaded crop
                }
            }
        }
        plugin.getLogger().info("Loaded " + protectedCrops.size() + " protected crops and started growth tasks.");
    }

    public void saveCrops() {
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
        // Do not start a new task if one is already running for this location
        if (growthTasks.containsKey(location)) {
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                // Ensure the world is loaded before getting the block
                if (!location.isWorldLoaded()) {
                    return;
                }
                Block currentBlock = location.getBlock();
                if (!(currentBlock.getBlockData() instanceof Ageable)) {
                    removeCrop(location);
                    return;
                }

                Ageable ageable = (Ageable) currentBlock.getBlockData();
                if (ageable.getAge() >= ageable.getMaximumAge()) {
                    // This should be handled by BlockGrowEvent, but as a fallback:
                    removeCrop(location);
                    return;
                }

                ageable.setAge(ageable.getAge() + 1);
                currentBlock.setBlockData(ageable);
            }
        }.runTaskTimer(plugin, 143L, 143L); // Approx. 7.15 seconds interval

        growthTasks.put(location, task);
    }

    public boolean isProtected(Location location) {
        return protectedCrops.containsKey(location);
    }

    public ProtectedCropInfo getCropInfo(Location location) {
        return protectedCrops.get(location);
    }
}