package com.contractboard.contracts;

import java.util.List;

public class TemplateRequirements {
    private final int minRep;
    private final List<String> worldWhitelist;
    private final List<String> biomeWhitelist;

    public TemplateRequirements(int minRep, List<String> worldWhitelist, List<String> biomeWhitelist) {
        this.minRep = minRep;
        this.worldWhitelist = worldWhitelist;
        this.biomeWhitelist = biomeWhitelist;
    }

    public int getMinRep() {
        return minRep;
    }

    public List<String> getWorldWhitelist() {
        return worldWhitelist;
    }

    public List<String> getBiomeWhitelist() {
        return biomeWhitelist;
    }
}
