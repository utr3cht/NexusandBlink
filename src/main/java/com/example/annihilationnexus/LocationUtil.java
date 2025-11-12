package com.example.annihilationnexus;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtil {

    /**
     * Serializes a Location into a string format "world,x,y,z".
     * @param loc The Location to serialize.
     * @return A string representation of the location.
     */
    public static String locationToString(Location loc) {
        if (loc == null) {
            return "";
        }
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    /**
     * Deserializes a string into a Location.
     * @param str The string to deserialize, format "world,x,y,z".
     * @return A Location object, or null if the string is invalid.
     */
    public static Location stringToLocation(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        String[] parts = str.split(",");
        if (parts.length == 4) {
            try {
                String worldName = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                double z = Double.parseDouble(parts[3]);
                return new Location(Bukkit.getWorld(worldName), x, y, z);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
