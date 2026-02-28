package mc.core.basecommands.player;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "fly", permission = "gy-core.fly", cooldown = 3)
public class FlyCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length > 0) {
            if (!player.hasPermission("gy-core.admin")) {
                GY.getMsg().sendPermissionMessage(player);
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                GY.getMsg().sendUnknownPlayerMessage(sender, args[0]);
                return true;
            }

            return toggleFlyMode(player, target);
        }

        return toggleFlyMode(player, player);
    }

    private boolean toggleFlyMode(Player sender, Player target) {
        target.setAllowFlight(!target.getAllowFlight());
        boolean isFlying = target.getAllowFlight();

        String status = isFlying ? "&aвключён" : "&cвыключен";

        if (target == sender) {
            GY.getMsg().sendMessage(target, "Полёт " + status);
        } else {
            GY.getMsg().sendMessage(sender, "Полёт " + status + " для &#30578C" + target.getName());
            GY.getMsg().sendMessage(target, "Полёт " + status + " &7(§e" + sender.getName() + "&7)");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("gy-core.admin")) {
            Player player = sender instanceof Player ? (Player) sender : null;
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .map(Player::getName)
                    .filter(name -> args[0].isEmpty() || name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
