package com.contractboard.objectives;

import java.util.List;

public record KillMobsData(List<String> types, int amount) implements ObjectiveData {
    @Override
    public int target() {
        return amount;
    }
}
