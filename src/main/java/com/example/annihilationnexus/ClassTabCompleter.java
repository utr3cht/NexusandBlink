package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ClassTabCompleter implements TabCompleter {

    private final AnnihilationNexus plugin; // Reference to the main plugin class to access PlayerClassManager

    public ClassTabCompleter(AnnihilationNexus plugin) { // Constructor now takes AnnihilationNexus
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> candidates = new ArrayList<>();

        if (args.length == 1) {
            candidates.add("item"); // Non-admin players can use /class item

            if (sender.hasPermission("annihilation.admin")) {
                // Admins can set class for other players: /class <player> <class>
                Bukkit.getOnlinePlayers().stream()
                      .map(Player::getName)
                      .forEach(candidates::add);
            }
            // Add class names for non-admin context if it was designed to allow /class <class>
            // Based on ClassCommand, non-admins only get GUI or 'item'
            // So we only add class names as a second argument for admin player class setting
            StringUtil.copyPartialMatches(args[0], candidates, completions);
        } else if (args.length == 2) {
            if (sender.hasPermission("annihilation.admin")) {
                // If the first argument is a player name (for admin command)
                if (Bukkit.getPlayer(args[0]) != null) {
                    candidates.addAll(plugin.getPlayerClassManager().getAllClassNames());
                    StringUtil.copyPartialMatches(args[1], candidates, completions);
                }
            }
        }
        Collections.sort(completions);
        return completions;
    }
}
