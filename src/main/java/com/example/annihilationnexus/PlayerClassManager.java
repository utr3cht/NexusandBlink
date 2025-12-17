package com.example.annihilationnexus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
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
    private final Map<UUID, FarmerAbility> farmerAbilities = new HashMap<>();
    private final Map<UUID, RiftWalkerAbility> riftWalkerAbilities = new HashMap<>();
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

            // Also check the cursor
            ItemStack cursorItem = player.getItemOnCursor();
            if (cursorItem != null && plugin.isClassItem(cursorItem)) {
                player.setItemOnCursor(null);
            }
        }
    }

    private void givePlayerItem(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Your inventory is full. Could not receive class item: "
                    + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
                            : item.getType().name()));
            return;
        }
        player.getInventory().addItem(item);
    }

    private void addGrappleItem(Player player) {
        givePlayerItem(player, plugin.getGrappleItem());
    }

    private void addBlinkItem(Player player) {
        givePlayerItem(player, plugin.getBlinkItem());
    }

    private void addScorpioItem(Player player) {
        givePlayerItem(player, plugin.getScorpioItem());
    }

    private void addAssassinItem(Player player) {
        givePlayerItem(player, plugin.getAssassinItem());
    }

    private void addSpyItem(Player player) {
        givePlayerItem(player, plugin.getSpyItem());
    }

    private void addTransporterItem(Player player) {
        givePlayerItem(player, plugin.getTransporterItem());
    }

    private void addFarmerItems(Player player) {
        givePlayerItem(player, new ItemStack(Material.BONE_MEAL, 1));
        givePlayerItem(player, plugin.getFeastItem());
        givePlayerItem(player, plugin.getFamineItem());
    }

    private void addRiftWalkerItem(Player player) {
        givePlayerItem(player, plugin.getRiftWalkerItem());
    }

    public void setPlayerClass(UUID playerId, String className) {
        String previousClass = playerClasses.get(playerId);

        playerClasses.put(playerId, className);
        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null)
            return; // Player might be offline

        // Before removing old abilities, check if the player was Transporter
        // and destroy their portals
        if (previousClass != null && previousClass.equalsIgnoreCase("transporter")) {
            TransporterAbility existingTransporterAbility = transporterAbilities.get(playerId);
            if (existingTransporterAbility != null) {
                existingTransporterAbility.destroyAllPortalsForPlayer(playerId);
            } else {
                plugin.getLogger().warning("TransporterAbility instance not found for player " + playerId
                        + " despite being Transporter class.");
            }
        }

        // Remove old abilities first
        dasherAbilities.remove(playerId);
        grappleAbilities.remove(playerId);
        scorpioAbilities.remove(playerId);
        assassinAbilities.remove(playerId);
        spyAbilities.remove(playerId);
        transporterAbilities.remove(playerId);
        farmerAbilities.remove(playerId);

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
            TransporterAbility ability = new TransporterAbility(plugin);
            transporterAbilities.put(playerId, ability);
            addTransporterItem(player);
            ability.restoreParticleEffects(player);
        } else if (className.equalsIgnoreCase("farmer")) {
            farmerAbilities.put(playerId, new FarmerAbility(plugin));
            addFarmerItems(player);
        } else if (className.equalsIgnoreCase("riftwalker")) {
            riftWalkerAbilities.put(playerId, new RiftWalkerAbility(plugin));
            addRiftWalkerItem(player);
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
        return ability;
    }

    public FarmerAbility getFarmerAbility(UUID playerId) {
        return farmerAbilities.get(playerId);
    }

    public RiftWalkerAbility getRiftWalkerAbility(UUID playerId) {
        return riftWalkerAbilities.get(playerId);
    }

    private final Map<UUID, Set<String>> unlockedClasses = new HashMap<>(); // New field
    private final Set<String> bannedClasses = new HashSet<>(); // New field for banned classes

    public void banClass(String className) {
        bannedClasses.add(className.toLowerCase());
        saveClasses();
    }

    public void unbanClass(String className) {
        bannedClasses.remove(className.toLowerCase());
        saveClasses();
    }

    public boolean isClassBanned(String className) {
        return bannedClasses.contains(className.toLowerCase());
    }

    public void loadClasses() {
        if (!classesFile.exists()) {
            return;
        }
        classesConfig = YamlConfiguration.loadConfiguration(classesFile);
        for (String uuidString : classesConfig.getKeys(false)) {
            if (uuidString.equals("unlocked-classes"))
                continue; // Skip the special section

            try {
                UUID uuid = UUID.fromString(uuidString);
                String className = classesConfig.getString(uuidString);
                playerClasses.put(uuid, className);
            } catch (IllegalArgumentException e) {
                // Ignore invalid UUIDs (like "unlocked-classes" if it was at root, but we
                // handle it separately)
            }
        }

        // Load unlocked classes
        if (classesConfig.contains("unlocked-classes")) {
            for (String uuidString : classesConfig.getConfigurationSection("unlocked-classes").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    List<String> unlocked = classesConfig.getStringList("unlocked-classes." + uuidString);
                    unlockedClasses.put(uuid, new HashSet<>(unlocked));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in unlocked-classes: " + uuidString);
                }
            }
        }

        // Load banned classes
        if (classesConfig.contains("banned-classes")) {
            bannedClasses.clear();
            bannedClasses.addAll(classesConfig.getStringList("banned-classes"));
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

        // Save unlocked classes
        for (Map.Entry<UUID, Set<String>> entry : unlockedClasses.entrySet()) {
            classesConfig.set("unlocked-classes." + entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }

        // Save banned classes
        classesConfig.set("banned-classes", new ArrayList<>(bannedClasses));

        try {
            classesConfig.save(classesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save classes.yml!");
            e.printStackTrace();
        }
    }

    public void unlockClass(UUID playerId, String className) {
        unlockedClasses.computeIfAbsent(playerId, k -> new HashSet<>()).add(className.toLowerCase());
        saveClasses();
    }

    public boolean isClassUnlocked(UUID playerId, String className) {
        // Default classes (if any) should be checked here.
        // For now, assume all listed classes need unlocking, or maybe "civilian" is
        // free if we had it.
        // Let's assume all classes in the GUI need unlocking.
        // Wait, usually there is a free class. Let's make "Civilian" free if it exists,
        // but it's not in the list.
        // Let's assume "Handyman" or similar is default.
        // For this request, I will just check the map.
        // However, we should probably check if the cost is 0 in config, implying free.
        if (getClassCost(className) <= 0)
            return true;

        Set<String> unlocked = unlockedClasses.get(playerId);
        return unlocked != null && unlocked.contains(className.toLowerCase());
    }

    public int getClassCost(String className) {
        return plugin.getConfig().getInt("class-costs." + className.toLowerCase(), 0);
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
            } else if (className.equalsIgnoreCase("assassin")) {
                assassinAbilities.put(player.getUniqueId(), new AssassinAbility(player, plugin));
                addAssassinItem(player);
            } else if (className.equalsIgnoreCase("spy")) {
                spyAbilities.put(player.getUniqueId(), new SpyAbility(player, plugin));
                addSpyItem(player);
            } else if (className.equalsIgnoreCase("transporter")) {
                TransporterAbility ability = new TransporterAbility(plugin);
                transporterAbilities.put(player.getUniqueId(), ability);
                addTransporterItem(player);
                ability.restoreParticleEffects(player);
            } else if (className.equalsIgnoreCase("farmer")) {
                farmerAbilities.put(player.getUniqueId(), new FarmerAbility(plugin));
                addFarmerItems(player);
            } else if (className.equalsIgnoreCase("riftwalker")) {
                riftWalkerAbilities.put(player.getUniqueId(), new RiftWalkerAbility(plugin));
                addRiftWalkerItem(player);
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
        farmerAbilities.remove(playerId);
        riftWalkerAbilities.remove(playerId);
        // We don't remove from playerClasses map, as we want to persist it
    }

    // New methods for post-death portal cooldown
    public void addPlayerToPostDeathPortalCooldown(UUID playerUUID) {
        postDeathPortalCooldown.add(playerUUID);
    }

    public void removePlayerFromPostDeathPortalCooldown(UUID playerUUID) {
        postDeathPortalCooldown.remove(playerUUID);
    }

    public boolean isPlayerInPostDeathPortalCooldown(UUID playerUUID) {
        return postDeathPortalCooldown.contains(playerUUID);
    }

    // New method for tab completion
    public List<String> getAllClassNames() {
        return Arrays.asList("dasher", "scout", "scorpio", "assassin", "spy", "transporter", "farmer", "riftwalker");
    }
}
