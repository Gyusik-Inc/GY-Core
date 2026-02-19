package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@BaseCommandInfo(name = "balance", permission = "", cooldown = 3)
public class BalanceCmd implements BaseCommand {

    private static final String PERM_OTHERS = "gy-core.admin";

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cУкажи ник игрока :>");
                return true;
            }

            sendBalanceMessage(sender, player, player.getName(), true);
            return true;
        }

        if (!sender.hasPermission(PERM_OTHERS)) {
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendUsageMessage(sender, "/balance [игрок]");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && target.getUniqueId().version() == 4) {
            MessageUtil.sendUnknownPlayerMessage(sender, targetName);
            return true;
        }

        sendBalanceMessage(sender, target.getPlayer(), targetName, false);
        return true;
    }

    private void sendBalanceMessage(CommandSender sender, Player viewerPlayer, String targetName, boolean isOwnBalance) {
        String headerName = isOwnBalance ? "&#30578CВаш" : targetName;
        String header = "&#30578C┃ &7" + (isOwnBalance ? "&#30578CВаш баланс" : "Баланс &#30578C" + headerName);

        sender.sendMessage("");
        sender.sendMessage(MessageUtil.colorize(header));
        sender.sendMessage(MessageUtil.colorize("&#30578C┃"));

        String lineMoney = "&#30578C┃ &7Монет: &e%gy_money%";
        String lineRub   = "&#30578C┃ &7Рублей: &#30578C%gy_rub%";

        Player parsePlayer = viewerPlayer;

        if (parsePlayer != null) {
            lineMoney = PlaceholderAPI.setPlaceholders(parsePlayer, lineMoney);
            lineRub   = PlaceholderAPI.setPlaceholders(parsePlayer, lineRub);
        } else {
            lineMoney = PlaceholderAPI.setPlaceholders(null, lineMoney);
            lineRub   = PlaceholderAPI.setPlaceholders(null, lineRub);
        }


        sender.sendMessage(MessageUtil.colorize(lineMoney));
        sender.sendMessage(MessageUtil.colorize(lineRub));
        sender.sendMessage("");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission(PERM_OTHERS)) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}