package mc.core.basecommands.player;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import mc.core.utilites.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static mc.core.basecommands.player.MsgCmd.lastMessageMap;

@BaseCommandInfo(name = "reply", permission = "gy-core.msg", cooldown = 3)
public class ReplyCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            GY.getMsg().sendUsageMessage(player, "/reply [Сообщение]");
            return true;
        }

        UUID lastUUID = lastMessageMap.get(player.getUniqueId());

        if (lastUUID == null) {
            GY.getMsg().sendMessage(player, "&cВам некому отвечать.");
            return true;
        }

        Player target = Bukkit.getPlayer(lastUUID);

        if (target == null || !target.isOnline()) {
            GY.getMsg().sendMessage(player, "&cИгрок вышел с сервера.");
            return true;
        }

        if (target.equals(player)) {
            GY.getMsg().sendMessage(player, "&cОшибка ответа.");
            return true;
        }

        PlayerData targetData = new PlayerData(target.getUniqueId());
        if (!targetData.isMsgEnabled()) {
            GY.getMsg().sendMessage(player, "&fИгрок '&#30578C" + target.getName() + "&f' отключил личные сообщения.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        String message = String.join(" ", args);

        lastMessageMap.put(player.getUniqueId(), target.getUniqueId());
        lastMessageMap.put(target.getUniqueId(), player.getUniqueId());

        String toTarget = GY.getMsg().colorize(
                "&#30578C✉ #30578CВам &7от &#B1B7BE" + player.getName() +
                        " &8» #30578C" + message
        );

        String toSender = GY.getMsg().colorize(
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
