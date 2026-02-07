package mc.core.autorestart;

import lombok.Getter;
import mc.core.GY;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class AutoRestart {

    @Getter
    private boolean restartScheduled = false;
    private BukkitRunnable countdownTask;

    private final List<Integer> MINUTES_NOTIFY = Arrays.asList(15, 10, 5, 4, 3, 2, 1);
    private final List<Integer> SECONDS_NOTIFY = Arrays.asList(30, 15, 10, 5, 4, 3, 2, 1);

    private final List<LocalTime> AUTO_RESTART_TIMES = Arrays.asList(
            LocalTime.of(0, 0),
            LocalTime.of(6, 0),
            LocalTime.of(17, 0)
    );

    public AutoRestart() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (restartScheduled) return;

                LocalTime now = LocalTime.now().withSecond(0).withNano(0);

                for (LocalTime restartTime : AUTO_RESTART_TIMES) {
                    long secondsUntilRestart = Duration.between(now, restartTime).getSeconds();
                    if (secondsUntilRestart < 0) {
                        secondsUntilRestart += 24 * 3600;
                    }

                    if (secondsUntilRestart <= 15 * 60) {
                        startRestartInSeconds(secondsUntilRestart);
                        break;
                    }
                }
            }
        }.runTaskTimer(GY.getInstance(), 0L, 20L * 60);
    }

    public void startRestartInSeconds(long seconds) {
        if (restartScheduled) return;
        restartScheduled = true;

        startCountdown(seconds);
    }

    public void startRestartInMinutes(long minutes) {
        startRestartInSeconds(minutes * 60);
    }

    public void scheduleRestart(String timeStr) throws IllegalArgumentException {
        timeStr = timeStr.toLowerCase().replace(" ", "");
        if (timeStr.endsWith("m")) {
            long minutes = Long.parseLong(timeStr.replace("m", ""));
            startRestartInMinutes(minutes);
        } else if (timeStr.endsWith("s")) {
            long seconds = Long.parseLong(timeStr.replace("s", ""));
            startRestartInSeconds(seconds);
        } else {
            throw new IllegalArgumentException("Неверный формат времени! Используй 1m или 30s");
        }
    }

    private void startCountdown(long totalSeconds) {
        countdownTask = new BukkitRunnable() {
            long remaining = totalSeconds;

            @Override
            public void run() {
                if (!restartScheduled) {
                    cancel();
                    return;
                }

                if (remaining <= 0) {
                    broadcastMessage("Сервер перезагружается!");
                    cancel();
                    Bukkit.shutdown();
                    return;
                }

                long minutes = remaining / 60;
                long seconds = remaining % 60;

                if (MINUTES_NOTIFY.contains((int) minutes) && seconds == 0) {
                    broadcastMessage("До перезагрузки: &#30578C" + minutes + " мин.");
                }

                if (SECONDS_NOTIFY.contains((int) remaining) && remaining <= 5 * 60) {
                    broadcastMessage("До перезагрузки: &#30578C" + seconds + " сек.");
                }

                remaining--;
            }
        };

        restartScheduled = true;
        countdownTask.runTaskTimer(GY.getInstance(), 0L, 20L);
    }

    public boolean cancelRestart() {
        if (countdownTask != null) {
            restartScheduled = false;
            countdownTask.cancel();
            countdownTask = null;

            broadcastMessage("Перезагрузка отменена!");
            return true;
        }
        return false;
    }


    private void broadcastMessage(String message) {
        Bukkit.broadcast(
                MessageUtil.colorize(
                        "\n" +
                                "&#30578C┃ &#30578CОбъявление\n" +
                                "&#30578C┃ &7Содержимое: &f" + message + "\n" +
                                "&#30578C┃ &7От: &#B1B7BE&nCONSOLE" + "\n"
                ),
                ""
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(
                    MessageUtil.colorize("&#30578CПерезагрузка"),
                    MessageUtil.colorize("&7" + message),
                    10,
                    70,
                    10
            );

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        }
    }
}
