package com.example.annihilationnexus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NexusAdminTabCompleter implements TabCompleter {

    private final NexusManager nexusManager;

    public NexusAdminTabCompleter(NexusManager nexusManager) {
        this.nexusManager = nexusManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "setnexushp");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("setnexushp"))) {
            return new ArrayList<>(nexusManager.getAllNexuses().keySet());
        }
        return null;
    }
}
