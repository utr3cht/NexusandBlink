package com.example.annihilationnexus;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataManager {

    private final NamespacedKey targetLangKey;
    private final NamespacedKey translationEnabledKey;
    private final String defaultLanguage;

    public PlayerDataManager(JavaPlugin plugin, String defaultLanguage) {
        this.targetLangKey = new NamespacedKey(plugin, "target_language");
        this.translationEnabledKey = new NamespacedKey(plugin, "translation_enabled");
        this.defaultLanguage = defaultLanguage;
    }

    public String getPlayerLanguage(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        return container.getOrDefault(targetLangKey, PersistentDataType.STRING, defaultLanguage);
    }

    public void setPlayerLanguage(Player player, String language) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(targetLangKey, PersistentDataType.STRING, language);
    }

    public boolean isTranslationEnabled(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        return container.getOrDefault(translationEnabledKey, PersistentDataType.BYTE, (byte) 0) == 1;
    }

    public void setTranslationEnabled(Player player, boolean enabled) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(translationEnabledKey, PersistentDataType.BYTE, enabled ? (byte) 1 : (byte) 0);
    }
}
