package mc.core.basecommands.world;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import mc.core.utilites.data.WarpData;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

@BaseCommandInfo(name = "setwarp", permission = "gy-core.setwarp", cooldown = 60)
public class SetWarp implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length < 1) {
            GY.msg.sendUsageMessage(player, "/setwarp [Название]");
            return true;
        }

        boolean adminWarp = false;
        String name;

        if (args[0].equalsIgnoreCase("admin")) {

            if (!player.hasPermission("gy-core.admin")) {
                GY.msg.sendPermissionMessage(player);
                return true;
            }

            if (args.length < 2) {
                GY.msg.sendMessage(player, "/setwarp admin <название>");
                return true;
            }

            adminWarp = true;
            name = args[1];

            if (WarpData.getAdminWarp(name) != null) {
                GY.msg.sendMessage(player, "Этот админ-варп уже существует и будет перезаписан.");
            }

        } else {
            name = args[0];
            int limit = 1;
            if (player.hasPermission("gy-core.warp.plus")) limit = 3;
            if (player.hasPermission("gy-core.admin")) limit = Integer.MAX_VALUE;

            boolean exists = WarpData.getWarp(name) != null;

            if (exists) {
                UUID owner = WarpData.getWarp(name).owner();
                if (owner != null && !owner.equals(player.getUniqueId())) {
                    GY.msg.sendMessage(player, "Вы не можете перезаписать этот варп, он принадлежит другому игроку.");
                    return true;
                }
            }

            if (!exists && WarpData.getPlayerWarpCount(player.getUniqueId()) >= limit) {
                GY.msg.sendMessage(player, "Вы достигли лимита варпов &#30578C(" + limit + ")");
                return true;
            }
        }

        Location loc = player.getLocation();
        WarpData.setWarp(name, loc, adminWarp ? null : player.getUniqueId(), adminWarp);

        if (adminWarp) {
            GY.msg.sendMessage(player, "Админ-варп &#30578C" + name + "&f установлен!");
        } else {
            GY.msg.sendMessage(player, "Варп &#30578C" + name + "&f установлен!");
        }

        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("gy-core.admin")) {
            return List.of("admin");
        }
        return List.of();
    }
}
