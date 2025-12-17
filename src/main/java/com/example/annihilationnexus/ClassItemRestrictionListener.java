package com.example.annihilationnexus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.PlayerInventory;

public class ClassItemRestrictionListener implements Listener {

    private final AnnihilationNexus plugin;

    public ClassItemRestrictionListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Check if the clicked item or the item on the cursor is a class item
        boolean isClassItemInvolved = (clickedItem != null && plugin.isClassItem(clickedItem)) ||
                (cursorItem != null && plugin.isClassItem(cursorItem));

        // Bundle Restriction
        if (isClassItemInvolved) {
            boolean isBundleInvolved = (clickedItem != null && clickedItem.getType() == org.bukkit.Material.BUNDLE) ||
                    (cursorItem != null && cursorItem.getType() == org.bukkit.Material.BUNDLE);

            // Also check for right-click on bundle with item, or right-click item on bundle
            if (isBundleInvolved) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                    ((org.bukkit.entity.Player) event.getWhoClicked())
                            .sendMessage(org.bukkit.ChatColor.RED + "You cannot put class items in bundles!");
                }
                return;
            }
        }

        // Also check for hotbar swap (number keys)
        if (event.getClick() == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
            ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
            if (hotbarItem != null && plugin.isClassItem(hotbarItem)) {
                isClassItemInvolved = true;
            }
        }

        if (!isClassItemInvolved) {
            return;
        }

        // Check if any GUI other than the player's inventory is open
        InventoryType topType = event.getView().getTopInventory().getType();
        if (topType != InventoryType.CRAFTING && topType != InventoryType.CREATIVE) {
            // A GUI is open (Chest, Furnace, Anvil, etc.)
            // Prevent ANY interaction with class items
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                ((org.bukkit.entity.Player) event.getWhoClicked())
                        .sendMessage(org.bukkit.ChatColor.RED + "You cannot move class items while a GUI is open!");
            }
            return;
        }

        // If a class item is involved, prevent it from being moved into non-player
        // inventories (This part might be redundant now if we block all GUIs, but good
        // for safety)
        if (event.getClickedInventory() != null && !(event.getClickedInventory() instanceof PlayerInventory)) {
            // This handles clicks inside the non-player inventory
            event.setCancelled(true);
        }

        // This handles shift-clicking a class item from the player inventory to the
        // other inventory
        if (event.isShiftClick() && event.getClickedInventory() instanceof PlayerInventory) {
            if (clickedItem != null && plugin.isClassItem(clickedItem)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // If the dragged item is a class item
        if (plugin.isClassItem(event.getOldCursor())) {
            // Check if any GUI other than the player's inventory is open
            InventoryType topType = event.getView().getTopInventory().getType();
            if (topType != InventoryType.CRAFTING && topType != InventoryType.CREATIVE) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof org.bukkit.entity.Player) {
                    ((org.bukkit.entity.Player) event.getWhoClicked())
                            .sendMessage(org.bukkit.ChatColor.RED + "You cannot move class items while a GUI is open!");
                }
                return;
            }

            // Check if any of the affected slots are outside the player's inventory
            for (int slot : event.getRawSlots()) {
                // Raw slots are unique integers for each slot in the combined inventory view.
                // If a raw slot index is less than the size of the top inventory, it's not in
                // the player inventory.
                if (slot < event.getView().getTopInventory().getSize()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof org.bukkit.entity.ItemFrame) {
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (plugin.isClassItem(item)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(org.bukkit.ChatColor.RED + "You cannot put class items in Item Frames!");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(org.bukkit.event.player.PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof org.bukkit.entity.ArmorStand) {
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (plugin.isClassItem(item)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(org.bukkit.ChatColor.RED + "You cannot put class items on Armor Stands!");
            }
        }
    }
}
