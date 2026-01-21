package com.contractboard.contracts;

import com.contractboard.objectives.ObjectiveData;
import com.contractboard.objectives.ObjectiveType;

import java.util.UUID;

public class ContractInstance {
    private final UUID contractId;
    private final UUID playerUuid;
    private final String templateId;
    private final Faction faction;
    private final ObjectiveType objectiveType;
    private final ObjectiveData objectiveData;
    private int progress;
    private final int target;
    private ContractStatus status;
    private long createdAt;
    private long updatedAt;

    public ContractInstance(UUID contractId,
                            UUID playerUuid,
                            String templateId,
                            Faction faction,
                            ObjectiveType objectiveType,
                            ObjectiveData objectiveData,
                            int progress,
                            int target,
                            ContractStatus status,
                            long createdAt,
                            long updatedAt) {
        this.contractId = contractId;
        this.playerUuid = playerUuid;
        this.templateId = templateId;
        this.faction = faction;
        this.objectiveType = objectiveType;
        this.objectiveData = objectiveData;
        this.progress = progress;
        this.target = target;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getContractId() {
        return contractId;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Faction getFaction() {
        return faction;
    }

    public ObjectiveType getObjectiveType() {
        return objectiveType;
    }

    public ObjectiveData getObjectiveData() {
        return objectiveData;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTarget() {
        return target;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
