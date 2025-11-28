package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.example.annihilationnexus.translation.TranslationService;

public class PlayerChatListener implements Listener {

    private final PlayerTeamManager playerTeamManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerDataManager playerDataManager;
    private final TranslationService translationService;
    private final RankManager rankManager;
    private final JavaPlugin plugin;
    private final PlayerClassManager playerClassManager;

    public PlayerChatListener(JavaPlugin plugin, PlayerTeamManager playerTeamManager,
            ScoreboardManager scoreboardManager,
            PlayerDataManager playerDataManager, TranslationService translationService, RankManager rankManager,
            PlayerClassManager playerClassManager) {
        this.plugin = plugin;
        this.playerTeamManager = playerTeamManager;
        this.scoreboardManager = scoreboardManager;
        this.playerDataManager = playerDataManager;
        this.translationService = translationService;
        this.rankManager = rankManager;
        this.playerClassManager = playerClassManager;
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
            String format = ChatColor.RESET + "[" + teamColor + "Team" + ChatColor.RESET + "] "
                    + player.getDisplayName() + ": " + ChatColor.WHITE + teamMessage;

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
        event.setCancelled(true); // Always cancel to handle formatting and translation

        ChatColor teamColor = ChatColor.WHITE;
        String teamPrefix = "";

        if (teamName != null && !teamName.isEmpty()) {
            teamColor = scoreboardManager.getTeamColor(teamName);
            String teamInitial = teamName.substring(0, 1).toUpperCase();
            teamPrefix = ChatColor.RESET + "[" + teamColor + teamInitial + ChatColor.RESET + "] ";
        }

        // Rank Prefix
        Rank rank = rankManager.getDisplayRank(player);
        String rankPrefix = (rank != null) ? rank.getPrefix() : "";

        // Manually construct the player name part with team color
        String coloredPlayerName = teamColor + player.getName() + ChatColor.RESET;

        // Construct the final message format (without message content yet)
        // Format: [Rank] [Team] Name : Message
        String prefix = rankPrefix + teamPrefix + coloredPlayerName + ChatColor.RESET + " : " + ChatColor.WHITE;

        // Send to all players with translation if needed
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            String recipientLang = playerDataManager.getPlayerLanguage(recipient);
            boolean translationEnabled = playerDataManager.isTranslationEnabled(recipient);
            String senderLang = playerDataManager.getPlayerLanguage(player);

            if (!translationEnabled || senderLang.equals(recipientLang)) {
                // No translation
                recipient.sendMessage(prefix + message);
            } else {
                // Translate
                translationService.translate(message, recipientLang)
                        .thenAccept(translatedText -> {
                            recipient.sendMessage(prefix + message + ChatColor.GOLD + " (" + translatedText + ")");
                        })
                        .exceptionally(ex -> {
                            plugin.getLogger().warning("Translation failed: " + ex.getMessage());
                            recipient.sendMessage(prefix + message);
                            return null;
                        });
            }
        }

        // Log to console
        Bukkit.getConsoleSender().sendMessage(prefix + message);
    }

    private String getClassAbbreviation(String className) {
        if (className == null || className.isEmpty()) {
            return "CIV";
        }
        if (className.equalsIgnoreCase("Assassin")) {
            return "ASN";
        }
        if (className.length() <= 3) {
            return className.toUpperCase();
        }
        return className.substring(0, 3).toUpperCase();
    }
}
