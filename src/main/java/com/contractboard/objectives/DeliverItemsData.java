package com.contractboard.objectives;

public record DeliverItemsData(String material, int amount) implements ObjectiveData {
    @Override
    public int target() {
        return amount;
    }
}
