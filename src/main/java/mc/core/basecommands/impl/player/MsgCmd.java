package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@BaseCommandInfo(name = "msg", permission = "gy-core.msg", cooldown = 3)
public class MsgCmd implements BaseCommand {

    public static final Map<UUID, UUID> lastMessageMap = new HashMap<>();

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length < 2) {
            MessageUtil.sendUsageMessage(player, "/msg [Игрок] [Сообщение]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            MessageUtil.sendUnknownPlayerMessage(sender, args[0]);
            return true;
        }

        if (target.equals(player)) {
            MessageUtil.sendMessage(player, "&cНельзя писать самому себе.");
            return true;
        }

        PlayerData targetData = new PlayerData(target.getUniqueId());
        if (!targetData.isMsgEnabled()) {
            MessageUtil.sendMessage(player, "&fИгрок '&#30578C" + target.getName() + "&f' отключил личные сообщения.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
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
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        }
        return List.of();
    }
}
