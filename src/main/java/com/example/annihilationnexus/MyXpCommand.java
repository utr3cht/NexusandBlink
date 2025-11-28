package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MyXpCommand implements CommandExecutor {

    private final XpManager xpManager;

    public MyXpCommand(XpManager xpManager) {
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        int xp = xpManager.getXp(player);

        player.sendMessage(ChatColor.GREEN + "You have " + ChatColor.GOLD + xp + ChatColor.GREEN + " Shrektbow XP.");
        return true;
    }
}
