package com.example.annihilationnexus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public class ClassItemRestrictionListener implements Listener {

    private final AnnihilationNexus plugin;

    public ClassItemRestrictionListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the clicked item is a class item
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !plugin.isClassItem(clickedItem)) {
            return;
        }

        // If the item is a class item, prevent it from being placed in non-player inventories
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() != event.getWhoClicked()) {
            event.setCancelled(true);
        }
        // Also prevent moving class items out of player inventory into other inventories
        if (event.getWhoClicked().getOpenInventory().getBottomInventory().getHolder() == event.getWhoClicked() &&
            event.getClickedInventory() != event.getWhoClicked().getInventory() &&
            event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() != event.getWhoClicked()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Check if any of the dragged items are class items
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null && plugin.isClassItem(item)) {
                // If a class item is being dragged into a non-player inventory, cancel the event
                if (event.getInventory().getHolder() != event.getWhoClicked()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
