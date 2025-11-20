package com.example.annihilationnexus;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TeamCommand implements CommandExecutor, TabCompleter {

    private final AnnihilationNexus plugin;
    private final NexusManager nexusManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerTeamManager playerTeamManager;
    private final TeamColorManager teamColorManager;

    public TeamCommand(AnnihilationNexus plugin, NexusManager nexusManager, ScoreboardManager scoreboardManager, PlayerTeamManager playerTeamManager, TeamColorManager teamColorManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
        this.scoreboardManager = scoreboardManager;
        this.playerTeamManager = playerTeamManager;
        this.teamColorManager = teamColorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("color")) {
            handleColorCommand(sender, args);
            return true;
        }

        return handleJoinCommand(sender, args);
    }

    private void handleColorCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("annihilation.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /team color <team_name> <color>");
            return;
        }

        String teamName = args[1].toUpperCase(Locale.ROOT);
        String colorName = args[2].toUpperCase(Locale.ROOT);

        if (nexusManager.getNexus(teamName) == null) {
            sender.sendMessage(ChatColor.RED + "Team '" + teamName + "' does not exist.");
            return;
        }

        ChatColor color;
        try {
            color = ChatColor.valueOf(colorName);
            if (!color.isColor()) {
                sender.sendMessage(ChatColor.RED + "'" + colorName + "' is a format, not a color.");
                return;
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid color: " + colorName);
            return;
        }

        teamColorManager.setTeamColor(teamName, color);
        scoreboardManager.updateTeamConfiguration(teamName);

        sender.sendMessage(ChatColor.GREEN + "Team " + teamName + "'s color has been set to " + color + color.name() + ChatColor.GREEN + ".");
    }

    private boolean handleJoinCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /team <team_name>");
            // Also show admin usage if they have perm
            if (sender.hasPermission("annihilation.admin")) {
                sender.sendMessage(ChatColor.YELLOW + "Admin Usage: /team color <team_name> <color>");
            }
            return false;
        }

        Player player = (Player) sender;
        String teamName = args[0].toUpperCase(Locale.ROOT);

        if (nexusManager.getNexus(teamName) == null) {
            player.sendMessage(ChatColor.RED + "Team '" + teamName + "' does not exist.");
            return true;
        }

        scoreboardManager.setPlayerTeam(player, teamName);
        playerTeamManager.setPlayerTeam(player.getUniqueId(), teamName); // Save team choice
        player.sendMessage(ChatColor.GREEN + "You have joined team " + teamName + ".");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> candidates = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("annihilation.admin")) {
                candidates.add("color");
            }
            candidates.addAll(nexusManager.getAllNexuses().keySet());
            StringUtil.copyPartialMatches(args[0], candidates, completions);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("color")) {
            if (sender.hasPermission("annihilation.admin")) {
                candidates.addAll(nexusManager.getAllNexuses().keySet());
                StringUtil.copyPartialMatches(args[1], candidates, completions);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("color")) {
            if (sender.hasPermission("annihilation.admin")) {
                candidates.addAll(Arrays.stream(ChatColor.values())
                        .filter(ChatColor::isColor)
                        .map(Enum::name)
                        .collect(Collectors.toList()));
                StringUtil.copyPartialMatches(args[2], candidates, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}
