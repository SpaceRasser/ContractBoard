package com.contractboard.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public Database(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        File dbFile = new File(plugin.getDataFolder(), "contractboard.db");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(5);
        config.setPoolName("ContractBoardPool");
        dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_profiles (
                    uuid TEXT PRIMARY KEY,
                    rep_fishers INTEGER NOT NULL,
                    rep_miners INTEGER NOT NULL,
                    rep_hunters INTEGER NOT NULL,
                    last_rotation_epoch_day INTEGER NOT NULL
                )
                """);
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_contracts (
                    contract_id TEXT PRIMARY KEY,
                    player_uuid TEXT NOT NULL,
                    template_id TEXT NOT NULL,
                    faction TEXT NOT NULL,
                    objective_type TEXT NOT NULL,
                    objective_data_json TEXT NOT NULL,
                    progress INTEGER NOT NULL,
                    target INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """);
            statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_contracts_player_uuid ON player_contracts(player_uuid)");
        } catch (SQLException ex) {
            plugin.getLogger().severe("Failed to initialize database: " + ex.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
