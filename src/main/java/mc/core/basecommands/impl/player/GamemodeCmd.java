package mc.core.basecommands.impl.player;


import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;

import java.util.List;
import java.util.Arrays;

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

        if (args.length == 0) {
            MessageUtil.sendUsageMessage(player, "gm [0/1/2/3]");
            return true;
        }

        String modeArg = args[0].toLowerCase();
        GameMode gameMode;
        String permission;

        switch (modeArg) {
            case "0", "survival" -> {
                gameMode = GameMode.SURVIVAL;
                permission = "gy-core.gamemode.survival";
            }
            case "1", "creative" -> {
                gameMode = GameMode.CREATIVE;
                permission = "gy-core.gamemode.creative";
            }
            case "2", "adventure" -> {
                gameMode = GameMode.ADVENTURE;
                permission = "gy-core.gamemode.adventure";
            }
            case "3", "spectator" -> {
                gameMode = GameMode.SPECTATOR;
                permission = "gy-core.gamemode.spectator";
            }
            default -> {
                return true;
            }
        }

        if (!player.hasPermission(permission)) {
            MessageUtil.sendMessage(player, "Нет прав!");
            return true;
        }

        player.setGameMode(gameMode);
        MessageUtil.sendMessage(player, "&fРежим изменён на &#30578C" + getRussianName(gameMode));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("0", "1", "2", "3", "survival", "creative", "adventure", "spectator");
        }
        return List.of();
    }
}
