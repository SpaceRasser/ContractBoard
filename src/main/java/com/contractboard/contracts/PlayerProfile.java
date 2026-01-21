package com.contractboard.contracts;

import java.util.UUID;

public class PlayerProfile {
    private final UUID uuid;
    private int repFishers;
    private int repMiners;
    private int repHunters;
    private long lastRotationEpochDay;

    public PlayerProfile(UUID uuid, int repFishers, int repMiners, int repHunters, long lastRotationEpochDay) {
        this.uuid = uuid;
        this.repFishers = repFishers;
        this.repMiners = repMiners;
        this.repHunters = repHunters;
        this.lastRotationEpochDay = lastRotationEpochDay;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getRep(Faction faction) {
        return switch (faction) {
            case FISHERS -> repFishers;
            case MINERS -> repMiners;
            case HUNTERS -> repHunters;
        };
    }

    public void setRep(Faction faction, int value) {
        switch (faction) {
            case FISHERS -> repFishers = value;
            case MINERS -> repMiners = value;
            case HUNTERS -> repHunters = value;
        }
    }

    public void addRep(Faction faction, int delta) {
        setRep(faction, getRep(faction) + delta);
    }

    public int getRepFishers() {
        return repFishers;
    }

    public int getRepMiners() {
        return repMiners;
    }

    public int getRepHunters() {
        return repHunters;
    }

    public long getLastRotationEpochDay() {
        return lastRotationEpochDay;
    }

    public void setLastRotationEpochDay(long lastRotationEpochDay) {
        this.lastRotationEpochDay = lastRotationEpochDay;
    }
}
