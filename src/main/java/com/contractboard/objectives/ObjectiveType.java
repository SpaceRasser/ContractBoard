package com.contractboard.objectives;

public enum ObjectiveType {
    DELIVER_ITEMS,
    KILL_MOBS,
    MINE_BLOCKS,
    CATCH_FISH;

    public static ObjectiveType fromString(String input) {
        for (ObjectiveType type : values()) {
            if (type.name().equalsIgnoreCase(input)) {
                return type;
            }
        }
        return null;
    }
}
