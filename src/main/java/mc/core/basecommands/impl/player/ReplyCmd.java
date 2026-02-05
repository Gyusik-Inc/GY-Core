package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static mc.core.basecommands.impl.player.MsgCmd.lastMessageMap;

@BaseCommandInfo(name = "reply", permission = "gy-core.msg", cooldown = 3)
public class ReplyCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendUsageMessage(player, "/reply [Сообщение]");
            return true;
        }

        UUID lastUUID = lastMessageMap.get(player.getUniqueId());

        if (lastUUID == null) {
            MessageUtil.sendMessage(player, "&cВам некому отвечать.");
            return true;
        }

        Player target = Bukkit.getPlayer(lastUUID);

        if (target == null || !target.isOnline()) {
            MessageUtil.sendMessage(player, "&cИгрок вышел с сервера.");
            return true;
        }

        if (target.equals(player)) {
            MessageUtil.sendMessage(player, "&cОшибка ответа.");
            return true;
        }

        String message = String.join(" ", args);
        lastMessageMap.put(player.getUniqueId(), target.getUniqueId());
        lastMessageMap.put(target.getUniqueId(), player.getUniqueId());

        String toTarget = MessageUtil.colorize(
                "&#30578C✉ #30578CВам &7от &#B1B7BE" + player.getName() +
                        " &8» #30578C" + message
        );

        String toSender = MessageUtil.colorize(
                "&#30578C✉ #30578CВы &8→ &#B1B7BE" + target.getName() +
                        " &8» #30578C" + message
        );

        target.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1, 1);

        target.sendMessage(toTarget);
        player.sendMessage(toSender);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
