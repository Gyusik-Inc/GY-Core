package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Vanish с босс-баром
 *
 * @author Gyusik
 * @since 03.02.2026
 */
@BaseCommandInfo(name = "vanish", permission = "gy-core.vanish")
public class VanishCmd implements BaseCommand {

    private static final Set<UUID> vanishedPlayers = new HashSet<>();
    private static final Map<UUID, BossBar> bossBars = new HashMap<>();

    public static boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "Только игрок!");
            return true;
        }

        boolean nowVanished;
        if (vanishedPlayers.contains(player.getUniqueId())) {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(player);
            }
            removeBossBar(player);
            nowVanished = false;
        } else {
            vanishedPlayers.add(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("gy-core.admin")) {
                    p.hidePlayer(player);
                }
            }
            createBossBar(player);
            nowVanished = true;
        }

        MessageUtil.sendMessage(player, nowVanished ? "Вы скрыты от игроков." : "Вы снова видимы игрокам.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    private void createBossBar(Player player) {
        BossBar bar = Bukkit.createBossBar("Вы в режиме наблюдения", BarColor.WHITE, BarStyle.SOLID);
        bar.setVisible(true);
        bar.addPlayer(player);
        bossBars.put(player.getUniqueId(), bar);
    }

    private void removeBossBar(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    public static void removeAllVanishes() {
        for (UUID uuid : vanishedPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.showPlayer(player);
                }
            }
        }
        vanishedPlayers.clear();

        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }
        bossBars.clear();
    }
}
