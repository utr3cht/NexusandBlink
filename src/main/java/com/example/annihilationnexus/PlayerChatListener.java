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
import com.example.annihilationnexus.Clan;

public class PlayerChatListener implements Listener {

    private final PlayerTeamManager playerTeamManager;
    private final ScoreboardManager scoreboardManager;
    private final PlayerDataManager playerDataManager;
    private final TranslationService translationService;
    private final RankManager rankManager;
    private final JavaPlugin plugin;
    private final PlayerClassManager playerClassManager;
    private final com.example.annihilationnexus.translation.TranslationCache translationCache;
    private final ClanManager clanManager;

    public PlayerChatListener(JavaPlugin plugin, PlayerTeamManager playerTeamManager,
            ScoreboardManager scoreboardManager,
            PlayerDataManager playerDataManager, TranslationService translationService, RankManager rankManager,
            PlayerClassManager playerClassManager, ClanManager clanManager) {
        this.plugin = plugin;
        this.playerTeamManager = playerTeamManager;
        this.scoreboardManager = scoreboardManager;
        this.playerDataManager = playerDataManager;
        this.translationService = translationService;
        this.rankManager = rankManager;
        this.playerClassManager = playerClassManager;
        this.clanManager = clanManager;
        this.translationCache = new com.example.annihilationnexus.translation.TranslationCache();
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

        // Clan Tag
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        String clanTag = "";
        if (clan != null) {
            clanTag = ChatColor.GOLD + " [" + clan.getColor() + clan.getName() + ChatColor.GOLD + "]";
        }

        // Rank Prefix
        Rank rank = rankManager.getDisplayRank(player);
        String rankPrefix = (rank != null) ? rank.getPrefix() : "";

        // Manually construct the player name part with team color
        String coloredPlayerName = teamColor + player.getName() + ChatColor.RESET;

        // Construct the final message format (without message content yet)
        // Format: [Rank][Team]Name[Clan] : Message
        String prefix = rankPrefix + teamPrefix + coloredPlayerName + clanTag + ChatColor.RESET + " : "
                + ChatColor.WHITE;

        // Send to all players with translation if needed
        java.util.Map<String, java.util.List<Player>> recipientsByLang = new java.util.HashMap<>();
        String senderLang = playerDataManager.getPlayerLanguage(player);

        // Group recipients by language
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            String recipientLang = playerDataManager.getPlayerLanguage(recipient);
            boolean translationEnabled = playerDataManager.isTranslationEnabled(recipient);

            if (!translationEnabled || senderLang.equals(recipientLang)) {
                // No translation needed
                recipient.sendMessage(prefix + message);
            } else {
                recipientsByLang.computeIfAbsent(recipientLang, k -> new java.util.ArrayList<>()).add(recipient);
            }
        }

        // Process translations
        for (java.util.Map.Entry<String, java.util.List<Player>> entry : recipientsByLang.entrySet()) {
            String targetLang = entry.getKey();
            java.util.List<Player> recipients = entry.getValue();

            // Filtering: Skip translation for short messages, URLs, or symbols
            if (shouldSkipTranslation(message)) {
                for (Player p : recipients) {
                    p.sendMessage(prefix + message);
                }
                continue;
            }

            // Check Cache
            String cachedTranslation = translationCache.get(message, targetLang);
            if (cachedTranslation != null) {
                for (Player p : recipients) {
                    p.sendMessage(prefix + message + ChatColor.GOLD + " (" + cachedTranslation + ")");
                }
                continue;
            }

            // Translate
            translationService.translate(message, targetLang)
                    .thenAccept(translatedText -> {
                        // Cache the result
                        translationCache.put(message, targetLang, translatedText);
                        // Send to all recipients in this group
                        for (Player p : recipients) {
                            p.sendMessage(prefix + message + ChatColor.GOLD + " (" + translatedText + ")");
                        }
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().warning("Translation failed: " + ex.getMessage());
                        for (Player p : recipients) {
                            p.sendMessage(prefix + message);
                        }
                        return null;
                    });
        }

        // Log to console
        Bukkit.getConsoleSender().sendMessage(prefix + message);
    }

    private boolean shouldSkipTranslation(String message) {
        if (message.length() <= 1)
            return true; // Too short
        if (message.matches("^[\\p{Punct}\\d\\s]+$"))
            return true; // Only symbols/numbers
        if (message.toLowerCase().startsWith("http"))
            return true; // URL
        return false;
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
