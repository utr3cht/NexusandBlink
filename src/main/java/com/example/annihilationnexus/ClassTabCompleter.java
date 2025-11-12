package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassTabCompleter implements TabCompleter {

    private final List<String> CLASS_NAMES = Arrays.asList("dasher", "scout", "scorpio", "assassin", "spy", "transporter", "farmer");

    public ClassTabCompleter(PlayerClassManager playerClassManager) {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender instanceof Player) {
                completions.add("item");
            }
            if (sender.hasPermission("annihilation.admin")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
            return completions;
        }

        if (args.length == 2) {
            if (sender.hasPermission("annihilation.admin")) {
                // Check if the first argument is a player name, not "item"
                if (Bukkit.getPlayer(args[0]) != null) {
                    return CLASS_NAMES.stream()
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
