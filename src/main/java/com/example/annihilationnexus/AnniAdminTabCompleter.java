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

    private static final List<String> SUBCOMMANDS = Arrays.asList("start", "stop", "reload", "friendlyfire");
    private static final List<String> FRIENDLYFIRE_ARGS = Arrays.asList("on", "off", "true", "false");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completions);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("friendlyfire")) {
            StringUtil.copyPartialMatches(args[1], FRIENDLYFIRE_ARGS, completions);
        }
        
        Collections.sort(completions);
        return completions;
    }
}
