package mc.core.chat;

import mc.core.utilites.chat.AnimateGradientUtil;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.PlayerData;
import mc.core.utilites.data.SpawnData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        PlayerData data = new PlayerData(player.getUniqueId());

        if (!data.hasPlayedBefore()) {
            data.setFirstJoin();
            Bukkit.broadcast(MessageUtil.getGYString(
                    "Игрок &#30578C" + player.getName() + "&f первый раз зашел на сервер! &7(#" +
                            PlayerData.getTotalNewPlayers() + ")"), "");
            Location spawn = SpawnData.getSpawn();
            if (spawn != null) {
                player.teleport(spawn);
            }
        }

        AnimateGradientUtil.animateGradientTitleNoDelay(
                player, "#30578C", "#7495C1", "ɴᴏʀᴛʜ-ᴍᴄ", "Добро пожаловать!", 1000
        );

        player.sendMessage("");
        MessageUtil.sendMessage(player, "Добро пожаловать, #30578C" + player.getName());
        MessageUtil.sendMessage(player, "Помощь по серверу - #30578C/help");
        player.sendMessage("");
    }


}