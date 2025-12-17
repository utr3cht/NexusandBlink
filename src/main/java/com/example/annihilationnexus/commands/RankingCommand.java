package com.example.annihilationnexus.commands;

import com.example.annihilationnexus.AnnihilationNexus;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RankingCommand implements CommandExecutor {

    private final AnnihilationNexus plugin;

    public RankingCommand(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid page number.");
                return true;
            }
        }

        List<Map.Entry<UUID, Integer>> allPlayers = plugin.getXpManager().getAllPlayersSorted();
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
            Map.Entry<UUID, Integer> entry = allPlayers.get(i);
            String playerName = org.bukkit.Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (playerName == null)
                playerName = "Unknown";

            sender.sendMessage(ChatColor.YELLOW + "#" + (i + 1) + " " + ChatColor.WHITE + playerName + ": "
                    + ChatColor.GREEN + entry.getValue() + " XP");
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            int rank = plugin.getXpManager().getPlayerRank(player.getUniqueId());
            int xp = plugin.getXpManager().getXp(player);
            sender.sendMessage(ChatColor.GOLD + "-----------------------------");
            if (rank != -1) {
                sender.sendMessage(ChatColor.AQUA + "Your Rank: " + ChatColor.WHITE + "#" + rank + " ("
                        + ChatColor.GREEN + xp + " XP" + ChatColor.WHITE + ")");
            } else {
                sender.sendMessage(ChatColor.AQUA + "Your Rank: " + ChatColor.GRAY + "Unranked");
            }
        }

        return true;
    }
}
