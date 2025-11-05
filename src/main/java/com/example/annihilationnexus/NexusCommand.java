package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class NexusCommand implements CommandExecutor {

    private final AnnihilationNexus plugin;
    private final NexusManager nexusManager;

    public NexusCommand(AnnihilationNexus plugin, NexusManager nexusManager) {
        this.plugin = plugin;
        this.nexusManager = nexusManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /nexus <create|delete|togglehealth|sethealth|reload|class|getblinkitem>");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /nexus create <teamName>");
                return true;
            }
            Player player = (Player) sender;
            String teamName = args[1];

            // Use the location of the block the player is standing on.
            Block nexusBlock = player.getLocation().getBlock();
            Location nexusLocation = nexusBlock.getLocation();

            // Set the block to the configured material
            nexusBlock.setType(plugin.getNexusMaterial());

            nexusManager.createNexus(teamName, nexusLocation);
            plugin.getScoreboardManager().updateForAllPlayers();
            player.sendMessage("Nexus for team " + teamName + " created at your location with material " + plugin.getNexusMaterial().name());
            return true;
        }

        if (args[0].equalsIgnoreCase("togglehealth")) {
            plugin.setShowHealthOnHit(!plugin.isShowHealthOnHit());
            sender.sendMessage("Nexus health display is now " + (plugin.isShowHealthOnHit() ? "enabled" : "disabled") + ".");
            return true;
        }

        if (args[0].equalsIgnoreCase("sethealth")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /nexus sethealth <teamName> <amount>");
                return true;
            }
            String teamName = args[1];
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid health amount.");
                return true;
            }

            Nexus nexus = nexusManager.getNexus(teamName);
            if (nexus != null) {
                nexus.setHealth(amount);
                plugin.getScoreboardManager().updateForAllPlayers();
                sender.sendMessage(teamName + " nexus health set to " + amount + ".");
            } else {
                sender.sendMessage("Nexus for team " + teamName + " not found.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /nexus delete <teamName>");
                return true;
            }
            String teamName = args[1];
            Nexus nexus = nexusManager.getNexus(teamName);
            if (nexus != null) {
                // Set the nexus block back to air
                nexus.getLocation().getBlock().setType(org.bukkit.Material.AIR);
                nexusManager.removeNexus(teamName);
                plugin.getScoreboardManager().updateForAllPlayers();
                sender.sendMessage("Nexus for team " + teamName + " has been deleted.");
            } else {
                sender.sendMessage("Nexus for team " + teamName + " not found.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
            sender.sendMessage("AnnihilationNexus config reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("class")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /nexus class <player> <className>");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("Player not found.");
                return true;
            }
            String className = args[2];
            plugin.getPlayerClassManager().setPlayerClass(target, className);
            sender.sendMessage(target.getName() + "'s class set to " + className);
            return true;
        }

        if (args[0].equalsIgnoreCase("getblinkitem")) {
            Player player = (Player) sender;
            ItemStack blinkItem = new ItemStack(Material.PURPLE_DYE);
            ItemMeta meta = blinkItem.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Blink");
            blinkItem.setItemMeta(meta);
            player.getInventory().addItem(blinkItem);
            player.sendMessage("You have received the Blink item.");
            return true;
        }

        return false;
    }
}
