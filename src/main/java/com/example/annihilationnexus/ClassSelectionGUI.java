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

        // Dasher
        ItemStack dasher = plugin.getBlinkItem();
        ItemMeta dasherMeta = dasher.getItemMeta();
        if (dasherMeta != null) {
            dasherMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Dasher"); // Explicitly set display name for GUI
            dasherMeta.setLore(Arrays.asList(ChatColor.GRAY + "Fast and agile."));
            dasher.setItemMeta(dasherMeta);
        }
        gui.setItem(0, dasher);

        // Scout
        ItemStack scout = plugin.getGrappleItem();
        ItemMeta scoutMeta = scout.getItemMeta();
        if (scoutMeta != null) { // Add null check for ItemMeta
            scoutMeta.setDisplayName("§aScout"); // Explicitly set display name for GUI
            scoutMeta.setLore(Arrays.asList(ChatColor.GRAY + "Ranged combat specialist."));
            scout.setItemMeta(scoutMeta);
        }
        gui.setItem(1, scout);

        // Scorpio
        ItemStack scorpio = plugin.getScorpioItem();
        ItemMeta scorpioMeta = scorpio.getItemMeta();
        if (scorpioMeta != null) {
            scorpioMeta.setDisplayName(ChatColor.GOLD + "Scorpio"); // Explicitly set display name for GUI
            scorpioMeta.setLore(Arrays.asList(ChatColor.GRAY + "Melee combatant with a hook."));
            scorpio.setItemMeta(scorpioMeta);
        }
        gui.setItem(2, scorpio);

        // Assassin
        ItemStack assassin = plugin.getAssassinItem();
        ItemMeta assassinMeta = assassin.getItemMeta();
        if (assassinMeta != null) {
            assassinMeta.setDisplayName(ChatColor.GRAY + "Assassin"); // Explicitly set display name for GUI
            assassinMeta.setLore(Arrays.asList(ChatColor.GRAY + "Stealthy and deadly."));
            assassin.setItemMeta(assassinMeta);
        }
        gui.setItem(3, assassin);

        // Spy
        ItemStack spy = plugin.getSpyItem();
        ItemMeta spyMeta = spy.getItemMeta();
        if (spyMeta != null) {
            spyMeta.setDisplayName(ChatColor.AQUA + "Spy"); // Explicitly set display name for GUI
            spyMeta.setLore(Arrays.asList(ChatColor.GRAY + "Infiltrator and information gatherer."));
            spy.setItemMeta(spyMeta);
        }
        gui.setItem(4, spy);

        // Transporter
        ItemStack transporter = plugin.getTransporterItem();
        ItemMeta transporterMeta = transporter.getItemMeta();
        transporterMeta.setLore(Arrays.asList(ChatColor.GRAY + "Creates teleportation portals."));
        transporter.setItemMeta(transporterMeta);
        gui.setItem(5, transporter);

        // Farmer
        ItemStack farmer = new ItemStack(Material.WHEAT);
        ItemMeta farmerMeta = farmer.getItemMeta();
        if (farmerMeta != null) {
            farmerMeta.setDisplayName(ChatColor.YELLOW + "Farmer");
            farmerMeta.setLore(Arrays.asList(ChatColor.GRAY + "Sustains the team with food and resources."));
            farmer.setItemMeta(farmerMeta);
        }
        gui.setItem(6, farmer);


        player.openInventory(gui);
    }
}
