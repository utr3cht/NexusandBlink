package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AnnihilationNexus extends JavaPlugin implements Listener {

    private NexusManager nexusManager;
    private ScoreboardManager scoreboardManager;
    private PlayerClassManager playerClassManager;
    private final Set<UUID> noFall = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        // Register this class as a listener for EntityDamageEvent
        getServer().getPluginManager().registerEvents(this, this);

        // Config handling
        updateConfig();

        // Plugin startup logic
        this.nexusManager = new NexusManager(this);
        this.scoreboardManager = new ScoreboardManager(this, nexusManager);
        this.playerClassManager = new PlayerClassManager(this);

        // Load data
        this.nexusManager.loadNexuses();
        this.playerClassManager.loadClasses();

        // Registering events and commands
        getServer().getPluginManager().registerEvents(new NexusListener(this, nexusManager), this);
        getServer().getPluginManager().registerEvents(new PlayerLifecycleListener(this, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(new BlinkListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerToggleSneakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GrappleListener(this), this);
        getServer().getPluginManager().registerEvents(new LauncherPadListener(this), this);
        getServer().getPluginManager().registerEvents(new LauncherPadBreakListener(), this);
        getServer().getPluginManager().registerEvents(new ScorpioListener(this, playerClassManager), this);
        getServer().getPluginManager().registerEvents(new AssassinListener(this, playerClassManager), this);
        getServer().getPluginManager().registerEvents(new SpyListener(this, playerClassManager), this);
        this.getCommand("class").setExecutor(new ClassCommand(playerClassManager));
        this.getCommand("class").setTabCompleter(new ClassTabCompleter(playerClassManager));
        this.getCommand("nexus").setExecutor(new NexusAdminCommand(this, nexusManager));
        this.getCommand("nexus").setTabCompleter(new NexusAdminTabCompleter(nexusManager));
        this.getCommand("anni").setExecutor(new AnniAdminCommand(this));

        // Assassin cooldown display task
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
                    if (playerClass != null && playerClass.equalsIgnoreCase("assassin")) {
                        AssassinAbility ability = playerClassManager.getAssassinAbility(player.getUniqueId());
                        if (ability != null) {
                            ability.updateItemLore();
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        // Spy cooldown display task
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
                    if (playerClass != null && playerClass.equalsIgnoreCase("spy")) {
                        SpyAbility ability = playerClassManager.getSpyAbility(player.getUniqueId());
                        if (ability != null) {
                            ability.updateItemLore();
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        getLogger().info("AnnihilationNexus plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.nexusManager.saveNexuses();
        this.playerClassManager.saveClasses();
        getLogger().info("AnnihilationNexus plugin has been disabled!");
    }

    public Material getNexusMaterial() {
        try {
            return Material.valueOf(getConfig().getString("nexus-material", "END_STONE").toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid nexus-material in config.yml. Defaulting to END_STONE.");
            return Material.END_STONE;
        }
    }

    public int getNexusHealth() {
        return getConfig().getInt("nexus-health", 75);
    }

    public String getXpMessage() {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("xp-message", "&a+12 Shotbow XP"));
    }

    public int getNexusDestructionDelay() {
        return getConfig().getInt("nexus-destruction-delay", 1);
    }

    public int getNexusHitDelay() {
        return getConfig().getInt("nexus-hit-delay", 20);
    }

    public double getGrapplePullStrength() {
        return getConfig().getDouble("grapple.pull-strength-multiplier", 0.15);
    }

    public double getGrappleDurabilityLossChance() {
        return getConfig().getDouble("grapple.durability-loss-chance", 0.25);
    }

    public double getGrappleHookSpeed() {
        return getConfig().getDouble("grapple.hook-speed-multiplier", 1.0);
    }

    public double getGrappleUpwardBoost() {
        return getConfig().getDouble("grapple.upward-pull-boost", 0.1);
    }

    public boolean grappleHookHasGravity() {
        return getConfig().getBoolean("grapple.hook-has-gravity", true);
    }

    public double getLauncherPadIronPower() {
        return getConfig().getDouble("launcher-pad.iron-power", 2.0);
    }

    public double getLauncherPadDiamondPower() {
        return getConfig().getDouble("launcher-pad.diamond-power", 4.0);
    }

    public int getScorpioHookCooldown() {
        return getConfig().getInt("scorpio.hook-cooldown", 3);
    }

    public int getScorpioEnemyPullFallImmunity() {
        return getConfig().getInt("scorpio.enemy-pull-fall-immunity", 10);
    }

    public int getScorpioFriendlyPullFallImmunity() {
        return getConfig().getInt("scorpio.friendly-pull-fall-immunity", 5);
    }

    public NexusManager getNexusManager() {
        return nexusManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public PlayerClassManager getPlayerClassManager() {
        return playerClassManager;
    }

    public void reload() {
        updateConfig();
        reloadConfig();
        // We should also update the scoreboard for all players after a reload
        scoreboardManager.updateForAllPlayers();
    }

    public void grantNoFall(org.bukkit.entity.Player p, int seconds) {
        UUID id = p.getUniqueId();
        noFall.add(id);
        p.setFallDistance(0f); // Reset current fall distance
        long ticks = seconds * 20L;
        Bukkit.getScheduler().runTaskLater(this, () -> noFall.remove(id), ticks);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof org.bukkit.entity.Player p)) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && noFall.contains(p.getUniqueId())) {
            e.setCancelled(true);
            p.setFallDistance(0f); // Ensure fall distance is reset
        }
    }

    private void updateConfig() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        // Get the default config from the JAR
        InputStream defaultConfigStream = getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
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

    public ItemStack getSpyItem() {
        ItemStack spyItem = new ItemStack(Material.SUGAR);
        ItemMeta meta = spyItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Flee");
            spyItem.setItemMeta(meta);
        }
        return spyItem;
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
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.GRAY + "Leap");
    }

    public boolean isSpyItem(ItemStack item) {
        if (item == null || item.getType() != Material.SUGAR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.AQUA + "Flee");
    }
}
