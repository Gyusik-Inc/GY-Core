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

@BaseCommandInfo(name = "tpaccept", permission = "", cooldown = 3)
public class TpacceptCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player target)) {
            MessageUtil.sendMessage(sender, "Только игрок!");
            return true;
        }

        UUID senderId = TpaCmd.getTarget(target.getUniqueId());
        if (senderId == null) {
            MessageUtil.sendMessage(target, "Нет активных запросов.");
            return true;
        }

        Player player = Bukkit.getPlayer(senderId);
        if (player == null || !player.isOnline()) {
            MessageUtil.sendMessage(target, "Игрок не в сети.");
            TpaCmd.removeRequest(senderId);
            return true;
        }

        TpaCmd.removeRequest(senderId);

        player.teleport(target.getLocation());
        MessageUtil.sendMessage(player, "Вы телепортировались к &#30578C" + target.getName());
        MessageUtil.sendMessage(target, "&#30578C" + player.getName() + "&f успешно телепортирован.");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}

