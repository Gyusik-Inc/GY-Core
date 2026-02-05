package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
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
                MessageUtil.sendMessage(p, "Спавн не найден.");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            } else {
                sender.sendMessage("Спавн не найден.");
            }
            return true;
        }

        if (args.length == 1) {

            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessageUtil.sendUnknownPlayerMessage(sender, args[0]);
                return true;
            }

            if (sender instanceof Player player && !player.hasPermission("gy-core.admin")) {
                MessageUtil.sendMessage(player, "У вас нет прав.");
                return true;
            }

            target.teleport(SpawnData.getSpawn());
            target.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);

            MessageUtil.sendMessage(target, "Вы были телепортированы на &#30578Cспавн.");

            if (sender instanceof Player player) {
                MessageUtil.sendMessage(player, "Вы телепортировали игрока &#30578C" + target.getName() + "&f на спавн.");
            } else {
                MessageUtil.sendMessage(sender, "Игрок &#30578C" + target.getName() + " &fтелепортирован на спавн.");
            }

            return true;
        }

        if (!(sender instanceof Player player)) {
            return true;
        }

        player.teleport(SpawnData.getSpawn());
        MessageUtil.sendMessage(player, "Успешная телепортация на &#30578Cспавн.");
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
