package com.example.annihilationnexus;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

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

        player.setVelocity(direction.multiply(launchPower));
    }
}
