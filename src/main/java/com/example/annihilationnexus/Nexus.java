package com.example.annihilationnexus;

import org.bukkit.Location;

public class Nexus {

    private final String teamName;
    private Location location;
    private int health;

    public Nexus(String teamName, Location location, int initialHealth) {
        this.teamName = teamName;
        this.location = location;
        this.health = initialHealth;
    }

    public String getTeamName() {
        return teamName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void damage(int amount) {
        this.health -= amount;
        if (this.health < 0) {
            this.health = 0;
        }
    }

    public boolean isDestroyed() {
        return this.health <= 0;
    }
}
