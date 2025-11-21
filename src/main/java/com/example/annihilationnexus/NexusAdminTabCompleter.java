package com.example.annihilationnexus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NexusAdminTabCompleter implements TabCompleter {

    private final NexusManager nexusManager;

    public NexusAdminTabCompleter(NexusManager nexusManager) {
        this.nexusManager = nexusManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> candidates = new ArrayList<>();

        if (args.length == 1) {
            candidates.addAll(Arrays.asList("create", "delete", "sethp"));
            StringUtil.copyPartialMatches(args[0], candidates, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("sethp")) {
                candidates.addAll(nexusManager.getAllNexuses().keySet());
                StringUtil.copyPartialMatches(args[1], candidates, completions);
            }
            // For 'create', no specific second argument needed, user types name
        }
        Collections.sort(completions);
        return completions;
    }
}
