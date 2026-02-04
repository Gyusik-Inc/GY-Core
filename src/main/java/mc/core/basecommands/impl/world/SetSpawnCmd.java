package mc.core.basecommands.impl.world;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
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
        MessageUtil.sendMessage(player,
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
