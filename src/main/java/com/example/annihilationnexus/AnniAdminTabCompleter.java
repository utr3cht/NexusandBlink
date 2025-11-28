package com.example.annihilationnexus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnniAdminTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "friendlyfire", "rank");
    private static final List<String> FRIENDLYFIRE_ARGS = Arrays.asList("on", "off", "true", "false");
    private static final List<String> RANK_ACTIONS = Arrays.asList("set", "give", "remove");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("friendlyfire")) {
                StringUtil.copyPartialMatches(args[1], FRIENDLYFIRE_ARGS, completions);
            } else if (args[0].equalsIgnoreCase("rank")) {
                StringUtil.copyPartialMatches(args[1], RANK_ACTIONS, completions);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("rank")) {
            return null; // Return null to suggest player names
        } else if (args.length == 4 && args[0].equalsIgnoreCase("rank")
                && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("give"))) {
            List<String> ranks = new ArrayList<>();
            for (Rank rank : Rank.values()) {
                ranks.add(rank.name());
            }
            StringUtil.copyPartialMatches(args[3].toUpperCase(), ranks, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}
