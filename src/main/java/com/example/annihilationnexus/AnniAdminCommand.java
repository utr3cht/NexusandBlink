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
            sender.sendMessage(ChatColor.RED + "Usage: /anni <reload|friendlyfire|rank|xp>");
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
                sender.sendMessage(
                        ChatColor.GREEN + "Friendly fire has been " + (enabled ? "enabled" : "disabled") + ".");
                break;

            case "rank":
                if (!sender.hasPermission("annihilation.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /anni rank <set|give|remove> <player> [rank]");
                    return true;
                }
                String sub = args[1].toLowerCase();
                if (sub.equals("set") || sub.equals("give")) {
                    if (args.length != 4) {
                        sender.sendMessage(ChatColor.RED + "Usage: /anni rank " + sub + " <player> <rank>");
                        return true;
                    }
                    org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    String rankName = args[3].toUpperCase();
                    try {
                        Rank rank = Rank.valueOf(rankName);
                        plugin.getRankManager().setRank(target, rank);
                        plugin.getScoreboardManager().updatePlayerPrefix(target);
                        sender.sendMessage(ChatColor.GREEN + "Set rank of " + target.getName() + " to "
                                + rank.getDisplayName() + ".");
                        target.sendMessage(
                                ChatColor.GREEN + "Your rank has been set to " + rank.getDisplayName() + ".");
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(
                                ChatColor.RED + "Invalid rank. Please use tab completion for available ranks.");
                    }
                } else if (sub.equals("remove")) {
                    if (args.length != 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /anni rank remove <player>");
                        return true;
                    }
                    org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found.");
                        return true;
                    }
                    plugin.getRankManager().removeRank(target);
                    sender.sendMessage(ChatColor.GREEN + "Removed rank from " + target.getName() + ".");
                    target.sendMessage(ChatColor.YELLOW + "Your rank has been removed.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /anni rank <set|give|remove>");
                }
                break;

            case "xp":
                if (!sender.hasPermission("annihilation.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(
                            ChatColor.RED + "Usage: /anni xp <give|remove|set|list|multiple> [player] [amount]");
                    return true;
                }
                String xpSub = args[1].toLowerCase();

                if (xpSub.equals("list")) {
                    int page = 1;
                    if (args.length > 2) {
                        try {
                            page = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "Invalid page number.");
                            return true;
                        }
                    }

                    java.util.List<java.util.Map.Entry<java.util.UUID, Integer>> allPlayers = plugin.getXpManager()
                            .getAllPlayersSorted();
                    int totalPlayers = allPlayers.size();
                    int itemsPerPage = 10;
                    int totalPages = (int) Math.ceil((double) totalPlayers / itemsPerPage);

                    if (page < 1)
                        page = 1;
                    if (page > totalPages && totalPages > 0)
                        page = totalPages;

                    sender.sendMessage(ChatColor.GOLD + "=== XP Ranking (Page " + page + "/" + totalPages + ") ===");

                    int startIndex = (page - 1) * itemsPerPage;
                    int endIndex = Math.min(startIndex + itemsPerPage, totalPlayers);

                    for (int i = startIndex; i < endIndex; i++) {
                        java.util.Map.Entry<java.util.UUID, Integer> entry = allPlayers.get(i);
                        String playerName = org.bukkit.Bukkit.getOfflinePlayer(entry.getKey()).getName();
                        if (playerName == null)
                            playerName = "Unknown";

                        sender.sendMessage(ChatColor.YELLOW + "#" + (i + 1) + " " + ChatColor.WHITE + playerName + ": "
                                + ChatColor.GREEN + entry.getValue() + " XP");
                    }
                    return true;
                }

                if (xpSub.equals("multiple")) {
                    if (!sender.hasPermission("annihilation.admin.xp.multiple")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                        return true;
                    }
                    if (args.length < 3) {
                        sender.sendMessage(ChatColor.RED + "Usage: /anni xp multiple <amount>");
                        return true;
                    }
                    try {
                        double amount = Double.parseDouble(args[2]);
                        plugin.getXpManager().setGlobalMultiplier(amount);
                        sender.sendMessage(ChatColor.GREEN + "Global XP Multiplier set to " + amount);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid number format.");
                    }
                    return true;
                }

                if (args.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /anni xp " + xpSub + " <player> <amount>");
                    return true;
                }

                org.bukkit.entity.Player target = org.bukkit.Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }

                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount.");
                    return true;
                }

                if (xpSub.equals("give")) {
                    plugin.getXpManager().addXp(target, amount);
                    sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " XP to " + target.getName() + ".");
                    target.sendMessage(ChatColor.GREEN + "You received " + amount + " XP.");
                } else if (xpSub.equals("remove")) {
                    plugin.getXpManager().addXp(target, -amount);
                    sender.sendMessage(ChatColor.GREEN + "Removed " + amount + " XP from " + target.getName() + ".");
                    target.sendMessage(ChatColor.RED + "You lost " + amount + " XP.");
                } else if (xpSub.equals("set")) {
                    plugin.getXpManager().setXp(target, amount);
                    sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s XP to " + amount + ".");
                    target.sendMessage(ChatColor.GREEN + "Your XP has been set to " + amount + ".");
                } else {
                    sender.sendMessage(
                            ChatColor.RED + "Unknown subcommand. Usage: /anni xp <give|remove|set|list|multiple>");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /anni <reload|friendlyfire|rank|xp>");
                break;
        }
        return true;
    }
}
