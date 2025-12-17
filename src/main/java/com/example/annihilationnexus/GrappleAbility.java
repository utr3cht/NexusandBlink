package com.example.annihilationnexus;

import org.bukkit.Location;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import org.bukkit.entity.FishHook;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GrappleAbility {

    private final AnnihilationNexus plugin;
    private final Player player;
    private final Map<UUID, FishHook> activeHooks = new HashMap<>();
    private final Set<FishHook> groundedHooks = new HashSet<>();

    private static final int MAX_GRAPPLE_DURABILITY = 64;

    public GrappleAbility(AnnihilationNexus plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public void handleClick() {
        if (activeHooks.containsKey(player.getUniqueId())) {
            pull(player.getInventory().getItemInMainHand());
        } else {
            _launch();
        }
    }

    private void _launch() {
        // Launch the projectile with default velocity first
        final FishHook hook = player.launchProjectile(FishHook.class);
        hook.setGravity(plugin.grappleHookHasGravity()); // Revert to boolean gravity setting
        activeHooks.put(player.getUniqueId(), hook);
        player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 1.0f);

        // Schedule a task to modify the velocity 1 tick later to override default
        // behavior
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (hook.isValid()) { // Make sure the hook still exists
                // Cap the speed to prevent the hook from becoming too fast and disappearing
                double speed = Math.min(plugin.getGrappleHookSpeed(), 4.0);
                hook.setVelocity(player.getLocation().getDirection().multiply(speed));
            }
        }, 1L); // 1L means 1 tick delay
    }

    public void pull(ItemStack grappleItem) {
        if (!activeHooks.containsKey(player.getUniqueId())) {
            return;
        }

        FishHook hook = activeHooks.get(player.getUniqueId());
        if (hook != null && hook.isValid()) {

            boolean isGrounded = false;
            Location loc = hook.getLocation();

            // Modified Scan: Check 1.5 blocks vertically down (Ground) AND up (Ceiling)
            boolean foundSolidSurface = false;

            // Check Ground (Down)
            for (double y = 0; y >= -1.5; y -= 0.5) {
                if (loc.clone().add(0, y, 0).getBlock().getType().isSolid()) {
                    foundSolidSurface = true;
                    break;
                }
            }

            // Check Ceiling (Up)
            if (!foundSolidSurface) {
                for (double y = 0; y <= 1.5; y += 0.5) {
                    if (loc.clone().add(0, y, 0).getBlock().getType().isSolid()) {
                        foundSolidSurface = true;
                        break;
                    }
                }
            }

            if (!foundSolidSurface) {
                hook.remove();
                activeHooks.remove(player.getUniqueId());
                groundedHooks.remove(hook);
                return;
            }

            // If near valid surface, isGrounded true
            isGrounded = true;

            if (isGrounded) {
                // Check restrictions
                if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS)) {
                    player.sendMessage("§cYou cannot use the Grapple while you have Slowness!");
                    return;
                }
                if (player.getFireTicks() > 0) {
                    player.sendMessage("§cYou cannot use the Grapple while on fire!");
                    return;
                }

                Location playerLoc = player.getLocation();
                Location hookLoc = hook.getLocation();

                double distance = playerLoc.distance(hookLoc);
                if (distance < 1.0) { // Prevent pulling if too close
                    hook.remove();
                    activeHooks.remove(player.getUniqueId());
                    groundedHooks.remove(hook);
                    return;
                }

                Vector pullVector = hookLoc.toVector().subtract(playerLoc.toVector()).normalize();
                // Add a configurable upward boost
                pullVector.setY(pullVector.getY() + plugin.getGrappleUpwardBoost());
                pullVector.normalize(); // Re-normalize to maintain speed consistency

                double pullStrength = distance * plugin.getGrapplePullStrength();
                // Set velocity directly instead of adding to it for more predictable force
                player.setVelocity(player.getVelocity().add(pullVector.multiply(pullStrength)));
                player.playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f);

                // consumeDurability(grappleItem); // Durability disabled by user request
            } else if (hook.isInWater()) {
                player.sendMessage("§cThe hook cannot be floating on water!");
            }
            hook.remove();
            activeHooks.remove(player.getUniqueId());
            groundedHooks.remove(hook);
        } else {
            // If the hook is no longer valid, it means it was removed by vanilla or
            // despawned.
            // We need to clean up our internal state.
            activeHooks.remove(player.getUniqueId());
            // Remove any invalid hooks from groundedHooks as well
            groundedHooks.removeIf(h -> !h.isValid());
        }
    }

    public void setHook(Player player, FishHook hook) {
        activeHooks.put(player.getUniqueId(), hook);
    }

    public void removeHook(UUID playerId) {
        FishHook hook = activeHooks.remove(playerId);
        if (hook != null) {
            groundedHooks.remove(hook);
        }
    }

    public void setHookAsGrounded(FishHook hook) {
        groundedHooks.add(hook);
    }

    public boolean hasActiveHook(Player player) {
        return activeHooks.containsKey(player.getUniqueId());
    }

}