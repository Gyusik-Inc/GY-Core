package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.AnimateGradientUtil;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 05.02.2026
 */

@BaseCommandInfo(name = "broadcast", permission = "gy-core.broadcast", cooldown = 120)
public class BroadcastCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (args.length == 0) {
            MessageUtil.sendUsageMessage(sender, "/bc [Сообщение]");
            return true;
        }

        String message = String.join(" ", args);

        if (message.length() > 80) {
            MessageUtil.sendMessage(sender, "Сообщение слишком длинное! Максимум 80 символов.");
            return true;
        }

        Bukkit.broadcast(
                MessageUtil.colorize(
                        "\n" +
                                "&#30578C┃ &#30578CОбъявление\n" +
                                "&#30578C┃ &7Содержимое: &f" + message + "\n" +
                                "&#30578C┃ &7От: &#B1B7BE&n" + sender.getName() + "\n"
                ),
                ""
        );

        Bukkit.getOnlinePlayers().forEach(p ->
                AnimateGradientUtil.animateGradientTitleNoDelay(
                        p,
                        "#30578C",
                        "#7495C1",
                        "ɴᴏʀᴛʜ-ᴍᴄ",
                        "Объявление в чате!",
                        1000
                )
        );

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}