package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set; // Import Set
import java.util.HashSet; // Import HashSet
import java.util.UUID;

public class TransporterAbility {

    private final AnnihilationNexus plugin;
    // Map to store player's portal selections: Player UUID -> {pos1, pos2}
    private final Map<UUID, Location> playerPortalSelections = new HashMap<>();
    // Map to store active portals: Portal ID -> {Location1, Location2}
    private static final Map<UUID, Map<Location, Location>> activePortals = new HashMap<>(); // Player UUID -> {Portal Location -> Linked Location}
    // Map to track active teleportation tasks for each player
    private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();
    // Map to track continuous particle tasks for each player's portal selection
    private final Map<UUID, Map<Location, BukkitTask>> continuousParticleTasks = new HashMap<>();
    private final Set<UUID> portalCreationCooldown = new HashSet<>(); // Cooldown set

    // Metadata keys
    private static final String PORTAL_METADATA_KEY = "TransporterPortal";
    private static final String PORTAL_OWNER_METADATA_KEY = "TransporterPortalOwner";
    private static final String PORTAL_LINK_METADATA_KEY = "TransporterPortalLink";
        private static final String PORTAL_ORIGINAL_BLOCK_DATA_KEY = "TransporterPortalOriginalBlockData";
        private static final String PORTAL_PARTICLE_TASK_ID_KEY = "TransporterPortalParticleTaskID";
    
            private static final Set<Material> unsuitableMaterials;
            private static final Set<Material> inventoryBlocks;
        
            static {
                unsuitableMaterials = java.util.stream.Stream.of(
                        Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
                        Material.DIRT, Material.COARSE_DIRT, Material.PODZOL,
                        Material.SAND, Material.RED_SAND, Material.GRAVEL,
                        Material.WATER, Material.LAVA,
                        Material.OBSIDIAN, Material.BEACON,
                        Material.getMaterial("TALL_GRASS"), Material.getMaterial("GRASS"), Material.getMaterial("FERN"), Material.getMaterial("LARGE_FERN"),
                        Material.getMaterial("DANDELION"), Material.getMaterial("POPPY"), Material.getMaterial("BLUE_ORCHID"), Material.getMaterial("ALLIUM"), Material.getMaterial("AZURE_BLUET"),
                        Material.getMaterial("RED_TULIP"), Material.getMaterial("ORANGE_TULIP"), Material.getMaterial("WHITE_TULIP"), Material.getMaterial("PINK_TULIP"),
                        Material.getMaterial("OXEYE_DAISY"), Material.getMaterial("CORNFLOWER"), Material.getMaterial("LILY_OF_THE_VALLEY"), Material.getMaterial("WITHER_ROSE"),
                        Material.getMaterial("SUNFLOWER"), Material.getMaterial("LILAC"), Material.getMaterial("ROSE_BUSH"), Material.getMaterial("PEONY"),
                        Material.getMaterial("SUGAR_CANE"), Material.getMaterial("CACTUS"), Material.getMaterial("KELP"), Material.getMaterial("SEAGRASS"), Material.getMaterial("TALL_SEAGRASS"),
                        Material.getMaterial("VINE"), Material.getMaterial("LADDER"), Material.getMaterial("TRIPWIRE"), Material.getMaterial("STRING"),
                        Material.getMaterial("TORCH"), Material.getMaterial("WALL_TORCH"), Material.getMaterial("REDSTONE_TORCH"), Material.getMaterial("REDSTONE_WALL_TORCH"),
                        Material.getMaterial("LEVER"), Material.getMaterial("STONE_BUTTON"), Material.getMaterial("WOODEN_BUTTON"),
                        Material.getMaterial("RAIL"), Material.getMaterial("POWERED_RAIL"), Material.getMaterial("DETECTOR_RAIL"), Material.getMaterial("ACTIVATOR_RAIL"),
                        Material.getMaterial("SNOW"), Material.getMaterial("SNOW_BLOCK"),
                        Material.getMaterial("COBWEB"), Material.getMaterial("FIRE"), Material.getMaterial("SOUL_FIRE")
                ).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        
                inventoryBlocks = java.util.stream.Stream.of(
                        Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
                        Material.SHULKER_BOX, Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX,
                        Material.CYAN_SHULKER_BOX, Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX,
                        Material.LIGHT_GRAY_SHULKER_BOX, Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX,
                        Material.PINK_SHULKER_BOX, Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX,
                        Material.YELLOW_SHULKER_BOX, Material.BARREL, Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
                        Material.HOPPER, Material.DROPPER, Material.DISPENSER, Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE,
                        Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.LOOM, Material.STONECUTTER,
                        Material.GRINDSTONE, Material.CARTOGRAPHY_TABLE, Material.SMITHING_TABLE, Material.BREWING_STAND
                ).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
            }
        
            public TransporterAbility(AnnihilationNexus plugin) {
                this.plugin = plugin;
            }
        
            public void handlePortalCreation(Player player, Block clickedBlock) {
                // Check for post-death portal creation cooldown
                if (plugin.getPlayerClassManager().isPlayerInPostDeathPortalCooldown(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You cannot create portals after death until you respawn or re-select your class.");
                    return;
                }
        
                // Cooldown check to prevent double-processing of clicks
                if (portalCreationCooldown.contains(player.getUniqueId())) {
                    return;
                }
                portalCreationCooldown.add(player.getUniqueId());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        portalCreationCooldown.remove(player.getUniqueId());
                    }
                }.runTaskLater(plugin, 5L); // 5 ticks cooldown (0.25 seconds)
        
                Location blockLocation = clickedBlock.getLocation();
        
                // Check if the block is already a portal
                if (isPortalBlock(clickedBlock)) {
                    player.sendMessage(ChatColor.RED + "This block is already part of a portal.");
                    return;
                }
        
                // Check placement restrictions
                if (plugin.getClassRegionManager().isLocationInRestrictedRegion(blockLocation)) { // Changed from isLocationInAllowedRegion
                    player.sendMessage(ChatColor.RED + "You cannot place portals in class command restricted areas.");
                    return;
                }
                if (blockLocation.getBlock().getType() == Material.WATER || blockLocation.getBlock().getType() == Material.LAVA) {
                    player.sendMessage(ChatColor.RED + "You cannot place portals underwater or in lava.");
                    return;
                }
        
                if (unsuitableMaterials.contains(clickedBlock.getType())) {
                    player.sendMessage(ChatColor.RED + "You cannot place portals on " + clickedBlock.getType().name().toLowerCase().replace("_", " ") + ".");
                    return;
                }
        
                if (inventoryBlocks.contains(clickedBlock.getType())) {
                    player.sendMessage(ChatColor.RED + "You cannot place portals on inventory blocks like chests.");
                    return;
                }
        
                // Store the first position
                if (!playerPortalSelections.containsKey(player.getUniqueId())) {
                    // If player already has active portals, destroy them before creating new ones
                    if (activePortals.containsKey(player.getUniqueId())) {
                        Map<Location, Location> playerActivePortals = activePortals.get(player.getUniqueId());
                        // Create a copy to avoid ConcurrentModificationException
                                        new HashMap<>(playerActivePortals).keySet().forEach(loc -> destroyPortal(loc.getBlock()));
                                        resetPortalMakerItemName(player);
                                        player.sendMessage(ChatColor.YELLOW + "Your previous Transporter portals have been destroyed.");
                                    }    
                    playerPortalSelections.put(player.getUniqueId(), blockLocation);
                    player.sendMessage(ChatColor.YELLOW + "Portal position 1 set. Right-click another block to set position 2.");
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.0f); // Blaze damage sound
        
                    // Immediately change the block to NETHER_QUARTZ_ORE and set metadata for the first portal
                    setPortalMetadata(clickedBlock, player.getUniqueId(), null); // Link is null for now
                    clickedBlock.setType(Material.NETHER_QUARTZ_ORE);
        
                                    // Update item name
        
                                    ItemStack heldItem = player.getInventory().getItemInMainHand();
        
                                    if (heldItem.getType() == plugin.getTransporterItem().getType()) {
        
                                        ItemMeta meta = heldItem.getItemMeta();
        
                                        if (meta != null) {
        
                                            meta.setDisplayName(ChatColor.GREEN + "Transporter" + ChatColor.GRAY + " - Entrance Placed");
        
                                            heldItem.setItemMeta(meta);
        
                                        }
        
                                    }
        
                                    // Start continuous particle effect for the first selected block
        
                                    startContinuousPortalParticleEffect(blockLocation, Particle.SMOKE, player.getUniqueId());
        
                                } else {
        
                                    // Second position is set, attempt to create portal
        
                                    Location pos1 = playerPortalSelections.remove(player.getUniqueId());
        
                                    // Stop continuous particle effect for the first selected block
        
                                    stopContinuousParticleEffect(player.getUniqueId());
        
                                    Location pos2 = blockLocation;
        
                        
        
                                    // Distance check
        
                                    if (pos1.distance(pos2) > plugin.getTpDistanceLimit()) {
        
                                        player.sendMessage(ChatColor.RED + "Portals are too far apart! Max distance: " + plugin.getTpDistanceLimit());
        
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        
                                        forceDestroyPortal(plugin, pos1.getBlock()); // Revert pos1
        
                                        resetPortalMakerItemName(player);
        
                                        return;
        
                                    }
        
                        
        
                                    // Ensure both blocks are in the same world
        
                                    if (!pos1.getWorld().equals(pos2.getWorld())) {
        
                                        player.sendMessage(ChatColor.RED + "Portals must be in the same world!");
        
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        
                                        forceDestroyPortal(plugin, pos1.getBlock()); // Revert pos1
        
                                        resetPortalMakerItemName(player);
        
                                        return;
        
                                    }
        
                        
        
                                    // Create the portals
        
                                    createLinkedPortals(player, pos1, pos2);
        
                                    player.sendMessage(ChatColor.GREEN + "Portals created successfully!");
        
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
                                    // Update item name
        
                                    ItemStack heldItem = player.getInventory().getItemInMainHand();
        
                                    if (heldItem.getType() == plugin.getTransporterItem().getType()) { // Only modify if it's the Transporter item
        
                                        ItemMeta meta = heldItem.getItemMeta();
        
                                        if (meta != null) {
        
                                            meta.setDisplayName(ChatColor.GREEN + "Transporter" + ChatColor.GRAY + " - Teleport Up!");
        
                                            heldItem.setItemMeta(meta);
        
                                        }
        
                                    }
        
                                }
        
                            }
        
                        
        
                                private void createLinkedPortals(Player owner, Location loc1, Location loc2) {
        
                                    // Store the link
        
                                    activePortals.putIfAbsent(owner.getUniqueId(), new HashMap<>());
        
                                    activePortals.get(owner.getUniqueId()).put(loc1, loc2);
        
                                    activePortals.get(owner.getUniqueId()).put(loc2, loc1);
        
                        
        
                                    Block block1 = loc1.getBlock();
        
                                    Block block2 = loc2.getBlock();
        
                        
        
                                    // For the second block (block2), save its original data and set it as a portal block linked to loc1.
        
                                    setPortalMetadata(block2, owner.getUniqueId(), loc1);
        
                                    block2.setType(Material.NETHER_QUARTZ_ORE);
        
                        
        
                                    // For the first block (block1), which is already a quartz ore,
        
                                    // we only need to add the metadata linking it to loc2.
        
                                    block1.setMetadata(PORTAL_LINK_METADATA_KEY, new FixedMetadataValue(plugin, LocationUtil.locationToString(loc2)));
        
                        
        
                        
        
                                    // Spawn white smoke particles
        
                                    spawnPortalParticles(loc1, Particle.CLOUD);
        
                                    spawnPortalParticles(loc2, Particle.CLOUD);
        
                        
        
                                    // Start continuous particle effect on the placed portal blocks
        
                                    startContinuousPortalParticleEffect(loc1, Particle.CLOUD, owner.getUniqueId()); // Changed to CLOUD
        
                                    startContinuousPortalParticleEffect(loc2, Particle.CLOUD, owner.getUniqueId()); // Changed to CLOUD
        
                                }    
        
                        private void setPortalMetadata(Block block, UUID ownerUUID, Location linkedLocation) {
        
                            plugin.getLogger().info("Setting metadata for block: " + block.getLocation() + ", Original BlockData: " + block.getBlockData().getAsString());
        
                            block.setMetadata(PORTAL_METADATA_KEY, new FixedMetadataValue(plugin, true));
        
                            block.setMetadata(PORTAL_OWNER_METADATA_KEY, new FixedMetadataValue(plugin, ownerUUID.toString()));
        
                            if (linkedLocation != null) { // Add null check
        
                                block.setMetadata(PORTAL_LINK_METADATA_KEY, new FixedMetadataValue(plugin, LocationUtil.locationToString(linkedLocation)));
        
                            }
        
                            block.setMetadata(PORTAL_ORIGINAL_BLOCK_DATA_KEY, new FixedMetadataValue(plugin, block.getBlockData().getAsString())); // Store original block data
        
                        }
        
                    
        
                    public static boolean isPortalBlock(Block block) {
        
                        return block.hasMetadata(PORTAL_METADATA_KEY);
        
                    }
        
                    
        
                    public UUID getPortalOwner(Block block) {
        
                        if (isPortalBlock(block)) {
        
                            List<MetadataValue> values = block.getMetadata(PORTAL_OWNER_METADATA_KEY);
        
                            if (!values.isEmpty()) {
        
                                return UUID.fromString(values.get(0).asString());
        
                            }
        
                        }
        
                        return null;
        
                    }
        
                    
        
                    public static Location getLinkedPortalLocation(AnnihilationNexus plugin, Block block) {
        
                        if (isPortalBlock(block)) {
        
                            List<MetadataValue> values = block.getMetadata(PORTAL_LINK_METADATA_KEY);
        
                            if (!values.isEmpty()) {
        
                                String locString = values.get(0).asString();
        
                                // plugin.getLogger().info("Raw metadata value for PORTAL_LINK_METADATA_KEY: " + locString);
        
                                return LocationUtil.stringToLocation(locString);
        
                            }
        
                        }
        
                        return null;
        
                    }
        
                    
        
                    public Location getLinkedPortalLocation(Block block) {
        
                        return getLinkedPortalLocation(this.plugin, block);
        
                    }
        
                    
        
                    public void startTeleportTask(Player player, Block portalBlock) {
        
                        UUID playerUUID = player.getUniqueId();
        
                        // Cancel any existing task for this player
        
                        if (teleportTasks.containsKey(playerUUID)) {
        
                            teleportTasks.get(playerUUID).cancel();
        
                            teleportTasks.remove(playerUUID);
        
                        }
        
                    
        
                        // Display owner's name in green chat
        
                        UUID portalOwnerUUID = getPortalOwner(portalBlock);
        
                        if (portalOwnerUUID != null) {
        
                            Player owner = plugin.getServer().getPlayer(portalOwnerUUID);
        
                            if (owner != null) {
        
                                player.sendMessage(ChatColor.GREEN + "This is " + owner.getName() + "'s Transporter portal.");
        
                            } else {
        
                                player.sendMessage(ChatColor.GREEN + "This is an unknown owner's Transporter portal.");
        
                            }
        
                        }
        
                    
        
                        BukkitTask task = new BukkitRunnable() {
        
                            @Override
        
                            public void run() {
        
                                // Check if player is still sneaking and on the portal block
        
                                if (player.isSneaking() && player.getLocation().getBlock().getRelative(0, -1, 0).equals(portalBlock)) {
        
                                    teleportPlayer(player, portalBlock);
        
                                } else {
        
                                    player.sendMessage(ChatColor.RED + "Teleportation cancelled: You stopped sneaking or moved off the portal.");
        
                                }
        
                                teleportTasks.remove(playerUUID); // Remove task after execution or cancellation
        
                            }
        
                        }.runTaskLater(plugin, 20L); // 1 second delay
        
                    
        
                        teleportTasks.put(playerUUID, task);
        
                        player.sendMessage(ChatColor.YELLOW + "Sneak for 1 second to teleport...");
        
                    }
        
                    
        
                    public void cancelTeleportTask(Player player) {
        
                        UUID playerUUID = player.getUniqueId();
        
                        if (teleportTasks.containsKey(playerUUID)) {
        
                            teleportTasks.get(playerUUID).cancel();
        
                            teleportTasks.remove(playerUUID);
        
                            player.sendMessage(ChatColor.RED + "Teleportation charge cancelled.");
        
                        }
        
                    }
        
                    
        
                    public void teleportPlayer(Player player, Block portalBlock) {
                        plugin.getLogger().info("teleportPlayer called for player: " + player.getName() + " at portal: " + portalBlock.getLocation().toString());
                
                        Location linkedLocation = getLinkedPortalLocation(portalBlock);
                
                        if (linkedLocation != null) {
                            // --- Safety Check ---
                            Location destination = linkedLocation.clone().add(0, 1, 0); // Location where player's feet will be
                            Block feetBlock = destination.getBlock();
                            Block headBlock = destination.clone().add(0, 1, 0).getBlock();
                
                            if (!feetBlock.isPassable() || !headBlock.isPassable()) {
                                player.sendMessage(ChatColor.RED + "Teleportation failed: The destination is obstructed!");
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                                plugin.getLogger().warning("Teleportation for " + player.getName() + " to " + destination + " was cancelled due to obstruction.");
                                return;
                            }
                            // --- End Safety Check ---
                
                            // Teleport to the center of the linked block, slightly above
                            Location teleportLoc = linkedLocation.add(0.5, 1.1, 0.5);
                            teleportLoc.setDirection(player.getLocation().getDirection()); // Maintain player's direction
                
                            player.teleport(teleportLoc);
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                            plugin.getLogger().info("Player " + player.getName() + " teleported to: " + teleportLoc.toString());
                
                        } else {
                            player.sendMessage(ChatColor.RED + "Portal link not found!");
                            plugin.getLogger().warning("Portal link not found for portal at: " + portalBlock.getLocation().toString());
                        }
                    }
        
                    
        
                    public void destroyPortal(Block block) {
        
                        plugin.getLogger().info("destroyPortal called for block: " + block.getLocation());
        
                        if (!isPortalBlock(block)) {
        
                            plugin.getLogger().warning("destroyPortal called on a non-portal block: " + block.getLocation());
        
                            return;
        
                        }
        
                    
        
                        UUID ownerUUID = getPortalOwner(block);
        
                        Location linkedLocation = getLinkedPortalLocation(block);
        
                        Location blockLocation = block.getLocation();
        
                    
        
                        // Clean up activePortals map first to prevent chain reactions
        
                        if (ownerUUID != null && activePortals.containsKey(ownerUUID)) {
        
                            Map<Location, Location> playerPortals = activePortals.get(ownerUUID);
        
                            playerPortals.remove(blockLocation);
        
                            if (linkedLocation != null) {
        
                                playerPortals.remove(linkedLocation);
        
                            }
        
                            if (playerPortals.isEmpty()) {
        
                                activePortals.remove(ownerUUID);
        
                            }
        
                            plugin.getLogger().info("Removed portal entries from activePortals map for owner " + ownerUUID);
        
                        }
        
                    
        
                        // Destroy the initial portal
        
                        forceDestroyPortal(plugin, block);
        
                    
        
                        // Destroy the linked portal
        
                        if (linkedLocation != null) {
        
                            Block linkedBlock = linkedLocation.getBlock();
        
                            if (isPortalBlock(linkedBlock)) { // Check if it's still a portal before destroying
        
                                plugin.getLogger().info("Attempting to destroy linked portal block: " + linkedBlock.getLocation());
        
                                forceDestroyPortal(plugin, linkedBlock);
        
                            } else {
        
                                plugin.getLogger().warning("Linked block is not a portal (or was already destroyed): " + linkedBlock.getLocation());
        
                            }
        
                        }
        
                    
        
                        plugin.getLogger().info("Transporter portal pair fully destroyed for owner " + ownerUUID);
        
                    }
        
                    
        
                    // Particle effect for portals (fixed duration)
        
                    private void spawnPortalParticles(Location loc, Particle particle) {
        
                        new BukkitRunnable() {
        
                            int count = 0;
        
                            @Override
        
                            public void run() {
        
                                if (count >= 40) { // Run for 2 seconds
        
                                    this.cancel();
        
                                    return;
        
                                }
        
                                loc.getWorld().spawnParticle(particle, loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.01);
        
                                count++;
        
                            }
        
                        }.runTaskTimer(plugin, 0L, 2L); // Every 2 ticks
        
                    }
        
                    
        
                    // New methods for continuous particle effects
        
                    private void startContinuousPortalParticleEffect(Location loc, Particle particle, UUID playerUUID) {
        
                        // Cancel any existing continuous particle task for this player at this location
        
                        stopContinuousPortalParticleEffect(loc, playerUUID);
        
                    
        
                        BukkitTask task = new BukkitRunnable() {
        
                            @Override
        
                            public void run() {
        
                                loc.getWorld().spawnParticle(particle, loc.getX() + 0.5, loc.getY() + 1.0, loc.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.01);
        
                            }
        
                        }.runTaskTimer(plugin, 0L, 5L); // Every 5 ticks (0.25 seconds)
        
                    
        
                        // Store task by player UUID and location
        
                        continuousParticleTasks.computeIfAbsent(playerUUID, k -> new HashMap<>()).put(loc, task);
        
                    
        
                        // Store the task ID as metadata on the block
        
                        loc.getBlock().setMetadata(PORTAL_PARTICLE_TASK_ID_KEY, new FixedMetadataValue(plugin, task.getTaskId()));
        
                    }
        
                    
        
                    private void stopContinuousPortalParticleEffect(Location loc, UUID playerUUID) {
        
                        if (continuousParticleTasks.containsKey(playerUUID)) {
        
                            Map<Location, BukkitTask> playerTasks = continuousParticleTasks.get(playerUUID);
        
                            if (playerTasks.containsKey(loc)) {
        
                                playerTasks.get(loc).cancel();
        
                                playerTasks.remove(loc);
        
                                if (playerTasks.isEmpty()) {
        
                                    continuousParticleTasks.remove(playerUUID);
        
                                }
        
                            }
        
                        }
        
                    }
        
                    
        
                    // Overload for stopping all continuous particle effects for a player (e.g., on death or new portal creation)
        
                    private void stopContinuousParticleEffect(UUID playerUUID) {
        
                        if (continuousParticleTasks.containsKey(playerUUID)) {
        
                            continuousParticleTasks.get(playerUUID).values().forEach(BukkitTask::cancel);
        
                            continuousParticleTasks.remove(playerUUID);
        
                        }
        
                    }
        
                    
        
                    // New method to destroy all portals for a specific player
        
                    public void destroyAllPortalsForPlayer(UUID playerUUID) {
        
                        plugin.getLogger().info("Attempting to destroy all portals for player: " + playerUUID);
        
                        if (activePortals.containsKey(playerUUID)) {
        
                            Map<Location, Location> playerActivePortals = activePortals.get(playerUUID);
        
                            plugin.getLogger().info("Player " + playerUUID + " has " + playerActivePortals.size() + " active portals.");
        
                            // Create a copy of keys to avoid ConcurrentModificationException
        
                            new HashSet<>(playerActivePortals.keySet()).forEach(loc -> {
        
                                Block block = loc.getBlock();
        
                                if (TransporterAbility.isPortalBlock(block)) {
        
                                    plugin.getLogger().info("Destroying portal block at: " + loc.toString());
        
                                    destroyPortal(block);
        
                                }
        
                            });
        
                            activePortals.remove(playerUUID); // Ensure the map entry for the player is cleared
        
                            plugin.getLogger().info("All portals for player " + playerUUID + " destroyed and removed from activePortals map.");
        
                        } else {
        
                            plugin.getLogger().info("Player " + playerUUID + " has no active portals in activePortals map.");
        
                        }
        
                        // Also clear any pending selections if the player changes class mid-selection
        
                        if (playerPortalSelections.containsKey(playerUUID)) {
        
                            playerPortalSelections.remove(playerUUID);
        
                            plugin.getLogger().info("Cleared pending portal selection for player " + playerUUID);
        
                        }
        
                        stopContinuousParticleEffect(playerUUID); // Stop any particle effects
        
                        resetPortalMakerItemName(plugin.getServer().getPlayer(playerUUID));
        
                        plugin.getLogger().info("Stopped continuous particle effects for player " + playerUUID);
        
                    }
        
                    
        
                    public void resetPortalMakerItemName(Player player) {
        
                        if (player == null || !player.isOnline()) return;
        
                    
        
                        ItemStack originalItem = plugin.getTransporterItem();
        
                        if (originalItem == null || !originalItem.hasItemMeta()) return;
        
                        String originalName = originalItem.getItemMeta().getDisplayName();
        
                        Material itemType = originalItem.getType();
        
                    
        
                        for (ItemStack item : player.getInventory().getContents()) {
        
                            if (item != null && item.getType() == itemType && item.hasItemMeta()) {
        
                                ItemMeta meta = item.getItemMeta();
        
                                if (meta.hasDisplayName()) {
        
                                    String displayName = meta.getDisplayName();
        
                                    if (displayName.equals(ChatColor.GREEN + "Transporter" + ChatColor.GRAY + " - Entrance Placed") || displayName.equals(ChatColor.GREEN + "Transporter" + ChatColor.GRAY + " - Teleport Up!")) {
        
                                        meta.setDisplayName(originalName);
        
                                        item.setItemMeta(meta);
        
                                    }
        
                                }
        
                            }
        
                        }
        
                    }

    // Public static method to destroy a portal and revert its blocks, even if the owner's ability instance is not active
    public static void forceDestroyPortal(AnnihilationNexus plugin, Block block) {
        plugin.getLogger().info("forceDestroyPortal called for block: " + block.getLocation().toString());

        if (!block.hasMetadata(PORTAL_METADATA_KEY)) {
            plugin.getLogger().warning("forceDestroyPortal called on a non-portal block: " + block.getLocation());
            return;
        }

        // Stop continuous particle effects
        if (block.hasMetadata(PORTAL_PARTICLE_TASK_ID_KEY)) {
            int taskId = block.getMetadata(PORTAL_PARTICLE_TASK_ID_KEY).get(0).asInt();
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        // Revert block to its original state
        if (block.hasMetadata(PORTAL_ORIGINAL_BLOCK_DATA_KEY)) {
            String blockDataString = block.getMetadata(PORTAL_ORIGINAL_BLOCK_DATA_KEY).get(0).asString();
            try {
                block.setBlockData(plugin.getServer().createBlockData(blockDataString));
                plugin.getLogger().info("Reverted block " + block.getLocation() + " to " + blockDataString);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Failed to create block data from string: " + blockDataString);
                block.setType(Material.AIR); // Fallback
            }
        } else {
            block.setType(Material.AIR); // Fallback if no original data found
            plugin.getLogger().warning("No original block data found for portal at " + block.getLocation() + ". Reverting to AIR.");
        }

        // Remove all portal metadata
        block.removeMetadata(PORTAL_METADATA_KEY, plugin);
        block.removeMetadata(PORTAL_OWNER_METADATA_KEY, plugin);
        block.removeMetadata(PORTAL_LINK_METADATA_KEY, plugin);
        block.removeMetadata(PORTAL_ORIGINAL_BLOCK_DATA_KEY, plugin);
        block.removeMetadata(PORTAL_PARTICLE_TASK_ID_KEY, plugin);

        plugin.getLogger().info("Transporter portal block at " + block.getLocation() + " destroyed and reverted.");
    }
}