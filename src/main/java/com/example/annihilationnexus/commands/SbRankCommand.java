package com.example.annihilationnexus.commands;

import com.example.annihilationnexus.AnnihilationNexus;
import com.example.annihilationnexus.gui.SbRankGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SbRankCommand implements CommandExecutor {

    private final AnnihilationNexus plugin;

    public SbRankCommand(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        new SbRankGUI(plugin).open(player);
        return true;
    }
}
