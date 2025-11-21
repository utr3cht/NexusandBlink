package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class NexusAdminCommand implements CommandExecutor {

    private final NexusManager nexusManager;
    private final AnnihilationNexus plugin;

    public NexusAdminCommand(AnnihilationNexus plugin, NexusManager nexusManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /nexus <create|delete|setnexushp>");
            return false;
        }

        Player player = (Player) sender;

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nexus create <team>");
                    return false;
                }
                String team = args[1].toUpperCase(Locale.ROOT);
                nexusManager.createNexus(team, player.getLocation());
                sender.sendMessage(ChatColor.GREEN + "Nexus for team " + team + " created at your location.");
                plugin.getScoreboardManager().updateForAllPlayers();
                break;
            case "delete":
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nexus delete <team>");
                    return false;
                }
                String teamToDelete = args[1];
                Nexus nexusToDelete = nexusManager.getNexus(teamToDelete);
                if (nexusToDelete == null) {
                    sender.sendMessage(ChatColor.RED + "Nexus for team " + teamToDelete + " not found.");
                    return false;
                }
                // Remove the physical block
                nexusToDelete.getLocation().getBlock().setType(org.bukkit.Material.AIR);
                nexusManager.removeNexus(teamToDelete);
                sender.sendMessage(ChatColor.GREEN + "Nexus for team " + teamToDelete + " deleted.");
                plugin.getScoreboardManager().updateForAllPlayers(); // Refresh scoreboards
                break;
            case "sethp":
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nexus sethp <team> <hp>");
                    return false;
                }
                String teamName = args[1].toUpperCase(Locale.ROOT);
                Nexus teamNexus = nexusManager.getNexus(teamName);
                if (teamNexus == null) {
                    sender.sendMessage(ChatColor.RED + "Nexus for team " + teamName + " not found.");
                    return false;
                }
                try {
                    int hp = Integer.parseInt(args[2]);
                    teamNexus.setHealth(hp);
                    sender.sendMessage(ChatColor.GREEN + "Nexus health for team " + teamName + " set to " + hp);
                    plugin.getScoreboardManager().updateForAllPlayers(); // Refresh scoreboards
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid health amount.");
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /nexus <create|delete|setnexushp>");
                break;
        }

        return true;
    }
}
