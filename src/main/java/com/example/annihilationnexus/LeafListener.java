package com.example.annihilationnexus;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class LeafListener implements Listener {

    private final AnnihilationNexus plugin;

    public LeafListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        Material blockType = event.getBlockState().getType();

        // We only care about leaves
        if (!isLeaves(blockType)) {
            return;
        }

        // If it's OAK_LEAVES, we allow apples (Vanilla 1/200).
        // If it's DARK_OAK_LEAVES, we MUST remove apples.
        // If it's any other leaves, they shouldn't drop apples anyway, but we can
        // ensure it.

        if (blockType == Material.OAK_LEAVES) {
            // Allow apples.
            // We don't need to do anything as vanilla handles the 1/200 chance.
            return;
        }

        // For all other leaves (especially Dark Oak), remove apples.
        List<Item> items = event.getItems();
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            ItemStack stack = item.getItemStack();
            if (stack.getType() == Material.APPLE) {
                iterator.remove(); // Remove the apple drop
            }
        }
    }

    private boolean isLeaves(Material material) {
        return material.name().endsWith("_LEAVES");
    }
}
