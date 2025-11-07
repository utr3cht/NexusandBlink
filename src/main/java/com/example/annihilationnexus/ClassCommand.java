package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClassCommand implements CommandExecutor {

    private final PlayerClassManager playerClassManager;

    public ClassCommand(PlayerClassManager playerClassManager) {
        this.playerClassManager = playerClassManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
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
        if (!className.equalsIgnoreCase("dasher") && !className.equalsIgnoreCase("scout") && !className.equalsIgnoreCase("scorpio") && !className.equalsIgnoreCase("assassin") && !className.equalsIgnoreCase("spy")) {
            sender.sendMessage(ChatColor.RED + "Class not found.");
            return false;
        }

        playerClassManager.setPlayerClass(target.getUniqueId(), className.toLowerCase());
        sender.sendMessage(ChatColor.GREEN + "Set class for " + target.getName() + " to " + className);
        return true;
    }
}
