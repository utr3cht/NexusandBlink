package com.example.annihilationnexus;

import org.bukkit.Bukkit;
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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Random;
import java.util.UUID;
import java.util.Set;
import java.util.EnumSet;

public class FarmerListener implements Listener {

    private final AnnihilationNexus plugin;
    private final PlayerClassManager playerClassManager;
    private final ProtectedCropManager protectedCropManager;
    private final Random random = new Random();

    private static final Set<Material> ALLOWED_CROPS = EnumSet.of(
            Material.WHEAT,
            Material.CARROTS,
            Material.POTATOES,
            Material.BEETROOTS,
            Material.NETHER_WART,
            Material.MELON,
            Material.PUMPKIN);

    public FarmerListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager,
            ProtectedCropManager protectedCropManager) {
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
            if (item == null)
                return;

            FarmerAbility ability = playerClassManager.getFarmerAbility(player.getUniqueId());
            if (ability == null)
                return;

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

        // --- Farmland Protection Check ---
        if (block.getType() == Material.FARMLAND) {
            Block blockAbove = block.getRelative(0, 1, 0);
            if (protectedCropManager.isProtected(blockAbove.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot break the soil under a protected crop!");
                return;
            }
        }
        // --- End Farmland Protection Check ---

        // --- Crop Protection Check ---
        if (protectedCropManager.isProtected(blockLocation)) {
            ProtectedCropInfo cropInfo = protectedCropManager.getCropInfo(blockLocation);
            if (cropInfo == null) {
                return;
            }

            UUID planterUUID = cropInfo.getPlanterUUID();
            UUID breakerUUID = player.getUniqueId();

            // Planter can always break their own crops, so we only check if it's someone
            // else
            if (!planterUUID.equals(breakerUUID)) {
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Team planterTeam = scoreboard.getEntryTeam(Bukkit.getOfflinePlayer(planterUUID).getName());
                Team breakerTeam = scoreboard.getEntryTeam(player.getName());

                // If they are on the same team
                if (planterTeam != null && planterTeam.equals(breakerTeam)) {
                    // Teammates can only break fully grown crops
                    if (block.getBlockData() instanceof Ageable) {
                        Ageable ageable = (Ageable) block.getBlockData();
                        if (ageable.getAge() < ageable.getMaximumAge()) {
                            event.setCancelled(true);
                            player.sendMessage(
                                    ChatColor.RED + "This crop is still growing and cannot be broken by teammates.");
                            return;
                        }
                    }
                } else {
                    // Enemy teams can always break protected crops.
                    // We remove the protection so it doesn't get auto-replanted by the original
                    // farmer's logic.
                    protectedCropManager.removeCrop(blockLocation);
                }
            }
        }
        // --- End Crop Protection Check ---

        String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
        if (playerClass == null || !playerClass.equalsIgnoreCase("farmer")) {
            return;
        }

        Material type = block.getType();

        // Handle Crop Breaking
        if (block.getBlockData() instanceof Ageable) {
            // Fix: Check if it is an allowed crop
            if (!ALLOWED_CROPS.contains(type)) {
                return;
            }

            Ageable ageable = (Ageable) block.getBlockData();
            if (ageable.getAge() == ageable.getMaximumAge()) {
                // It's a fully grown crop. Remove any existing protection before replanting.
                if (protectedCropManager.isProtected(blockLocation)) {
                    protectedCropManager.removeCrop(blockLocation);
                }

                // Auto-replant
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Block blockBelow = block.getRelative(0, -1, 0);

                        // Special handling for Nether Wart
                        if (type == Material.NETHER_WART) {
                            if (blockBelow.getType() != Material.SOUL_SAND) {
                                blockBelow.setType(Material.SOUL_SAND);
                            }
                        } else {
                            // Default to Farmland for other crops
                            if (blockBelow.getType() != Material.FARMLAND) {
                                blockBelow.setType(Material.FARMLAND);
                            }
                            // Only set moisture if it is farmland
                            if (blockBelow.getType() == Material.FARMLAND) {
                                Farmland farmland = (Farmland) blockBelow.getBlockData();
                                farmland.setMoisture(farmland.getMaximumMoisture());
                                blockBelow.setBlockData(farmland);
                            }
                        }

                        block.setType(type);
                        if (block.getBlockData() instanceof Ageable) {
                            Ageable newCrop = (Ageable) block.getBlockData();
                            newCrop.setAge(0); // Set age to 0 for natural growth
                            block.setBlockData(newCrop);
                        }

                        // Add to protected crops after replanting
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
            if (event.getNewState().getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) event.getNewState().getBlockData();
                if (ageable.getAge() == ageable.getMaximumAge()) {
                    // Crop is fully grown, remove protection
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
                double newHealth = Math.min(player.getHealth() + plugin.getEatingBonusExtraHealth(),
                        player.getMaxHealth());
                player.setHealth(newHealth);
                player.setFoodLevel(Math.min(player.getFoodLevel() + plugin.getEatingBonusExtraFood(), 20));
            }
        }
    }
}