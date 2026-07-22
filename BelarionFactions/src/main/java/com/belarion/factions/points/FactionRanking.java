package com.belarion.factions.points;

/**
 * Une entree du classement des factions : une faction, ses points, et son rang.
 * Objet de transport simple, immuable, independant de toute source de donnees.
 */
public final class FactionRanking {

    private final String factionId;
    private final String factionTag;
    private final int points;
    private final int rank;

    public FactionRanking(String factionId, String factionTag, int points, int rank) {
        this.factionId = factionId;
        this.factionTag = factionTag;
        this.points = points;
        this.rank = rank;
    }

    public String getFactionId() {
        return factionId;
    }

    public String getFactionTag() {
        return factionTag;
    }

    public int getPoints() {
        return points;
    }

    /**
     * Rang 1 = premiere place, 2 = deuxieme, etc.
     */
    public int getRank() {
        return rank;
    }
}

