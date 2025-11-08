package com.example.annihilationnexus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;

public class DasherAbility {

    private final Player player;
    private final AnnihilationNexus plugin;
    private BukkitTask visualizerTask;
    private Location lastVisualizedLocation;
    private Material lastVisualizedMaterial;

    private static final int MAX_BLINK_DISTANCE = 20;
    private static final int MIN_BLINK_DISTANCE = 4;

    public DasherAbility(Player player, AnnihilationNexus plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public double blink() {
        Location targetLocation = calculateTeleportLocation();
        if (targetLocation == null) {
            player.sendMessage("No valid blink location found.");
            return 0;
        }

        double distance = player.getLocation().distance(targetLocation);
        if (distance < MIN_BLINK_DISTANCE) {
            player.sendMessage("Blink target is too close.");
            return 0;
        }

        // Stop the visualizer before blinking
        stopVisualizer();

        // Capture current pitch and yaw
        float pitch = player.getLocation().getPitch();
        float yaw = player.getLocation().getYaw();

        // Apply the captured pitch and yaw to the target location
        targetLocation.setPitch(pitch);
        targetLocation.setYaw(yaw);

        // Perform the blink
        Location originalLocation = player.getLocation();
        player.teleport(targetLocation);
        player.playSound(targetLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Visual effects
        spawnBlinkTrail(originalLocation, targetLocation);
        spawnDestinationCircle(targetLocation);

        return distance;
    }

    public void startVisualizer() {
        if (visualizerTask != null && !visualizerTask.isCancelled()) {
            return; // Visualizer is already running
        }

        visualizerTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Stop if player is no longer sneaking or holding the blink item
                if (!player.isSneaking() || !plugin.isBlinkItem(player.getInventory().getItemInMainHand())) {
                    stopVisualizer();
                    return;
                }

                Location target = calculateTeleportLocation();
                if (target != null) {
                    visualizeBlinkLocation(target);
                } else {
                    revertVisualizedBlock(); // No valid location, clear any existing visualization
                }
            }
        }.runTaskTimer(plugin, 0, 5); // Run every 5 ticks (0.25 seconds)
    }

    public void stopVisualizer() {
        if (visualizerTask != null) {
            visualizerTask.cancel();
            visualizerTask = null;
        }
        revertVisualizedBlock();
    }

    private void visualizeBlinkLocation(Location location) {
        revertVisualizedBlock(); // Revert the previous block before visualizing a new one

        Block blockBelow = location.clone().subtract(0, 1, 0).getBlock();
        if (blockBelow.getType() == Material.AIR || blockBelow.getType() == Material.WATER) {
            return; // Don't visualize on non-solid ground
        }

        this.lastVisualizedLocation = blockBelow.getLocation();
        this.lastVisualizedMaterial = blockBelow.getType();

        double distance = player.getLocation().distance(location);
        Material visualMaterial;

        if (distance <= 8) {
            visualMaterial = Material.EMERALD_BLOCK;
        } else if (distance <= 14) {
            visualMaterial = Material.GOLD_BLOCK;
        } else {
            visualMaterial = Material.DIAMOND_BLOCK;
        }

        player.sendBlockChange(lastVisualizedLocation, visualMaterial.createBlockData());
    }

    private void revertVisualizedBlock() {
        if (lastVisualizedLocation != null && lastVisualizedMaterial != null) {
            player.sendBlockChange(lastVisualizedLocation, lastVisualizedMaterial.createBlockData());
            lastVisualizedLocation = null;
            lastVisualizedMaterial = null;
        }
    }

    private Location calculateTeleportLocation() {
        BlockIterator iterator = new BlockIterator(player.getEyeLocation(), 0, MAX_BLINK_DISTANCE);
        Block lastNonSolidBlock = null;

        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (block.getType().isSolid()) {
                if (lastNonSolidBlock != null) {
                    return findSafeLocation(lastNonSolidBlock.getLocation());
                }
                return null; // Ran into a wall immediately
            }
            lastNonSolidBlock = block;
        }

        if (lastNonSolidBlock != null) {
            return findSafeLocation(lastNonSolidBlock.getLocation());
        }

        return null;
    }

    private Location findSafeLocation(Location location) {
        // Check upwards from the target for a 2-block high space
        for (int i = 0; i < 5; i++) {
            Location checkLoc = location.clone().add(0, i, 0);
            if (isSafe(checkLoc)) {
                return checkLoc;
            }
        }
        return null;
    }

    private boolean isSafe(Location location) {
        Block ground = location.clone().subtract(0, 1, 0).getBlock();
        Block feet = location.getBlock();
        Block head = location.clone().add(0, 1, 0).getBlock();

        return ground.getType().isSolid() && !feet.getType().isSolid() && !head.getType().isSolid() && feet.getType() != Material.WATER;
    }

    private void spawnBlinkTrail(Location start, Location end) {
        // A simple particle trail
        new BukkitRunnable() {
            double t = 0;
            @Override
            public void run() {
                t += 0.2;
                if (t > 1) {
                    this.cancel();
                    return;
                }
                double x = (1 - t) * start.getX() + t * end.getX();
                double y = (1 - t) * start.getY() + t * end.getY();
                double z = (1 - t) * start.getZ() + t * end.getZ();
                player.getWorld().spawnParticle(Particle.CLOUD, x, y, z, 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void spawnDestinationCircle(Location location) {
        // A particle circle at the destination
        new BukkitRunnable() {
            double angle = 0;
            @Override
            public void run() {
                if (angle >= 360) {
                    this.cancel();
                    return;
                }
                double x = location.getX() + Math.cos(Math.toRadians(angle)) * 0.5;
                double z = location.getZ() + Math.sin(Math.toRadians(angle)) * 0.5;
                player.getWorld().spawnParticle(Particle.END_ROD, x, location.getY(), z, 1, 0, 0, 0, 0);
                player.getWorld().spawnParticle(Particle.SPIT, x, location.getY(), z, 1, 0, 0, 0, 0);
                angle += 30;
            }
        }.runTaskTimer(plugin, 0, 1);
    }
}
