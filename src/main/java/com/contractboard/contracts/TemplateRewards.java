package com.contractboard.contracts;

import java.util.List;

public class TemplateRewards {
    private final int rep;
    private final Double money;
    private final List<ItemReward> items;

    public TemplateRewards(int rep, Double money, List<ItemReward> items) {
        this.rep = rep;
        this.money = money;
        this.items = items;
    }

    public int getRep() {
        return rep;
    }

    public Double getMoney() {
        return money;
    }

    public List<ItemReward> getItems() {
        return items;
    }
}
