package com.example.annihilationnexus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class GrappleListener implements Listener {

    private final AnnihilationNexus plugin;

    public GrappleListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (plugin.isGrappleItem(handItem)) {
            GrappleAbility ability = plugin.getPlayerClassManager().getGrappleAbility(player.getUniqueId());
            if (ability == null) return; // Should not happen if class is assigned

            // For grapple items, we only want to prevent vanilla fishing behavior.
            // The actual state management (launch/pull/cleanup) is handled by PlayerInteractEvent -> handleClick().
            switch (event.getState()) {
                case CAUGHT_ENTITY:
                case CAUGHT_FISH:
                case FAILED_ATTEMPT:
                case REEL_IN:
                    event.setCancelled(true); // Prevent all vanilla actions for these states
                    break;
                case FISHING:
                    // Let this pass. It's just a notification that a hook was launched.
                    // The launch itself is handled by PlayerInteractEvent.
                    break;
            }
        }
        // If it's not a grapple item, the event is not cancelled and normal fishing proceeds.
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.FishHook)) {
            return;
        }
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity().getShooter();
        GrappleAbility ability = plugin.getPlayerClassManager().getGrappleAbility(player.getUniqueId());

        // Only mark the hook as grounded if it hits the top face of a block.
        if (ability != null && event.getHitBlock() != null && event.getHitBlockFace() == org.bukkit.block.BlockFace.UP) {
            ability.setHookAsGrounded((org.bukkit.entity.FishHook) event.getEntity());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (plugin.isGrappleItem(handItem)) {
            GrappleAbility ability = plugin.getPlayerClassManager().getGrappleAbility(player.getUniqueId());
            if (ability != null && event.getAction().name().contains("RIGHT_CLICK")) {
                ability.handleClick();
                event.setCancelled(true);
            }
        }
    }
}