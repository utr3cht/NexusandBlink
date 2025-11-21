package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NexusListener implements Listener {

    private final AnnihilationNexus plugin;
    private final NexusManager nexusManager;
    private final Map<String, Long> lastHitTimes = new HashMap<>();

    public NexusListener(AnnihilationNexus plugin, NexusManager nexusManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if the broken block is the correct material
        if (event.getBlock().getType() != plugin.getNexusMaterial()) {
            return;
        }

        Location blockLocation = event.getBlock().getLocation();
        Nexus nexus = nexusManager.getNexusAt(blockLocation);

        if (nexus != null) {
            Player player = event.getPlayer();

            // Cooldown check for hitting the nexus
            long currentTime = System.currentTimeMillis();
            long lastHitTime = lastHitTimes.getOrDefault(nexus.getTeamName(), 0L);
            long hitDelayMillis = plugin.getNexusHitDelay() * 50; // Convert ticks to milliseconds (20 ticks = 1 second)

            if (currentTime - lastHitTime < hitDelayMillis) {
                event.setCancelled(true);
                player.sendMessage(
                        ChatColor.RED + "You can only hit the Nexus every " + (hitDelayMillis / 1000.0) + " seconds.");
                return;
            }

            // Update last hit time
            lastHitTimes.put(nexus.getTeamName(), currentTime);

            // Prevent players from damaging their own team's nexus
            String playerTeam = plugin.getPlayerTeamManager().getPlayerTeam(player.getUniqueId());
            if (playerTeam != null && playerTeam.equalsIgnoreCase(nexus.getTeamName())) {
                player.sendMessage(ChatColor.RED + "You cannot damage your own Nexus!");
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true); // Prevent the nexus block from actually breaking

            // Don't damage if it's already destroyed
            if (nexus.isDestroyed()) {
                return;
            }

            nexus.damage(1); // Damage the nexus by 1 for each hit

            // Damage the player's tool, respecting Unbreaking enchantment
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool.getType() != Material.AIR && tool.getItemMeta() instanceof Damageable) {
                Damageable meta = (Damageable) tool.getItemMeta();

                Enchantment unbreaking = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
                int unbreakingLevel = 0;
                if (unbreaking != null) {
                    unbreakingLevel = tool.getEnchantmentLevel(unbreaking);
                }

                // Chance to take damage is 1 / (level + 1)
                if (ThreadLocalRandom.current().nextDouble() < (1.0 / (unbreakingLevel + 1))) {
                    meta.setDamage(meta.getDamage() + 1);
                    tool.setItemMeta(meta);

                    if (meta.getDamage() >= tool.getType().getMaxDurability()) {
                        // Tool breaks
                        player.getInventory().setItemInMainHand(null);
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    }
                }
            }

            // Play sound and particles on attack
            World world = blockLocation.getWorld();
            if (world != null) {
                float pitch = (float) ThreadLocalRandom.current().nextDouble(0.5, 1.0); // Random pitch between 0.5 and
                                                                                        // 1.0
                world.playSound(blockLocation, Sound.BLOCK_ANVIL_LAND, 1.0f, pitch);
                world.spawnParticle(Particle.LAVA, blockLocation.clone().add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0.05);
            }

            // Send messages
            player.sendMessage(plugin.getXpMessage());

            // Update scoreboard
            plugin.getScoreboardManager().updateForAllPlayers();

            if (nexus.isDestroyed()) {
                player.getServer().broadcastMessage("The " + nexus.getTeamName() + " nexus has been destroyed!");

                // Schedule explosion effect and replace with bedrock with a 1-tick delay
                if (world != null) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        world.createExplosion(blockLocation, 0.0f, false); // Visual-only explosion
                        world.spawnParticle(Particle.EXPLOSION_EMITTER, blockLocation.clone().add(0.5, 0.5, 0.5), 50,
                                0.5, 0.5, 0.5, 0.1);
                        world.spawnParticle(Particle.LAVA, blockLocation.clone().add(0.5, 0.5, 0.5), 100, 0.5, 0.5, 0.5,
                                0.1);
                        blockLocation.getBlock().setType(Material.BEDROCK);
                        // Update scoreboard one last time after destruction effects
                        plugin.getScoreboardManager().updateForAllPlayers();
                    }, plugin.getNexusDestructionDelay());
                }
                // Here you would add logic to handle the team\'s loss.
            }
        }
    }
}
