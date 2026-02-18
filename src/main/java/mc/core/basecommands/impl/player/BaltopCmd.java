package mc.core.basecommands.impl.player;

import mc.GY;
import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.economy.money.MoneyDB;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@BaseCommandInfo(name = "baltop", permission = "", cooldown = 5)
public class BaltopCmd implements BaseCommand {

    private static final int PAGE_SIZE = 10;

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException ignored) {}
        }

        Map<UUID, Double> balances = new HashMap<>();
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            Player onlinePlayer = Bukkit.getPlayer(offlinePlayer.getUniqueId());
            if (onlinePlayer != null && onlinePlayer.hasPermission("gy-core.admin")) {
                continue;
            }

            MoneyDB moneyDB = GY.getMoneySystem().getDatabase();
            double balance = moneyDB.getBalance(offlinePlayer);
            balances.put(offlinePlayer.getUniqueId(), balance);
        }

        List<Map.Entry<UUID, Double>> sortedList = balances.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .toList();

        int totalPages = Math.max(1, (int) Math.ceil((double) sortedList.size() / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        sender.sendMessage(Component.text(" "));
        MessageUtil.sendMessage(sender, "&7Топ игроков по балансу &#30578C(" + page + "&8/&#30578C" + totalPages + ")");
        sender.sendMessage(Component.text(" "));

        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, sortedList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<UUID, Double> entry = sortedList.get(i);
            int rank = i + 1;
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            double balance = entry.getValue();

            TextColor rankColor;
            if (rank == 1) rankColor = TextColor.color(0x30578C);
            else if (rank == 2) rankColor = TextColor.color(0x2ECC71);
            else if (rank == 3) rankColor = TextColor.color(0xF39C12);
            else rankColor = TextColor.color(0xAAAAAA);

            assert name != null;
            Component line = Component.text("#" + rank + ". ", rankColor)
                    .append(Component.text(name, TextColor.color(0xB1B7BE)))
                    .append(Component.text(MessageUtil.colorize(" &8» ")))
                    .append(Component.text(MessageUtil.colorize("&e" + balance + "$")));

            sender.sendMessage(line);
        }

        sender.sendMessage(Component.text(" "));

        if (sender instanceof Player player) {
            Player online = Bukkit.getPlayer(player.getUniqueId());
            if (online != null && online.hasPermission("gy-core.admin")) {
                MessageUtil.sendMessage(sender, "Ваше место: &#30578CАдмин");
            } else {
                int playerRank = 1;
                double playerBalance = 0;
                for (int i = 0; i < sortedList.size(); i++) {
                    if (sortedList.get(i).getKey().equals(player.getUniqueId())) {
                        playerRank = i + 1;
                        playerBalance = sortedList.get(i).getValue();
                        break;
                    }
                }
                double percentile = sortedList.size() > 1
                        ? ((double) (sortedList.size() - playerRank) / (sortedList.size() - 1)) * 100
                        : 100;

                MessageUtil.sendMessage(sender, "Ваше место: &#30578C#" + playerRank +
                        " &7(Вы богаче: &#30578C#&n" + String.format("%.1f", percentile) + "&7 игроков)");
            }
        }


        TextComponent nav = new TextComponent();
        if (page > 1) {
            TextComponent prev = new TextComponent("« ");
            prev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Предыдущая страница")));
            prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/baltop " + (page - 1)));
            nav.addExtra(prev);
        }

        if (page < totalPages) {
            TextComponent next = new TextComponent(" »");
            next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Следующая страница")));
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/baltop " + (page + 1)));
            nav.addExtra(next);
        }

        if (nav.getExtra() != null && !nav.getExtra().isEmpty()) {
            sender.spigot().sendMessage(nav);
        }

        sender.sendMessage(Component.text(" "));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }
}
