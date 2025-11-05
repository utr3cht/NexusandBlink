package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlinkListener implements Listener {

    private final AnnihilationNexus plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>(); // Stores the time when the cooldown ends

    public BlinkListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player is a dasher, holding the blink item, and sneaking
        if (item == null || item.getType() != Material.PURPLE_DYE) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals(plugin.getBlinkItem().getItemMeta().getDisplayName())) {
            return;
        }

        String playerClass = plugin.getPlayerClassManager().getPlayerClass(player.getUniqueId());
        if (playerClass == null || !playerClass.equalsIgnoreCase("dasher") || !player.isSneaking()) {
            return;
        }

        event.setCancelled(true); // Prevent dye from being used

        // Cooldown check
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = cooldowns.get(player.getUniqueId()) - System.currentTimeMillis();
            if (timeLeft > 0) {
                player.sendMessage("You must wait " + (timeLeft / 1000 + 1) + " more seconds to use Blink again.");
                return;
            }
        }

        DasherAbility ability = plugin.getPlayerClassManager().getDasherAbility(player.getUniqueId());
        if (ability != null) {
            double distance = ability.blink(); // This teleports the player and returns the distance

            if (distance > 0) {
                // Cooldown is equal to the distance in blocks, capped at 20 seconds.
                long cooldownSeconds = Math.min(20, (long) distance);
                long cooldownEndTime = System.currentTimeMillis() + (cooldownSeconds * 1000);
                cooldowns.put(player.getUniqueId(), cooldownEndTime);
                player.sendMessage("Cooldown: " + cooldownSeconds + " seconds.");

                // Update item name with cooldown
                updateBlinkItemName(player, item, cooldownSeconds);

                // Schedule a task to update the item name every second
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        long timeLeft = cooldowns.getOrDefault(player.getUniqueId(), 0L) - System.currentTimeMillis();
                        plugin.getLogger().info("Player " + player.getName() + " Blink Cooldown Left: " + (timeLeft / 1000 + 1) + "s");
                        if (timeLeft <= 0) {
                            // Cooldown expired, reset item name and remove from map
                            updateBlinkItemName(player, item, 0);
                            cooldowns.remove(player.getUniqueId());
                            this.cancel();
                        } else {
                            // Update item name with remaining cooldown
                            updateBlinkItemName(player, item, timeLeft / 1000 + 1);
                        }
                    }
                }.runTaskTimer(plugin, 20, 20); // Start after 1 second, repeat every second
            }
        }
    }

    private void updateBlinkItemName(Player player, ItemStack referenceItem, long remainingSeconds) {
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() != Material.PURPLE_DYE) {
                continue;
            }

            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().startsWith(ChatColor.LIGHT_PURPLE + "Blink")) {
                continue;
            }

            // Found the Blink item, update its name
            if (remainingSeconds > 0) {
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Blink " + ChatColor.RED + "(" + remainingSeconds + "s)");
            } else {
                meta.setDisplayName(plugin.getBlinkItem().getItemMeta().getDisplayName());
            }
            itemStack.setItemMeta(meta);
            // No need to continue searching, assuming only one Blink item per player
            return;
        }
    }
}