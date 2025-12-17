package com.example.annihilationnexus.commands;

import com.example.annihilationnexus.Clan;
import com.example.annihilationnexus.ClanManager;
import com.example.annihilationnexus.XpManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Collectors;

public class ClanCommand implements CommandExecutor, org.bukkit.command.TabCompleter {

    private final ClanManager clanManager;
    private final XpManager xpManager;

    public ClanCommand(ClanManager clanManager, XpManager xpManager) {
        this.clanManager = clanManager;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player, args);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "view":
                handleView(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "delete":
                handleDelete(player);
                break;
            case "color":
                handleColor(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /clan create <name>");
            return;
        }

        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You are already in a clan.");
            return;
        }

        String name = args[1];
        if (name.length() > 10) {
            player.sendMessage(ChatColor.RED + "Clan name must be 10 characters or less.");
            return;
        }

        int requiredXp = 100;
        int currentXp = xpManager.getXp(player);

        if (currentXp < requiredXp) {
            player.sendMessage(ChatColor.RED + "You need at least " + requiredXp + " Shrektbow XP to create a clan.");
            return;
        }

        Clan clan = clanManager.createClan(name, player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "A clan with that name already exists.");
            return;
        }

        // xpManager.addXp(player, -cost); // No longer consuming XP
        player.sendMessage(ChatColor.GREEN + "Clan " + name + " created!");
        Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " created a new clan: " + ChatColor.AQUA + name);
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /clan invite <player>");
            return;
        }

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "You are not in a clan.");
            return;
        }

        if (!clan.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the clan owner can invite players.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return;
        }

        if (clanManager.getClanByPlayer(target.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + target.getName() + " is already in a clan.");
            return;
        }

        if (clan.isInvited(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + target.getName() + " is already invited.");
            return;
        }

        clan.addInvite(target.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Invited " + target.getName() + " to the clan.");
        target.sendMessage(ChatColor.GREEN + "You have been invited to join clan " + ChatColor.GOLD + clan.getName());
        target.sendMessage(ChatColor.GREEN + "Type " + ChatColor.AQUA + "/clan join " + clan.getName() + ChatColor.GREEN
                + " to join.");
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /clan join <clan>");
            return;
        }

        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "You are already in a clan.");
            return;
        }

        String clanName = args[1];
        Clan clan = clanManager.getClan(clanName);

        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Clan not found.");
            return;
        }

        if (!clan.isInvited(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You have not been invited to this clan.");
            return;
        }

        clanManager.addMember(clan, player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You joined clan " + clan.getName() + "!");

        for (UUID memberId : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && !member.getUniqueId().equals(player.getUniqueId())) {
                member.sendMessage(ChatColor.GREEN + player.getName() + " has joined the clan!");
            }
        }
    }

    private void handleView(Player player, String[] args) {
        Clan clan;
        if (args.length > 1) {
            clan = clanManager.getClan(args[1]);
        } else {
            clan = clanManager.getClanByPlayer(player.getUniqueId());
        }

        if (clan == null) {
            player.sendMessage(ChatColor.RED + "Clan not found or you are not in one.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "=== Clan: " + clan.getColor() + clan.getName() + ChatColor.GOLD + " ===");
        player.sendMessage(ChatColor.YELLOW + "Owner: " + Bukkit.getOfflinePlayer(clan.getOwner()).getName());
        String members = clan.getMembers().stream()
                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                .collect(Collectors.joining(", "));
        player.sendMessage(ChatColor.YELLOW + "Members: " + members);
    }

    private void handleDelete(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "You are not in a clan.");
            return;
        }

        if (!clan.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the clan owner can delete the clan.");
            return;
        }

        String clanName = clan.getName();
        clanManager.deleteClan(clanName);
        Bukkit.broadcastMessage(
                ChatColor.RED + "Clan " + clanName + " has been disbanded by " + player.getName() + ".");
    }

    private void handleLeave(Player player) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "You are not in a clan.");
            return;
        }

        if (clan.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED
                    + "Owners cannot leave their clan. You must disband it (not implemented) or transfer ownership (not implemented).");
            if (clan.getMembers().size() == 1) {
                clanManager.removeMember(clan, player.getUniqueId()); // This triggers deleteClan in manager
                player.sendMessage(ChatColor.GREEN + "Clan disbanded.");
            } else {
                player.sendMessage(ChatColor.RED + "You cannot leave as owner unless you are the only member.");
            }
            return;
        }

        clanManager.removeMember(clan, player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "You left the clan.");

        for (UUID memberId : clan.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(ChatColor.YELLOW + player.getName() + " has left the clan.");
            }
        }
    }

    private void handleColor(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /clan color <color>");
            return;
        }

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ChatColor.RED + "You are not in a clan.");
            return;
        }

        if (!clan.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the clan owner can change the clan color.");
            return;
        }

        String colorName = args[1].toUpperCase();
        ChatColor color;
        try {
            color = ChatColor.valueOf(colorName);
        } catch (IllegalArgumentException e) {
            // Try to parse as & code
            if (colorName.length() == 2 && colorName.startsWith("&")) {
                color = ChatColor.getByChar(colorName.charAt(1));
            } else {
                color = null;
            }
        }

        if (color == null || !color.isColor()) {
            player.sendMessage(ChatColor.RED + "Invalid color. Please use a valid color name or code (e.g., RED, &c).");
            return;
        }

        clan.setColor(color.toString());
        clanManager.saveClans(); // Ensure it's saved
        player.sendMessage(ChatColor.GREEN + "Clan color updated to " + color + color.name());
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Clan Commands ===");
        player.sendMessage(
                ChatColor.YELLOW + "/clan create <name> " + ChatColor.GRAY
                        + "- Create a clan (Requires 100 XP, no cost)");
        player.sendMessage(ChatColor.YELLOW + "/clan invite <player> " + ChatColor.GRAY + "- Invite a player");
        player.sendMessage(ChatColor.YELLOW + "/clan join <clan> " + ChatColor.GRAY + "- Join a clan");
        player.sendMessage(ChatColor.YELLOW + "/clan view [clan] " + ChatColor.GRAY + "- View clan info");
        player.sendMessage(ChatColor.YELLOW + "/clan leave " + ChatColor.GRAY + "- Leave your clan");
        player.sendMessage(ChatColor.YELLOW + "/clan delete " + ChatColor.GRAY + "- Delete your clan (Owner only)");
        player.sendMessage(
                ChatColor.YELLOW + "/clan color <color> " + ChatColor.GRAY + "- Change clan tag color (Owner only)");
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        java.util.List<String> completions = new java.util.ArrayList<>();
        if (args.length == 1) {
            java.util.List<String> subCommands = java.util.Arrays.asList("create", "invite", "join", "view", "leave",
                    "delete", "color");
            org.bukkit.util.StringUtil.copyPartialMatches(args[0], subCommands, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite")) {
                return null; // Suggest player names
            } else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("view")) {
                java.util.List<String> clanNames = clanManager.getAllClans().stream()
                        .map(Clan::getName)
                        .collect(Collectors.toList());
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], clanNames, completions);
            } else if (args[0].equalsIgnoreCase("color")) {
                java.util.List<String> colors = java.util.Arrays.stream(ChatColor.values())
                        .filter(ChatColor::isColor)
                        .map(ChatColor::name)
                        .collect(Collectors.toList());
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], colors, completions);
            }
        }
        java.util.Collections.sort(completions);
        return completions;
    }
}
