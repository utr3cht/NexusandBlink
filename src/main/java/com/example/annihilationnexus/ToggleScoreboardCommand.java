package com.example.annihilationnexus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleScoreboardCommand implements CommandExecutor {

    private final ScoreboardManager scoreboardManager;

    public ToggleScoreboardCommand(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        scoreboardManager.toggleScoreboard(player);
        return true;
    }
}
