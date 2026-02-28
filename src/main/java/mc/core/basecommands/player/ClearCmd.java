package mc.core.basecommands.player;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static mc.core.GY.msg;

@BaseCommandInfo(name = "clear", permission = "gy-core.clear", cooldown = 3)
public class ClearCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                return true;
            }

            
            clearInventory(player);
            msg.sendMessage(player, "Инвентарь очищен.");
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
            return true;
        }

        if (!sender.hasPermission("gy-core.clear.others")) {
            GY.msg.sendPermissionMessage(sender);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            GY.msg.sendUnknownPlayerMessage(sender, args[0]);
            return true;
        }

        clearInventory(target);
        GY.msg.sendMessage(sender, "Вы очистили инвентарь игрока &#30578C" + target.getName());
        GY.msg.sendMessage(target, "Ваш инвентарь был очищен &#30578C" + sender.getName());
        target.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    private void clearInventory(Player player) {
        var inv = player.getInventory();
        inv.clear();
        inv.setArmorContents(null);
        inv.setItemInOffHand(null);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return org.bukkit.Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
