package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClassManager {

    private final AnnihilationNexus plugin;
    private final Map<UUID, String> playerClasses = new HashMap<>();
    private final Map<UUID, DasherAbility> dasherAbilities = new HashMap<>();
    private final File classesFile;

    public PlayerClassManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.classesFile = new File(plugin.getDataFolder(), "classes.dat");
    }

    public void setPlayerClass(Player player, String className) {
        // Clean up old class ability if it exists
        if ("dasher".equalsIgnoreCase(playerClasses.get(player.getUniqueId()))) {
            DasherAbility ability = dasherAbilities.remove(player.getUniqueId());
            if (ability != null) {
                ability.stopVisualizer();
            }
        }

        playerClasses.put(player.getUniqueId(), className.toLowerCase());

        // Create new class ability if it's a dasher
        if ("dasher".equalsIgnoreCase(className)) {
            dasherAbilities.put(player.getUniqueId(), new DasherAbility(player, plugin));
        }
    }

    public String getPlayerClass(UUID playerUuid) {
        return playerClasses.get(playerUuid);
    }

    public DasherAbility getDasherAbility(UUID playerUuid) {
        return dasherAbilities.get(playerUuid);
    }

    public void removePlayer(UUID playerUuid) {
        // Clean up on player quit
        if ("dasher".equalsIgnoreCase(playerClasses.get(playerUuid))) {
            DasherAbility ability = dasherAbilities.remove(playerUuid);
            if (ability != null) {
                ability.stopVisualizer();
            }
        }
        playerClasses.remove(playerUuid);
    }

    public void saveClasses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(classesFile))) {
            for (Map.Entry<UUID, String> entry : playerClasses.entrySet()) {
                writer.write(entry.getKey().toString() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save classes to file: " + e.getMessage());
        }
    }

    public void loadClasses() {
        if (!classesFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(classesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    UUID uuid = UUID.fromString(parts[0]);
                    String className = parts[1];
                    playerClasses.put(uuid, className);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            plugin.getLogger().severe("Could not load classes from file: " + e.getMessage());
        }
    }
}
