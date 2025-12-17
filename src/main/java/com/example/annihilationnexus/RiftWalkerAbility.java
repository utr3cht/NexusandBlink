package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RiftWalkerAbility {

    private final AnnihilationNexus plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> activeRifts = new ConcurrentHashMap<>();
    private final Map<UUID, Location> riftLocations = new ConcurrentHashMap<>();

    public RiftWalkerAbility(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    public void useAbility(Player player) {
        if (isOnCooldown(player)) {
            long remaining = getCooldownRemaining(player);
            player.sendMessage(ChatColor.RED + "Rift is on cooldown for " + remaining + " seconds.");
            return;
        }

        if (activeRifts.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already opening a Rift!");
            return;
        }

        openTeammateSelector(player);
    }

    private void openTeammateSelector(Player player) {
        // Allow selecting any player, not just teammates
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        List<Player> validTargets = new ArrayList<>();

        for (Player p : onlinePlayers) {
            if (p.getUniqueId().equals(player.getUniqueId()))
                continue;
            validTargets.add(p);
        }

        if (validTargets.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No teammates found to teleport to.");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "RiftWalker: Select Target");

        for (Player target : validTargets) {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName(ChatColor.GREEN + target.getName());

                String distanceStr;
                if (player.getWorld().equals(target.getWorld())) {
                    int distance = (int) player.getLocation().distance(target.getLocation());
                    distanceStr = distance + "m";
                } else {
                    distanceStr = "Different World";
                }

                meta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Distance: " + ChatColor.YELLOW + distanceStr,
                        ChatColor.GRAY + "Click to open Rift"));
                skull.setItemMeta(meta);
            }
            gui.addItem(skull);
        }

        player.openInventory(gui);
    }

    public void startRift(Player player, Player target) {
        if (activeRifts.containsKey(player.getUniqueId()))
            return;

        Location startLoc = player.getLocation();
        riftLocations.put(player.getUniqueId(), startLoc);

        player.sendMessage(
                ChatColor.LIGHT_PURPLE + "Rift opening to " + target.getName() + " in 10 seconds! Stay in the ring!");
        target.sendMessage(ChatColor.LIGHT_PURPLE + player.getName() + " is opening a Rift to you!");

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            final int duration = 10 * 20; // 10 seconds

            @Override
            public void run() {
                if (!player.isOnline() || !target.isOnline()) {
                    cancelRift(player, "Target or player went offline.");
                    return;
                }

                // Check distance from start location (Green Ring radius approx 3 blocks)
                if (player.getLocation().distance(startLoc) > 3.0) {
                    cancelRift(player, "You left the Rift circle!");
                    return;
                }

                // Particles for Green Ring
                if (ticks % 5 == 0) {
                    spawnRiftParticles(startLoc);
                }

                // Action Bar and Chat Notification
                int remainingSeconds = (duration - ticks) / 20;

                // Every second (20 ticks), notify players in range
                if (remainingSeconds > 0 && ticks % 20 == 0) {
                    player.sendActionBar(ChatColor.LIGHT_PURPLE + "Rift Opening in: " + remainingSeconds + "s");

                    // Notify everyone in the circle
                    String notification = ChatColor.LIGHT_PURPLE + "Rifting to " + ChatColor.GREEN + target.getName() +
                            ChatColor.LIGHT_PURPLE + " - " + ChatColor.YELLOW + remainingSeconds + "s remaining";

                    for (Player p : player.getWorld().getPlayers()) {
                        if (p.getLocation().distance(startLoc) <= 3.0) {
                            p.sendMessage(notification);
                        }
                    }
                }

                if (ticks >= duration) {
                    teleport(player, target);
                    this.cancel();
                }

                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);

        activeRifts.put(player.getUniqueId(), task);
    }

    private void spawnRiftParticles(Location center) {
        for (double i = 0; i < Math.PI * 2; i += Math.PI / 16) {
            double x = Math.cos(i) * 3;
            double z = Math.sin(i) * 3;
            center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, center.clone().add(x, 0.1, z), 1, 0, 0, 0, 0);
        }
    }

    private void cancelRift(Player player, String reason) {
        BukkitTask task = activeRifts.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        riftLocations.remove(player.getUniqueId());
        player.sendMessage(ChatColor.RED + "Rift cancelled: " + reason);
        player.sendActionBar(ChatColor.RED + "Rift Cancelled");
    }

    private void teleport(Player player, Player target) {
        activeRifts.remove(player.getUniqueId());
        riftLocations.remove(player.getUniqueId());

        List<Player> toTeleport = new ArrayList<>();
        toTeleport.add(player);

        // Find nearby sneaking players (regardless of team)
        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player) || p.equals(target))
                continue;
            if (p.getLocation().distance(player.getLocation()) <= 5.0) { // 5 block radius for passengers
                if (p.isSneaking()) {
                    if (toTeleport.size() < 4) { // Max 3 teammates + user
                        toTeleport.add(p);
                    }
                }
            }
        }

        Location targetLoc = target.getLocation();
        for (Player p : toTeleport) {
            p.teleport(targetLoc);
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 5 * 20, 1)); // Weakness II for 5s
            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Warped to " + target.getName() + " via Rift!");
        }

        // Calculate cooldown
        int cooldownSeconds = toTeleport.size() * 30;
        setCooldown(player, cooldownSeconds);
        player.sendMessage(ChatColor.YELLOW + "Rift used! Cooldown: " + cooldownSeconds + " seconds.");
    }

    private void setCooldown(Player player, int seconds) {
        long expiry = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.put(player.getUniqueId(), expiry);
        startCooldownTask(player, expiry);
    }

    private void startCooldownTask(Player player, long expiry) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                long remainingMillis = expiry - System.currentTimeMillis();
                if (remainingMillis <= 0) {
                    updateItemName(player, null); // Reset name
                    this.cancel();
                    return;
                }

                updateItemName(player, remainingMillis / 1000);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Update every second
    }

    private void updateItemName(Player player, Long remainingSeconds) {
        for (ItemStack item : player.getInventory().getContents()) {
            // Check type directly first to avoid isClassItem failing on already renamed
            // items if logic was strict,
            // but here we need to ensure we are targeting the right item.
            // Since we are fixing the name to match isClassItem, we can keep using
            // isClassItem check
            // BUT we must ensure the new name starts with the expected prefix.
            if (item != null && item.getType() == Material.BLAZE_ROD) {
                // We can't strictly rely on isClassItem if the name is already changed to
                // something unrecognized.
                // However, we are fixing it so it IS recognized.
                // But for the FIRST update, it is recognized.
                // For subsequent updates, if we changed it to GOLD, it wasn't recognized.
                // Now we change it to LIGHT_PURPLE "Rift Walker...", so it SHOULD be recognized
                // by isClassItem.

                if (plugin.isClassItem(item)) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        if (remainingSeconds == null) {
                            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Rift Walker" + ChatColor.GREEN + " READY");
                        } else {
                            meta.setDisplayName(
                                    ChatColor.LIGHT_PURPLE + "Rift Walker " + ChatColor.RED + remainingSeconds);
                        }
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }

    public boolean isOnCooldown(Player player) {
        return cooldowns.containsKey(player.getUniqueId())
                && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public long getCooldownRemaining(Player player) {
        if (!isOnCooldown(player))
            return 0;
        return (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
    }
}
