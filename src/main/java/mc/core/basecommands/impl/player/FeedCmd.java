package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "feed", permission = "gy-core.feed", cooldown = 30)
public class FeedCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        Player target = player;

        if (args.length > 0) {
            if (!player.hasPermission("gy-core.heal.admin")) {
                MessageUtil.sendPermissionMessage(player);
                return true;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtil.sendUnknownPlayerMessage(sender, args[0]);
                return true;
            }
        }

        target.setFoodLevel(20);
        target.setSaturation(20.0f);

        if (target == player) {
            MessageUtil.sendMessage(player, "Голод восстановлен.");
        } else {
            MessageUtil.sendMessage(player, "Голод для &#30578C" + target.getName() + "&f восстановлен.");
            MessageUtil.sendMessage(target, "Голод восстановлен.");
        }

        return true;
    }

    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("gy-core.feed.admin")) {
            Player player = sender instanceof Player ? (Player) sender : null;
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .map(Player::getName)
                    .filter(name -> args[0].isEmpty() || name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

}