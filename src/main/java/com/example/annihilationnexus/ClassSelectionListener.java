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
    private final XpManager xpManager; // Add XpManager

    public ClassSelectionListener(AnnihilationNexus plugin, PlayerClassManager playerClassManager,
            XpManager xpManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
        this.xpManager = xpManager;
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
                if (playerClassManager.isClassBanned(className)) {
                    player.sendMessage(ChatColor.RED + "This class is currently banned.");
                    player.closeInventory();
                    return;
                }

                // Check if unlocked
                if (playerClassManager.isClassUnlocked(player.getUniqueId(), className)) {
                    playerClassManager.setPlayerClass(player.getUniqueId(), className.toLowerCase());
                    player.sendMessage(ChatColor.GREEN + "You have selected the " + className + " class!");
                    player.closeInventory();
                } else {
                    // Try to purchase
                    int cost = playerClassManager.getClassCost(className);
                    int currentXp = xpManager.getXp(player);

                    if (currentXp >= cost) {
                        xpManager.addXp(player, -cost);
                        playerClassManager.unlockClass(player.getUniqueId(), className);
                        playerClassManager.setPlayerClass(player.getUniqueId(), className.toLowerCase());
                        player.sendMessage(ChatColor.GREEN + "You purchased and selected the " + className + " class! -"
                                + cost + " XP");
                        player.closeInventory();
                    } else {
                        player.sendMessage(ChatColor.RED + "You need " + cost + " XP to unlock this class. You have "
                                + currentXp + " XP.");
                        player.closeInventory();
                    }
                }
            }
        }
    }
}
