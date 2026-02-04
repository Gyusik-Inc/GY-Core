package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "tphere", permission = "gy-core.tphere")
public class TphereCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, "Использование: /tphere <ник>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            MessageUtil.sendMessage(player, "Игрок не найден.");
            return true;
        }

        if (target.equals(player)) {
            MessageUtil.sendMessage(player, "Вы не можете телепортировать себя к себе.");
            return true;
        }

        target.teleport(player.getLocation());
        MessageUtil.sendMessage(target, "Вы были телепортированы к игроку &#30578C" + player.getName());
        MessageUtil.sendMessage(player, "Вы телепортировали игрока &#30578C" + target.getName() + " &fк себе.");

        target.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            String start = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(start))
                    .toList();
        }
        return List.of();
    }
}
