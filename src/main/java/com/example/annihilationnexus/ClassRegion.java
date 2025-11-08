package com.example.annihilationnexus;

import org.bukkit.Location;
import org.bukkit.World;

public class ClassRegion {
    private String name;
    private Location min;
    private Location max;

    public ClassRegion(String name, Location min, Location max) {
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public String getName() {
        return name;
    }

    public Location getMin() {
        return min;
    }

    public Location getMax() {
        return max;
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(min.getWorld())) {
            return false;
        }

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= min.getX() && x <= max.getX() &&
               y >= min.getY() && y <= max.getY() &&
               z >= min.getZ() && z <= max.getZ();
    }

    // Serialization for saving to config
    public String serialize() {
        return name + ";" +
               min.getWorld().getName() + ";" +
               min.getX() + "," + min.getY() + "," + min.getZ() + ";" +
               max.getX() + "," + max.getY() + "," + max.getZ();
    }

    // Deserialization for loading from config
    public static ClassRegion deserialize(String serialized, World world) {
        String[] parts = serialized.split(";");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid serialized ClassRegion string: " + serialized);
        }

        String name = parts[0];
        // World name is parts[1], but we pass the world directly for safety/consistency
        String[] minCoords = parts[2].split(",");
        String[] maxCoords = parts[3].split(",");

        Location min = new Location(world, Double.parseDouble(minCoords[0]), Double.parseDouble(minCoords[1]), Double.parseDouble(minCoords[2]));
        Location max = new Location(world, Double.parseDouble(maxCoords[0]), Double.parseDouble(maxCoords[1]), Double.parseDouble(maxCoords[2]));

        return new ClassRegion(name, min, max);
    }
}