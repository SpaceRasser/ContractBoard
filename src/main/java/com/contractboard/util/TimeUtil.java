package com.contractboard.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Logger;
import org.bukkit.World;

public class TimeUtil {
    private TimeUtil() {
    }

    public static LocalTime parseTime(String input, Logger logger) {
        try {
            return LocalTime.parse(input);
        } catch (Exception ex) {
            logger.warning("Invalid dailyResetTime: " + input + ", using 05:00");
            return LocalTime.of(5, 0);
        }
    }

    public static long currentEpochDay(LocalTime resetTime) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);
        LocalDate date = now.toLocalDate();
        LocalDateTime resetDateTime = LocalDateTime.of(date, resetTime);
        if (now.toLocalDateTime().isBefore(resetDateTime)) {
            date = date.minusDays(1);
        }
        return date.toEpochDay();
    }

    public static long currentMinecraftDay(World world) {
        long fullTime = world.getFullTime();
        return Math.floorDiv(fullTime, 24000L);
    }
}
