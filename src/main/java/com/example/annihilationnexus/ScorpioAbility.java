package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.bukkit.scheduler.BukkitTask;

public class ScorpioAbility {

    private final Player player;
    private final AnnihilationNexus plugin;
    private long lastAbilityTime = 0;
    private boolean isStuck = false; // New field to track if the hook is stuck
    private BukkitTask stuckTask = null; // New field to hold the stuck task
    private Item currentHook = null; // New field to track the current hook item

    public ScorpioAbility(Player player, AnnihilationNexus plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void use(boolean isRightClick) {
        // If there's an existing hook, remove it and cancel its task
        if (currentHook != null && currentHook.isValid()) {
            currentHook.remove();
            if (stuckTask != null) {
                stuckTask.cancel();
                stuckTask = null;
            }
            isStuck = false;
        }

        if (System.currentTimeMillis() - lastAbilityTime < plugin.getScorpioHookCooldown() * 1000L) {
            return;
        }

        lastAbilityTime = System.currentTimeMillis();

        ItemStack hookItem = plugin.getScorpioItem();
        Item hook = player.getWorld().dropItem(player.getEyeLocation(), hookItem);
        hook.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));
        hook.setPickupDelay(Integer.MAX_VALUE);
        hook.setOwner(player.getUniqueId());
        this.currentHook = hook; // Set the new hook as the current one

        new BukkitRunnable() {
            @Override
            public void run() {
                if (hook.isDead() || !hook.isValid()) {
                    this.cancel();
                    return;
                }

                // Player and Mob collision check
                for (Entity entity : hook.getNearbyEntities(1.0, 1.0, 1.0)) {
                    if (entity instanceof org.bukkit.entity.LivingEntity && !entity.equals(player)) {
                        org.bukkit.entity.LivingEntity hitEntity = (org.bukkit.entity.LivingEntity) entity;

                        if (hitEntity instanceof Player) {
                            Player hitPlayer = (Player) hitEntity;
                            if (isRightClick) { // Right-click pulls enemy to you
                                if (!isFriendly(hitPlayer)) {
                                    pullEntity(hitPlayer);
                                    hook.remove();
                                    this.cancel();
                                    return;
                                }
                            } else { // Left-click pulls you to any hit player
                                if (isFriendly(hitPlayer)) {
                                    pullToTarget(hitPlayer);
                                    hook.remove();
                                    this.cancel();
                                    return;
                                }
                            }
                        } else {
                            // It's a mob
                            if (isRightClick) {
                                pullEntity(hitEntity); // Pull mob to player
                            } else {
                                pullToTarget(hitEntity); // Pull player to mob
                            }
                            hook.remove();
                            this.cancel();
                            return;
                        }
                    }
                }

                // Ground collision check
                if (hook.isOnGround()) {
                    isStuck = true;
                    // Schedule the stuck task
                    stuckTask = new BukkitRunnable() {
                        private int ticks = 0;
                        private final int maxTicks = (int) (plugin.getScorpioStuckDuration() * 20); // Convert seconds
                                                                                                    // to ticks

                        @Override
                        public void run() {
                            if (hook.isDead() || !hook.isValid() || ticks >= maxTicks) {
                                hook.remove();
                                currentHook = null;
                                isStuck = false;
                                this.cancel();
                                return;
                            }

                            // Continuous pulling logic
                            for (Entity entity : hook.getNearbyEntities(1.5, 1.5, 1.5)) { // Check a slightly larger
                                                                                          // radius
                                if (entity instanceof org.bukkit.entity.LivingEntity && !entity.equals(player)) {
                                    org.bukkit.entity.LivingEntity affectedEntity = (org.bukkit.entity.LivingEntity) entity;

                                    if (affectedEntity instanceof Player) {
                                        Player affectedPlayer = (Player) affectedEntity;
                                        if (isRightClick) { // Right-click pulls enemy to hook
                                            if (!isFriendly(affectedPlayer)) {
                                                pullEntityToHook(affectedPlayer, hook.getLocation());
                                                // Remove hook after successful pull
                                                hook.remove();
                                                currentHook = null;
                                                isStuck = false;
                                                this.cancel();
                                                return;
                                            }
                                        } else { // Left-click pulls player to hook
                                            pullPlayerToHook(player, hook.getLocation());
                                            // Remove hook after successful pull
                                            hook.remove();
                                            currentHook = null;
                                            isStuck = false;
                                            this.cancel();
                                            return;
                                        }
                                    } else {
                                        // Mob logic
                                        if (isRightClick) {
                                            pullEntityToHook(affectedEntity, hook.getLocation());
                                        } else {
                                            pullPlayerToHook(player, hook.getLocation());
                                        }
                                        // Remove hook after successful pull
                                        hook.remove();
                                        currentHook = null;
                                        isStuck = false;
                                        this.cancel();
                                        return;
                                    }
                                }
                            }
                            ticks++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L); // Run every tick

                    this.cancel(); // Cancel the initial hook movement runnable
                    return;
                }

                // Particle trail
                player.spawnParticle(Particle.CRIT, hook.getLocation(), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void pullEntity(org.bukkit.entity.LivingEntity entity) {
        Location pullLocation = player.getLocation();
        if (!isSafeLocation(pullLocation)) {
            player.sendMessage("Not enough space to pull the target.");
            return;
        }

        entity.teleport(pullLocation.add(0, 1.5, 0));
        entity.setFallDistance(0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                entity.setFallDistance(0);
            }
        }.runTaskLater(plugin, 20L * plugin.getScorpioEnemyPullFallImmunity());
    }

    public void pullToTarget(org.bukkit.entity.LivingEntity target) {
        if (isHalfBlockInFront()) {
            return;
        }

        double distance = player.getLocation().distance(target.getLocation());
        double distanceMultiplier = plugin.getScorpioDistancePullMultiplier();
        double pullStrength = Math.min(1.0 + (distance * distanceMultiplier), 2.5); // Reduced max strength from 3.0 to
                                                                                    // 2.5

        Vector vector = target.getLocation().toVector().subtract(player.getLocation().toVector());
        vector.normalize().multiply(pullStrength);

        // Rubber band fix: Add a small vertical boost
        vector.setY(vector.getY() + 0.4);

        player.setVelocity(vector);
        player.setFallDistance(-10f);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setFallDistance(0);
            }
        }.runTaskLater(plugin, 20L * plugin.getScorpioFriendlyPullFallImmunity());
    }

    private boolean isSafeLocation(Location location) {
        return location.clone().add(0, 1, 0).getBlock().isPassable() &&
                location.clone().add(0, 2, 0).getBlock().isPassable() &&
                location.clone().add(0, 3, 0).getBlock().isPassable();
    }

    private boolean isHalfBlockInFront() {
        Location front = player.getLocation().add(player.getLocation().getDirection().normalize());
        return front.getBlock().getType().toString().contains("SLAB");
    }

    private boolean isFriendly(Player otherPlayer) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team casterTeam = scoreboard.getEntryTeam(this.player.getName());
        Team targetTeam = scoreboard.getEntryTeam(otherPlayer.getName());

        // If either player is not on a team, they are not friendly.
        // If both are on a team, check if the teams are the same.
        if (casterTeam != null && casterTeam.equals(targetTeam)) {
            return true;
        }

        return false;
    }

    // New helper method to pull an entity towards the hook's location
    private void pullEntityToHook(org.bukkit.entity.LivingEntity entity, Location hookLocation) {
        Location pullLocation = hookLocation.clone();
        if (!isSafeLocation(pullLocation)) {
            // Optionally send a message or log if not enough space
            return;
        }

        entity.teleport(pullLocation.add(0, 1.5, 0));
        entity.setFallDistance(0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                entity.setFallDistance(0);
            }
        }.runTaskLater(plugin, 20L * plugin.getScorpioEnemyPullFallImmunity());
    }

    // New helper method to pull the player towards the hook's location
    private void pullPlayerToHook(Player playerToPull, Location hookLocation) {
        if (isHalfBlockInFront()) { // This check might need to be re-evaluated for pulling to hook
            return;
        }

        double distance = playerToPull.getLocation().distance(hookLocation);
        double distanceMultiplier = plugin.getScorpioDistancePullMultiplier();
        double pullStrength = Math.min(1.0 + (distance * distanceMultiplier), 3.0); // Base strength 1.0, max 3.0

        Vector vector = hookLocation.toVector().subtract(playerToPull.getLocation().toVector());
        playerToPull.setVelocity(vector.normalize().multiply(pullStrength));
        playerToPull.setFallDistance(-10f);

        new BukkitRunnable() {
            @Override
            public void run() {
                playerToPull.setFallDistance(0);
            }
        }.runTaskLater(plugin, 20L * plugin.getScorpioFriendlyPullFallImmunity());
    }
}
