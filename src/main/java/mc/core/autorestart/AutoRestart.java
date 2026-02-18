package mc.core.autorestart;

import lombok.Getter;
import mc.core.GY;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class AutoRestart {

    @Getter private static AutoRestart instance;

    @Getter private boolean restartScheduled = false;
    private BukkitTask countdownTask;
    private LocalTime nextRestartTime = null;
    private final Set<LocalTime> skippedToday = new HashSet<>();

    private final List<Integer> MINUTES_NOTIFY = Arrays.asList(15, 10, 5, 4, 3, 2, 1);
    private final List<Integer> SECONDS_NOTIFY = Arrays.asList(30, 15, 10, 5, 4, 3, 2, 1);

    private final List<LocalTime> AUTO_RESTART_TIMES = Arrays.asList(
            LocalTime.of(0, 0),
            LocalTime.of(21, 0),
            LocalTime.of(6, 0)
    );

    public AutoRestart() {
        instance = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (restartScheduled) return;

                LocalTime now = LocalTime.now().withSecond(0).withNano(0);
                if (now.isBefore(LocalTime.of(0, 1))) {
                    skippedToday.clear();
                }

                LocalTime candidate = null;
                long minSeconds = Long.MAX_VALUE;

                for (LocalTime restartTime : AUTO_RESTART_TIMES) {
                    if (skippedToday.contains(restartTime)) continue;

                    long seconds = Duration.between(now, restartTime).getSeconds();
                    if (seconds < 0) seconds += 24 * 3600;

                    if (seconds < minSeconds) {
                        minSeconds = seconds;
                        candidate = restartTime;
                    }
                }

                if (candidate != null && minSeconds <= 15 * 60) {
                    nextRestartTime = candidate;
                    startRestartInSeconds(minSeconds);
                }
            }
        }.runTaskTimer(GY.getInstance(), 0L, 20L * 60);
    }

    public void startRestartInSeconds(long seconds) {
        if (restartScheduled) return;
        restartScheduled = true;
        startCountdown(seconds);
    }

    public void cancelRestart() {
        if (!restartScheduled) {
            broadcastMessage("Перезагрузка и так не запланирована.");
            return;
        }

        restartScheduled = false;
        if (nextRestartTime != null) {
            skippedToday.add(nextRestartTime);
        }

        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }

        nextRestartTime = null;

        broadcastMessage("Перезагрузка отменена!");
    }

    private void startCountdown(long totalSeconds) {
        countdownTask = new BukkitRunnable() {
            long remaining = totalSeconds;

            @Override
            public void run() {
                if (!restartScheduled || remaining <= 0) {
                    if (remaining <= 0 && restartScheduled) {
                        broadcastMessage("Сервер перезагружается!");
                        Bukkit.shutdown();
                    }
                    cancel();
                    return;
                }

                long minutes = remaining / 60;
                long secondsLeft = remaining % 60;

                if (secondsLeft == 0 && MINUTES_NOTIFY.contains((int) minutes)) {
                    broadcastMessage("До перезагрузки: &#30578C" + minutes + " мин.");
                }
                if (SECONDS_NOTIFY.contains((int) remaining)) {
                    broadcastMessage("До перезагрузки: &#30578C" + remaining + " сек.");
                }

                remaining--;
            }
        }.runTaskTimer(GY.getInstance(), 0L, 20L);
    }

    private void broadcastMessage(String message) {
        String prefix = "\n&#30578C┃ &#30578CОбъявление\n&#30578C┃ &7Содержимое: &f" + message + "\n&#30578C┃ &7От: &#B1B7BE&nCONSOLE\n";
        Bukkit.broadcast(MessageUtil.colorize(prefix), "");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(
                    MessageUtil.colorize("&#30578CПерезагрузка"),
                    MessageUtil.colorize("&7" + message),
                    10, 70, 10
            );
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        }
    }
}