package mc.core.basecommands.world;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 03.02.2026
 */

@BaseCommandInfo(name = "sun", permission = "gy-core.weather", cooldown = 60)
public class SunCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        List<org.bukkit.World> worlds;
        if (sender instanceof Player player) {
            worlds = List.of(player.getWorld());
        } else {
            worlds = sender.getServer().getWorlds();
        }

        for (org.bukkit.World world : worlds) {
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(48000);
        }

        GY.getMsg().sendMessage(sender, "&#30578CЯсная &fпогода установлена.");
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}