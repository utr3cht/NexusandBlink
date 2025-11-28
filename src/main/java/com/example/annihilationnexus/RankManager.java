package com.example.annihilationnexus;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class RankManager {

    private final NamespacedKey rankKey;
    private final NamespacedKey displayRankKey;

    public RankManager(JavaPlugin plugin) {
        this.rankKey = new NamespacedKey(plugin, "player_rank");
        this.displayRankKey = new NamespacedKey(plugin, "player_display_rank");
    }

    public void setRank(Player player, Rank rank) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(rankKey, PersistentDataType.STRING, rank.name());
        // Reset display rank when real rank changes, or set it to the new rank?
        // For now, let's reset it to ensure consistency unless they manually change it
        // again.
        setDisplayRank(player, rank);
    }

    public Rank getRank(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (!container.has(rankKey, PersistentDataType.STRING)) {
            return null;
        }
        String rankName = container.get(rankKey, PersistentDataType.STRING);
        try {
            return Rank.valueOf(rankName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setDisplayRank(Player player, Rank rank) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(displayRankKey, PersistentDataType.STRING, rank.name());
    }

    public Rank getDisplayRank(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (container.has(displayRankKey, PersistentDataType.STRING)) {
            String rankName = container.get(displayRankKey, PersistentDataType.STRING);
            try {
                return Rank.valueOf(rankName);
            } catch (IllegalArgumentException e) {
                // Fallback to real rank if invalid
            }
        }
        return getRank(player);
    }

    public void removeRank(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.remove(rankKey);
        container.remove(displayRankKey);
    }

    public boolean hasRank(Player player) {
        return getRank(player) != null;
    }
}
