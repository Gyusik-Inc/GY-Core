package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.HomeData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@BaseCommandInfo(name = "home", permission = "gy-core.home", cooldown = 3)
public class HomeCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendUsageMessage(player, "/home [Дом, list]");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("list")) {
            if (args.length == 2 && player.hasPermission("gy-core.home.admin")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    MessageUtil.sendUnknownPlayerMessage(player, args[1]);
                    return true;
                }
                var homes = HomeData.getHomes(target.getUniqueId());
                if (homes.isEmpty()) {
                    MessageUtil.sendMessage(player, "У игрока " + target.getName() + " нет установленных домов.");
                } else {
                    String list = homes.entrySet().stream()
                            .map(e -> e.getKey() + " §7(" + formatLocation(e.getValue().location()) + " Мир: " + e.getValue().location().getWorld().getName() + ")")
                            .collect(Collectors.joining(", "));
                    MessageUtil.sendMessage(player, "Дома игрока &#30578C" + target.getName() + ": &#30578C" + list);
                }
            } else {
                var homes = HomeData.getHomes(player.getUniqueId());
                if (homes.isEmpty()) {
                    MessageUtil.sendMessage(player, "У вас нет установленных домов.");
                } else {
                    String list = String.join(", ", homes.keySet());
                    MessageUtil.sendMessage(player, "Ваши дома: &#30578C" + list);
                }
            }
            return true;
        }

        String homeName = args[0];
        if (!HomeData.hasHome(player.getUniqueId(), homeName)) {
            MessageUtil.sendMessage(player, "Дом с названием '&#30578C" + homeName + "&f' не найден.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        Location loc = HomeData.getHome(player.getUniqueId(), homeName).location();
        player.teleport(loc);
        MessageUtil.sendMessage(player, "Вы телепортировались в дом '&#30578C" + homeName + "&f'.");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            if (player.hasPermission("gy-core.admin")) {
                return List.of("list");
            } else {
                return HomeData.getHomes(player.getUniqueId()).keySet().stream().toList();
            }
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list") && player.hasPermission("gy-core.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }

        return List.of();
    }

    private String formatLocation(Location loc) {
        return String.format("%.0f, %.0f, %.0f", loc.getX(), loc.getY(), loc.getZ());
    }
}
