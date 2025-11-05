package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.Map;

public class ScoreboardManager {

    private final AnnihilationNexus plugin;
    private final NexusManager nexusManager;

    public ScoreboardManager(AnnihilationNexus plugin, NexusManager nexusManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
        }

        Objective objective = board.getObjective("nexusHealth");
        if (objective == null) {
            objective = board.registerNewObjective("nexusHealth", "dummy", ChatColor.YELLOW + "Nexus Health");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Reset scores to avoid old teams showing up
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        for (Nexus nexus : nexusManager.getAllNexuses().values()) {
            objective.getScore(nexus.getTeamName()).setScore(nexus.getHealth());
        }
        
        player.setScoreboard(board);
    }

    public void updateForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }
}
