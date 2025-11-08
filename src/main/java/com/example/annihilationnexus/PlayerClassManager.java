package com.example.annihilationnexus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerClassManager {

    private final AnnihilationNexus plugin;
    private final Map<UUID, String> playerClasses = new HashMap<>();
    private final Map<UUID, DasherAbility> dasherAbilities = new HashMap<>();
    private final Map<UUID, GrappleAbility> grappleAbilities = new HashMap<>();
    private final Map<UUID, ScorpioAbility> scorpioAbilities = new HashMap<>();
    private final Map<UUID, AssassinAbility> assassinAbilities = new HashMap<>();
    private final Map<UUID, SpyAbility> spyAbilities = new HashMap<>();
    private final Map<UUID, TransporterAbility> transporterAbilities = new HashMap<>();
    private final Set<UUID> postDeathPortalCooldown = ConcurrentHashMap.newKeySet(); // New field
    private File classesFile;
    private FileConfiguration classesConfig;

    public PlayerClassManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.classesFile = new File(plugin.getDataFolder(), "classes.yml");
        this.classesConfig = YamlConfiguration.loadConfiguration(classesFile);
    }

    // Generic method to remove all class items safely
    private void removeAllClassItems(Player player) {
        if (player != null && player.getInventory() != null) {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && plugin.isClassItem(item)) {
                    player.getInventory().setItem(i, null);
                }
            }
        }
    }

    private void addGrappleItem(Player player) {
        player.getInventory().addItem(plugin.getGrappleItem());
    }

    private void addBlinkItem(Player player) {
        player.getInventory().addItem(plugin.getBlinkItem());
    }

    private void addScorpioItem(Player player) {
        player.getInventory().addItem(plugin.getScorpioItem());
    }

    private void addAssassinItem(Player player) {
        player.getInventory().addItem(plugin.getAssassinItem());
    }

    private void addSpyItem(Player player) {
        player.getInventory().addItem(plugin.getSpyItem());
    }

    private void addTransporterItem(Player player) {
        player.getInventory().addItem(plugin.getTransporterItem());
    }

    public void setPlayerClass(UUID playerId, String className) {
        plugin.getLogger().info("setPlayerClass called for player " + playerId + " to class " + className);
        String previousClass = playerClasses.get(playerId);
        plugin.getLogger().info("Previous class for " + playerId + ": " + previousClass);

        playerClasses.put(playerId, className);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null) return; // Player might be offline

        // Before removing old abilities, check if the player was Transporter
        // and destroy their portals
        if (previousClass != null && previousClass.equalsIgnoreCase("transporter")) {
            plugin.getLogger().info("Player " + playerId + " was Transporter. Attempting to destroy portals.");
            TransporterAbility existingTransporterAbility = transporterAbilities.get(playerId);
            if (existingTransporterAbility != null) {
                existingTransporterAbility.destroyAllPortalsForPlayer(playerId);
            } else {
                plugin.getLogger().warning("TransporterAbility instance not found for player " + playerId + " despite being Transporter class.");
            }
        }

        // Remove old abilities first
        dasherAbilities.remove(playerId);
        grappleAbilities.remove(playerId);
        scorpioAbilities.remove(playerId);
        assassinAbilities.remove(playerId);
        spyAbilities.remove(playerId);
        transporterAbilities.remove(playerId);

        // Handle inventory changes
        // Remove all class-specific items first
        removeAllClassItems(player);

        if (className.equalsIgnoreCase("dasher")) {
            dasherAbilities.put(playerId, new DasherAbility(player, plugin));
            addBlinkItem(player);
        } else if (className.equalsIgnoreCase("scout")) {
            grappleAbilities.put(playerId, new GrappleAbility(plugin, player));
            addGrappleItem(player);
        } else if (className.equalsIgnoreCase("scorpio")) {
            scorpioAbilities.put(playerId, new ScorpioAbility(player, plugin));
            addScorpioItem(player);
        } else if (className.equalsIgnoreCase("assassin")) {
            assassinAbilities.put(playerId, new AssassinAbility(player, plugin));
            addAssassinItem(player);
        } else if (className.equalsIgnoreCase("spy")) {
            spyAbilities.put(playerId, new SpyAbility(player, plugin));
            addSpyItem(player);
        } else if (className.equalsIgnoreCase("transporter")) {
            transporterAbilities.put(playerId, new TransporterAbility(plugin));
            addTransporterItem(player);
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

    public ScorpioAbility getScorpioAbility(UUID playerId) {
        return scorpioAbilities.get(playerId);
    }

    public AssassinAbility getAssassinAbility(UUID playerId) {
        return assassinAbilities.get(playerId);
    }

    public SpyAbility getSpyAbility(UUID playerId) {
        return spyAbilities.get(playerId);
    }

    public TransporterAbility getTransporterAbility(UUID playerId) {
        TransporterAbility ability = transporterAbilities.get(playerId);
        plugin.getLogger().info("getTransporterActiveAbility called for player " + playerId + ". Returning: " + (ability != null ? "instance" : "null"));
        return ability;
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
            removeAllClassItems(player);

            if (className.equalsIgnoreCase("dasher")) {
                dasherAbilities.put(player.getUniqueId(), new DasherAbility(player, plugin));
                addBlinkItem(player);
            } else if (className.equalsIgnoreCase("scout")) {
                grappleAbilities.put(player.getUniqueId(), new GrappleAbility(plugin, player));
                addGrappleItem(player);
            } else if (className.equalsIgnoreCase("scorpio")) {
                scorpioAbilities.put(player.getUniqueId(), new ScorpioAbility(player, plugin));
                addScorpioItem(player);
            }
            else if (className.equalsIgnoreCase("assassin")) {
                assassinAbilities.put(player.getUniqueId(), new AssassinAbility(player, plugin));
                addAssassinItem(player);
            }
            else if (className.equalsIgnoreCase("spy")) {
                spyAbilities.put(player.getUniqueId(), new SpyAbility(player, plugin));
                addSpyItem(player);
            }
            else if (className.equalsIgnoreCase("transporter")) {
                transporterAbilities.put(player.getUniqueId(), new TransporterAbility(plugin));
                addTransporterItem(player);
            }
        }
    }

    public void removePlayer(UUID playerId) {
        dasherAbilities.remove(playerId);
        grappleAbilities.remove(playerId);
        scorpioAbilities.remove(playerId);
        assassinAbilities.remove(playerId);
        spyAbilities.remove(playerId);
        transporterAbilities.remove(playerId);
        // We don't remove from playerClasses map, as we want to persist it
    }

    // New methods for post-death portal cooldown
    public void addPlayerToPostDeathPortalCooldown(UUID playerUUID) {
        postDeathPortalCooldown.add(playerUUID);
        plugin.getLogger().info("Player " + playerUUID + " added to post-death portal cooldown.");
    }

    public void removePlayerFromPostDeathPortalCooldown(UUID playerUUID) {
        postDeathPortalCooldown.remove(playerUUID);
        plugin.getLogger().info("Player " + playerUUID + " removed from post-death portal cooldown.");
    }

    public boolean isPlayerInPostDeathPortalCooldown(UUID playerUUID) {
        return postDeathPortalCooldown.contains(playerUUID);
    }
}

