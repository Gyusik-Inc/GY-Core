package mc.core.basecommands.impl.player;

import mc.core.GY;
import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

@BaseCommandInfo(name = "pay", permission = "gy-core.pay", cooldown = 1)
public class PayCmd implements BaseCommand {

    private static final DecimalFormat FORMAT = (DecimalFormat) DecimalFormat.getInstance(java.util.Locale.US);


    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length != 2) {
            MessageUtil.sendUsageMessage(player, "/pay [Игрок] [Сумма]");
            return true;
        }

        Economy econ = GY.getEcon();
        if (econ == null) {
            MessageUtil.sendMessage(player, "&cЭкономика недоступна.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !target.isOnline()) {
            MessageUtil.sendUnknownPlayerMessage(sender, args[0]);
            return true;
        }

        if (target.equals(player)) {
            MessageUtil.sendMessage(player, "Нельзя перевести деньги самому себе.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        double amount;
        try {
            amount = parseAmount(args[1]);
        } catch (Exception e) {
            MessageUtil.sendMessage(player, "Неверная сумма.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        if (amount <= 0) {
            MessageUtil.sendMessage(player, "Сумма должна быть больше 0.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        double balance = econ.getBalance(player);

        if (balance < amount) {
            MessageUtil.sendMessage(player, "Недостаточно средств. &7(" + balance + "&8/&#30578C" + amount + ")");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return true;
        }

        econ.withdrawPlayer(player, amount);
        econ.depositPlayer(target, amount);

        String formatted = FORMAT.format(amount);

        MessageUtil.sendMessage(player,
                "&#30578C✔ &fВы перевели &e" + formatted + "$ &fигроку &#30578C" + target.getName());

        MessageUtil.sendMessage(target,
                "&#30578C$ &fВы получили &e" + formatted + "$ &fот &#30578C" + player.getName());
        target.playSound(target.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
        return true;
    }

    private double parseAmount(String input) {
        input = input.toLowerCase().replace(" ", "").replace(",", ".");
        double multiplier = 1;
        if (input.endsWith("kkk")) {
            multiplier = 1_000_000_000D;
            input = input.substring(0, input.length() - 3);
        } else if (input.endsWith("b") || input.endsWith("bn") || input.endsWith("млрд")) {
            multiplier = 1_000_000_000D;
            input = input.replaceAll("(b|bn|млрд)$", "");

        } else if (input.endsWith("kk") || input.endsWith("кк")) {           // 1
            multiplier = 1_000_000D;
            input = input.substring(0, input.length() - 2);
        } else if (input.endsWith("m") || input.endsWith("mln") || input.endsWith("млн")) {
            multiplier = 1_000_000D;
            input = input.replaceAll("(m|mln|млн)$", "");
        } else if (input.endsWith("k") || input.endsWith("к") || input.endsWith("тыс")) {
            multiplier = 1_000D;
            input = input.replaceAll("(k|к|тыс)$", "");
        }

        return Double.parseDouble(input) * multiplier;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
