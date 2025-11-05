package com.example.annihilationnexus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NexusTabCompleter implements TabCompleter {

    private final NexusManager nexusManager;
    private static final List<String> COMMANDS = Arrays.asList("create", "delete", "togglehealth", "sethealth", "reload", "class", "getblinkitem", "getgrappleitem");
    private static final List<String> CLASS_NAMES = Arrays.asList("dasher", "scout");

    public NexusTabCompleter(NexusManager nexusManager) {
        this.nexusManager = nexusManager;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("sethealth")) {
                StringUtil.copyPartialMatches(args[1], nexusManager.getAllNexuses().keySet(), completions);
            } else if (args[0].equalsIgnoreCase("class")) {
                List<String> playerNames = sender.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
                StringUtil.copyPartialMatches(args[1], playerNames, completions);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("class")) {
            StringUtil.copyPartialMatches(args[2], CLASS_NAMES, completions);
        }

        return completions;
    }
}