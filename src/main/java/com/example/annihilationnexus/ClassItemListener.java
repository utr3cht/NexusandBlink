package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class ClassItemListener implements Listener {

    private final AnnihilationNexus plugin;

    public ClassItemListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null && plugin.isClassItem(clickedItem)) {
            // Prevent placing class items in crafting grids, furnaces, etc.
            if (event.getClickedInventory() != null
                    && (event.getClickedInventory().getType().name().contains("CRAFTING") ||
                            event.getClickedInventory().getType().name().contains("FURNACE") ||
                            event.getClickedInventory().getType().name().contains("CHEST") ||
                            event.getClickedInventory().getType().name().contains("SHULKER_BOX") ||
                            event.getClickedInventory().getType().name().contains("BARREL") ||
                            event.getClickedInventory().getType().name().contains("HOPPER") ||
                            event.getClickedInventory().getType().name().contains("DROPPER") ||
                            event.getClickedInventory().getType().name().contains("DISPENSER"))) {
                event.setCancelled(true);
                event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot place class items here!");
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        ItemStack draggedItem = event.getOldCursor();
        if (draggedItem != null && plugin.isClassItem(draggedItem)) {
            // Prevent dragging class items into crafting grids, furnaces, etc.
            for (Integer slot : event.getRawSlots()) {
                if (event.getView().getInventory(slot) != null
                        && (event.getView().getInventory(slot).getType().name().contains("CRAFTING") ||
                                event.getView().getInventory(slot).getType().name().contains("FURNACE") ||
                                event.getView().getInventory(slot).getType().name().contains("CHEST") ||
                                event.getView().getInventory(slot).getType().name().contains("SHULKER_BOX") ||
                                event.getView().getInventory(slot).getType().name().contains("BARREL") ||
                                event.getView().getInventory(slot).getType().name().contains("HOPPER") ||
                                event.getView().getInventory(slot).getType().name().contains("DROPPER") ||
                                event.getView().getInventory(slot).getType().name().contains("DISPENSER"))) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot place class items here!");
                    break;
                }
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.getDrops().removeIf(plugin::isClassItem); // Remove class items from drops

        // Also remove from inventory to prevent plugins (like DeadChest) from grabbing
        // them directly
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && plugin.isClassItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }
        // Also check cursor (though unlikely to be held on death, good measure)
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && plugin.isClassItem(cursor)) {
            player.setItemOnCursor(null);
        }

        player.sendMessage(ChatColor.YELLOW + "Your class items disappeared upon death.");
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (plugin.isClassItem(droppedItem)) {
            event.setCancelled(true); // Prevent dropping class items
            event.getPlayer().sendMessage(ChatColor.RED + "You cannot drop class items!");
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (plugin.isClassItem(item)) {
            // Prevent interaction with any entity using a class item if it's a dye
            if (item.getType().name().endsWith("_DYE")) {
                event.setCancelled(true);
            }
        }
    }
}