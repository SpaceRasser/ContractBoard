package com.contractboard.config;

import com.contractboard.util.TimeUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalTime;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ConfigService {
    private final JavaPlugin plugin;

    private LocalTime dailyResetTime;
    private int contractsPerDay;
    private boolean generateOnJoin;
    private int maxActiveContracts;

    private long placedBlockTtlMinutes;
    private int cleanupIntervalSeconds;

    private String titleMain;
    private String titleFaction;

    private NavigableMap<Integer, String> rankThresholds;

    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        String resetRaw = plugin.getConfig().getString("dailyResetTime", "05:00");
        dailyResetTime = TimeUtil.parseTime(resetRaw, plugin.getLogger());
        contractsPerDay = plugin.getConfig().getInt("contractsPerDay", 3);
        generateOnJoin = plugin.getConfig().getBoolean("generateOnJoin", true);
        maxActiveContracts = plugin.getConfig().getInt("maxActiveContracts", contractsPerDay);

        placedBlockTtlMinutes = plugin.getConfig().getLong("antiAbuse.placedBlockTtlMinutes", 360L);
        cleanupIntervalSeconds = plugin.getConfig().getInt("antiAbuse.cleanupIntervalSeconds", 120);

        titleMain = plugin.getConfig().getString("gui.titleMain", "<gold>Contracts</gold>");
        titleFaction = plugin.getConfig().getString("gui.titleFaction", "<gold>Contracts - %faction%</gold>");

        rankThresholds = new TreeMap<>();
        ConfigurationSection rankSection = plugin.getConfig().getConfigurationSection("ranks");
        if (rankSection != null) {
            for (String key : rankSection.getKeys(false)) {
                try {
                    int value = Integer.parseInt(key);
                    rankThresholds.put(value, rankSection.getString(key, "Unknown"));
                } catch (NumberFormatException ex) {
                    plugin.getLogger().warning("Invalid rank threshold: " + key);
                }
            }
        }
        if (rankThresholds.isEmpty()) {
            rankThresholds.put(0, "Novice");
        }
    }

    public LocalTime getDailyResetTime() {
        return dailyResetTime;
    }

    public int getContractsPerDay() {
        return contractsPerDay;
    }

    public boolean isGenerateOnJoin() {
        return generateOnJoin;
    }

    public int getMaxActiveContracts() {
        return maxActiveContracts;
    }

    public long getPlacedBlockTtlMinutes() {
        return placedBlockTtlMinutes;
    }

    public int getCleanupIntervalSeconds() {
        return cleanupIntervalSeconds;
    }

    public String getTitleMain() {
        return titleMain;
    }

    public String getTitleFaction() {
        return titleFaction;
    }

    public NavigableMap<Integer, String> getRankThresholds() {
        return rankThresholds;
    }
}
