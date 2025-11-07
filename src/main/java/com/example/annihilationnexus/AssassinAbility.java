package com.example.annihilationnexus;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class AssassinAbility {

    private final Player player;
    private final AnnihilationNexus plugin;
    private long lastLeapTime = -40000;
    private final long cooldown = 40000; // 40 seconds
    private boolean isLeaping = false;

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

        player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(1.0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0)); // 6 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 0)); // 6 seconds
        player.setFallDistance(-1000);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                isLeaping = false;
            }
        }.runTaskLater(plugin, 120L); // 6 seconds
    }

    public void reduceCooldown() {
        lastLeapTime -= 8000; // 8 seconds
    }

    public boolean isLeaping() {
        return isLeaping;
    }
}
