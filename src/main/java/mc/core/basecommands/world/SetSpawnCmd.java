package mc.core.basecommands.world;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import mc.core.utilites.data.SpawnData;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "setspawn", permission = "gy-core.admin")
public class SetSpawnCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        SpawnData.setSpawn(player.getLocation());
        Location loc = SpawnData.getSpawn();
        GY.getMsg().sendMessage(player,
                "Спавн установлен. &#30578CЛокация: &7"
                        + loc.getBlockX() + ", "
                        + loc.getBlockY() + ", "
                        + loc.getBlockZ() + " Мир: "
                        + loc.getWorld().getName()
        );
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
