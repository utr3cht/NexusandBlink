package com.example.annihilationnexus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;

public class Clan {
    private final String name;
    private final UUID owner;
    private final Set<UUID> members;
    private final Set<UUID> invites;
    private String color;

    public Clan(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
        this.invites = new HashSet<>();
        this.members.add(owner);
        this.color = ChatColor.GOLD.toString(); // Default color
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getInvites() {
        return invites;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void addMember(UUID player) {
        members.add(player);
        invites.remove(player);
    }

    public void removeMember(UUID player) {
        members.remove(player);
    }

    public void addInvite(UUID player) {
        invites.add(player);
    }

    public void removeInvite(UUID player) {
        invites.remove(player);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isInvited(UUID player) {
        return invites.contains(player);
    }
}
