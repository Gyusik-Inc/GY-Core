package mc.core.basecommands.impl.world;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.WarpData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BaseCommandInfo(name = "delwarp", permission = "gy-core.delwarp")
public class DelWarp implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "Только игрок!");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendUsageMessage(player, "/delwarp [Название]");
            return true;
        }

        boolean success = WarpData.deleteWarp(
                args[0],
                player.getUniqueId(),
                player.hasPermission("gy-core.admin")
        );

        if (!success) {
            MessageUtil.sendMessage(player, "Вы не можете удалить этот варп.");
            return true;
        }

        MessageUtil.sendMessage(player, "Варп &#30578C" + args[0] + "&f удалён.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();
        if (args.length != 1) return List.of();

        boolean isAdmin = player.hasPermission("gy-core.admin");

        return Stream.concat(
                        WarpData.getWarps().entrySet().stream()
                                .filter(e -> e.getValue().owner() != null && (isAdmin || e.getValue().owner().equals(player.getUniqueId())))
                                .map(Map.Entry::getKey),
                        isAdmin ? WarpData.getAdminWarps().keySet().stream() : Stream.empty()
                )
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

}
