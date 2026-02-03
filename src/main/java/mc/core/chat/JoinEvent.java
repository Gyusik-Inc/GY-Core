package mc.core.chat;

import mc.core.utilites.chat.AnimateGradientUtil;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 03.02.2026
 */

public class JoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        AnimateGradientUtil.animateGradientTitle(
                player,
                "#30578C",
                "#7495C1",
                "ɴᴏʀᴛʜ-ᴍᴄ",
                "Добро пожаловать!",
                1000
        );

        player.sendMessage("");
        MessageUtil.sendMessage(player, "Добро пожаловать, #30578C" + player.getName());
        MessageUtil.sendMessage(player, "Помощь по серверу - #30578C/help");
        player.sendMessage("");
    }
}