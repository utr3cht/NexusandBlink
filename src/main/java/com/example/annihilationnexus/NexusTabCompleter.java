package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NexusTabCompleter implements TabCompleter {

    private final NexusManager nexusManager;
    private static final List<String> SUB_COMMANDS = Arrays.asList("create", "delete", "sethealth", "togglehealth", "reload", "class", "getblinkitem");

    public NexusTabCompleter(NexusManager nexusManager) {
        this.nexusManager = nexusManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], SUB_COMMANDS, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("sethealth")) {
                StringUtil.copyPartialMatches(args[1], nexusManager.getAllNexuses().keySet(), completions);
            } else if (args[0].equalsIgnoreCase("class")) {
                List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[1], playerNames, completions);
            }
        }

        return completions;
    }
}