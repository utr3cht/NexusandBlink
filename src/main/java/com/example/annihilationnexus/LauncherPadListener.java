package com.example.annihilationnexus;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LauncherPadListener implements Listener {

    private final AnnihilationNexus plugin;

    public LauncherPadListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.STONE_PRESSURE_PLATE) {
            return;
        }

        Block blockBelow = block.getRelative(0, -1, 0);
        Material belowType = blockBelow.getType();

        if (belowType != Material.IRON_BLOCK && belowType != Material.DIAMOND_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        Vector direction = player.getLocation().getDirection().normalize();

        double launchPower = (belowType == Material.DIAMOND_BLOCK) ? plugin.getLauncherPadDiamondPower() : plugin.getLauncherPadIronPower();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1, 1);
        player.setVelocity(direction.multiply(launchPower));
        plugin.grantNoFall(player, 5); // Grant 5 seconds of fall damage immunity
    }




}
