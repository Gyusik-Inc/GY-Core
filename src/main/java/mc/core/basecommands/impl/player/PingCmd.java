package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 19.02.2026
 */

@BaseCommandInfo(name = "ping", permission = "", cooldown = 1)
public class PingCmd implements BaseCommand {

    private static final String PERM_OTHERS = "gy-core.admin";

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                return true;
            }

            sendPingMessage(sender, player, player.getName(), player.getPing());
            return true;
        }

        if (!sender.hasPermission(PERM_OTHERS)) {
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendUsageMessage(sender, "/ping [игрок]");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            MessageUtil.sendUnknownPlayerMessage(sender, targetName);
            return true;
        }

        sendPingMessage(sender, target, target.getName(), target.getPing());
        return true;
    }

    private void sendPingMessage(CommandSender sender, Player who, String displayName, int ping) {
        String color;
        if (ping <= 50)       color = "&a";
        else if (ping <= 100) color = "&2";
        else if (ping <= 150) color = "&e";
        else if (ping <= 250) color = "&6";
        else                  color = "&c";

        boolean isOwn = (who == sender);

        String header;
        if (isOwn) {
            header = "Ваш пинг: " + color + ping;
        } else {
            header = "Пинг игрока &#30578C" + displayName + "&f равен: " + color + ping;
        }

        MessageUtil.sendMessage(sender, header);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(PERM_OTHERS)) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}