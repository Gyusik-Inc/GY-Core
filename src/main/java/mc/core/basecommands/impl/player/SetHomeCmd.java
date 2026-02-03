package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.HomeData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "sethome", permission = "gy-core.sethome", cooldown = 3)
public class SetHomeCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "Только игрок!");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, "Использование: /sethome <название>");
            return true;
        }

        int maxHomes = 1;
        if (player.hasPermission("gy-core.home.admin")) maxHomes = 999;

        if (HomeData.getHomes(player.getUniqueId()).size() >= maxHomes) {
            MessageUtil.sendMessage(player, "Вы достигли лимита домов &#30578C(" + maxHomes + ")");
            return true;
        }

        String homeName = args[0];
        Location loc = player.getLocation();
        HomeData.setHome(player.getUniqueId(), homeName, loc);

        MessageUtil.sendMessage(player, "Дом '&#30578C" + homeName + "&f' установлен!");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
