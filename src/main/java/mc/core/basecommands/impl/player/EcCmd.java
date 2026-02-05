package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "ec", permission = "gy-core.enderchest", cooldown = 3)
public class EcCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player viewer)) {
            return true;
        }

        if (args.length == 0) {
            viewer.openInventory(viewer.getEnderChest());
            return true;
        }

        if (!viewer.hasPermission("gy-core.admin")) {
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtil.sendUnknownPlayerMessage(viewer, args[0]);
            return true;
        }

        viewer.openInventory(target.getEnderChest());
        MessageUtil.sendMessage(viewer, "Вы открыли эндер-сундук игрока '&#30578C" + target.getName() + "&f'");
        viewer.playSound(viewer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("gy-core.admin")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
