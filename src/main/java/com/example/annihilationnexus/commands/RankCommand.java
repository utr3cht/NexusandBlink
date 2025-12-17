package com.example.annihilationnexus.commands;

import com.example.annihilationnexus.AnnihilationNexus;
import com.example.annihilationnexus.Rank;
import com.example.annihilationnexus.XpRank;
import com.example.annihilationnexus.RankManager;
import com.example.annihilationnexus.XpManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {

    private final AnnihilationNexus plugin;
    private final RankManager rankManager;
    private final XpManager xpManager;

    public RankCommand(AnnihilationNexus plugin, RankManager rankManager, XpManager xpManager) {
        this.plugin = plugin;
        this.rankManager = rankManager;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        int currentXp = xpManager.getXp(player);
        XpRank currentRank = XpRank.getRankForXp(currentXp);

        // Find next rank
        XpRank nextRank = null;
        if (currentRank == null) {
            nextRank = XpRank.NOVICE_I;
        } else {
            nextRank = currentRank.getNextRank();
        }

        player.sendMessage(ChatColor.GOLD + "========================================");
        String rankName = (currentRank != null) ? currentRank.getDisplayName() : "Unranked";
        player.sendMessage(ChatColor.YELLOW + "Current Rank: " + ChatColor.WHITE + rankName);
        player.sendMessage(ChatColor.YELLOW + "Current XP: " + ChatColor.WHITE + currentXp);

        if (nextRank != null) {
            player.sendMessage(ChatColor.YELLOW + "Next Rank: " + ChatColor.WHITE + nextRank.getDisplayName());
            player.sendMessage(
                    ChatColor.YELLOW + "XP Needed: " + ChatColor.WHITE + (nextRank.getRequiredXp() - currentXp));

            // Progress Bar
            int prevRankXp = 0;
            if (currentRank != null) {
                prevRankXp = currentRank.getRequiredXp();
            }

            int totalNeeded = nextRank.getRequiredXp() - prevRankXp;
            int currentProgress = currentXp - prevRankXp;

            // Avoid division by zero if totalNeeded is 0 (shouldn't happen with valid
            // ranks)
            if (totalNeeded <= 0)
                totalNeeded = 1;

            float percentage = (float) currentProgress / totalNeeded;
            // Clamp percentage
            if (percentage < 0)
                percentage = 0;
            if (percentage > 1)
                percentage = 1;

            int bars = 20;
            int filledBars = (int) (percentage * bars);

            StringBuilder progressBar = new StringBuilder(ChatColor.GRAY + "[");
            for (int i = 0; i < bars; i++) {
                if (i < filledBars) {
                    progressBar.append(ChatColor.GREEN + "|");
                } else {
                    progressBar.append(ChatColor.RED + "|");
                }
            }
            progressBar.append(ChatColor.GRAY + "]");
            player.sendMessage(
                    progressBar.toString() + ChatColor.WHITE + " " + String.format("%.1f", percentage * 100) + "%");

        } else {
            player.sendMessage(ChatColor.GREEN + "You have reached the maximum rank!");
        }
        player.sendMessage(ChatColor.GOLD + "========================================");

        return true;
    }
}
