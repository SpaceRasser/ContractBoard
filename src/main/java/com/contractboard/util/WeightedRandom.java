package com.contractboard.util;

import java.util.List;
import java.util.Random;

public class WeightedRandom {
    private WeightedRandom() {
    }

    public static <T extends Weighted> T pick(List<T> items, Random random) {
        int totalWeight = items.stream().mapToInt(Weighted::weight).sum();
        if (totalWeight <= 0) {
            return items.get(random.nextInt(items.size()));
        }
        int roll = random.nextInt(totalWeight) + 1;
        int cumulative = 0;
        for (T item : items) {
            cumulative += item.weight();
            if (roll <= cumulative) {
                return item;
            }
        }
        return items.getLast();
    }

    public interface Weighted {
        int weight();
    }
}
