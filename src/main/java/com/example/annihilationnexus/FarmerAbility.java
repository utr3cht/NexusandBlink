package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class FarmerAbility {

    private final AnnihilationNexus plugin;
    private final Map<UUID, Long> feastCooldowns = new HashMap<>();
    private final Map<UUID, Long> famineCooldowns = new HashMap<>();

    public FarmerAbility(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    public void useFeast(Player player) {
        long remaining = getRemainingCooldown(feastCooldowns, player.getUniqueId(), plugin.getFeastCooldown());
        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "Feast is on cooldown for " + remaining + " more seconds.");
            return;
        }

        double radius = plugin.getFeastRadius();
        float saturation = plugin.getFeastSaturation();

        // --- Affect the user first ---
        player.setFoodLevel(20);
        player.setSaturation(player.getSaturation() + saturation);
        player.removePotionEffect(PotionEffectType.HUNGER);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f); // Feast sound

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team casterTeam = scoreboard.getEntryTeam(player.getName());
        int affectedCount = 0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                if (target.equals(player)) continue; // Self is already affected

                // --- Team Check ---
                Team targetTeam = scoreboard.getEntryTeam(target.getName());
                if (casterTeam != null && casterTeam.equals(targetTeam)) {
                    // It's a teammate, apply the effect regardless of class
                    target.setFoodLevel(20);
                    target.setSaturation(target.getSaturation() + saturation);
                    target.removePotionEffect(PotionEffectType.HUNGER);
                    target.sendMessage(ChatColor.GREEN + player.getName() + " has used Feast to restore your hunger!");
                    affectedCount++;
                }
            }
        }

        // Particle Effect for Feast
        Location center = player.getLocation();
        org.bukkit.Particle.DustOptions dustOptions = new org.bukkit.Particle.DustOptions(Color.YELLOW, 1.0F);
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            center.getWorld().spawnParticle(org.bukkit.Particle.DUST, new Location(center.getWorld(), x, center.getY(), z), 1, dustOptions);
        }

        player.sendMessage(ChatColor.GREEN + "You used Feast, affecting " + affectedCount + " teammates.");
        feastCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void useFamine(Player player) {
        long remaining = getRemainingCooldown(famineCooldowns, player.getUniqueId(), plugin.getFamineCooldown());
        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "Famine is on cooldown for " + remaining + " more seconds.");
            return;
        }

        double radius = plugin.getFamineRadius();
        int hungerLevel = plugin.getFamineHungerLevel();
        int hungerDuration = plugin.getFamineHungerDuration() * 20; // To ticks
        int foodLevel = plugin.getFamineFoodLevel();
        int affectedCount = 0;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team casterTeam = scoreboard.getEntryTeam(player.getName());

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                if (target.equals(player)) continue; // Don't affect self

                // --- Team Check ---
                Team targetTeam = scoreboard.getEntryTeam(target.getName());
                // Affect if not on the same team (or if one is not on a team)
                if (casterTeam == null || !casterTeam.equals(targetTeam)) {
                    target.setFoodLevel(foodLevel);
                    target.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, hungerDuration, hungerLevel - 1, true, true));
                    target.sendMessage(ChatColor.DARK_RED + "You feel a sudden famine, caused by " + player.getName() + "!");
                    affectedCount++;
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_SKELETON_HORSE_DEATH, 1.0f, 1.0f); // Famine sound

        // Particle Effect for Famine
        Location center = player.getLocation();
        for (int i = 0; i < 100; i++) {
            double offsetX = ThreadLocalRandom.current().nextDouble(-radius, radius);
            double offsetY = ThreadLocalRandom.current().nextDouble(0, 2.5);
            double offsetZ = ThreadLocalRandom.current().nextDouble(-radius, radius);
            center.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, center.getX() + offsetX, center.getY() + offsetY, center.getZ() + offsetZ, 1);
        }

        player.sendMessage(ChatColor.DARK_GRAY + "You used Famine, affecting " + affectedCount + " enemies.");
        famineCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void updateItemLore(Player player) {
        ItemStack feastItem = plugin.getFeastItem();
        if (feastItem == null || !player.getInventory().contains(feastItem.getType())) {
            return;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.isFeastItem(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;

                String originalDisplayName = ChatColor.GOLD + "Feast"; // Assuming this is the base name
                long remainingCooldown = getRemainingCooldown(feastCooldowns, player.getUniqueId(), plugin.getFeastCooldown());

                if (remainingCooldown > 0) {
                    meta.setDisplayName(originalDisplayName + ChatColor.RED + " " + remainingCooldown );
                } else {
                    meta.setDisplayName(originalDisplayName);
                }
                // Clear existing lore and set new lore if needed (e.g., for other info)
                meta.setLore(new ArrayList<>()); // Clear lore related to cooldown
                item.setItemMeta(meta);
            } else if (plugin.isFamineItem(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) continue;

                String originalDisplayName = ChatColor.DARK_GRAY + "Famine"; // Assuming this is the base name
                long remainingCooldown = getRemainingCooldown(famineCooldowns, player.getUniqueId(), plugin.getFamineCooldown());

                if (remainingCooldown > 0) {
                    meta.setDisplayName(originalDisplayName + ChatColor.RED + " " + remainingCooldown);
                } else {
                    meta.setDisplayName(originalDisplayName);
                }
                meta.setLore(new ArrayList<>()); // Clear lore related to cooldown
                item.setItemMeta(meta);
            }
        }
    }    private long getRemainingCooldown(Map<UUID, Long> cooldownMap, UUID playerId, int totalCooldownSeconds) {
        if (cooldownMap.containsKey(playerId)) {
            long timeSince = System.currentTimeMillis() - cooldownMap.get(playerId);
            long remainingMillis = (totalCooldownSeconds * 1000L) - timeSince;
            if (remainingMillis > 0) {
                return TimeUnit.MILLISECONDS.toSeconds(remainingMillis);
            }
        }
        return 0;
    }
}

