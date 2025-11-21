package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClassRegionCommand implements CommandExecutor, TabCompleter {

    private final AnnihilationNexus plugin;
    private final ClassRegionManager classRegionManager;
    private final Map<UUID, Location> pos1Selections = new HashMap<>();
    private final Map<UUID, Location> pos2Selections = new HashMap<>();

    public ClassRegionCommand(AnnihilationNexus plugin, ClassRegionManager classRegionManager) {
        this.plugin = plugin;
        this.classRegionManager = classRegionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("annihilation.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "pos1":
                pos1Selections.put(player.getUniqueId(), player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Position 1 set to your current location.");
                break;
            case "pos2":
                pos2Selections.put(player.getUniqueId(), player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Position 2 set to your current location.");
                break;
            case "create":
                if (args.length == 2) {
                    String regionName = args[1];
                    Location loc1 = pos1Selections.get(player.getUniqueId());
                    Location loc2 = pos2Selections.get(player.getUniqueId());

                    if (loc1 == null || loc2 == null) {
                        player.sendMessage(ChatColor.RED
                                + "You must select two positions first using /classregion pos1 and /classregion pos2.");
                        return true;
                    }

                    if (!loc1.getWorld().equals(loc2.getWorld())) {
                        player.sendMessage(ChatColor.RED + "Positions must be in the same world.");
                        return true;
                    }

                    // Ensure min and max locations are correct
                    Location min = new Location(loc1.getWorld(), Math.min(loc1.getX(), loc2.getX()),
                            Math.min(loc1.getY(), loc2.getY()), Math.min(loc1.getZ(), loc2.getZ()));
                    Location max = new Location(loc1.getWorld(), Math.max(loc1.getX(), loc2.getX()),
                            Math.max(loc1.getY(), loc2.getY()), Math.max(loc1.getZ(), loc2.getZ()));

                    ClassRegion region = new ClassRegion(regionName, min, max);
                    classRegionManager.addRegion(region);
                    player.sendMessage(ChatColor.GREEN + "Class region '" + regionName + "' created successfully.");
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /classregion create <name>");
                }
                break;
            case "delete":
                if (args.length == 2) {
                    String regionName = args[1];
                    if (classRegionManager.removeRegion(regionName)) {
                        player.sendMessage(ChatColor.GREEN + "Class region '" + regionName + "' deleted successfully.");
                    } else {
                        player.sendMessage(ChatColor.RED + "Class region '" + regionName + "' not found.");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Usage: /classregion delete <name>");
                }
                break;
            case "list":
                List<ClassRegion> regions = classRegionManager.getAllRegions();
                if (regions.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "No class regions defined.");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "--- Class Regions ---");
                    for (ClassRegion region : regions) {
                        player.sendMessage(ChatColor.AQUA + "- " + region.getName() + ": " +
                                ChatColor.GRAY + "Min(" + formatLocation(region.getMin()) + ") " +
                                "Max(" + formatLocation(region.getMax()) + ")");
                    }
                }
                break;
            default:
                sendUsage(player);
                break;
        }

        return true;
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "--- Class Region Commands ---");
        player.sendMessage(
                ChatColor.AQUA + "/classregion pos1" + ChatColor.GRAY + " - Set position 1 to your location.");
        player.sendMessage(
                ChatColor.AQUA + "/classregion pos2" + ChatColor.GRAY + " - Set position 2 to your location.");
        player.sendMessage(ChatColor.AQUA + "/classregion create <name>" + ChatColor.GRAY
                + " - Create a region with pos1 and pos2.");
        player.sendMessage(
                ChatColor.AQUA + "/classregion delete <name>" + ChatColor.GRAY + " - Delete an existing region.");
        player.sendMessage(ChatColor.AQUA + "/classregion list" + ChatColor.GRAY + " - List all defined regions.");
    }

    private String formatLocation(Location loc) {
        return String.format("%.0f,%.0f,%.0f", loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> candidates = new ArrayList<>();

        if (args.length == 1) {
            candidates.addAll(Arrays.asList("pos1", "pos2", "create", "delete", "list"));
            StringUtil.copyPartialMatches(args[0], candidates, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete")) {
                candidates.addAll(classRegionManager.getAllRegions().stream()
                        .map(ClassRegion::getName)
                        .collect(Collectors.toList()));
                StringUtil.copyPartialMatches(args[1], candidates, completions);
            }
        }

        // If completions is empty, return an empty list to prevent player name
        // completion
        // But we need to be careful. If we return an empty list, it shows nothing.
        // If we return null, it shows player names.
        // We want to show nothing if there are no matches for our commands.
        return completions;
    }
}
