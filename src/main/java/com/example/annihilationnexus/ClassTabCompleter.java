package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassTabCompleter implements TabCompleter {

    private final PlayerClassManager playerClassManager;

    public ClassTabCompleter(PlayerClassManager playerClassManager) {
        this.playerClassManager = playerClassManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) { // Suggest online players for the first argument (player name)
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }
        if (args.length == 2) { // No suggestions for class names
            return new ArrayList<>(); // Return empty list to prevent default tab completion
        }
        return null;
    }
}
