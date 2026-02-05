package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "customization", permission = "gy-core.customization", cooldown = 5)
public class CustomizationCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length != 2) {
            MessageUtil.sendMessage(player, "Использование: &#30578C/customization <msg|tp|pay|scoreboard> <enable|disable>");
            return true;
        }

        String feature = args[0].toLowerCase();
        String action = args[1].toLowerCase();

        PlayerData data = new PlayerData(player.getUniqueId());
        boolean enable;

        if (action.equals("enable")) enable = true;
        else if (action.equals("disable")) enable = false;
        else {
            MessageUtil.sendMessage(player, "&cДоступные действия: enable, disable");
            return true;
        }

        switch (feature) {
            case "msg" -> {
                data.setMsgEnabled(enable);
                MessageUtil.sendMessage(player, "Личные сообщения " + (enable ? "&aвключены" : "&cотключены"));
            }
            case "tp" -> {
                data.setTpEnabled(enable);
                MessageUtil.sendMessage(player, "Запросы на телепортацию " + (enable ? "&aвключены" : "&cотключены"));
            }
            case "pay" -> {
                data.setPayEnabled(enable);
                MessageUtil.sendMessage(player, "Переводы денег " + (enable ? "&aвключены" : "&cотключены"));
            }
            case "scoreboard" -> {
                data.setScoreboardEnabled(enable);
                MessageUtil.sendMessage(player, "Scoreboard " + (enable ? "&aвключён" : "&cотключён"));
                Bukkit.getServer().dispatchCommand(player, "sb");
            }
            default -> MessageUtil.sendMessage(player, "&cДоступные функции: msg, tp, pay, scoreboard");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) return List.of("msg", "tp", "pay", "scoreboard");
        if (args.length == 2) return List.of("enable", "disable");
        return List.of();
    }
}
