package com.example.annihilationnexus;

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
        // If a player runs /class with no arguments, open the GUI
        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;
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
        if (!className.equalsIgnoreCase("dasher") && !className.equalsIgnoreCase("scout") && !className.equalsIgnoreCase("scorpio") && !className.equalsIgnoreCase("assassin") && !className.equalsIgnoreCase("spy") && !className.equalsIgnoreCase("transporter")) {
            sender.sendMessage(ChatColor.RED + "Class not found.");
            return false;
        }

        playerClassManager.setPlayerClass(target.getUniqueId(), className.toLowerCase());
        sender.sendMessage(ChatColor.GREEN + "Set class for " + target.getName() + " to " + className);
        return true;
    }
}
