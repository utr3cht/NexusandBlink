package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClassSelectionListener implements Listener {

    private final AnnihilationNexus plugin;
    private final PlayerClassManager playerClassManager;

    public ClassSelectionListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.BLUE + "Select Your Class")) {
            event.setCancelled(true); // Prevent players from taking items from the GUI

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            String className = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (className != null && !className.isEmpty()) {
                playerClassManager.setPlayerClass(player.getUniqueId(), className.toLowerCase());
                player.sendMessage(ChatColor.GREEN + "You have selected the " + className + " class!");
                player.closeInventory();
            }
        }
    }
}
