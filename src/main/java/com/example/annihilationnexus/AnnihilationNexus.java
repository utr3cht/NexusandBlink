package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;

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
    private double grapplePullStrength;
    private double grappleDurabilityLossChance;
    private double grappleHookSpeed;
    private double grappleUpwardBoost;
    private boolean grappleHookHasGravity;
    private double launcherPadIronPower;
    private double launcherPadDiamondPower;
    private int scorpioHookCooldown;
    private int scorpioEnemyPullFallImmunity;
    private int scorpioFriendlyPullFallImmunity;

    @Override
    public void onEnable() {
        // Load config
        updateConfig();
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        loadNexusMaterial();
        loadNexusHealth();
        loadXpMessage();
        loadNexusDestructionDelay();
        loadNexusHitDelay();
        loadGrappleSettings();
        loadLauncherPadSettings();
        loadScorpioSettings();

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
        getServer().getPluginManager().registerEvents(new GrappleListener(this), this);
        getServer().getPluginManager().registerEvents(new LauncherPadListener(this), this);
        getServer().getPluginManager().registerEvents(new LauncherPadBreakListener(), this);
        getServer().getPluginManager().registerEvents(new ScorpioListener(this, playerClassManager), this);
        getServer().getPluginManager().registerEvents(new AssassinListener(this, playerClassManager), this);
        this.getCommand("class").setExecutor(new ClassCommand(playerClassManager));
        this.getCommand("class").setTabCompleter(new ClassTabCompleter(playerClassManager));
        this.getCommand("nexus").setExecutor(new NexusAdminCommand(this, nexusManager));
        this.getCommand("nexus").setTabCompleter(new NexusAdminTabCompleter(nexusManager));
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

    private void loadGrappleSettings() {
        this.grapplePullStrength = getConfig().getDouble("grapple.pull-strength-multiplier", 0.15);
        this.grappleDurabilityLossChance = getConfig().getDouble("grapple.durability-loss-chance", 0.25);
        this.grappleHookSpeed = getConfig().getDouble("grapple.hook-speed-multiplier", 1.0);
        this.grappleUpwardBoost = getConfig().getDouble("grapple.upward-pull-boost", 0.1);
        this.grappleHookHasGravity = getConfig().getBoolean("grapple.hook-has-gravity", true);
    }

    private void loadLauncherPadSettings() {
        this.launcherPadIronPower = getConfig().getDouble("launcher-pad.iron-power", 2.0);
        this.launcherPadDiamondPower = getConfig().getDouble("launcher-pad.diamond-power", 4.0);
    }

    private void loadScorpioSettings() {
        this.scorpioHookCooldown = getConfig().getInt("scorpio.hook-cooldown", 3);
        this.scorpioEnemyPullFallImmunity = getConfig().getInt("scorpio.enemy-pull-fall-immunity", 10);
        this.scorpioFriendlyPullFallImmunity = getConfig().getInt("scorpio.friendly-pull-fall-immunity", 5);
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

    public double getGrapplePullStrength() {
        return grapplePullStrength;
    }

    public double getGrappleDurabilityLossChance() {
        return grappleDurabilityLossChance;
    }

    public double getGrappleHookSpeed() {
        return grappleHookSpeed;
    }

    public double getGrappleUpwardBoost() {
        return grappleUpwardBoost;
    }

    public boolean grappleHookHasGravity() {
        return grappleHookHasGravity;
    }

    public double getLauncherPadIronPower() {
        return launcherPadIronPower;
    }

    public double getLauncherPadDiamondPower() {
        return launcherPadDiamondPower;
    }

    public int getScorpioHookCooldown() {
        return scorpioHookCooldown;
    }

    public int getScorpioEnemyPullFallImmunity() {
        return scorpioEnemyPullFallImmunity;
    }

    public int getScorpioFriendlyPullFallImmunity() {
        return scorpioFriendlyPullFallImmunity;
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

    private void updateConfig() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        // Get the default config from the JAR
        InputStream defaultConfigStream = getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            // Add any missing default values to the user's config
            config.addDefaults(defaultConfig);
            config.options().copyDefaults(true);
            saveConfig();
        }
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

    public org.bukkit.inventory.ItemStack getGrappleItem() {
        ItemStack grapple = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = grapple.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aGrapple");
            grapple.setItemMeta(meta);
        }
        return grapple;
    }

    public org.bukkit.inventory.ItemStack getScorpioItem() {
        ItemStack scorpioItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = scorpioItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Scorpio Hook");
            scorpioItem.setItemMeta(meta);
        }
        return scorpioItem;
    }

    public org.bukkit.inventory.ItemStack getAssassinItem() {
        ItemStack assassinItem = new ItemStack(Material.FEATHER);
        ItemMeta meta = assassinItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "Leap");
            assassinItem.setItemMeta(meta);
        }
        return assassinItem;
    }

    public boolean isGrappleItem(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("§aGrapple");
    }

    public boolean isBlinkItem(ItemStack item) {
        if (item == null || item.getType() != Material.PURPLE_DYE) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.LIGHT_PURPLE + "Blink");
    }

    public boolean isScorpioItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.GOLD + "Scorpio Hook");
    }

    public boolean isAssassinItem(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.GRAY + "Leap");
    }
}
