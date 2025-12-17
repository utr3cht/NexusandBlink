package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobDeathListener implements Listener {

    private final AnnihilationNexus plugin;
    private final XpManager xpManager;

    public MobDeathListener(AnnihilationNexus plugin, XpManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        EntityType type = entity.getType();

        // Exclude Silverfish
        if (type == EntityType.SILVERFISH) {
            return;
        }

        // Handle Boss XP (Global)
        int bossXp = plugin.getBossXp(type);
        if (bossXp > 0) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                xpManager.giveXp(p, bossXp, "Boss Defeated: " + type.name());
            }
            return;
        }

        // Handle Mob XP (Killer only)
        if (killer != null) {
            int mobXp = plugin.getMobXp(type);
            if (mobXp > 0) {
                xpManager.giveXp(killer, mobXp, "Mob Kill: " + type.name());
            }
        }
    }
}
