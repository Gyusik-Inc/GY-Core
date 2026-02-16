package mc.core.grant;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.GrantData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@BaseCommandInfo(name = "grant", permission = "gy-core.grant.use", cooldown = 5)
public class GrantCmd implements BaseCommand {

    private static GrantManager grantManager;
    private final List<String> donatOrder = Arrays.asList("cunt", "erbus", "warden", "strider", "merchant");

    public GrantCmd() {
        // Пустой конструктор для CommandManager
    }

    public static void setGrantManager(GrantManager manager) {
        grantManager = manager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }

        Player player = (Player) sender;

        if (grantManager == null) {
            MessageUtil.sendMessage(player, "&cОшибка инициализации системы выдач!");
            return true;
        }

        if (args.length == 0) {
            MessageUtil.sendMessage(player, "&6&l/grant list &7- список доступных выдач");
            MessageUtil.sendMessage(player, "&6&l/grant give <ник> <донат> &7- выдать донат игроку");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 3) {
                    MessageUtil.sendMessage(player, "&cИспользование: /grant give <ник> <донат>");
                    return true;
                }
                handleGive(player, args[1], args[2]);
                break;

            case "list":
                handleList(player);
                break;

            default:
                MessageUtil.sendMessage(player, "&6&l/grant list &7- список доступных выдач");
                MessageUtil.sendMessage(player, "&6&l/grant give <ник> <донат> &7- выдать донат игроку");
                break;
        }

        return true;
    }

    private void handleGive(Player player, String targetName, String grantName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            MessageUtil.sendMessage(player, "&cИгрок " + targetName + " не найден или не в сети!");
            return;
        }

        GrantData granterData = grantManager.getPlayerGrantData(player.getUniqueId());
        if (granterData == null || !granterData.hasGrant(grantName)) {
            MessageUtil.sendMessage(player, "&cУ вас нет доната &e" + grantName + " &cдля выдачи!");
            return;
        }

        int remaining = granterData.getRemainingGrants().get(grantName);
        if (remaining <= 0) {
            MessageUtil.sendMessage(player, "&cУ вас закончились выдачи доната &e" + grantName + "&c!");
            return;
        }

        grantManager.giveGrant(player, targetName, grantName).thenAccept(success -> {
            if (success) {
                String grantPrefix = grantManager.getGroupPrefix(grantName);
                String targetPrefix = grantManager.getPlayerPrefix(target);

                MessageUtil.sendMessage(player, "&aВы выдали " + grantPrefix + " &aигроку " + targetPrefix);
                MessageUtil.sendMessage(target, "&aВам выдали донат " + grantPrefix + " &aот игрока " + grantManager.getPlayerPrefix(player));

                int newRemaining = granterData.getRemainingGrants().get(grantName);
                MessageUtil.sendMessage(player, "&7Осталось выдач " + grantPrefix + "&7: &f" + newRemaining);
            } else {
                MessageUtil.sendMessage(player, "&cНе удалось выдать донат! У игрока уже есть донат лучше.");
            }
        });
    }

    private void handleList(Player player) {
        GrantData data = grantManager.getPlayerGrantData(player.getUniqueId());
        if (data == null) {
            MessageUtil.sendMessage(player, "&cОшибка загрузки данных!");
            return;
        }

        MessageUtil.sendMessage(player, "&6&lДоступные команды к выдаче:");
        MessageUtil.sendMessage(player, "&7------------------------");

        boolean hasAny = false;

        // Выводим в нужном порядке: cunt, erbus, warden, strider, merchant
        for (String donat : donatOrder) {
            if (data.getRemainingGrants().containsKey(donat)) {
                int remaining = data.getRemainingGrants().get(donat);
                String prefix = grantManager.getGroupPrefix(donat);

                if (remaining > 0) {
                    hasAny = true;
                    MessageUtil.sendMessage(player, prefix + " &8- &f" + remaining + " шт.");
                }
            }
        }

        if (!hasAny) {
            MessageUtil.sendMessage(player, "&cУ вас нет доступных выдач!");
        }

        if (!data.getGrantHistory().isEmpty()) {
            MessageUtil.sendMessage(player, "&7------------------------");
            MessageUtil.sendMessage(player, "&6&lИстория выдач:");

            data.getGrantHistory().values().stream()
                    .sorted((h1, h2) -> Long.compare(h2.getTimestamp(), h1.getTimestamp()))
                    .limit(5)
                    .forEach(history -> {
                        String grantPrefix = grantManager.getGroupPrefix(history.getGrantName());
                        MessageUtil.sendMessage(player, "&7- " + grantPrefix + " &7→ &f" + history.getTargetPlayer());
                    });
        }

        MessageUtil.sendMessage(player, "&7------------------------");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player) || grantManager == null) return Collections.emptyList();

        Player player = (Player) sender;

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("give".startsWith(args[0].toLowerCase())) {
                completions.add("give");
            }
            if ("list".startsWith(args[0].toLowerCase())) {
                completions.add("list");
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            String partial = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            GrantData data = grantManager.getPlayerGrantData(player.getUniqueId());
            if (data == null) return Collections.emptyList();

            String partial = args[2].toLowerCase();
            List<String> available = new ArrayList<>();

            // Предлагаем донаты в том же порядке
            for (String donat : donatOrder) {
                if (data.getRemainingGrants().containsKey(donat) &&
                        data.getRemainingGrants().get(donat) > 0 &&
                        donat.toLowerCase().startsWith(partial)) {
                    available.add(donat);
                }
            }

            return available;
        }

        return Collections.emptyList();
    }
}