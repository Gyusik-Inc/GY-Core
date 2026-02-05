package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.WarpData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BaseCommandInfo(name = "warp", permission = "")
public class WarpCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length < 1 || args.length > 2) {
            MessageUtil.sendUsageMessage(player, "/warp [Название]");
            return true;
        }

        String warpName = args[0];
        Player targetPlayer = player;
        if (args.length == 2) {
            if (!player.hasPermission("gy-core.admin")) {
                return true;
            }
            targetPlayer = Bukkit.getPlayerExact(args[1]);
            if (targetPlayer == null) {
                MessageUtil.sendUnknownPlayerMessage(sender, args[1]);
                return true;
            }
        }

        WarpData.WarpEntry entry = WarpData.getAdminWarp(warpName);
        if (entry == null) entry = WarpData.getWarp(warpName);

        if (entry == null) {
            MessageUtil.sendMessage(player, "Варп не найден.");
            return true;
        }

        targetPlayer.teleport(entry.location());
        MessageUtil.sendMessage(targetPlayer, "Телепортация на варп &#30578C" + warpName);
        targetPlayer.playSound(targetPlayer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);

        if (targetPlayer != player) {
            MessageUtil.sendMessage(player, "Вы телепортировали игрока &#30578C" + targetPlayer.getName() + "&f на варп &#30578C" + warpName);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            String start = args[0].toLowerCase();
            return Stream.concat(
                            WarpData.getWarps().keySet().stream(),
                            WarpData.getAdminWarps().keySet().stream()
                    )
                    .filter(name -> name.toLowerCase().startsWith(start))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2 && player.hasPermission("gy-core.admin")) {
            String start = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(start))
                    .sorted()
                    .toList();
        }

        return List.of();
    }
}
