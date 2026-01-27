package mc.core.utilites.math;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 22.01.2026
 */

public class MathUtil {
    public static boolean chance(double percent) {
        return Math.random() * 100 >= percent;
    }

    public static String formatTime(double seconds) {
        if (seconds < 60) {
            return String.format("%.1f сек.", seconds);
        }

        int totalSeconds = (int) seconds;
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;

        if (secs == 0) {
            return minutes + " мин.";
        }

        return minutes + " мин. " + secs + " сек.";
    }

    public static String formatTime(long milliseconds) {
        return formatTime(milliseconds / 1000.0);
    }
}