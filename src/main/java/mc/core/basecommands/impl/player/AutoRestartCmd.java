package mc.core.basecommands.impl.player;

import mc.core.GY;
import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.autorestart.AutoRestart;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "autorestart", permission = "gy-core.admin")
public class AutoRestartCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("gy-core.admin")) {
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendUsageMessage(sender, "/autorestart [Время/cancel]");
            return true;
        }

        String input = args[0].toLowerCase();
        AutoRestart autoRestart = GY.getInstance().getAutoRestart();
        if (input.equals("cancel")) {
            if (autoRestart.cancelRestart()) {
                MessageUtil.sendMessage(player, "&Перезагрузка отменена.");
            } else {
                MessageUtil.sendMessage(player, "&cПерезагрузка не была запланирована.");
            }
            return true;
        }

        try {
            autoRestart.scheduleRestart(input);
            MessageUtil.sendMessage(player, "Перезагрузка сервера запланирована через &#30578C" + input);
        } catch (IllegalArgumentException e) {
            MessageUtil.sendMessage(player, "&c" + e.getMessage());
        }

        return true;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) return List.of("1s", "5s", "10s", "30s", "1m", "5m", "10m", "cancel");
        return List.of();
    }
}
