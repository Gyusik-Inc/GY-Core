package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
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
            return true;
        }

        if (args.length == 1) {
            return teleportToPlayer(player, args[0]);
        } else if (args.length >= 3 && args.length <= 4) {
            return teleportToCoords(player, args);
        }

        MessageUtil.sendUsageMessage(sender, "/tp [Игрок] | /tp [x y z]| /tp [Мир] [x y z]");
        return true;
    }

    private boolean teleportToPlayer(Player player, String targetName) {
        if (targetName.equalsIgnoreCase(player.getName())) {
            MessageUtil.sendMessage(player, "Нельзя телепортироваться к себе!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

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

    private boolean teleportToCoords(Player player, String[] args) {
        try {
            World world = player.getWorld();
            double x, y, z;

            if (args.length == 4) {
                World targetWorld = Bukkit.getWorld(args[0]);
                if (targetWorld == null) {
                    MessageUtil.sendMessage(player, "&#c84f21Мир '&#30578C" + args[0] + "&c84f21' не найден.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return true;
                }
                world = targetWorld;
                x = Double.parseDouble(args[1]);
                y = Double.parseDouble(args[2]);
                z = Double.parseDouble(args[3]);
            } else {
                x = Double.parseDouble(args[0]);
                y = Double.parseDouble(args[1]);
                z = Double.parseDouble(args[2]);
            }

            Location targetLoc = new Location(world, x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
            player.teleport(targetLoc);

            MessageUtil.sendMessage(player, String.format("Телепортация на локацию &7(%s %.0f, %.0f, %.0f)",
                    world.getName(), x, y, z));
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
            return true;

        } catch (NumberFormatException e) {
            MessageUtil.sendMessage(player, "&#c84f21Координаты должны быть числами!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            Player player = (Player) sender;
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> !name.equalsIgnoreCase(player.getName()))
                    .toList();
        }
        return List.of();
    }
}
