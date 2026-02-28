package mc.core.basecommands.world;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

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
            return true;
        }

        if (args.length != 1) {
            GY.getMsg().sendUsageMessage(player, "/sethome [Дом]");
            return true;
        }

        int maxHomes = 1;
        if (player.hasPermission("gy-core.home.admin")) maxHomes = 999;

        if (HomeData.getHomes(player.getUniqueId()).size() >= maxHomes) {
            GY.getMsg().sendMessage(player, "Вы достигли лимита домов &#30578C(" + maxHomes + ")");
            return true;
        }

        String homeName = args[0];
        Location loc = player.getLocation();
        HomeData.setHome(player.getUniqueId(), homeName, loc);

        GY.getMsg().sendMessage(player, "Дом '&#30578C" + homeName + "&f' установлен!");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
