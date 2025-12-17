package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class XpManager {

    private final AnnihilationNexus plugin;
    private final RankManager rankManager;
    private final NamespacedKey xpKey;
    private final java.io.File xpFile;
    private final org.bukkit.configuration.file.FileConfiguration xpConfig;
    private final java.util.Map<java.util.UUID, Integer> globalXpCache = new java.util.HashMap<>();

    public XpManager(AnnihilationNexus plugin, RankManager rankManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.xpKey = new NamespacedKey(plugin, "player_xp");
        this.xpFile = new java.io.File(plugin.getDataFolder(), "xp.yml");
        this.xpConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(xpFile);
        loadXpData();
    }

    private void loadXpData() {
        if (!xpFile.exists())
            return;
        for (String key : xpConfig.getKeys(false)) {
            try {
                java.util.UUID uuid = java.util.UUID.fromString(key);
                int xp = xpConfig.getInt(key);
                globalXpCache.put(uuid, xp);
            } catch (IllegalArgumentException e) {
                // Ignore invalid UUIDs
            }
        }
    }

    public void saveXpData() {
        for (java.util.Map.Entry<java.util.UUID, Integer> entry : globalXpCache.entrySet()) {
            xpConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            xpConfig.save(xpFile);
        } catch (java.io.IOException e) {
            plugin.getLogger().severe("Could not save xp.yml!");
            e.printStackTrace();
        }
    }

    public int getXp(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        return container.getOrDefault(xpKey, PersistentDataType.INTEGER, 0);
    }

    public void setXp(Player player, int amount) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(xpKey, PersistentDataType.INTEGER, amount);

        // Update cache
        globalXpCache.put(player.getUniqueId(), amount);
        saveXpData(); // Save immediately or periodically? Immediately for now to be safe.
    }

    public void addXp(Player player, int amount) {
        int currentXp = getXp(player);
        setXp(player, currentXp + amount);
    }

    private double globalMultiplier = 1.0;

    public void setGlobalMultiplier(double multiplier) {
        this.globalMultiplier = multiplier;
    }

    public double getGlobalMultiplier() {
        return globalMultiplier;
    }

    public void giveXp(Player player, int baseAmount, String reason) {
        Rank rank = rankManager.getRank(player);
        double bonus = (rank != null) ? rank.getXpMultiplier() : 0.0;
        int finalAmount = (int) (baseAmount * (1.0 + bonus) * globalMultiplier);

        addXp(player, finalAmount);
        player.sendMessage(ChatColor.GREEN + "+" + finalAmount + " Shrektbow XP");
    }

    public void giveXp(Player player, int baseAmount) {
        Rank rank = rankManager.getRank(player);
        double bonus = (rank != null) ? rank.getXpMultiplier() : 0.0;
        int finalAmount = (int) (baseAmount * (1.0 + bonus) * globalMultiplier);

        addXp(player, finalAmount);
        player.sendMessage(ChatColor.GREEN + "+" + finalAmount + " Shrektbow XP");
    }

    public java.util.List<java.util.Map.Entry<java.util.UUID, Integer>> getTopPlayers(int limit) {
        java.util.List<java.util.Map.Entry<java.util.UUID, Integer>> list = new java.util.ArrayList<>(
                globalXpCache.entrySet());
        list.sort(java.util.Map.Entry.<java.util.UUID, Integer>comparingByValue().reversed());
        if (list.size() > limit) {
            return list.subList(0, limit);
        }
        return list;
    }

    public java.util.List<java.util.Map.Entry<java.util.UUID, Integer>> getAllPlayersSorted() {
        java.util.List<java.util.Map.Entry<java.util.UUID, Integer>> list = new java.util.ArrayList<>(
                globalXpCache.entrySet());
        list.sort(java.util.Map.Entry.<java.util.UUID, Integer>comparingByValue().reversed());
        return list;
    }

    public int getPlayerRank(java.util.UUID uuid) {
        java.util.List<java.util.Map.Entry<java.util.UUID, Integer>> list = getAllPlayersSorted();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        return -1; // Not found
    }
}
