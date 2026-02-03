package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.HomeData;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "delhome", permission = "gy-core.home", cooldown = 3)
public class DelHomeCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "Только игрок!");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, "Использование: &#30578C/delhome <название>");
            return true;
        }

        String homeName = args[0];

        if (!HomeData.hasHome(player.getUniqueId(), homeName)) {
            MessageUtil.sendMessage(player, "Дом с названием '&#30578C" + homeName + "&f' не найден.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        HomeData.getHomes(player.getUniqueId()).remove(homeName);
        HomeData.saveHomes();

        MessageUtil.sendMessage(player, "Дом '&#30578C" + homeName + "&f' успешно удалён!");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            return HomeData.getHomes(player.getUniqueId()).keySet().stream().toList();
        }
        return List.of();
    }
}
