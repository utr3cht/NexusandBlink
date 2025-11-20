package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final PlayerTeamManager playerTeamManager;
    private final ScoreboardManager scoreboardManager;

    public PlayerChatListener(PlayerTeamManager playerTeamManager, ScoreboardManager scoreboardManager) {
        this.playerTeamManager = playerTeamManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String teamName = playerTeamManager.getPlayerTeam(player.getUniqueId());
        String message = event.getMessage();

        // Team Chat feature
        if (message.startsWith("!")) {
            event.setCancelled(true); // Cancel the public message

            if (teamName == null || teamName.isEmpty()) {
                player.sendMessage(ChatColor.RED + "You are not on a team, so you cannot use team chat.");
                return;
            }

            if (message.length() == 1) {
                player.sendMessage(ChatColor.RED + "Please type a message after the '!'.");
                return;
            }

            String teamMessage = message.substring(1).trim();
            ChatColor teamColor = scoreboardManager.getTeamColor(teamName);
            String format = ChatColor.RESET + "[" + teamColor + "Team" + ChatColor.RESET + "] " + player.getDisplayName() + ": " + ChatColor.WHITE + teamMessage;

            // Send message to teammates
            for (Player recipient : Bukkit.getOnlinePlayers()) {
                String recipientTeam = playerTeamManager.getPlayerTeam(recipient.getUniqueId());
                if (teamName.equals(recipientTeam)) {
                    recipient.sendMessage(format);
                }
            }
            // Also send to console for logging
            Bukkit.getConsoleSender().sendMessage(format);

            return; // Stop further processing
        }

        // Global Chat Prefix
        if (teamName != null && !teamName.isEmpty()) {
            event.setCancelled(true); // Cancel the default chat event

            ChatColor teamColor = scoreboardManager.getTeamColor(teamName);
            String teamInitial = teamName.substring(0, 1).toUpperCase();
            String teamPrefix = ChatColor.RESET + "[" + teamColor + teamInitial + ChatColor.RESET + "] ";
            
            // Manually construct the player name part with team color
            String coloredPlayerName = teamColor + player.getName() + ChatColor.RESET;

            // Construct the final message and broadcast it
            String finalMessage = teamPrefix + coloredPlayerName + " : " + ChatColor.WHITE + message;
            Bukkit.broadcastMessage(finalMessage);
        }
    }
}
