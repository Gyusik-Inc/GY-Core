package mc.core.basecommands.player;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import mc.core.utilites.data.SpawnData;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@BaseCommandInfo(name = "spawn", permission = "", cooldown = 5)
public class SpawnCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (SpawnData.getSpawn() == null) {
            if (sender instanceof Player p) {
                GY.msg.sendMessage(p, "Спавн не найден.");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            } else {
                sender.sendMessage("Спавн не найден.");
            }
            return true;
        }

        if (args.length == 1) {

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                GY.msg.sendUnknownPlayerMessage(sender, args[0]);
                return true;
            }

            if (sender instanceof Player player && !player.hasPermission("gy-core.admin")) {
                GY.msg.sendPermissionMessage(player);
                return true;
            }

            target.teleport(SpawnData.getSpawn());
            target.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);

            GY.msg.sendMessage(target, "Вы были телепортированы на &#30578Cспавн.");

            if (sender instanceof Player player) {
                GY.msg.sendMessage(player, "Вы телепортировали игрока &#30578C" + target.getName() + "&f на спавн.");
            } else {
                GY.msg.sendMessage(sender, "Игрок &#30578C" + target.getName() + " &fтелепортирован на спавн.");
            }

            return true;
        }

        if (!(sender instanceof Player player)) {
            return true;
        }

        player.teleport(SpawnData.getSpawn());
        GY.msg.sendMessage(player, "Успешная телепортация на &#30578Cспавн.");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);

        return true;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {

        if (!(sender instanceof Player player)) return List.of();
        if (!player.hasPermission("gy-core.admin")) return List.of();

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
