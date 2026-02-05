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

@BaseCommandInfo(name = "tpadeny", permission = "", cooldown = 3)
public class TpaDenyCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player target)) {
            return true;
        }

        UUID senderId = TpaCmd.getTarget(target.getUniqueId());
        if (senderId == null) {
            MessageUtil.sendMessage(target, "Нет активных запросов.");
            return true;
        }

        Player player = Bukkit.getPlayer(senderId);
        if (player != null && player.isOnline()) {
            MessageUtil.sendMessage(player, target.getName() + " отклонил ваш запрос на телепортацию.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        }

        TpaCmd.removeRequest(senderId);
        TpaCmd.teleportTasks.remove(senderId);

        MessageUtil.sendMessage(target, "Вы отклонили запрос телепортации.");
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
