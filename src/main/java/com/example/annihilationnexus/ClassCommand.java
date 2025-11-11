package com.example.annihilationnexus;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class ClassCommand implements CommandExecutor {

    private final PlayerClassManager playerClassManager;
    private final AnnihilationNexus plugin; // Add plugin instance

    public ClassCommand(AnnihilationNexus plugin, PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerClassManager = playerClassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        // Handle /class item
        if (args.length == 1 && args[0].equalsIgnoreCase("item")) {
            String className = playerClassManager.getPlayerClass(player.getUniqueId());
            if (className == null) {
                player.sendMessage(ChatColor.RED + "You have not selected a class yet.");
                return true;
            }

            handleClassItemToggle(player, className);
            return true;
        }

        // If a player runs /class with no arguments, open the GUI
        if (args.length == 0) {
            // Check if player is in a class region
            if (!plugin.getClassRegionManager().isLocationInRestrictedRegion(player.getLocation())) {
                player.sendMessage(ChatColor.RED + "You can only use the /class command within a designated class region.");
                return true;
            }

            ClassSelectionGUI gui = new ClassSelectionGUI(plugin, playerClassManager);
            gui.openGUI(player);
            return true;
        }

        // Admin command to set class for a player
        if (!sender.hasPermission("annihilation.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command in this way.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /class <player> <class>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return false;
        }

        String className = args[1];
        // Updated to include transporteractive
        if (!className.equalsIgnoreCase("dasher") && !className.equalsIgnoreCase("scout") && !className.equalsIgnoreCase("scorpio") && !className.equalsIgnoreCase("assassin") && !className.equalsIgnoreCase("spy") && !className.equalsIgnoreCase("transporter") && !className.equalsIgnoreCase("farmer")) {
            sender.sendMessage(ChatColor.RED + "Class not found.");
            return false;
        }

        playerClassManager.setPlayerClass(target.getUniqueId(), className.toLowerCase());
        sender.sendMessage(ChatColor.GREEN + "Set class for " + target.getName() + " to " + className);
        return true;
    }

    private void handleClassItemToggle(Player player, String className) {
        boolean removedAny = false;

        // Logic to remove items
        boolean removedFeast = false;
        boolean removedFamine = false;
        boolean removedOther = false;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;

            if (className.equalsIgnoreCase("farmer")) {
                if (plugin.isFeastItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedFeast = true;
                }
                if (plugin.isFamineItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedFamine = true;
                }
            } else {
                if (className.equalsIgnoreCase("dasher") && plugin.isBlinkItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedOther = true;
                } else if (className.equalsIgnoreCase("scout") && plugin.isGrappleItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedOther = true;
                } else if (className.equalsIgnoreCase("scorpio") && plugin.isScorpioItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedOther = true;
                } else if (className.equalsIgnoreCase("assassin") && plugin.isAssassinItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedOther = true;
                } else if (className.equalsIgnoreCase("spy") && plugin.isSpyItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedOther = true;
                } else if (className.equalsIgnoreCase("transporter") && plugin.isTransporterItem(item)) {
                    player.getInventory().setItem(i, null);
                    removedOther = true;
                }
            }
        }

        if (className.equalsIgnoreCase("farmer")) {
            removedAny = removedFeast || removedFamine;
        } else {
            removedAny = removedOther;
        }

        if (removedAny) {
            player.sendMessage(ChatColor.GREEN + "Your class item(s) have been removed.");
        } else {
            // Logic to add items
            if (className.equalsIgnoreCase("farmer")) {
                player.getInventory().addItem(plugin.getFeastItem());
                player.getInventory().addItem(plugin.getFamineItem());
            } else {
                ItemStack classItem = null;
                if (className.equalsIgnoreCase("dasher")) classItem = plugin.getBlinkItem();
                else if (className.equalsIgnoreCase("scout")) classItem = plugin.getGrappleItem();
                else if (className.equalsIgnoreCase("scorpio")) classItem = plugin.getScorpioItem();
                else if (className.equalsIgnoreCase("assassin")) classItem = plugin.getAssassinItem();
                else if (className.equalsIgnoreCase("spy")) classItem = plugin.getSpyItem();
                else if (className.equalsIgnoreCase("transporter")) classItem = plugin.getTransporterItem();

                if (classItem != null) {
                    player.getInventory().addItem(classItem);
                }
            }
            player.sendMessage(ChatColor.GREEN + "You have received your class item(s).");
        }
    }
}
