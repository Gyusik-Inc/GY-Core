package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.AnimateGradientUtil;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 05.02.2026
 */

@BaseCommandInfo(name = "helper", permission = "", cooldown = 15)
public class HelperCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Player suspect = null;
        if (args.length == 1) {
            suspect = Bukkit.getPlayer(args[0]);
            if (suspect == null) {
                MessageUtil.sendUnknownPlayerMessage(player, args[0]);
                return true;
            }
        } else if (args.length != 0) {
            return true;
        }

        String message;
        if (suspect != null) {
            message = MessageUtil.getGYString("Игрок &#30578C" + player.getName() +
                    "&f подозревает &#30578C" + suspect.getName() + "&f!");
        } else {
            message = MessageUtil.getGYString("Игрок &#30578C" + player.getName() + "&f просит проспекать!");
        }

        Bukkit.broadcast(message, "gy-core.helper");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("gy-core.helper")) {
                String titleText = suspect != null ?
                        "Подозрительный " + suspect.getName() :
                        "Модерирование";
                AnimateGradientUtil.animateGradientTitleNoDelay(
                        online,
                        "#30578C",
                        "#7495C1",
                        titleText,
                        "Проследите за " + (suspect != null ? suspect.getName() : player.getName()),
                        1500
                );
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

}