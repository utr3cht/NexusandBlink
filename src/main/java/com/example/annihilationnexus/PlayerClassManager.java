package com.example.annihilationnexus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClassManager {

    private final AnnihilationNexus plugin;
    private final Map<UUID, String> playerClasses = new HashMap<>();
    private final Map<UUID, DasherAbility> dasherAbilities = new HashMap<>();
    private final Map<UUID, GrappleAbility> grappleAbilities = new HashMap<>();
    private File classesFile;
    private FileConfiguration classesConfig;

    public PlayerClassManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.classesFile = new File(plugin.getDataFolder(), "classes.yml");
        this.classesConfig = YamlConfiguration.loadConfiguration(classesFile);
    }

    private void removeAllGrappleItems(Player player) {
        player.getInventory().forEach(item -> {
            if (plugin.isGrappleItem(item)) {
                player.getInventory().remove(item);
            }
        });
    }

    private void addGrappleItem(Player player) {
        player.getInventory().addItem(plugin.getGrappleItem());
    }

    private void removeAllBlinkItems(Player player) {
        player.getInventory().forEach(item -> {
            if (plugin.isBlinkItem(item)) {
                player.getInventory().remove(item);
            }
        });
    }

    private void addBlinkItem(Player player) {
        player.getInventory().addItem(plugin.getBlinkItem());
    }

    public void setPlayerClass(UUID playerId, String className) {
        playerClasses.put(playerId, className);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null) return; // Player might be offline

        // Remove old abilities first
        dasherAbilities.remove(playerId);
        grappleAbilities.remove(playerId);

        // Handle inventory changes
        if (player != null) {
            // Remove all class-specific items first
            removeAllGrappleItems(player);
            removeAllBlinkItems(player);

            if (className.equalsIgnoreCase("dasher")) {
                dasherAbilities.put(playerId, new DasherAbility(player, plugin));
                addBlinkItem(player);
            } else if (className.equalsIgnoreCase("scout")) {
                grappleAbilities.put(playerId, new GrappleAbility(plugin, player));
                addGrappleItem(player);
            }
        }
    }

    public String getPlayerClass(UUID playerId) {
        return playerClasses.get(playerId);
    }

    public DasherAbility getDasherAbility(UUID playerId) {
        return dasherAbilities.get(playerId);
    }

    public GrappleAbility getGrappleAbility(UUID playerId) {
        return grappleAbilities.get(playerId);
    }

    public void loadClasses() {
        if (!classesFile.exists()) {
            return;
        }
        classesConfig = YamlConfiguration.loadConfiguration(classesFile);
        for (String uuidString : classesConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidString);
            String className = classesConfig.getString(uuidString);
            playerClasses.put(uuid, className);
        }
    }

    public void saveClasses() {
        // Clear the config before saving
        for (String key : classesConfig.getKeys(false)) {
            classesConfig.set(key, null);
        }

        for (Map.Entry<UUID, String> entry : playerClasses.entrySet()) {
            classesConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            classesConfig.save(classesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save classes.yml!");
            e.printStackTrace();
        }
    }

    public void addPlayer(Player player) {
        // This method is called when a player joins, ensure abilities are initialized
        String className = getPlayerClass(player.getUniqueId());
        if (className != null) {
            // Ensure old abilities are cleared before assigning new ones
            removePlayer(player.getUniqueId());

            // Clear existing class items from inventory
            removeAllGrappleItems(player);
            removeAllBlinkItems(player);

            if (className.equalsIgnoreCase("dasher")) {
                dasherAbilities.put(player.getUniqueId(), new DasherAbility(player, plugin));
                addBlinkItem(player);
            } else if (className.equalsIgnoreCase("scout")) {
                grappleAbilities.put(player.getUniqueId(), new GrappleAbility(plugin, player));
                addGrappleItem(player);
            }
        }
    }

    public void removePlayer(UUID playerId) {
        dasherAbilities.remove(playerId);
        grappleAbilities.remove(playerId);
        // We don't remove from playerClasses map, as we want to persist it
    }
}

