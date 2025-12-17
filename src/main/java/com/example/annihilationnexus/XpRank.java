package com.example.annihilationnexus;

public enum XpRank {
    NOVICE_I("Novice-I", 25),
    NOVICE_II("Novice-II", 50),
    NOVICE_III("Novice-III", 100),
    SILVER_I("Silver-I", 200),
    SILVER_II("Silver-II", 500),
    SILVER_III("Silver-III", 1000),
    GOLD_I("Gold-I", 2000),
    GOLD_II("Gold-II", 3000),
    GOLD_III("Gold-III", 4000),
    MASTER_I("Master-I", 5000),
    MASTER_II("Master-II", 7500),
    MASTER_III("Master-III", 12500),
    GRANDMASTER_I("GrandMaster-I", 25000),
    GRANDMASTER_II("GrandMaster-II", 50000),
    GRANDMASTER_III("GrandMaster-III", 75000),
    ANNIHILATOR("Annihilator", 500000);

    private final String displayName;
    private final int requiredXp;

    XpRank(String displayName, int requiredXp) {
        this.displayName = displayName;
        this.requiredXp = requiredXp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRequiredXp() {
        return requiredXp;
    }

    public static XpRank getRankForXp(int xp) {
        XpRank highestRank = NOVICE_I; // Default? Or null if < 25? User didn't specify < 25.
        // Assuming < 25 is "Unranked" or just Novice-I as base?
        // Let's assume Novice-I is the start, but maybe you need 25 to GET it?
        // "Novice-I 25" implies you reach it at 25.
        // So below 25 is unranked.

        if (xp < NOVICE_I.requiredXp)
            return null;

        for (XpRank rank : values()) {
            if (xp >= rank.requiredXp) {
                highestRank = rank;
            }
        }
        return highestRank;
    }

    public XpRank getNextRank() {
        int ordinal = this.ordinal();
        if (ordinal + 1 < values().length) {
            return values()[ordinal + 1];
        }
        return null;
    }
}
