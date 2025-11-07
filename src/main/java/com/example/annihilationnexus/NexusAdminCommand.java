package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                String team = args[1];
                nexusManager.createNexus(team, player.getLocation());
                sender.sendMessage(ChatColor.GREEN + "Nexus for team " + team + " created at your location.");
                break;
            case "delete":
                Nexus nexus = nexusManager.getNexusAt(player.getLocation());
                if (nexus == null) {
                    sender.sendMessage(ChatColor.RED + "There is no nexus at your location.");
                    return false;
                }
                nexusManager.removeNexus(nexus.getTeamName());
                sender.sendMessage(ChatColor.GREEN + "Nexus deleted.");
                break;
            case "setnexushp":
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /nexus setnexushp <team> <hp>");
                    return false;
                }
                String teamName = args[1];
                Nexus teamNexus = nexusManager.getNexus(teamName);
                if (teamNexus == null) {
                    sender.sendMessage(ChatColor.RED + "Nexus for team " + teamName + " not found.");
                    return false;
                }
                try {
                    int hp = Integer.parseInt(args[2]);
                    teamNexus.setHealth(hp);
                    sender.sendMessage(ChatColor.GREEN + "Nexus health for team " + teamName + " set to " + hp);
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
