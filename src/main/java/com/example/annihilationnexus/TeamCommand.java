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

    public TeamCommand(AnnihilationNexus plugin, NexusManager nexusManager, ScoreboardManager scoreboardManager,
            PlayerTeamManager playerTeamManager, TeamColorManager teamColorManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
        this.scoreboardManager = scoreboardManager;
        this.playerTeamManager = playerTeamManager;
        this.teamColorManager = teamColorManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /team join <team_name>");
                    return true;
                }
                handleJoinCommand(player, args[1]);
                break;
            case "leave":
                handleLeaveCommand(player);
                break;
            case "color":
                handleColorCommand(player, args);
                break;
            default:
                // Fallback for old usage or invalid subcommand
                // If the first argument is a team name, treat it as a join attempt for backward
                // compatibility if desired,
                // OR strictly enforce new usage. Given the request "team join colorで参加するように",
                // strict enforcement is better.
                sendUsage(player);
                break;
        }
        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "--- Team Commands ---");
        player.sendMessage(ChatColor.AQUA + "/team join <team>" + ChatColor.GRAY + " - Join a team.");
        player.sendMessage(ChatColor.AQUA + "/team leave" + ChatColor.GRAY + " - Leave your current team.");
        if (player.hasPermission("annihilation.admin")) {
            player.sendMessage(ChatColor.AQUA + "/team color <team> <color>" + ChatColor.GRAY + " - Set team color.");
        }
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

        sender.sendMessage(ChatColor.GREEN + "Team " + teamName + "'s color has been set to " + color + color.name()
                + ChatColor.GREEN + ".");
    }

    private void handleJoinCommand(Player player, String teamNameInput) {
        String teamName = teamNameInput.toUpperCase(Locale.ROOT);

        if (nexusManager.getNexus(teamName) == null) {
            player.sendMessage(ChatColor.RED + "Team '" + teamName + "' does not exist.");
            return;
        }

        String currentTeam = playerTeamManager.getPlayerTeam(player.getUniqueId());
        if (currentTeam != null && currentTeam.equals(teamName)) {
            player.sendMessage(ChatColor.RED + "You are already on team " + teamName + ".");
            return;
        }

        // Prevent team change if player is Spy with active abilities or invisibility
        PlayerClassManager classManager = plugin.getPlayerClassManager();
        String playerClass = classManager.getPlayerClass(player.getUniqueId());
        if (playerClass != null && playerClass.equalsIgnoreCase("spy")) {
            SpyAbility spyAbility = classManager.getSpyAbility(player.getUniqueId());
            if (spyAbility != null) {
                if (spyAbility.isVanished()) {
                    player.sendMessage(ChatColor.RED + "You cannot change teams while vanished.");
                    return;
                }
                if (spyAbility.isFleeing()) {
                    player.sendMessage(ChatColor.RED + "You cannot change teams while fleeing.");
                    return;
                }
            }
        }
        // Also check for invisibility potion effect (any class)
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)) {
            player.sendMessage(ChatColor.RED + "You cannot change teams while invisible.");
            return;
        }

        scoreboardManager.setPlayerTeam(player, teamName);
        playerTeamManager.setPlayerTeam(player.getUniqueId(), teamName); // Save team choice
        player.sendMessage(ChatColor.GREEN + "You have joined team " + teamName + ".");
    }

    private void handleLeaveCommand(Player player) {
        String currentTeam = playerTeamManager.getPlayerTeam(player.getUniqueId());
        if (currentTeam == null) {
            player.sendMessage(ChatColor.RED + "You are not in a team.");
            return;
        }

        // Prevent team change if player is Spy with active abilities or invisibility
        PlayerClassManager classManager = plugin.getPlayerClassManager();
        String playerClass = classManager.getPlayerClass(player.getUniqueId());
        if (playerClass != null && playerClass.equalsIgnoreCase("spy")) {
            SpyAbility spyAbility = classManager.getSpyAbility(player.getUniqueId());
            if (spyAbility != null) {
                if (spyAbility.isVanished()) {
                    player.sendMessage(ChatColor.RED + "You cannot leave your team while vanished.");
                    return;
                }
                if (spyAbility.isFleeing()) {
                    player.sendMessage(ChatColor.RED + "You cannot leave your team while fleeing.");
                    return;
                }
            }
        }
        // Also check for invisibility potion effect (any class)
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.INVISIBILITY)) {
            player.sendMessage(ChatColor.RED + "You cannot leave your team while invisible.");
            return;
        }

        playerTeamManager.setPlayerTeam(player.getUniqueId(), null);
        scoreboardManager.removePlayerFromTeam(player);
        player.sendMessage(ChatColor.GREEN + "You have left your team.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> candidates = new ArrayList<>();

        if (args.length == 1) {
            candidates.add("join");
            candidates.add("leave");
            if (sender.hasPermission("annihilation.admin")) {
                candidates.add("color");
            }
            StringUtil.copyPartialMatches(args[0], candidates, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("join")) {
                candidates.addAll(nexusManager.getAllNexuses().keySet());
                StringUtil.copyPartialMatches(args[1], candidates, completions);
            } else if (args[0].equalsIgnoreCase("color") && sender.hasPermission("annihilation.admin")) {
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
