package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@BaseCommandInfo(name = "gm", permission = "", cooldown = 3)
public class GamemodeCmd implements BaseCommand {

    private String getRussianName(GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> "Выживание";
            case CREATIVE -> "Креатив";
            case ADVENTURE -> "Приключения";
            case SPECTATOR -> "Наблюдатель";
            default -> mode.name();
        };
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (player.hasPermission("gy-core.admin") && args.length == 2) {
            return handleAdminGamemode(player, args);
        }

        if (args.length != 1) {
            MessageUtil.sendUsageMessage(player, "/gm [0/1/2/3]");
            return true;
        }

        return handleSelfGamemode(player, args[0]);
    }

    private boolean handleSelfGamemode(Player player, String modeArg) {
        GameMode gameMode = parseGameMode(modeArg.toLowerCase());
        if (gameMode == null) {
            MessageUtil.sendMessage(player, "&#c84f21Неверный режим! 0/1/2/3/survival/creative/adventure/spectator");
            return true;
        }

        String permission = "gy-core.gamemode." + gameMode.name().toLowerCase();
        if (!player.hasPermission(permission)) {
            MessageUtil.sendPermissionMessage(player);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        player.setGameMode(gameMode);
        MessageUtil.sendMessage(player, "&fВаш режим изменён на &#30578C" + getRussianName(gameMode));
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    private boolean handleAdminGamemode(Player sender, String[] args) {
        GameMode gameMode = parseGameMode(args[0].toLowerCase());
        if (gameMode == null) {
            MessageUtil.sendMessage(sender, "&#c84f21Неверный режим!");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            MessageUtil.sendMessage(sender, "&#c84f21Игрок &#30578C" + args[1] + " &c84f21не в сети!");
            sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        if (target.equals(sender)) {
            MessageUtil.sendMessage(sender, "&#c84f21Используйте /gm [режим] для себя!");
            return true;
        }

        target.setGameMode(gameMode);
        MessageUtil.sendMessage(sender, "&fИгроку &#30578C" + target.getName() + " &fустановлен режим &#30578C" + getRussianName(gameMode));
        MessageUtil.sendMessage(target, "&fВам установили режим &#30578C" + getRussianName(gameMode) + " &f(§7" + sender.getName() + "&f)");

        sender.playSound(sender.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        target.playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        return true;
    }

    private GameMode parseGameMode(String modeArg) {
        return switch (modeArg) {
            case "0", "survival", "s" -> GameMode.SURVIVAL;
            case "1", "creative", "c" -> GameMode.CREATIVE;
            case "2", "adventure", "a" -> GameMode.ADVENTURE;
            case "3", "spectator", "sp" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("0", "1", "2", "3", "survival", "creative", "adventure", "spectator");
        }

        if (args.length == 2 && sender instanceof Player && ((Player) sender).hasPermission("gy-core.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName) // Без себя
                    .filter(name -> !name.equalsIgnoreCase(((Player) sender).getName()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
