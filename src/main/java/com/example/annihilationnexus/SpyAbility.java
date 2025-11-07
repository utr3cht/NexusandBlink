package com.example.annihilationnexus;

import org.bukkit.ChatColor;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class SpyAbility {

    private final Player player;
    private final AnnihilationNexus plugin;

    // Vanish
    private boolean isVanished = false;
    private BukkitTask vanishTask;
    private Location vanishLocation;

    // Flee
    private boolean isFleeing = false;
    private long lastFleeTime = -30000;
    private final long fleeCooldown = 30000; // 30 seconds
    private static final HashMap<UUID, ItemStack[]> fleeArmor = new HashMap<>();

    public SpyAbility(Player player, AnnihilationNexus plugin) {
        this.player = player;
        this.plugin = plugin;
    }

    public void startVanishTimer() {
        cancelVanishTimer(); // Cancel any existing timer
        vanishTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isSneaking()) {
                    vanish();
                }
            }
        }.runTaskLater(plugin, 40L); // 2 seconds
    }

    public void cancelVanishTimer() {
        if (vanishTask != null) {
            vanishTask.cancel();
            vanishTask = null;
        }
    }

    public void vanish() {
        if (isVanished()) return;
        this.isVanished = true;
        this.vanishLocation = player.getLocation();
        player.sendMessage(ChatColor.GRAY + "You have vanished.");

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.hidePlayer(plugin, player);
        }
    }

    public void unVanish() {
        if (!isVanished()) return;
        this.isVanished = false;
        this.vanishLocation = null;
        cancelVanishTimer();
        player.sendMessage(ChatColor.GRAY + "You are now visible.");

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
    }

    public boolean isVanished() {
        return isVanished;
    }

    public boolean isFleeing() {
        return isFleeing;
    }

    public Location getVanishLocation() {
        return vanishLocation;
    }

    public void flee() {
        if (System.currentTimeMillis() - lastFleeTime < fleeCooldown) {
            player.sendMessage("Flee is on cooldown for " + (fleeCooldown - (System.currentTimeMillis() - lastFleeTime)) / 1000 + " seconds.");
            return;
        }

        lastFleeTime = System.currentTimeMillis();
        unVanish();
        this.isFleeing = true;

        // Store and remove armor
        fleeArmor.put(player.getUniqueId(), player.getInventory().getArmorContents());
        player.getInventory().setArmorContents(null);

        spawnDecoy();

        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 0)); // 6 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1)); // 6 seconds
        updateItemLore(); // Update item lore after using ability
    }

    private void spawnDecoy() {
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, player.getName());

        npc.getOrAddTrait(SkinTrait.class).setSkinName(player.getName());

        LookClose lookClose = npc.getOrAddTrait(LookClose.class);
        lookClose.lookClose(true);

        npc.spawn(player.getLocation());

        npc.getNavigator().setTarget(player.getLocation().add(player.getLocation().getDirection().multiply(10)));

        new BukkitRunnable() {
            @Override
            public void run() {
                isFleeing = false; // Set fleeing to false when effect ends
                if (npc != null) {
                    npc.destroy();
                }
                // Restore armor
                if (fleeArmor.containsKey(player.getUniqueId())) {
                    player.getInventory().setArmorContents(fleeArmor.get(player.getUniqueId()));
                    fleeArmor.remove(player.getUniqueId());
                }
                updateItemLore(); // Update item lore after effect ends
            }
        }.runTaskLater(plugin, 120L); // Remove after 6 seconds
    }

    public long getRemainingCooldown() {
        long timePassed = System.currentTimeMillis() - lastFleeTime;
        if (timePassed >= fleeCooldown) {
            return 0;
        }
        return (fleeCooldown - timePassed) / 1000;
    }

    public void updateItemLore() {
        for (ItemStack item : player.getInventory().getContents()) {
            if (plugin.isSpyItem(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String baseName = ChatColor.AQUA + "Flee";
                    long remainingCooldown = getRemainingCooldown();
                    if (remainingCooldown > 0) {
                        meta.setDisplayName(baseName + ChatColor.RED + " " + remainingCooldown + "s");
                    } else {
                        meta.setDisplayName(baseName + ChatColor.GREEN + " READY");
                    }
                    meta.setLore(null); // Clear lore
                    item.setItemMeta(meta);
                }
            }
        }
    }
}