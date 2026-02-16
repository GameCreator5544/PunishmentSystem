package net.hyperlowmc.plugins.punish.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static long parseDuration(String input) {
        if (input.equalsIgnoreCase("perm") || input.equalsIgnoreCase("permanent")) {
            return -1;
        }

        char unit = input.charAt(input.length() - 1);
        int amount = Integer.parseInt(input.substring(0, input.length() - 1));

        switch (unit) {
            case 's':
                return TimeUnit.SECONDS.toMillis(amount);
            case 'm':
                return TimeUnit.MINUTES.toMillis(amount);
            case 'h':
                return TimeUnit.HOURS.toMillis(amount);
            case 'd':
                return TimeUnit.DAYS.toMillis(amount);
            default:
                return -1;
        }
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) return "Expired";
        if (millis == -1) return "Permanent";

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");

        return sb.toString().trim();
    }
}
