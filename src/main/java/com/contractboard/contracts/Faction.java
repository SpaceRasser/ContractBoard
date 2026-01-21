package com.contractboard.contracts;

public enum Faction {
    FISHERS("Fishers"),
    MINERS("Miners"),
    HUNTERS("Hunters");

    private final String displayName;

    Faction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Faction fromString(String input) {
        for (Faction faction : values()) {
            if (faction.name().equalsIgnoreCase(input)) {
                return faction;
            }
        }
        return null;
    }
}
