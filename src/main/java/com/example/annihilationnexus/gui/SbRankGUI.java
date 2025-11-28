package com.example.annihilationnexus.gui;

import com.example.annihilationnexus.AnnihilationNexus;
import com.example.annihilationnexus.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SbRankGUI implements InventoryHolder, Listener {

    private final AnnihilationNexus plugin;
    private Inventory inventory;

    public SbRankGUI(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        inventory = Bukkit.createInventory(this, 9, ChatColor.DARK_PURPLE + "Select Display Rank");

        Rank currentRealRank = plugin.getRankManager().getRank(player);
        if (currentRealRank == null) {
            player.sendMessage(ChatColor.RED + "You do not have a rank.");
            return;
        }

        int slot = 0;
        for (Rank rank : Rank.values()) {
            // Only show ranks up to the player's real rank (assuming enum order is
            // ascending)
            // Or if we want to show all but lock higher ones?
            // User request: "自分に付与されているRANKのPrefixのみに切り替えられるように"
            // So we check if ordinal <= currentRealRank.ordinal()

            if (rank.ordinal() <= currentRealRank.ordinal()) {
                ItemStack item = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(rank.getPrefix() + rank.getDisplayName());
                meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to select this prefix."));
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
            } else {
                // Optional: Show locked ranks? For now, just skip or show barrier.
                ItemStack item = new ItemStack(Material.BARRIER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.RED + rank.getDisplayName() + " (Locked)");
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
            }
            slot++;
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof SbRankGUI) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem.getType() == Material.BARRIER) {
                player.sendMessage(ChatColor.RED + "You have not unlocked this rank.");
                return;
            }

            // Find rank from display name (stripping colors/prefix might be tricky, let's
            // use slot index if possible)
            // Or just iterate ranks and match name?
            // Slot index matches Rank.values() index in our loop.
            int slot = event.getSlot();
            Rank[] ranks = Rank.values();

            if (slot >= 0 && slot < ranks.length) {
                Rank selectedRank = ranks[slot];
                Rank currentRealRank = plugin.getRankManager().getRank(player);

                if (currentRealRank != null && selectedRank.ordinal() <= currentRealRank.ordinal()) {
                    plugin.getRankManager().setDisplayRank(player, selectedRank);
                    plugin.getScoreboardManager().updatePlayerPrefix(player);
                    player.sendMessage(ChatColor.GREEN + "Display rank set to: " + selectedRank.getDisplayName());
                    player.closeInventory();
                }
            }
        }
    }
}
