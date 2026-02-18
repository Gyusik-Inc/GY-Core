package mc.core.basecommands.impl.player;

import mc.GY;
import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@BaseCommandInfo(name = "balance", permission = "", cooldown = 3)
public class BalanceCmd implements BaseCommand {

    private static final String PERM_OTHERS = "gy-core.admin";

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                return true;
            }
            target = player;
        }

        else if (args.length == 1) {
            if (!sender.hasPermission(PERM_OTHERS)) {
                return true;
            }

            String name = args[0];
            target = Bukkit.getPlayer(name);

            if (target == null) {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
                if (!offline.hasPlayedBefore()) {
                    MessageUtil.sendUnknownPlayerMessage(sender, name);
                    return true;
                }
                target = offline.getPlayer();
            }
        }
        else {
            MessageUtil.sendUsageMessage(sender, "/balance [игрок]");
            return true;
        }

        double money = GY.getMoneySystem().getDatabase().getBalance(target);
        double rub   = GY.getRubSystem().getDatabase().getBalance(target);

        sender.sendMessage("");
        MessageUtil.sendMessage(sender, "&#30578C┃ &fБаланс &#30578C" + target.getName());
        MessageUtil.sendMessage(sender, "&#30578C┃");
        MessageUtil.sendMessage(sender, "&#30578C┃ &7Монет: &e" + format(money) + "$");
        MessageUtil.sendMessage(sender, "&#30578C┃ &7Рублей: &#30578C" + format(rub) + "❖");
        sender.sendMessage("");

        return true;
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

    private String format(double value) {
        if (value < 1000) return String.format("%.0f", value);
        if (value < 1_000_000) return String.format("%.1f тыс", value / 1000);
        if (value < 1_000_000_000) return String.format("%.1f млн", value / 1_000_000);
        return String.format("%.1f млрд", value / 1_000_000_000);
    }
}