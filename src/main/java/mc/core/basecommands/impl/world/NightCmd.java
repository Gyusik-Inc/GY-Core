package mc.core.basecommands.impl.world;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.math.TimeUtil;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 03.02.2026
 */

@BaseCommandInfo(name = "night", permission = "gy-core.time", cooldown = 60)
public class NightCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        List<World> worlds;

        if (sender instanceof Player player) {
            worlds = List.of(player.getWorld());
        } else {
            worlds = sender.getServer().getWorlds();
        }

        TimeUtil.smoothTimeTransition(worlds, 15000, 200);

        MessageUtil.sendMessage(sender, "Начинаю смену времени на &#30578Cночь.");
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