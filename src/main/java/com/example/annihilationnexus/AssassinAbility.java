package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class AssassinAbility {

    private final Player player;
    private final AnnihilationNexus plugin;
    private long lastLeapTime = -40000;
    private final long cooldown = 40000; // 40 seconds
    private boolean isLeaping = false;
    private static final HashMap<UUID, ItemStack[]> armor = new HashMap<>();

    public AssassinAbility(Player player, AnnihilationNexus plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void leap() {
        if (System.currentTimeMillis() - lastLeapTime < cooldown) {
            player.sendMessage("Leap is on cooldown for " + (cooldown - (System.currentTimeMillis() - lastLeapTime)) / 1000 + " seconds.");
            return;
        }

        lastLeapTime = System.currentTimeMillis();
        isLeaping = true;

        // Save and remove armor
        armor.put(player.getUniqueId(), player.getInventory().getArmorContents());
        player.getInventory().setArmorContents(null);

        player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0)); // 6 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0)); // 6 seconds

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 1);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!isLeaping) {
                    this.cancel();
                    return;
                }

                player.setNoDamageTicks(20); // Continuously grant 1 second of invulnerability
                player.setFallDistance(0f); // Continuously reset fall distance

                if (player.isOnGround() && ticks > 5) { // Give a small buffer before ending leap
                    isLeaping = false;
                    player.setNoDamageTicks(0); // Remove invulnerability
                    // Restore armor
                    if (armor.containsKey(player.getUniqueId())) {
                        ItemStack[] originalArmor = armor.get(player.getUniqueId());
                        ItemStack[] currentArmor = player.getInventory().getArmorContents();

                        for (int i = 0; i < 4; i++) {
                            ItemStack originalPiece = (originalArmor != null && i < originalArmor.length) ? originalArmor[i] : null;
                            if (originalPiece == null || originalPiece.getType().isAir()) {
                                continue; // No original armor for this slot.
                            }

                            ItemStack currentPiece = (currentArmor != null && i < currentArmor.length) ? currentArmor[i] : null;

                            if (currentPiece == null || currentPiece.getType().isAir()) {
                                // The slot is free, so we can re-equip the original piece.
                                if (currentArmor != null) currentArmor[i] = originalPiece;
                            } else {
                                // The slot is occupied. Return the original piece to the inventory.
                                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(originalPiece);
                                if (!leftover.isEmpty()) {
                                    // Drop the item if inventory is full
                                    for (ItemStack drop : leftover.values()) {
                                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                                    }
                                }
                            }
                        }
                        // Set the final calculated armor
                        player.getInventory().setArmorContents(currentArmor);

                        armor.remove(player.getUniqueId());
                    }
                    this.cancel();
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Run every tick
    }

    public long getRemainingCooldown() {
        long timePassed = System.currentTimeMillis() - lastLeapTime;
        if (timePassed >= cooldown) {
            return 0;
        }
        return (cooldown - timePassed) / 1000;
    }

    public void updateItemLore() {
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.isAssassinItem(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String baseName = ChatColor.GRAY + "Leap";
                    long remainingCooldown = getRemainingCooldown();
                    if (remainingCooldown > 0) {
                        meta.setDisplayName(baseName + ChatColor.RED + " " + remainingCooldown);
                    } else {
                        meta.setDisplayName(baseName + ChatColor.GREEN + " READY");
                    }
                    meta.setLore(null); // Clear lore
                    item.setItemMeta(meta);
                }
            }
        }
    }

    public void reduceCooldown() {
        lastLeapTime -= 8000; // 8 seconds
    }

    public boolean isLeaping() {
        return isLeaping;
    }
}
