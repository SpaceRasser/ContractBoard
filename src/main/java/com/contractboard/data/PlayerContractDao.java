package com.contractboard.data;

import com.contractboard.contracts.ContractInstance;
import com.contractboard.contracts.ContractStatus;
import com.contractboard.contracts.Faction;
import com.contractboard.objectives.ObjectiveData;
import com.contractboard.objectives.ObjectiveDataJson;
import com.contractboard.objectives.ObjectiveType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerContractDao {
    private final Database database;

    public PlayerContractDao(Database database) {
        this.database = database;
    }

    public List<ContractInstance> findByPlayer(UUID playerUuid) throws SQLException {
        List<ContractInstance> results = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT * FROM player_contracts WHERE player_uuid = ?")) {
            statement.setString(1, playerUuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ObjectiveType type = ObjectiveType.fromString(rs.getString("objective_type"));
                    if (type == null) {
                        continue;
                    }
                    ObjectiveData data = ObjectiveDataJson.fromJson(type, rs.getString("objective_data_json"));
                    results.add(new ContractInstance(
                        UUID.fromString(rs.getString("contract_id")),
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("template_id"),
                        Faction.valueOf(rs.getString("faction")),
                        type,
                        data,
                        rs.getInt("progress"),
                        rs.getInt("target"),
                        ContractStatus.valueOf(rs.getString("status")),
                        rs.getLong("created_at"),
                        rs.getLong("updated_at")
                    ));
                }
            }
        }
        return results;
    }

    public void upsert(ContractInstance contract) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 """
                 INSERT INTO player_contracts(contract_id, player_uuid, template_id, faction, objective_type, objective_data_json, progress, target, status, created_at, updated_at)
                 VALUES(?,?,?,?,?,?,?,?,?,?,?)
                 ON CONFLICT(contract_id) DO UPDATE SET
                 progress=excluded.progress,
                 status=excluded.status,
                 updated_at=excluded.updated_at
                 """)) {
            statement.setString(1, contract.getContractId().toString());
            statement.setString(2, contract.getPlayerUuid().toString());
            statement.setString(3, contract.getTemplateId());
            statement.setString(4, contract.getFaction().name());
            statement.setString(5, contract.getObjectiveType().name());
            statement.setString(6, ObjectiveDataJson.toJson(contract.getObjectiveType(), contract.getObjectiveData()));
            statement.setInt(7, contract.getProgress());
            statement.setInt(8, contract.getTarget());
            statement.setString(9, contract.getStatus().name());
            statement.setLong(10, contract.getCreatedAt());
            statement.setLong(11, contract.getUpdatedAt());
            statement.executeUpdate();
        }
    }

    public void deleteByPlayer(UUID playerUuid) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM player_contracts WHERE player_uuid = ?")) {
            statement.setString(1, playerUuid.toString());
            statement.executeUpdate();
        }
    }
}
