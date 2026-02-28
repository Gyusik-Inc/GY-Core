package mc.core.basecommands.player;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "heal", permission = "gy-core.heal", cooldown = 60)
public class HealCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        Player target = player;

        if (args.length > 0) {
            if (!player.hasPermission("gy-core.heal.others")) {
                GY.getMsg().sendPermissionMessage(player);
                return true;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                GY.getMsg().sendUnknownPlayerMessage(sender, args[0]);
                return true;
            }
        }

        for (org.bukkit.potion.PotionEffect effect : target.getActivePotionEffects()) {
            target.removePotionEffect(effect.getType());
        }

        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(20.0f);

        if (target == player) {
            GY.getMsg().sendMessage(player, "Вы исцелены.");
        } else {
            GY.getMsg().sendMessage(player, "Игрок &#30578C" + target.getName() + "&f исцелен.");
            GY.getMsg().sendMessage(target, "Вы исцелены.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("gy-core.heal.others")) {
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