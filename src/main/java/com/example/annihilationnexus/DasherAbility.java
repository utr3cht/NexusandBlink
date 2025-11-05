package com.example.annihilationnexus;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DasherAbility {

    private final Player player;
    private final AnnihilationNexus plugin;
    private Location liveTargetLocation;
    private BukkitTask visualizerTask;

    private Block visualizedBlock;
    private Material originalMaterial;
    private BlockData originalBlockData;

    private static final int MIN_BLINK_DISTANCE = 5;
    private static final int MAX_BLINK_DISTANCE = 20;

    public DasherAbility(Player player, AnnihilationNexus plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void startVisualizer() {
        if (visualizerTask != null) {
            visualizerTask.cancel();
            revertVisualizedBlock(); // Revert previous block if task is cancelled
        }

        visualizerTask = new BukkitRunnable() {
            @Override
            public void run() {
                Block targetBlock = player.getTargetBlock(null, MAX_BLINK_DISTANCE);
                Location targetLocation = targetBlock.getLocation();
                double distance = player.getLocation().distance(targetLocation);

                if (distance >= MIN_BLINK_DISTANCE) {
                    Location safeLocation = findSafeLocation(targetLocation);
                    if (safeLocation != null) {
                        Location newLiveTargetLocation = safeLocation.clone().add(0.5, 0, 0.5);

                        // Only update block if target location has changed
                        if (liveTargetLocation == null || !liveTargetLocation.equals(newLiveTargetLocation)) {
                            revertVisualizedBlock(); // Revert old block

                            liveTargetLocation = newLiveTargetLocation;
                            
                            // Determine visualizer block material based on distance
                            Material visualizerMaterial;
                            if (distance < 10) {
                                visualizerMaterial = Material.GREEN_STAINED_GLASS;
                            } else if (distance < 15) {
                                visualizerMaterial = Material.YELLOW_STAINED_GLASS;
                            } else {
                                visualizerMaterial = Material.RED_STAINED_GLASS;
                            }

                            visualizedBlock = liveTargetLocation.getBlock().getRelative(0, -1, 0); // Place block one below player's feet
                            originalMaterial = visualizedBlock.getType();
                            originalBlockData = visualizedBlock.getBlockData();

                            // Set temporary block
                            visualizedBlock.setType(visualizerMaterial);
                        }
                    } else {
                        revertVisualizedBlock();
                        liveTargetLocation = null;
                    }
                } else {
                    revertVisualizedBlock();
                    liveTargetLocation = null;
                }
            }
        }.runTaskTimer(plugin, 0, 5); // Update every 5 ticks
    }

    public void stopVisualizer() {
        if (visualizerTask != null) {
            visualizerTask.cancel();
            visualizerTask = null;
        }
        revertVisualizedBlock(); // Ensure block is reverted when visualizer stops
        liveTargetLocation = null;
    }

    public double blink() {
        if (liveTargetLocation == null) {
            player.sendMessage("Cannot find a safe location to blink to.");
            return 0;
        }

        double distance = player.getLocation().distance(liveTargetLocation);
        revertVisualizedBlock(); // Revert block immediately before teleporting

        // Preserve player's facing direction
        liveTargetLocation.setYaw(player.getLocation().getYaw());
        liveTargetLocation.setPitch(player.getLocation().getPitch());

        player.teleport(liveTargetLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return distance;
    }

    private void revertVisualizedBlock() {
        if (visualizedBlock != null && originalMaterial != null && originalBlockData != null) {
            visualizedBlock.setType(originalMaterial);
            visualizedBlock.setBlockData(originalBlockData);
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
