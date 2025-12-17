package com.example.annihilationnexus;

import org.bukkit.ChatColor;

public enum Rank {
    SILVER("Silver", ChatColor.GRAY, 2.0),
    GOLD("Gold", ChatColor.GOLD, 2.5),
    PLATINUM("Platinum", ChatColor.AQUA, 3.0),
    EMERALD("Emerald", ChatColor.GREEN, 3.0),
    OBSIDIAN("Obsidian", ChatColor.DARK_PURPLE, 3.0),
    RUBY("Ruby", ChatColor.DARK_RED, 3.0),

    // Staff Ranks
    MINI_ADMIN("Mini Admin", ChatColor.RED, 3.0),
    ADMIN("Admin", ChatColor.LIGHT_PURPLE, 3.0),
    OWNER("Owner", ChatColor.DARK_RED, 3.0),
    DEV("Developer", ChatColor.DARK_RED, 3.0);

    private final String displayName;
    private final ChatColor color;
    private final double xpMultiplier;

    Rank(String displayName, ChatColor color, double xpMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.xpMultiplier = xpMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public double getXpMultiplier() {
        return xpMultiplier;
    }

    public String getPrefix() {
        if (this == OWNER) {
            return color + "[Owner] " + ChatColor.RESET;
        }
        if (this == ADMIN) {
            return color + "[Admin] " + ChatColor.RESET;
        }
        if (this == MINI_ADMIN) {
            return color + "[Mini Admin] " + ChatColor.RESET;
        }
        if (this == DEV) {
            return color + "[Dev] " + ChatColor.RESET;
        }

        String shortName = displayName.substring(0, 1);
        if (this == RUBY) {
            return color + "" + ChatColor.BOLD + "[" + shortName + "] " + ChatColor.RESET;
        }
        return color + "[" + shortName + "] " + ChatColor.RESET;
    }

}