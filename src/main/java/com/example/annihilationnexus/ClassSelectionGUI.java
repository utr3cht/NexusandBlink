package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ClassSelectionGUI {

    private final AnnihilationNexus plugin;
    private final PlayerClassManager playerClassManager;

    public ClassSelectionGUI(AnnihilationNexus plugin, PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.BLUE + "Select Your Class");

        addClassItem(gui, 0, "Dasher", plugin.getBlinkItem(), player, "Fast and agile.");
        addClassItem(gui, 1, "Scout", plugin.getGrappleItem(), player, "Ranged combat specialist.");
        addClassItem(gui, 2, "Scorpio", plugin.getScorpioItem(), player, "Melee combatant with a hook.");
        addClassItem(gui, 3, "Assassin", plugin.getAssassinItem(), player, "Stealthy and deadly.");
        addClassItem(gui, 4, "Spy", plugin.getSpyItem(), player, "Infiltrator and information gatherer.");
        addClassItem(gui, 5, "Transporter", plugin.getTransporterItem(), player, "Creates teleportation portals.");

        ItemStack farmer = new ItemStack(Material.WHEAT);
        addClassItem(gui, 6, "Farmer", farmer, player, "Sustains the team with food and resources.");
        addClassItem(gui, 7, "RiftWalker", plugin.getRiftWalkerItem(), player, "Teleports teammates to a target.");

        ItemStack civilian = new ItemStack(Material.CRAFTING_TABLE);
        addClassItem(gui, 8, "Civilian", civilian, player, "A regular civilian with no special abilities.");

        player.openInventory(gui);
    }

    private void addClassItem(Inventory gui, int slot, String className, ItemStack item, Player player,
            String description) {
        if (playerClassManager.isClassBanned(className)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        meta.setDisplayName(ChatColor.GOLD + className);

        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.GRAY + description);
        lore.add("");

        boolean unlocked = playerClassManager.isClassUnlocked(player.getUniqueId(), className);
        int cost = playerClassManager.getClassCost(className);

        if (unlocked) {
            lore.add(ChatColor.GREEN + "UNLOCKED");
            lore.add(ChatColor.YELLOW + "Click to select.");
        } else {
            lore.add(ChatColor.RED + "LOCKED");
            lore.add(ChatColor.GOLD + "Cost: " + cost + " XP");
            lore.add(ChatColor.YELLOW + "Click to purchase.");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        gui.setItem(slot, item);
    }
}
