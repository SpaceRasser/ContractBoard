package com.contractboard.data;

import com.contractboard.contracts.PlayerProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class PlayerProfileDao {
    private final Database database;

    public PlayerProfileDao(Database database) {
        this.database = database;
    }

    public Optional<PlayerProfile> findByUuid(UUID uuid) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT uuid, rep_fishers, rep_miners, rep_hunters, last_rotation_epoch_day FROM player_profiles WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new PlayerProfile(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getInt("rep_fishers"),
                    rs.getInt("rep_miners"),
                    rs.getInt("rep_hunters"),
                    rs.getLong("last_rotation_epoch_day")
                ));
            }
        }
    }

    public void upsert(PlayerProfile profile) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 """
                 INSERT INTO player_profiles(uuid, rep_fishers, rep_miners, rep_hunters, last_rotation_epoch_day)
                 VALUES(?,?,?,?,?)
                 ON CONFLICT(uuid) DO UPDATE SET
                 rep_fishers=excluded.rep_fishers,
                 rep_miners=excluded.rep_miners,
                 rep_hunters=excluded.rep_hunters,
                 last_rotation_epoch_day=excluded.last_rotation_epoch_day
                 """)) {
            statement.setString(1, profile.getUuid().toString());
            statement.setInt(2, profile.getRepFishers());
            statement.setInt(3, profile.getRepMiners());
            statement.setInt(4, profile.getRepHunters());
            statement.setLong(5, profile.getLastRotationEpochDay());
            statement.executeUpdate();
        }
    }
}
