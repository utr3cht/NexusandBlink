package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.NamespacedKey;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

public final class AnnihilationNexus extends JavaPlugin implements Listener {

    // Simple record to hold material and its drop chance
    public record DropInfo(Material material, double chance) {
    }

    private NexusManager nexusManager;
    private ScoreboardManager scoreboardManager;
    private PlayerClassManager playerClassManager;
    private ClassRegionManager classRegionManager;
    private ProtectedCropManager protectedCropManager;
    private PlayerTeamManager playerTeamManager;
    private TeamColorManager teamColorManager; // Add field
    private RankManager rankManager; // Add RankManager field
    private XpManager xpManager; // Add XpManager field
    private ClanManager clanManager; // Add ClanManager field
    private NamespacedKey classKey;
    private final Set<UUID> noFall = ConcurrentHashMap.newKeySet();
    private final Map<UUID, BukkitTask> noFallTasks = new ConcurrentHashMap<>();
    private final Set<UUID> achievedNetheriteHoeBreak = ConcurrentHashMap.newKeySet();
    private boolean friendlyFireEnabled;

    @Override
    public void onEnable() {
        // Serialization
        ConfigurationSerialization.registerClass(ProtectedCropInfo.class);

        // Register this class as a listener for EntityDamageEvent
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new NetheriteHoeAchievementListener(this), this); // Register new
                                                                                                        // listener

        // Config handling
        saveDefaultConfig(); // Ensure config.yml exists with defaults
        this.reload(); // Load the config from disk and merge defaults
        this.friendlyFireEnabled = getConfig().getBoolean("gameplay.friendly-fire", false);
        loadAchievedNetheriteHoeBreak(); // Load achievement data

        this.classKey = new NamespacedKey(this, "class_name");
        this.classRegionManager = new ClassRegionManager(this);
        this.protectedCropManager = new ProtectedCropManager(this);

        // Plugin startup logic
        this.nexusManager = new NexusManager(this);
        this.playerClassManager = new PlayerClassManager(this);
        this.playerTeamManager = new PlayerTeamManager(this);
        this.teamColorManager = new TeamColorManager(this); // Instantiate
        this.rankManager = new RankManager(this); // Instantiate RankManager
        this.xpManager = new XpManager(this, rankManager); // Instantiate XpManager
        this.scoreboardManager = new ScoreboardManager(this, nexusManager, teamColorManager, playerTeamManager,
                rankManager);

        // Translation Components
        ConfigManager configManager = new ConfigManager(this);
        PlayerDataManager playerDataManager = new PlayerDataManager(this, configManager.getDefaultLanguage());

        String mode = configManager.getTranslatorMode().toUpperCase();
        com.example.annihilationnexus.translation.DeepLTranslator deepLTranslator = new com.example.annihilationnexus.translation.DeepLTranslator(
                configManager.getDeepLApiKey());
        com.example.annihilationnexus.translation.AzureTranslator azureTranslator = new com.example.annihilationnexus.translation.AzureTranslator(
                configManager.getAzureApiKey(), configManager.getAzureRegion());
        com.example.annihilationnexus.translation.TranslationService translationService;

        switch (mode) {
            case "DEEPL":
                translationService = deepLTranslator;
                getLogger().info("Using DeepL translator mode.");
                break;
            case "AZURE":
                translationService = azureTranslator;
                getLogger().info("Using Azure translator mode.");
                break;
            case "HYBRID":
            default:
                translationService = new com.example.annihilationnexus.translation.HybridTranslator(deepLTranslator,
                        azureTranslator, getLogger());
                getLogger().info("Using Hybrid translator mode.");
                break;
        }

        // Load data
        this.nexusManager.loadNexuses();
        this.teamColorManager.loadColors(); // Load colors
        this.playerClassManager.loadClasses();
        this.classRegionManager.loadRegions();
        this.scoreboardManager.loadScoreboardVisibility();
        this.playerTeamManager.loadTeams();
        this.scoreboardManager.initializeTeams(); // Initialize teams on scoreboard

        // Delay crop loading by 20 ticks (1 second) to ensure worlds are loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                protectedCropManager.loadCropsAndStartGrowth();
            }
        }.runTaskLater(this, 20L);

        // Registering events and commands
        // Register Events
        getServer().getPluginManager().registerEvents(new NexusListener(this, nexusManager, xpManager), this);
        getServer().getPluginManager().registerEvents(
                new PlayerLifecycleListener(this, scoreboardManager, playerClassManager, playerTeamManager), this);
        getServer().getPluginManager().registerEvents(new BlinkListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerToggleSneakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new GrappleListener(this), this);
        getServer().getPluginManager().registerEvents(new LauncherPadListener(this), this);
        getServer().getPluginManager().registerEvents(new LauncherPadBreakListener(), this);
        getServer().getPluginManager().registerEvents(new ScorpioListener(this, playerClassManager), this);
        getServer().getPluginManager().registerEvents(new AssassinListener(this, playerClassManager), this);
        getServer().getPluginManager().registerEvents(new SpyListener(this, playerClassManager), this);
        getServer().getPluginManager().registerEvents(new TransporterListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ClassSelectionListener(this, playerClassManager, xpManager),
                this);
        getServer().getPluginManager().registerEvents(new com.example.annihilationnexus.gui.SbRankGUI(this), this);
        getServer().getPluginManager().registerEvents(new ClassItemRestrictionListener(this), this);
        getServer().getPluginManager()
                .registerEvents(new FarmerListener(this, playerClassManager, protectedCropManager), this);
        getServer().getPluginManager().registerEvents(
                new DeathMessageListener(playerClassManager, playerTeamManager, scoreboardManager, xpManager,
                        nexusManager),
                this);
        getServer().getPluginManager().registerEvents(new MobDeathListener(this, xpManager), this);
        getServer().getPluginManager().registerEvents(new RiftWalkerListener(this, playerClassManager), this);
        getServer().getPluginManager().registerEvents(new ConsumableListener(this), this);
        getServer().getPluginManager().registerEvents(new PistonListener(this), this);

        this.clanManager = new ClanManager(this);
        com.example.annihilationnexus.commands.ClanCommand clanCommand = new com.example.annihilationnexus.commands.ClanCommand(
                clanManager, xpManager);
        this.getCommand("clan").setExecutor(clanCommand);
        this.getCommand("clan").setTabCompleter(clanCommand);

        getServer().getPluginManager().registerEvents(
                new PlayerChatListener(this, playerTeamManager, scoreboardManager, playerDataManager,
                        translationService, rankManager, playerClassManager, clanManager),
                this);

        this.getCommand("chat").setExecutor(new com.example.annihilationnexus.commands.ChatCommand(playerDataManager));
        this.getCommand("class").setExecutor(new ClassCommand(this, playerClassManager));
        getCommand("ranking").setExecutor(new com.example.annihilationnexus.commands.RankingCommand(this));
        getCommand("team").setExecutor(
                new TeamCommand(this, nexusManager, scoreboardManager, playerTeamManager, teamColorManager));
        this.getCommand("class").setTabCompleter(new ClassTabCompleter(this));
        this.getCommand("nexus").setExecutor(new NexusAdminCommand(this, nexusManager));
        this.getCommand("nexus").setTabCompleter(new NexusAdminTabCompleter(nexusManager));
        this.getCommand("anni").setExecutor(new AnniAdminCommand(this));
        this.getCommand("anni").setTabCompleter(new AnniAdminTabCompleter());
        this.getCommand("myxp").setExecutor(new MyXpCommand(xpManager));
        this.getCommand("sbrank").setExecutor(new com.example.annihilationnexus.commands.SbRankCommand(this));
        this.getCommand("togglescoreboard").setExecutor(new ToggleScoreboardCommand(scoreboardManager));
        this.getCommand("classregion").setExecutor(new ClassRegionCommand(this, classRegionManager));
        this.getCommand("classregion").setTabCompleter(new ClassRegionCommand(this, classRegionManager));
        TeamCommand teamCommand = new TeamCommand(this, nexusManager, scoreboardManager, playerTeamManager,
                teamColorManager);
        this.getCommand("team").setExecutor(teamCommand);
        this.getCommand("team").setTabCompleter(teamCommand);
        this.getCommand("rank")
                .setExecutor(new com.example.annihilationnexus.commands.RankCommand(this, rankManager, xpManager));

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

        // Farmer Feast cooldown display task
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    String playerClass = playerClassManager.getPlayerClass(player.getUniqueId());
                    if (playerClass != null && playerClass.equalsIgnoreCase("farmer")) {
                        FarmerAbility ability = playerClassManager.getFarmerAbility(player.getUniqueId());
                        if (ability != null) {
                            ability.updateItemLore(player);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        // Register Recipe
        NamespacedKey key = new NamespacedKey(this, "enchanted_golden_apple");
        org.bukkit.inventory.ShapedRecipe recipe = new org.bukkit.inventory.ShapedRecipe(key,
                new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        recipe.shape("GGG", "GAG", "GGG");
        recipe.setIngredient('G', Material.GOLD_BLOCK);
        recipe.setIngredient('A', Material.APPLE);
        getServer().addRecipe(recipe);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.nexusManager.saveNexuses();
        this.playerClassManager.saveClasses();
        this.classRegionManager.saveRegions();
        this.scoreboardManager.saveScoreboardVisibility();
        this.protectedCropManager.saveCrops();
        this.playerTeamManager.saveTeams();
        this.teamColorManager.saveColors(); // Save colors
        if (clanManager != null) {
            clanManager.saveClans();
        }
        saveAchievedNetheriteHoeBreak(); // Save achievement data
    }

    public boolean isRemoveTransporterPortalOnLogout() {
        return getConfig().getBoolean("transporter.remove-on-logout", false);
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        // Add custom defaults for configurable crop drops if they don't exist
        FileConfiguration config = getConfig();
        if (!config.contains("transporter.remove-on-logout")) {
            config.set("transporter.remove-on-logout", false);
            saveConfig();
        }
        if (!config.contains("farmer.extra-drops.crops.custom-drops")) {
            List<Map<String, Object>> defaultDrops = new ArrayList<>();
            Map<String, Object> appleDrop = new java.util.HashMap<>();
            appleDrop.put("material", "APPLE");
            appleDrop.put("chance", 0.0025);
            defaultDrops.add(appleDrop);

            Map<String, Object> ghastTearDrop = new java.util.HashMap<>();
            ghastTearDrop.put("material", "GHAST_TEAR");
            ghastTearDrop.put("chance", 0.01);
            defaultDrops.add(ghastTearDrop);

            Map<String, Object> netherWartDrop = new java.util.HashMap<>();
            netherWartDrop.put("material", "NETHER_WART");
            netherWartDrop.put("chance", 0.01);
            defaultDrops.add(netherWartDrop);

            Map<String, Object> ironOreDrop = new java.util.HashMap<>();
            ironOreDrop.put("material", "IRON_ORE");
            ironOreDrop.put("chance", 0.01);
            defaultDrops.add(ironOreDrop);

            Map<String, Object> bookDrop = new java.util.HashMap<>();
            bookDrop.put("material", "BOOK");
            bookDrop.put("chance", 0.01);
            defaultDrops.add(bookDrop);

            Map<String, Object> soulSandDrop = new java.util.HashMap<>();
            soulSandDrop.put("material", "SOUL_SAND");
            soulSandDrop.put("chance", 0.01);
            defaultDrops.add(soulSandDrop);

            config.set("farmer.extra-drops.crops.custom-drops", defaultDrops);
            saveConfig();
        }

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
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString("xp-message", "&a+12 Shrektbow XP"));
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

    public double getLauncherPadEmeraldPower() {
        return getConfig().getDouble("launcher-pad.emerald-power", 1.5);
    }

    public double getLauncherPadGoldPower() {
        return getConfig().getDouble("launcher-pad.gold-power", 2.0);
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

    public double getScorpioStuckDuration() {
        return getConfig().getDouble("scorpio.stuck-duration-seconds", 1.5); // Default to 1.5 seconds
    }

    public double getScorpioDistancePullMultiplier() {
        return getConfig().getDouble("scorpio.distance-pull-multiplier", 0.1);
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

    public ClassRegionManager getClassRegionManager() {
        return classRegionManager;
    }

    public ProtectedCropManager getProtectedCropManager() {
        return protectedCropManager;
    }

    public PlayerTeamManager getPlayerTeamManager() {
        return playerTeamManager;
    }

    public TeamColorManager getTeamColorManager() {
        return teamColorManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public XpManager getXpManager() {
        return xpManager;
    }

    public void reload() {
        reloadConfig(); // Reload the config from disk
        // Ensure any new defaults are copied to the in-memory config
        FileConfiguration config = getConfig();
        InputStream defaultConfigStream = getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(defaultConfigStream));
            config.addDefaults(defaultConfig);
            config.options().copyDefaults(true);
        }
        saveConfig(); // Save any new defaults to disk
        getLogger().info("Configuration reloaded from disk.");

        // We should also update the scoreboard for all players after a reload
        if (scoreboardManager != null) {
            scoreboardManager.updateForAllPlayers();
        }
    }

    public void grantNoFall(org.bukkit.entity.Player p) { // Removed 'seconds' parameter
        UUID id = p.getUniqueId();
        noFall.add(id);
        p.setFallDistance(0f); // Reset current fall distance
        // No scheduled task here, will be handled by PlayerMoveEvent
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof org.bukkit.entity.Player p))
            return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && noFall.contains(p.getUniqueId())) {
            e.setCancelled(true);
            p.setFallDistance(0f); // Ensure fall distance is reset
        }
    }

    @EventHandler // Add this new event handler
    public void onPlayerMove(PlayerMoveEvent event) {
        org.bukkit.entity.Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // If player is in noFall set and just landed
        if (noFall.contains(playerId) && player.isOnGround()
                && !event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            // Cancel any existing task for this player
            if (noFallTasks.containsKey(playerId)) {
                noFallTasks.get(playerId).cancel();
            }

            // Schedule removal from noFall after 1 second
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {
                    noFall.remove(playerId);
                    noFallTasks.remove(playerId);
                }
            }.runTaskLater(this, 20L); // 1 second (20 ticks)

            noFallTasks.put(playerId, task);
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
        ItemStack spyItem = new ItemStack(Material.SUGAR); // Changed to SUGAR
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
        return meta != null && meta.hasDisplayName()
                && meta.getDisplayName().startsWith(ChatColor.LIGHT_PURPLE + "Blink");
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

    public org.bukkit.inventory.ItemStack getTransporterItem() {
        ItemStack transporterItem = new ItemStack(Material.QUARTZ);
        ItemMeta meta = transporterItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Transporter");
            transporterItem.setItemMeta(meta);
        }
        return transporterItem;
    }

    public boolean isTransporterItem(ItemStack item) {
        if (item == null || item.getType() != Material.QUARTZ) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName()
                && meta.getDisplayName().startsWith(ChatColor.GREEN + "Transporter");
    }

    public boolean isSpyItem(ItemStack item) {
        if (item == null || item.getType() != Material.SUGAR) { // Changed to SUGAR
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.AQUA + "Flee");
    }

    public ItemStack getFeastItem() {
        ItemStack item = new ItemStack(Material.GOLDEN_CARROT);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Feast");
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isFeastItem(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_CARROT) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.GOLD + "Feast");
    }

    public ItemStack getFamineItem() {
        ItemStack item = new ItemStack(Material.DEAD_BUSH);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_GRAY + "Famine");
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isFamineItem(ItemStack item) {
        if (item == null || item.getType() != Material.DEAD_BUSH) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName()
                && meta.getDisplayName().startsWith(ChatColor.DARK_GRAY + "Famine");
    }

    public NamespacedKey getClassKey() {
        return classKey;
    }

    public Set<UUID> getNoFall() {
        return noFall;
    }

    private NamespacedKey riftWalkerKey;

    public ItemStack getRiftWalkerItem() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Rift Walker");
            // Set persistent data tag
            if (riftWalkerKey == null) {
                riftWalkerKey = new NamespacedKey(this, "riftwalker_item");
            }
            meta.getPersistentDataContainer().set(riftWalkerKey, org.bukkit.persistence.PersistentDataType.BYTE,
                    (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isRiftWalkerItem(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;

        if (riftWalkerKey == null) {
            riftWalkerKey = new NamespacedKey(this, "riftwalker_item");
        }

        // Check for the tag
        if (meta.getPersistentDataContainer().has(riftWalkerKey, org.bukkit.persistence.PersistentDataType.BYTE)) {
            return true;
        }

        // Fallback for legacy items (or if tag is missing for some reason)
        // This ensures items created before the tag update are still recognized and
        // removed on death
        return meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.LIGHT_PURPLE + "Rift Walker");
    }

    // Helper method to check if an item is any class item
    public boolean isClassItem(ItemStack item) {
        return isBlinkItem(item) || isGrappleItem(item) || isScorpioItem(item) || isAssassinItem(item)
                || isSpyItem(item) || isTransporterItem(item) || isFeastItem(item) || isFamineItem(item)
                || isRiftWalkerItem(item);
    }

    public double getTpDistanceLimit() {
        return getConfig().getDouble("transporter.tp-distance-limit", 20.0); // Default to 20 blocks
    }

    public int getDasherMaxBlinkDistance() {
        return getConfig().getInt("dasher.max-blink-distance", 15); // Default to 15 blocks
    }

    public int getDasherMinBlinkDistance() {
        return getConfig().getInt("dasher.min-blink-distance", 3); // Default to 3 blocks
    }

    // Netherite Hoe Achievement methods
    public boolean hasAchievedNetheriteHoeBreak(UUID playerUuid) {
        return achievedNetheriteHoeBreak.contains(playerUuid);
    }

    public void addAchievedNetheriteHoeBreak(UUID playerUuid) {
        achievedNetheriteHoeBreak.add(playerUuid);
        saveAchievedNetheriteHoeBreak();
    }

    private void saveAchievedNetheriteHoeBreak() {
        getConfig().set("achievements.netherite-hoe-break", achievedNetheriteHoeBreak.stream().map(UUID::toString)
                .collect(java.util.ArrayList::new, java.util.ArrayList::add, java.util.ArrayList::addAll));
        saveConfig();
    }

    private void loadAchievedNetheriteHoeBreak() {
        java.util.List<String> uuids = getConfig().getStringList("achievements.netherite-hoe-break");
        achievedNetheriteHoeBreak.clear();
        for (String uuidString : uuids) {
            try {
                achievedNetheriteHoeBreak.add(UUID.fromString(uuidString));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid UUID found in config for netherite hoe achievement: " + uuidString);
            }
        }
    }

    public String getNetheriteHoeBreakAchievementMessageEn() {
        return getConfig().getString("achievements.netherite-hoe-break-message.en",
                "&aAchievement Unlocked: &fNetherite Hoe Breaker!");
    }

    public String getNetheriteHoeBreakAchievementMessageJa() {
        return getConfig().getString("achievements.netherite-hoe-break-message.ja", "&a実績解除: &fネザライトのクワ破壊者！");
    }

    // Farmer Config Getters
    public int getFeastCooldown() {
        return getConfig().getInt("farmer.feast.cooldown", 30);
    }

    public double getFeastRadius() {
        return getConfig().getDouble("farmer.feast.radius", 13.0);
    }

    public float getFeastSaturation() {
        return (float) getConfig().getDouble("farmer.feast.saturation", 4.0);
    }

    public int getFamineCooldown() {
        return getConfig().getInt("farmer.famine.cooldown", 90);
    }

    public double getFamineRadius() {
        return getConfig().getDouble("farmer.famine.radius", 13.0);
    }

    public int getFamineHungerLevel() {
        return getConfig().getInt("farmer.famine.hunger-level", 20);
    }

    public int getFamineHungerDuration() {
        return getConfig().getInt("farmer.famine.hunger-duration", 30);
    }

    public int getFamineFoodLevel() {
        return getConfig().getInt("farmer.famine.food-level", 3);
    }

    public int getAutoReplantDelay() {
        return getConfig().getInt("farmer.auto-replant-delay", 5);
    }

    public double getGrassSeedChance() {
        return getConfig().getDouble("farmer.extra-drops.grass.seed-chance", 0.1);
    }

    public double getGrassPotatoChance() {
        return getConfig().getDouble("farmer.extra-drops.grass.potato-chance", 0.05);
    }

    public double getGrassCarrotChance() {
        return getConfig().getDouble("farmer.extra-drops.grass.carrot-chance", 0.05);
    }

    public double getEatingBonusChance() {
        return getConfig().getDouble("farmer.eating-bonus.chance", 0.3);
    }

    public double getEatingBonusExtraHealth() {
        return getConfig().getDouble("farmer.eating-bonus.extra-health", 2.0);
    }

    public int getEatingBonusExtraFood() {
        return getConfig().getInt("farmer.eating-bonus.extra-food", 2);
    }

    public int getCropProtectionDuration() {
        return getConfig().getInt("farmer.crop-protection-duration", 80);
    }

    public boolean getPreventFarmlandTrample() {
        return getConfig().getBoolean("farmer.prevent-farmland-trample", true);
    }

    public List<DropInfo> getCustomCropDrops() {
        List<DropInfo> drops = new ArrayList<>();
        List<?> rawList = getConfig().getList("farmer.extra-drops.crops.custom-drops");

        if (rawList == null) {
            // This is normal if not configured, so no warning needed.
            return drops;
        }

        for (Object o : rawList) {
            if (!(o instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) o;

            // Ensure both material and chance exist and are of the correct type
            if (!(map.get("material") instanceof String) || !(map.get("chance") instanceof Number)) {
                // Silently ignore invalid or incomplete entries
                continue;
            }

            String materialName = (String) map.get("material");
            double chance = ((Number) map.get("chance")).doubleValue();

            if (materialName.isEmpty()) {
                continue; // Ignore entries with empty material names
            }

            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                drops.add(new DropInfo(material, chance));
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid material name '" + materialName + "' in custom crop drops config.");
            }
        }
        return drops;
    }

    public void setFriendlyFire(boolean enabled) {
        this.friendlyFireEnabled = enabled;
        getConfig().set("gameplay.friendly-fire", enabled);
        saveConfig();

        // Apply to all existing teams on the main scoreboard
        for (org.bukkit.scoreboard.Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            team.setAllowFriendlyFire(enabled);
        }

        // Also apply to all teams on individual player scoreboards
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            org.bukkit.scoreboard.Scoreboard playerSB = player.getScoreboard();
            if (playerSB != null && playerSB != Bukkit.getScoreboardManager().getMainScoreboard()) {
                for (org.bukkit.scoreboard.Team team : playerSB.getTeams()) {
                    team.setAllowFriendlyFire(enabled);
                }
            }
        }
    }

    public boolean isFriendlyFireEnabled() {
        return this.friendlyFireEnabled;
    }

    public String getNexusDestroyedMessage() {
        return ChatColor.translateAlternateColorCodes('&',
                getConfig().getString("nexus-destroyed-message", "&5The %team% nexus has been destroyed!"));
    }

    public int getNexusDamageXp() {
        return getConfig().getInt("xp-rewards.nexus-damage", 1);
    }

    public int getPlayerKillXp() {
        return getConfig().getInt("xp-rewards.player-kill", 3);
    }

    public int getDefenseBonusXp() {
        return getConfig().getInt("xp-rewards.defense-bonus", 6);
    }

    public int getOffenseBonusXp() {
        return getConfig().getInt("xp-rewards.offense-bonus", 6);
    }

    public int getAvengerBonusXp() {
        return getConfig().getInt("xp-rewards.avenger-bonus", 6);
    }

    public int getMobXp(org.bukkit.entity.EntityType type) {
        return getConfig().getInt("xp-rewards.mobs." + type.name(), 0);
    }

    public int getBossXp(org.bukkit.entity.EntityType type) {
        return getConfig().getInt("xp-rewards.bosses." + type.name(), 0);
    }
}