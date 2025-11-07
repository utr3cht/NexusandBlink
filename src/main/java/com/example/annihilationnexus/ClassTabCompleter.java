package com.example.annihilationnexus;

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
        if (args.length == 2) {
            return Arrays.asList("dasher", "scout", "scorpio", "assassin");
        }
        return null;
    }
}
