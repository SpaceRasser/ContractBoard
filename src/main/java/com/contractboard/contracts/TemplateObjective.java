package com.contractboard.contracts;

import java.util.List;

public class TemplateObjective {
    private final int target;
    private final List<String> types;

    public TemplateObjective(int target, List<String> types) {
        this.target = target;
        this.types = types;
    }

    public int getTarget() {
        return target;
    }

    public List<String> getTypes() {
        return types;
    }
}
