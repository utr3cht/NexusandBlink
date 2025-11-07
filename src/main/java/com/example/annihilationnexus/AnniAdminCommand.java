package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AnniAdminCommand implements CommandExecutor {

    private final AnnihilationNexus plugin;

    public AnniAdminCommand(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("annihilation.admin")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            plugin.reload();
            sender.sendMessage(ChatColor.GREEN + "AnnihilationNexus configuration reloaded.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /anni reload");
        return false;
    }
}
