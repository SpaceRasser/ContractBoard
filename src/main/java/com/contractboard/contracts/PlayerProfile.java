package com.contractboard.contracts;

import java.util.UUID;

public class PlayerProfile {
    private final UUID uuid;
    private int repFishers;
    private int repMiners;
    private int repHunters;
    private long lastRotationEpochDay;
    private long lastRewardDay;
    private int rewardsClaimedToday;

    public PlayerProfile(UUID uuid, int repFishers, int repMiners, int repHunters, long lastRotationEpochDay, long lastRewardDay, int rewardsClaimedToday) {
        this.uuid = uuid;
        this.repFishers = repFishers;
        this.repMiners = repMiners;
        this.repHunters = repHunters;
        this.lastRotationEpochDay = lastRotationEpochDay;
        this.lastRewardDay = lastRewardDay;
        this.rewardsClaimedToday = rewardsClaimedToday;
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

    public long getLastRewardDay() {
        return lastRewardDay;
    }

    public void setLastRewardDay(long lastRewardDay) {
        this.lastRewardDay = lastRewardDay;
    }

    public int getRewardsClaimedToday() {
        return rewardsClaimedToday;
    }

    public void setRewardsClaimedToday(int rewardsClaimedToday) {
        this.rewardsClaimedToday = rewardsClaimedToday;
    }

    public void incrementRewardsClaimedToday() {
        rewardsClaimedToday += 1;
    }
}
