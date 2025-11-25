package com.example.annihilationnexus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private String deeplApiKey;
    private String azureApiKey;
    private String azureRegion;
    private String defaultLanguage;
    private String translatorMode;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.deeplApiKey = config.getString("deepl-api-key", "");
        this.azureApiKey = config.getString("azure-api-key", "");
        this.azureRegion = config.getString("azure-region", "global");
        this.defaultLanguage = config.getString("default-language", "EN");
        this.translatorMode = config.getString("translator-mode", "HYBRID");
    }

    public String getDeepLApiKey() {
        return deeplApiKey;
    }

    public String getAzureApiKey() {
        return azureApiKey;
    }

    public String getAzureRegion() {
        return azureRegion;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public String getTranslatorMode() {
        return translatorMode;
    }
}
