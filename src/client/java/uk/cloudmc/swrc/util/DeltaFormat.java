package uk.cloudmc.swrc.util;

public class DeltaFormat {
    public static String formatMillis(long millis) {
        long absMillis = Math.abs(millis);
        long minutes = absMillis / 60000;
        long seconds = (absMillis % 60000) / 1000;
        long milliseconds = absMillis % 1000;

        if (minutes > 0) {
            return String.format("%02d:%02d.%03d", minutes, seconds, milliseconds);
        } else {
            return String.format("%02d.%03d", seconds, milliseconds);
        }
    }

    public static String formatDelta(long millis) {
        String prefix = "";

        if (millis > 0) {
            prefix = "+";
        } else if (millis < 0) {
            prefix = "-";
        }

        return prefix + formatMillis(millis);
    }
}
