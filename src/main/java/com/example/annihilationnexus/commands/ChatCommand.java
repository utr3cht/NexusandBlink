package com.example.annihilationnexus.commands;

import com.example.annihilationnexus.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;

    public ChatCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /chat <lang|on|off> [args]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "lang":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /chat lang <EN|JA>");
                    return true;
                }
                String langCode = args[1].toUpperCase();
                if (!langCode.equals("EN") && !langCode.equals("JA")) {
                    player.sendMessage(ChatColor.RED + "Only EN and JA are supported.");
                    return true;
                }
                playerDataManager.setPlayerLanguage(player, langCode);
                player.sendMessage(ChatColor.GREEN + "Target language set to: " + langCode);
                if (!playerDataManager.isTranslationEnabled(player)) {
                    player.sendMessage(ChatColor.YELLOW + "Tip: Run /chat on to enable translation!");
                }
                break;
            case "on":
                playerDataManager.setTranslationEnabled(player, true);
                player.sendMessage(ChatColor.GREEN + "Chat translation enabled.");
                break;
            case "off":
                playerDataManager.setTranslationEnabled(player, false);
                player.sendMessage(ChatColor.GREEN + "Chat translation disabled.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /chat <lang|on|off> [args]");
                break;
        }

        return true;
    }
}
