package com.example.annihilationnexus;

import org.bukkit.block.data.type.Farmland;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;

public class FarmerListener implements Listener {

    private final AnnihilationNexus plugin;
    private final PlayerClassManager playerClassManager;
    private final ProtectedCropManager protectedCropManager;
    private final Random random = new Random();

    public FarmerListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager, ProtectedCropManager protectedCropManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
        this.protectedCropManager = protectedCropManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Farmland trampling prevention
        if (plugin.getPreventFarmlandTrample() && event.getAction() == Action.PHYSICAL) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.FARMLAND) {
                event.setCancelled(true);
                return;
            }
        }

        Player player = event.getPlayer();
        String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());

        if (playerClass == null || !playerClass.equalsIgnoreCase("farmer")) {
            return;
        }

        Action action = event.getAction();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item == null) return;

            FarmerAbility ability = playerClassManager.getFarmerAbility(player.getUniqueId());
            if (ability == null) return;

            if (plugin.isFeastItem(item)) {
                ability.useFeast(player);
                event.setCancelled(true);
            } else if (plugin.isFamineItem(item)) {
                ability.useFamine(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();

        // --- Crop Protection Check ---
        if (protectedCropManager.isProtected(blockLocation)) {
            plugin.getLogger().info("[DEBUG] BlockBreakEvent at a protected location: " + blockLocation);
            // Protection is active, only the planter can break it, but only if it's fully grown.
            // If it's not fully grown, no one can break it.
            if (block.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) block.getBlockData();
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This crop is still growing and cannot be broken.");
                    plugin.getLogger().info("[DEBUG] Cancelled break event: Crop not fully grown.");
                    return;
                }
            }
            // If it IS fully grown, the code proceeds, and the crop will be broken and replanted below.
            plugin.getLogger().info("[DEBUG] Protected crop is fully grown, allowing break.");
        }
        // --- End Crop Protection Check ---

        String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
        if (playerClass == null || !playerClass.equalsIgnoreCase("farmer")) {
            return;
        }

        Material type = block.getType();

        // Handle Crop Breaking
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() == ageable.getMaximumAge()) {
                plugin.getLogger().info("[DEBUG] Farmer breaking fully grown crop at: " + blockLocation);
                // It's a fully grown crop. Remove any existing protection before replanting.
                // This ensures the growth task is cancelled and can be restarted for the new crop.
                if (protectedCropManager.isProtected(blockLocation)) {
                    plugin.getLogger().info("[DEBUG] Removing existing protection before replanting.");
                    protectedCropManager.removeCrop(blockLocation);
                }

                // Auto-replant
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getLogger().info("[DEBUG] Running auto-replant task for: " + blockLocation);
                        Block blockBelow = block.getRelative(0, -1, 0);
                        if (blockBelow.getType() != Material.FARMLAND) {
                            blockBelow.setType(Material.FARMLAND);
                        }
                        Farmland farmland = (Farmland) blockBelow.getBlockData();
                        farmland.setMoisture(farmland.getMaximumMoisture());
                        blockBelow.setBlockData(farmland);

                        block.setType(type);
                        Ageable newCrop = (Ageable) block.getBlockData();
                        newCrop.setAge(0); // Set age to 0 for natural growth
                        block.setBlockData(newCrop);

                        // Add to protected crops after replanting
                        plugin.getLogger().info("[DEBUG] Adding new protection after replanting.");
                        protectedCropManager.addCrop(blockLocation, player.getUniqueId());
                    }
                }.runTaskLater(plugin, plugin.getAutoReplantDelay() * 20L);

                // Extra Drops from Crops
                for (AnnihilationNexus.DropInfo dropInfo : plugin.getCustomCropDrops()) {
                    if (random.nextDouble() < dropInfo.chance()) {
                        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropInfo.material()));
                    }
                }
            }
        }

        // Handle Grass Breaking
        if (type == org.bukkit.Material.SHORT_GRASS || type == org.bukkit.Material.TALL_GRASS) {
            if (random.nextDouble() < plugin.getGrassSeedChance()) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.WHEAT_SEEDS));
            }
            if (random.nextDouble() < plugin.getGrassPotatoChance()) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.POTATO));
            }
            if (random.nextDouble() < plugin.getGrassCarrotChance()) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.CARROT));
            }
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();

        if (protectedCropManager.isProtected(location)) {
            plugin.getLogger().info("[DEBUG] BlockGrowEvent at protected location: " + location);
            if (event.getNewState().getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) event.getNewState().getBlockData();
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    // Crop is fully grown, remove protection
                    plugin.getLogger().info("[DEBUG] Crop reached max age naturally. Removing protection.");
                    protectedCropManager.removeCrop(location);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
        if (playerClass == null || !playerClass.equalsIgnoreCase("farmer")) {
            return;
        }

        if (event.getItem().getType().isEdible()) {
            if (random.nextDouble() < plugin.getEatingBonusChance()) {
                double newHealth = Math.min(player.getHealth() + plugin.getEatingBonusExtraHealth(), player.getMaxHealth());
                player.setHealth(newHealth);
                player.setFoodLevel(Math.min(player.getFoodLevel() + plugin.getEatingBonusExtraFood(), 20));
            }
        }
    }
}