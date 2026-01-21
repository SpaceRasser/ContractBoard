package com.contractboard.contracts;

import java.util.List;

public class ItemReward {
    private final String material;
    private final int amount;
    private final String name;
    private final List<String> lore;

    public ItemReward(String material, int amount, String name, List<String> lore) {
        this.material = material;
        this.amount = amount;
        this.name = name;
        this.lore = lore;
    }

    public String getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }
}
