package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.List;

@BaseCommandInfo(name = "fly", permission = "gy-core.fly", cooldown = 3)
public class FlyCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        Player target = player;

        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtil.sendUnknownPlayerMessage(sender, args[0]);
                return true;
            }
        }

        target.setAllowFlight(!target.getAllowFlight());
        boolean isFlying = target.getAllowFlight();

        if (target == player) {
            MessageUtil.sendMessage(player, isFlying ? "Полёт включён" : "Полёт выключен");
        } else {
            MessageUtil.sendMessage(player, "Полёт " + (isFlying ? "включён" : "выключен") + " для &#30578C" + target.getName());
            MessageUtil.sendMessage(target, "Ваш полёт " + (isFlying ? "включён" : "выключен") + "!");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
