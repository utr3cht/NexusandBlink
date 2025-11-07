package com.example.annihilationnexus;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class LauncherPadBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();

        if (brokenBlock.getType() != Material.STONE_PRESSURE_PLATE) {
            return;
        }

        Block blockBelow = brokenBlock.getRelative(0, -1, 0);
        Material belowType = blockBelow.getType();

        if (belowType == Material.IRON_BLOCK || belowType == Material.DIAMOND_BLOCK) {
            // Drop the block below as an item
            blockBelow.getWorld().dropItemNaturally(blockBelow.getLocation(), new ItemStack(belowType));
            // Break the block below
            blockBelow.setType(Material.AIR);
        }
    }
}
