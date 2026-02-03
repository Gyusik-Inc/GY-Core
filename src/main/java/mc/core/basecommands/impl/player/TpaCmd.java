package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.GY;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@BaseCommandInfo(name = "tpa", permission = "", cooldown = 3)
public class TpaCmd implements BaseCommand {

    static final Map<UUID, UUID> tpaRequests = new HashMap<>();
    public static final Map<UUID, BukkitRunnable> teleportTasks = new HashMap<>();

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "Только игрок!");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, "Использование: &#30578C/tpa <ник>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(player, "Игрок не найден!");
            return true;
        }

        if (target.equals(player)) {
            MessageUtil.sendMessage(player, "Нельзя отправить запрос себе!");
            return true;
        }

        tpaRequests.put(player.getUniqueId(), target.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (tpaRequests.remove(player.getUniqueId()) != null) {
                    MessageUtil.sendMessage(player, "Ваш запрос на телепортацию к &#30578C" + target.getName() + " &fистёк.");
                    MessageUtil.sendMessage(target, "Запрос на телепортацию от &#30578C" + player.getName() + "&f истёк.");
                }
            }
        }.runTaskLater(GY.getInstance(), 20L * 60);

        Component accept = Component.text("§a[Принять]").clickEvent(ClickEvent.runCommand("/tpaccept")).hoverEvent(HoverEvent.showText(Component.text("Клик")));
        Component deny = Component.text(" §c[Отклонить]").clickEvent(ClickEvent.runCommand("/tpadeny")).hoverEvent(HoverEvent.showText(Component.text("Клик")));

        Component message = Component.empty()
                .append(accept)
                .append(deny);

        MessageUtil.sendMessage(target, "&#30578C" + player.getName() + "&f хочет телепортироваться к тебе!" );
        MessageUtil.sendMessage(target, "Используй: &#30578C/tpaccept");
        target.sendMessage(message);
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        MessageUtil.sendMessage(player, "Запрос отправлен игроку &#30578C" + target.getName());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .map(Player::getName)
                    .toList();
        }
        return List.of();
    }

    public static UUID getTarget(UUID targetId) {
        return tpaRequests.entrySet().stream()
                .filter(e -> e.getValue().equals(targetId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static void removeRequest(UUID playerId) {
        tpaRequests.remove(playerId);
    }
}
