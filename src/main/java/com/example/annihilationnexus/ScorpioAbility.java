package com.example.annihilationnexus;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ScorpioAbility {

    private final Player player;
    private final AnnihilationNexus plugin;
    private long lastAbilityTime = 0;

    public ScorpioAbility(Player player, AnnihilationNexus plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void use(boolean isRightClick) {
        if (System.currentTimeMillis() - lastAbilityTime < plugin.getScorpioHookCooldown() * 1000L) {
            return;
        }

        lastAbilityTime = System.currentTimeMillis();

        ItemStack hookItem = plugin.getScorpioItem();
        Item hook = player.getWorld().dropItem(player.getEyeLocation(), hookItem);
        hook.setVelocity(player.getEyeLocation().getDirection().multiply(1.5));
        hook.setPickupDelay(Integer.MAX_VALUE);
        hook.setOwner(player.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (hook.isDead() || !hook.isValid()) {
                    this.cancel();
                    return;
                }

                if (hook.isOnGround()) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            hook.remove();
                        }
                    }.runTaskLater(plugin, 50L); // 2.5 seconds
                    this.cancel();
                    return;
                }

                player.spawnParticle(Particle.CRIT, hook.getLocation(), 1, 0, 0, 0, 0);

                for (Entity entity : hook.getNearbyEntities(0.5, 0.5, 0.5)) {
                    if (entity instanceof Player && !entity.equals(player)) {
                        Player hitPlayer = (Player) entity;
                        if (isRightClick && !isFriendly(hitPlayer)) {
                            pullEnemy(hitPlayer);
                            hook.remove();
                            this.cancel();
                            return;
                        } else if (!isRightClick && isFriendly(hitPlayer)) {
                            pullToFriendly(hitPlayer);
                            hook.remove();
                            this.cancel();
                            return;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void pullEnemy(Player enemy) {
        Location pullLocation = player.getLocation();
        if (!isSafeLocation(pullLocation)) {
            player.sendMessage("Not enough space to pull the enemy.");
            return;
        }

        enemy.teleport(pullLocation.add(0, 1.5, 0));
        enemy.setFallDistance(0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                enemy.setFallDistance(0);
            }
        }.runTaskLater(plugin, 20L * plugin.getScorpioEnemyPullFallImmunity());
    }

    public void pullToFriendly(Player friendly) {
        if (isHalfBlockInFront()) {
            return;
        }

        Vector vector = friendly.getLocation().toVector().subtract(player.getLocation().toVector());
        player.setVelocity(vector.normalize().multiply(2.0));
        player.setFallDistance(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                player.setFallDistance(0);
            }
        }.runTaskLater(plugin, 20L * plugin.getScorpioFriendlyPullFallImmunity());
    }

    private boolean isSafeLocation(Location location) {
        return location.clone().add(0, 1, 0).getBlock().isPassable() &&
               location.clone().add(0, 2, 0).getBlock().isPassable() &&
               location.clone().add(0, 3, 0).getBlock().isPassable();
    }

    private boolean isHalfBlockInFront() {
        Location front = player.getLocation().add(player.getLocation().getDirection().normalize());
        return front.getBlock().getType().toString().contains("SLAB");
    }

    private boolean isFriendly(Player otherPlayer) {
        // Assuming a team system is in place. For now, we'll consider everyone an enemy.
        // This should be replaced with actual team logic.
        return false;
    }
}
