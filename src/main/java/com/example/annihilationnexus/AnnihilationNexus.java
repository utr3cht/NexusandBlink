package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnnihilationNexus extends JavaPlugin {

    private NexusManager nexusManager;
    private ScoreboardManager scoreboardManager;
    private PlayerClassManager playerClassManager;
    private Material nexusMaterial;
    private int nexusHealth;
    private String xpMessage;
    private int nexusDestructionDelay;
    private int nexusHitDelay;
    private boolean showHealthOnHit = true; // Default to true

    @Override
    public void onEnable() {
        // Load config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadNexusMaterial();
        loadNexusHealth();
        loadXpMessage();
        loadNexusDestructionDelay();
        loadNexusHitDelay();

        // Plugin startup logic
        this.nexusManager = new NexusManager(this);
        this.scoreboardManager = new ScoreboardManager(this, nexusManager);
        this.playerClassManager = new PlayerClassManager(this);

        // Load data
        this.nexusManager.loadNexuses();
        this.playerClassManager.loadClasses();

        getServer().getPluginManager().registerEvents(new NexusListener(this, nexusManager), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlinkListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerToggleSneakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        this.getCommand("anni").setExecutor(new NexusCommand(this, nexusManager));
        this.getCommand("anni").setTabCompleter(new NexusTabCompleter(nexusManager));
        getLogger().info("AnnihilationNexus plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.nexusManager.saveNexuses();
        this.playerClassManager.saveClasses();
        getLogger().info("AnnihilationNexus plugin has been disabled!");
    }

    private void loadNexusMaterial() {
        String materialName = getConfig().getString("nexus-material", "END_STONE");
        try {
            this.nexusMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid nexus-material '" + materialName + "' in config.yml. Defaulting to END_STONE.");
            this.nexusMaterial = Material.END_STONE;
        }
    }

    private void loadNexusHealth() {
        this.nexusHealth = getConfig().getInt("nexus-health", 75);
    }

    private void loadXpMessage() {
        this.xpMessage = getConfig().getString("xp-message", "&a+12 Shotbow XP");
    }

    private void loadNexusDestructionDelay() {
        this.nexusDestructionDelay = getConfig().getInt("nexus-destruction-delay", 1);
    }

    private void loadNexusHitDelay() {
        this.nexusHitDelay = getConfig().getInt("nexus-hit-delay", 20);
    }

    public Material getNexusMaterial() {
        return nexusMaterial;
    }

    public int getNexusHealth() {
        return nexusHealth;
    }

    public String getXpMessage() {
        return ChatColor.translateAlternateColorCodes('&', xpMessage);
    }

    public int getNexusDestructionDelay() {
        return nexusDestructionDelay;
    }

    public int getNexusHitDelay() {
        return nexusHitDelay;
    }

    public NexusManager getNexusManager() {
        return nexusManager;
    }

    public boolean isShowHealthOnHit() {
        return showHealthOnHit;
    }

    public void setShowHealthOnHit(boolean showHealthOnHit) {
        this.showHealthOnHit = showHealthOnHit;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public PlayerClassManager getPlayerClassManager() {
        return playerClassManager;
    }

    public void reload() {
        reloadConfig();
        loadNexusMaterial();
        loadNexusHealth();
        loadXpMessage();
        loadNexusDestructionDelay();
        loadNexusHitDelay();
        // We should also update the scoreboard for all players after a reload
        scoreboardManager.updateForAllPlayers();
    }

    public org.bukkit.inventory.ItemStack getBlinkItem() {
        org.bukkit.inventory.ItemStack blinkItem = new org.bukkit.inventory.ItemStack(Material.PURPLE_DYE);
        org.bukkit.inventory.meta.ItemMeta meta = blinkItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Blink");
            blinkItem.setItemMeta(meta);
        }
        return blinkItem;
    }
}
