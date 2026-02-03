package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@BaseCommandInfo(name = "tp", permission = "gy-core.admin")
public class TpCmd implements BaseCommand {

    private static final Map<UUID, Location> lastLocations = new HashMap<>();

    public static void updateLastLocation(Player player) {
        lastLocations.put(player.getUniqueId(), player.getLocation());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, "Только игрок!");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, "Использование: &#30578C/tp <ник>");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        Location loc = null;
        if (target.isOnline()) {
            loc = ((Player) target).getLocation();
        } else {
            loc = lastLocations.get(target.getUniqueId());
        }

        if (loc == null) {
            MessageUtil.sendMessage(player, "Нет информации о последнем местоположении игрока '&#30578C" + targetName + "&f'.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        player.teleport(loc);
        MessageUtil.sendMessage(player, "Вы телепортировались к игроку '&#30578C" + targetName + "&f'.");
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
