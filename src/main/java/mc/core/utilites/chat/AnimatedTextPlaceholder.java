package mc.core.utilites.chat;

import lombok.Getter;
import mc.core.GY;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimatedTextPlaceholder {

    private final String text;
    private final String edgeColor;
    private final String centerColor;
    private final int updateIntervalTicks;

    private double wavePosition = 0.0; // 0 = центр, 1 = края
    private boolean outward = true;     // направление волны: true = к краям, false = к центру

    @Getter
    private String currentFrame = "";

    public AnimatedTextPlaceholder(String text, String edgeColor, String centerColor, int updateIntervalTicks) {
        this.text = text;
        this.edgeColor = edgeColor;
        this.centerColor = centerColor;
        this.updateIntervalTicks = updateIntervalTicks;

        startAnimation();
    }

    private void startAnimation() {
        new BukkitRunnable() {
            @Override
            public void run() {
                currentFrame = buildFrame();
                double speed = 0.035;
                if (outward) {
                    wavePosition += speed;
                    if (wavePosition >= 1.0) outward = false;
                } else {
                    wavePosition -= speed;
                    if (wavePosition <= 0.0) outward = true;
                }
            }
        }.runTaskTimer(GY.getInstance(), 0L, updateIntervalTicks);
    }

    private String buildFrame() {
        int length = text.length();
        double centerIndex = (length - 1) / 2.0;
        double softness = 0.4;

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < length; i++) {
            double normalizedDistFromCenter = Math.abs(i - centerIndex) / centerIndex;
            double distanceToWave;
            if (outward) {
                distanceToWave = normalizedDistFromCenter - wavePosition;
            } else {
                distanceToWave = wavePosition - normalizedDistFromCenter;
            }
            distanceToWave = Math.abs(distanceToWave);

            double t = Math.exp(-Math.pow(distanceToWave / softness, 3));
            out.append(lerpColor(edgeColor, centerColor, t))
                    .append("§l")
                    .append(text.charAt(i));
        }

        return out.toString();
    }


    private String lerpColor(String c1, String c2, double t) {
        int r1 = Integer.parseInt(c1.substring(1, 3), 16);
        int g1 = Integer.parseInt(c1.substring(3, 5), 16);
        int b1 = Integer.parseInt(c1.substring(5, 7), 16);

        int r2 = Integer.parseInt(c2.substring(1, 3), 16);
        int g2 = Integer.parseInt(c2.substring(3, 5), 16);
        int b2 = Integer.parseInt(c2.substring(5, 7), 16);

        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);

        return String.format("#%02X%02X%02X", r, g, b);
    }
}
