package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class XpManager {

    private final RankManager rankManager;
    private final NamespacedKey xpKey;

    public XpManager(JavaPlugin plugin, RankManager rankManager) {
        this.rankManager = rankManager;
        this.xpKey = new NamespacedKey(plugin, "player_xp");
    }

    public int getXp(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        return container.getOrDefault(xpKey, PersistentDataType.INTEGER, 0);
    }

    public void setXp(Player player, int amount) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(xpKey, PersistentDataType.INTEGER, amount);
    }

    public void addXp(Player player, int amount) {
        int currentXp = getXp(player);
        setXp(player, currentXp + amount);
    }

    public void giveXp(Player player, int baseAmount, String reason) {
        Rank rank = rankManager.getRank(player);
        double bonus = (rank != null) ? rank.getXpMultiplier() : 0.0;
        int finalAmount = (int) (baseAmount * (1.0 + bonus));

        addXp(player, finalAmount);
        player.sendMessage(ChatColor.GREEN + "+" + finalAmount + " Shrektbow XP");
    }

    public void giveXp(Player player, int baseAmount) {
        Rank rank = rankManager.getRank(player);
        double bonus = (rank != null) ? rank.getXpMultiplier() : 0.0;
        int finalAmount = (int) (baseAmount * (1.0 + bonus));

        addXp(player, finalAmount);
        player.sendMessage(ChatColor.GREEN + "+" + finalAmount + " Shrektbow XP");
    }
}
