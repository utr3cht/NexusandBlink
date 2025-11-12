package com.example.annihilationnexus;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;

public class DasherAbility {

    private final Player player;
    private final AnnihilationNexus plugin;
    private Location liveTargetLocation;
    private BukkitTask visualizerTask;

    private Block visualizedBlock;
    private Material originalMaterial;
    private BlockData originalBlockData;

    public DasherAbility(Player player, AnnihilationNexus plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void startVisualizer() {
        plugin.getLogger().info("startVisualizer called for " + player.getName()); // DEBUG
        if (visualizerTask != null) {
            visualizerTask.cancel();
            revertVisualizedBlock(); // Revert previous block if task is cancelled
        }

        visualizerTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Self-termination check
                if (!player.isSneaking() || !plugin.isBlinkItem(player.getInventory().getItemInMainHand())) {
                    plugin.getLogger().info("Visualizer task self-terminating for " + player.getName() + ". isSneaking: " + player.isSneaking() + ", isBlinkItem: " + plugin.isBlinkItem(player.getInventory().getItemInMainHand())); // DEBUG
                    stopVisualizer();
                    return;
                }

                // Always revert the previous block first to ensure cleanup.
                revertVisualizedBlock();

                Location newTarget = calculateTeleportLocation();
                liveTargetLocation = newTarget; // Update the live target for the blink method

                if (newTarget != null) {
                    // Determine visualizer block material based on distance
                    double distance = player.getLocation().distance(newTarget);
                    Material visualizerMaterial;
                    if (distance <= 10) {
                        visualizerMaterial = Material.EMERALD_BLOCK;
                    } else if (distance <= 15) {
                        visualizerMaterial = Material.GOLD_BLOCK;
                    } else {
                        visualizerMaterial = Material.DIAMOND_BLOCK;
                    }

                    visualizedBlock = newTarget.getBlock().getRelative(0, -1, 0);
                    originalMaterial = visualizedBlock.getType();
                    originalBlockData = visualizedBlock.getBlockData();

                    // Set temporary block using packets
                    player.sendBlockChange(visualizedBlock.getLocation(), visualizerMaterial.createBlockData());
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Update every tick for smoother tracking
    }

    public void stopVisualizer() {
        plugin.getLogger().info("stopVisualizer called for " + player.getName()); // DEBUG
        if (visualizerTask != null) {
            visualizerTask.cancel();
            visualizerTask = null;
        }
        revertVisualizedBlock(); // Ensure block is reverted when visualizer stops
        liveTargetLocation = null;
    }

    public double blink() {
        // Use the location from the visualizer, not recalculate
        Location targetLocation = liveTargetLocation;

        if (targetLocation == null) {
            player.sendMessage("Cannot find a safe location to blink to.");
            return 0;
        }

        double distance = player.getLocation().distance(targetLocation);

        Location originalLocation = player.getLocation();

        // Preserve player's facing direction
        Location teleportDest = targetLocation.clone(); // Clone to avoid modifying liveTargetLocation
        teleportDest.setYaw(player.getLocation().getYaw());
        teleportDest.setPitch(player.getLocation().getPitch());

        player.teleport(teleportDest);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // Spawn particles
        spawnBlinkTrail(originalLocation, teleportDest);
        spawnDestinationCircle(teleportDest);

        // Stop and reset the visualizer state after a successful blink
        stopVisualizer();

        return distance;
    }

    private Location calculateTeleportLocation() {
        BlockIterator iterator = new BlockIterator(player.getEyeLocation(), 0, plugin.getDasherMaxBlinkDistance());
        Block targetBlock = null;
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (block.getType().isSolid()) {
                targetBlock = block;
                break;
            }
        }

        if (targetBlock == null) {
            return null;
        }

        Location targetLocation = targetBlock.getLocation();
        double distance = player.getLocation().distance(targetLocation);

        if (distance >= plugin.getDasherMinBlinkDistance()) {
            Location safeLocation = findSafeLocation(targetLocation);
            if (safeLocation != null) {
                return safeLocation.clone().add(0.5, 0, 0.5);
            }
        }
        return null;
    }

    private void spawnBlinkTrail(Location start, Location end) {
        World world = start.getWorld();
        if (world == null) return;

        double distance = start.distance(end);
        Vector direction = end.toVector().subtract(start.toVector()).normalize();
        double step = 0.25;

        for (double d = 0; d < distance; d += step) {
            Location particleLoc = start.clone().add(direction.clone().multiply(d));
            world.spawnParticle(Particle.CLOUD, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private void spawnDestinationCircle(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        double radius = 0.75;
        int points = 20;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location particleLoc = center.clone().add(x, 0.1, z);
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);

            // Falling particles
            Location fallingParticleLoc = center.clone().add(x, 1.5, z);
            world.spawnParticle(Particle.SPIT, fallingParticleLoc, 1, 0, 0, 0, 0);
        }
    }

    private void revertVisualizedBlock() {
        if (visualizedBlock != null && originalBlockData != null) {
            player.sendBlockChange(visualizedBlock.getLocation(), originalBlockData);
            visualizedBlock = null;
            originalMaterial = null;
            originalBlockData = null;
        }
    }

    private Location findSafeLocation(Location location) {
        for (int i = 0; i < 2; i++) {
            Location loc = location.clone().add(0, i, 0);
            if (isSafe(loc)) {
                return loc;
            }
        }
        return null;
    }

    private boolean isSafe(Location location) {
        Block belowFeet = location.clone().subtract(0, 1, 0).getBlock();
        Block feet = location.getBlock();
        Block head = location.clone().add(0, 1, 0).getBlock();
        return belowFeet.getType().isSolid() && !feet.getType().isSolid() && !head.getType().isSolid();
    }
}