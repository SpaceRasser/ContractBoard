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
                 "SELECT uuid, rep_fishers, rep_miners, rep_hunters, last_rotation_epoch_day, last_reward_day, rewards_claimed_today FROM player_profiles WHERE uuid = ?")) {
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
                    rs.getLong("last_rotation_epoch_day"),
                    rs.getLong("last_reward_day"),
                    rs.getInt("rewards_claimed_today")
                ));
            }
        }
    }

    public void upsert(PlayerProfile profile) throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 """
                 INSERT INTO player_profiles(uuid, rep_fishers, rep_miners, rep_hunters, last_rotation_epoch_day, last_reward_day, rewards_claimed_today)
                 VALUES(?,?,?,?,?,?,?)
                 ON CONFLICT(uuid) DO UPDATE SET
                 rep_fishers=excluded.rep_fishers,
                 rep_miners=excluded.rep_miners,
                 rep_hunters=excluded.rep_hunters,
                 last_rotation_epoch_day=excluded.last_rotation_epoch_day,
                 last_reward_day=excluded.last_reward_day,
                 rewards_claimed_today=excluded.rewards_claimed_today
                 """)) {
            statement.setString(1, profile.getUuid().toString());
            statement.setInt(2, profile.getRepFishers());
            statement.setInt(3, profile.getRepMiners());
            statement.setInt(4, profile.getRepHunters());
            statement.setLong(5, profile.getLastRotationEpochDay());
            statement.setLong(6, profile.getLastRewardDay());
            statement.setInt(7, profile.getRewardsClaimedToday());
            statement.executeUpdate();
        }
    }
}
