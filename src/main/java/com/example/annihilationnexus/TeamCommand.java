package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor {

    private final AnnihilationNexus plugin;
    private final NexusManager nexusManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerTeamManager playerTeamManager;

    public TeamCommand(AnnihilationNexus plugin, NexusManager nexusManager, ScoreboardManager scoreboardManager, PlayerTeamManager playerTeamManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
        this.scoreboardManager = scoreboardManager;
        this.playerTeamManager = playerTeamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /team <team_name>");
            return false;
        }

        Player player = (Player) sender;
        String teamName = args[0].toUpperCase(Locale.ROOT);

        // --- DEBUG LOG ---
        String existingKeys = nexusManager.getAllNexuses().keySet().stream()
                .collect(Collectors.joining(", ", "[", "]"));
        plugin.getLogger().info("[DEBUG] /team command: Checking for team '" + teamName + "'. Existing keys in NexusManager: " + existingKeys);
        // --- END DEBUG LOG ---

        if (nexusManager.getNexus(teamName) == null) {
            player.sendMessage(ChatColor.RED + "Team '" + teamName + "' does not exist.");
            return true;
        }

        scoreboardManager.setPlayerTeam(player, teamName);
        playerTeamManager.setPlayerTeam(player.getUniqueId(), teamName); // Save team choice
        player.sendMessage(ChatColor.GREEN + "You have joined team " + teamName + ".");

        return true;
    }
}
