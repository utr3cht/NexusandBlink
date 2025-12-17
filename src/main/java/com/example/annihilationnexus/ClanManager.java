package com.example.annihilationnexus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClanManager {

    private final JavaPlugin plugin;
    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, Clan> playerClanMap = new HashMap<>();
    private final File clansFile;
    private FileConfiguration clansConfig;

    public ClanManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.clansFile = new File(plugin.getDataFolder(), "clans.yml");
        loadClans();
    }

    public Clan createClan(String name, UUID owner) {
        if (clans.containsKey(name.toLowerCase())) {
            return null;
        }
        Clan clan = new Clan(name, owner);
        clans.put(name.toLowerCase(), clan);
        playerClanMap.put(owner, clan);
        saveClans();
        return clan;
    }

    public void deleteClan(String name) {
        Clan clan = clans.remove(name.toLowerCase());
        if (clan != null) {
            for (UUID member : clan.getMembers()) {
                playerClanMap.remove(member);
            }
            saveClans();
        }
    }

    public Clan getClan(String name) {
        return clans.get(name.toLowerCase());
    }

    public Clan getClanByPlayer(UUID player) {
        return playerClanMap.get(player);
    }

    public void addMember(Clan clan, UUID player) {
        clan.addMember(player);
        playerClanMap.put(player, clan);
        saveClans();
    }

    public void removeMember(Clan clan, UUID player) {
        clan.removeMember(player);
        playerClanMap.remove(player);
        if (clan.getMembers().isEmpty()) {
            deleteClan(clan.getName());
        } else {
            saveClans();
        }
    }

    public void loadClans() {
        if (!clansFile.exists()) {
            return;
        }
        clansConfig = YamlConfiguration.loadConfiguration(clansFile);
        clans.clear();
        playerClanMap.clear();

        for (String key : clansConfig.getKeys(false)) {
            String name = clansConfig.getString(key + ".name");
            String ownerStr = clansConfig.getString(key + ".owner");
            if (name == null || ownerStr == null)
                continue;

            UUID owner = UUID.fromString(ownerStr);
            Clan clan = new Clan(name, owner);
            if (clansConfig.contains(key + ".color")) {
                clan.setColor(clansConfig.getString(key + ".color"));
            }

            List<String> members = clansConfig.getStringList(key + ".members");
            for (String memberStr : members) {
                try {
                    UUID member = UUID.fromString(memberStr);
                    clan.addMember(member);
                    playerClanMap.put(member, clan);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid member UUID in clan " + name);
                }
            }

            // Ensure owner is in map (addMember handles it but good to be safe)
            playerClanMap.put(owner, clan);

            clans.put(name.toLowerCase(), clan);
        }
    }

    public void saveClans() {
        if (clansConfig == null) {
            clansConfig = new YamlConfiguration();
        }

        // Clear existing config to avoid stale data
        for (String key : clansConfig.getKeys(false)) {
            clansConfig.set(key, null);
        }

        for (Clan clan : clans.values()) {
            String key = clan.getName().toLowerCase();
            clansConfig.set(key + ".name", clan.getName());
            clansConfig.set(key + ".owner", clan.getOwner().toString());
            clansConfig.set(key + ".color", clan.getColor());

            List<String> memberList = new ArrayList<>();
            for (UUID member : clan.getMembers()) {
                memberList.add(member.toString());
            }
            clansConfig.set(key + ".members", memberList);
        }

        try {
            clansConfig.save(clansFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save clans.yml!");
            e.printStackTrace();
        }
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }
}
