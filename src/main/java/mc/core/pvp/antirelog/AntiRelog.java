package mc.core.pvp.antirelog;

import mc.core.GY;
import mc.core.utilites.chat.AnimateGradientUtil;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AntiRelog {
    private static final Map<UUID, Long> endTimes = new HashMap<>();
    private static final Map<UUID, BossBar> bossBars = new HashMap<>();
    private static BukkitRunnable ticker;
    private static final long DURATION = TimeUnit.SECONDS.toMillis(30);

    public static void init() {
        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                List<UUID> toRemove = new ArrayList<>();
                for (UUID id : endTimes.keySet()) {
                    Player p = Bukkit.getPlayer(id);
                    if (p == null || !p.isOnline() || now >= endTimes.get(id)) {
                        toRemove.add(id);
                    } else {
                        updateBossBar(id);
                    }
                }

                for (UUID id : toRemove) {
                    endCombat(id);
                }
            }
        };
        ticker.runTaskTimerAsynchronously(GY.getInstance(), 0L, 1L);
    }


    public static void shutdown() {
        if (ticker != null) {
            ticker.cancel();
            ticker = null;
        }
        endTimes.clear();
        bossBars.values().forEach(BossBar::removeAll);
        bossBars.clear();
    }

    public static void addPlayer(Player player, Player target) {
        long newEndTime = System.currentTimeMillis() + DURATION;

        boolean playerNew = !endTimes.containsKey(player.getUniqueId());
        boolean targetNew = !endTimes.containsKey(target.getUniqueId());

        if (playerNew) {
            AnimateGradientUtil.animateGradientTitleNoDelay(
                    player, "#30578C", "#DB5858", "ɴᴏʀᴛʜ-ᴍᴄ", "Режим боя активирован!", 1000
            );
            MessageUtil.sendMessage(player, "Режим боя начался, противник: &#30578C" + target.getName());
        }

        if (targetNew) {
            AnimateGradientUtil.animateGradientTitleNoDelay(
                    target, "#30578C", "#DB5858", "ɴᴏʀᴛʜ-ᴍᴄ", "Режим боя активирован!", 1000
            );
            MessageUtil.sendMessage(target, "Режим боя начался, противник: &#30578C" + player.getName());
        }

        endTimes.put(player.getUniqueId(), newEndTime);
        endTimes.put(target.getUniqueId(), newEndTime);
        if (playerNew) createBossBar(player);
        if (targetNew) createBossBar(target);
    }


    public static boolean isInPvp(Player player) {
        return endTimes.containsKey(player.getUniqueId());
    }

    public static void removePlayer(Player player) {
        endCombat(player.getUniqueId());
    }

    private static void createBossBar(Player player) {
        UUID id = player.getUniqueId();
        BossBar bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SEGMENTED_12);
        bar.addPlayer(player);
        bossBars.put(id, bar);
    }

    private static void endCombat(UUID id) {
        Player player = Bukkit.getPlayer(id);

        endTimes.remove(id);
        AntiRelogEvent.clearCooldowns(id);

        BossBar bar = bossBars.remove(id);
        if (bar != null) {
            bar.removeAll();
        }

        if (player != null && player.isOnline()) {
            AnimateGradientUtil.animateGradientTitleNoDelay(
                    player,
                    "#30578C", "#75C28A",
                    "ɴᴏʀᴛʜ-ᴍᴄ",
                    "Режим боя завершён!",
                    1000
            );

            MessageUtil.sendMessage(player, "Режим боя завершён.");
        }
    }


    private static void updateBossBar(UUID id) {
        Long endTime = endTimes.get(id);
        if (endTime == null) return;

        Player p = Bukkit.getPlayer(id);
        if (p == null) return;

        BossBar bar = bossBars.get(id);
        if (bar == null) return;

        long remain = (endTime - System.currentTimeMillis()) / 1000;
        bar.setTitle(MessageUtil.colorize("&#30578C⚔ &fРежим боя &8» &#30578C") + Math.max(0, remain) + " сек.");
        bar.setProgress(Math.max(0, Math.min(1, (double) remain / 30)));
    }
}
