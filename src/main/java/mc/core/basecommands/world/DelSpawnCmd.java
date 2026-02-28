package mc.core.basecommands.world;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import mc.core.utilites.data.SpawnData;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "delspawn", permission = "gy-core.admin")
public class DelSpawnCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (SpawnData.getSpawn() == null) {
            GY.getMsg().sendMessage(sender, "Спавн не найден.");
            if (sender instanceof Player player) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            }
            return true;
        }

        SpawnData.deleteSpawn();
        GY.getMsg().sendMessage(sender, "Точка &#30578Cспавна &fуспешно удалена.");
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
