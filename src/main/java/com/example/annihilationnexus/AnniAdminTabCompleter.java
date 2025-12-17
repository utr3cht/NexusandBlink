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
    private static final List<String> XP_ACTIONS = Arrays.asList("give", "remove", "set", "list", "multiple");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(SUBCOMMANDS);
            subs.add("xp");
            StringUtil.copyPartialMatches(args[0], subs, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("friendlyfire")) {
                StringUtil.copyPartialMatches(args[1], FRIENDLYFIRE_ARGS, completions);
            } else if (args[0].equalsIgnoreCase("rank")) {
                StringUtil.copyPartialMatches(args[1], RANK_ACTIONS, completions);
            } else if (args[0].equalsIgnoreCase("xp")) {
                StringUtil.copyPartialMatches(args[1], XP_ACTIONS, completions);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("rank")) {
                return null; // Suggest player names
            } else if (args[0].equalsIgnoreCase("xp") && !args[1].equalsIgnoreCase("list")) {
                return null; // Suggest player names for xp give/remove/set
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("rank")
                    && (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("give"))) {
                List<String> ranks = new ArrayList<>();
                for (Rank rank : Rank.values()) {
                    ranks.add(rank.name());
                }
                StringUtil.copyPartialMatches(args[3].toUpperCase(), ranks, completions);
            } else if (args[0].equalsIgnoreCase("xp") && !args[1].equalsIgnoreCase("list")) {
                // Suggest amounts? Or just let them type.
                // Maybe suggest common amounts like 100, 1000, 3000
                List<String> amounts = Arrays.asList("100", "1000", "3000", "5000");
                StringUtil.copyPartialMatches(args[3], amounts, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }
}
