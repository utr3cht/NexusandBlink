package com.example.annihilationnexus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ConsumableListener implements Listener {

    private final AnnihilationNexus plugin;

    public ConsumableListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.ENCHANTED_GOLDEN_APPLE) {
            event.setCancelled(true); // Cancel vanilla behavior to prevent override

            Player player = event.getPlayer();

            // Manually consume item (if not in creative)
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                if (event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND) {
                    org.bukkit.inventory.ItemStack handItem = player.getInventory().getItemInMainHand();
                    handItem.setAmount(handItem.getAmount() - 1);
                    player.getInventory().setItemInMainHand(handItem);
                } else {
                    org.bukkit.inventory.ItemStack handItem = player.getInventory().getItemInOffHand();
                    handItem.setAmount(handItem.getAmount() - 1);
                    player.getInventory().setItemInOffHand(handItem);
                }
            }

            // Restore Food and Saturation (Standard God Apple: 4 food, 9.6 saturation)
            int oldFood = player.getFoodLevel();
            float oldSat = player.getSaturation();
            player.setFoodLevel(Math.min(oldFood + 4, 20));
            // Saturation is typically capped by food level in vanilla logic, but we'll just
            // add it here.
            player.setSaturation(Math.min(oldSat + 9.6f, player.getFoodLevel()));

            // Apply custom effects (Old God Apple effects)
            // Regeneration IV (Level 3) for 30 seconds
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30 * 20, 3));

            // Resistance I (Level 0) for 5 minutes
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 5 * 60 * 20, 0));

            // Fire Resistance I (Level 0) for 5 minutes
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 5 * 60 * 20, 0));

            // Absorption IV (Level 3) for 2 minutes
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2 * 60 * 20, 3));
        }
    }
}
