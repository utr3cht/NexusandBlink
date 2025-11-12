package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class NetheriteHoeAchievementListener implements Listener {

    private final AnnihilationNexus plugin;

    public NetheriteHoeAchievementListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack brokenItem = event.getBrokenItem();

        if (brokenItem.getType() == Material.NETHERITE_HOE) {
            // Check if the player has already received this achievement
            if (!plugin.hasAchievedNetheriteHoeBreak(player.getUniqueId())) {
                // Mark player as having achieved this
                plugin.addAchievedNetheriteHoeBreak(player.getUniqueId());

                // Get player's locale
                String playerLocale = player.getLocale();

                String message;
                if (playerLocale != null && playerLocale.startsWith("ja")) {
                    message = plugin.getNetheriteHoeBreakAchievementMessageJa();
                } else {
                    message = plugin.getNetheriteHoeBreakAchievementMessageEn();
                }

                org.bukkit.Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', player.getName() + " " + message));
                plugin.getLogger().info(player.getName() + " has achieved: " + ChatColor.stripColor(message));
            }
        }
    }
}
