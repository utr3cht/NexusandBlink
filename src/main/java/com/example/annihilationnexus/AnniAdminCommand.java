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
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /anni <reload|friendlyfire>");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("annihilation.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                plugin.reload();
                sender.sendMessage(ChatColor.GREEN + "AnnihilationNexus configuration reloaded.");
                break;

            case "friendlyfire":
                if (!sender.hasPermission("annihilation.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /anni friendlyfire <on|off>");
                    return true;
                }
                boolean enabled;
                if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true")) {
                    enabled = true;
                } else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")) {
                    enabled = false;
                } else {
                    sender.sendMessage(ChatColor.RED + "Usage: /anni friendlyfire <on|off|true|false>");
                    return true;
                }
                plugin.setFriendlyFire(enabled);
                sender.sendMessage(ChatColor.GREEN + "Friendly fire has been " + (enabled ? "enabled" : "disabled") + ".");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /anni <reload|friendlyfire>");
                break;
        }
        return true;
    }
}
