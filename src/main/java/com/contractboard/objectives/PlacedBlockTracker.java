package com.contractboard.objectives;

import com.contractboard.config.ConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlacedBlockTracker {
    private final JavaPlugin plugin;
    private final ConfigService configService;
    private final Map<String, Long> placedBlocks = new ConcurrentHashMap<>();
    private int taskId = -1;

    public PlacedBlockTracker(JavaPlugin plugin, ConfigService configService) {
        this.plugin = plugin;
        this.configService = configService;
    }

    public void track(Location location) {
        placedBlocks.put(key(location), System.currentTimeMillis());
    }

    public boolean wasPlacedByPlayer(Location location) {
        Long time = placedBlocks.get(key(location));
        if (time == null) {
            return false;
        }
        long ttlMillis = configService.getPlacedBlockTtlMinutes() * 60L * 1000L;
        if (System.currentTimeMillis() - time > ttlMillis) {
            placedBlocks.remove(key(location));
            return false;
        }
        return true;
    }

    public void startCleanupTask() {
        long intervalTicks = configService.getCleanupIntervalSeconds() * 20L;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::cleanup, intervalTicks, intervalTicks);
    }

    public void cleanup() {
        long ttlMillis = configService.getPlacedBlockTtlMinutes() * 60L * 1000L;
        long now = System.currentTimeMillis();
        placedBlocks.entrySet().removeIf(entry -> now - entry.getValue() > ttlMillis);
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        placedBlocks.clear();
    }

    private String key(Location location) {
        return location.getWorld().getUID() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }
}
