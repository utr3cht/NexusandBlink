package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class NexusManager {

    private final AnnihilationNexus plugin;
    private final Map<String, Nexus> nexusMap = new HashMap<>();
    private final File nexusFile;

    public NexusManager(AnnihilationNexus plugin) {
        this.plugin = plugin;
        this.nexusFile = new File(plugin.getDataFolder(), "nexuses.dat");
    }

    public void createNexus(String teamName, Location location) {
        Nexus existingNexus = nexusMap.get(teamName);
        if (existingNexus != null) {
            // If nexus exists, remove the old block
            Location oldLocation = existingNexus.getLocation();
            if (oldLocation != null) {
                oldLocation.getBlock().setType(Material.AIR);
            }
            // Update its location and reset its health
            existingNexus.setLocation(location);
            existingNexus.setHealth(plugin.getNexusHealth());
        } else {
            // If nexus doesn't exist, create a new one
            Nexus newNexus = new Nexus(teamName, location, plugin.getNexusHealth());
            nexusMap.put(teamName, newNexus);
        }
    }

    public void removeNexus(String teamName) {
        nexusMap.remove(teamName);
    }

    public Nexus getNexus(String teamName) {
        return nexusMap.get(teamName);
    }

    public Nexus getNexusAt(Location location) {
        for (Nexus nexus : nexusMap.values()) {
            if (nexus.getLocation() != null && nexus.getLocation().equals(location)) {
                return nexus;
            }
        }
        return null;
    }

    public Map<String, Nexus> getAllNexuses() {
        return nexusMap;
    }

    public void saveNexuses() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nexusFile))) {
            for (Nexus nexus : nexusMap.values()) {
                Location loc = nexus.getLocation();
                if (loc != null && loc.getWorld() != null) {
                    String line = String.join(",",
                            nexus.getTeamName(),
                            loc.getWorld().getName(),
                            String.valueOf(loc.getX()),
                            String.valueOf(loc.getY()),
                            String.valueOf(loc.getZ()),
                            String.valueOf(nexus.getHealth())
                    );
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save nexuses to file: " + e.getMessage());
        }
    }

    public void loadNexuses() {
        if (!nexusFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(nexusFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String teamName = parts[0];
                    World world = Bukkit.getWorld(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    double z = Double.parseDouble(parts[4]);
                    int health = Integer.parseInt(parts[5]);

                    if (world != null) {
                        Location loc = new Location(world, x, y, z);
                        Nexus nexus = new Nexus(teamName, loc, health);
                        nexusMap.put(teamName, nexus);
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load nexuses from file: " + e.getMessage());
        }
    }
}