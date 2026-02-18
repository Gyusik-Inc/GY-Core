package mc.core.autorestart;

import mc.core.GY;
import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@BaseCommandInfo(name = "autorestart", permission = "gy-core.admin")
public class AutoRestartCmd implements BaseCommand {

    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+)([smhd])?$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков.");
            return true;
        }

        if (!player.hasPermission("gy-core.admin")) {
            MessageUtil.sendMessage(player, "&cНет прав.");
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendUsageMessage(sender, "/autorestart <время | cancel>");
            MessageUtil.sendMessage(sender, "&7Примеры: &f30s  &75m  &f2h  &f90  &fcancel");
            return true;
        }

        String input = args[0].trim().toLowerCase();
        AutoRestart autoRestart = GY.getInstance().getAutoRestart();

        if (input.equals("cancel")) {
            autoRestart.cancelRestart();
            MessageUtil.sendMessage(player, "&aПерезагрузка отменена.");
            return true;
        }

        long seconds = parseTime(input);

        if (seconds <= 0) {
            MessageUtil.sendMessage(player, "&cНеверный формат времени. Используй: 30s, 5m, 2h, 1d или просто число (секунды)");
            return true;
        }

        if (seconds < 10) {
            MessageUtil.sendMessage(player, "&cСлишком мало — минимум 10 секунд.");
            return true;
        }

        try {
            autoRestart.startRestartInSeconds(seconds);
            String readableTime = formatReadableTime(seconds);
            MessageUtil.sendMessage(player, "&aПерезагрузка запланирована через &f" + readableTime);
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "&cОшибка: " + e.getMessage());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("10s", "30s", "1m", "5m", "10m", "30m", "1h", "cancel");
        }
        return List.of();
    }

    private long parseTime(String input) {
        if (input.isEmpty()) return -1;

        Matcher matcher = TIME_PATTERN.matcher(input);
        if (!matcher.matches()) {
            return -1;
        }

        long number;
        try {
            number = Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            return -1;
        }

        if (number <= 0) return -1;

        String unit = matcher.group(2);
        if (unit == null) {
            return number;
        }

        return switch (unit.toLowerCase()) {
            case "s" -> number;
            case "m" -> number * 60;
            case "h" -> number * 3600;
            case "d" -> number * 86400;
            default  -> -1;
        };
    }

    private String formatReadableTime(long totalSeconds) {
        if (totalSeconds <= 0) return "0 сек";

        long days    = totalSeconds / 86400;
        long hours   = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600)  / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0)    sb.append(days).append("д ");
        if (hours > 0)   sb.append(hours).append("ч ");
        if (minutes > 0) sb.append(minutes).append("мин ");
        if (seconds > 0 || sb.isEmpty()) sb.append(seconds).append("сек");

        return sb.toString().trim();
    }
}