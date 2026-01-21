package com.contractboard.contracts;

import com.contractboard.objectives.ObjectiveType;

import java.util.List;

public class ContractTemplate {
    private final String id;
    private final Faction faction;
    private final int weight;
    private final String displayName;
    private final List<String> description;
    private final TemplateRequirements requirements;
    private final ObjectiveType objectiveType;
    private final TemplateObjective objective;
    private final TemplateRewards rewards;

    public ContractTemplate(String id,
                            Faction faction,
                            int weight,
                            String displayName,
                            List<String> description,
                            TemplateRequirements requirements,
                            ObjectiveType objectiveType,
                            TemplateObjective objective,
                            TemplateRewards rewards) {
        this.id = id;
        this.faction = faction;
        this.weight = weight;
        this.displayName = displayName;
        this.description = description;
        this.requirements = requirements;
        this.objectiveType = objectiveType;
        this.objective = objective;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public Faction getFaction() {
        return faction;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public TemplateRequirements getRequirements() {
        return requirements;
    }

    public ObjectiveType getObjectiveType() {
        return objectiveType;
    }

    public TemplateObjective getObjective() {
        return objective;
    }

    public TemplateRewards getRewards() {
        return rewards;
    }
}
