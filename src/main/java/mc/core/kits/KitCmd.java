package mc.core.kits;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.AnimateGradientUtil;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.KitData;
import mc.core.utilites.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@BaseCommandInfo(name = "kit", permission = "")
public class KitCmd implements BaseCommand {

    private final KitData kitData = new KitData();

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            MessageUtil.sendUsageMessage(sender, "/kit [Название]");
            return true;
        }

        String sub = args[0].toLowerCase();

        if (List.of("create", "setinv", "give", "remove", "setcooldown", "resetcooldown", "resetallcooldown", "preview").contains(sub)) {
            return handleAdminCommand(sender, args);
        } else {
            return handlePlayerKit(sender, args[0]);
        }
    }

    private boolean handlePlayerKit(CommandSender sender, String kitName) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        String kitLower = kitName.toLowerCase();
        boolean isAdmin = player.hasPermission("gy-core.admin");

        if (!isAdmin && !player.hasPermission("gy-core.kits." + kitLower)) {
            MessageUtil.sendPermissionMessage(player);
            return true;
        }

        ItemStack[] kitItems = kitData.getKitItems(kitLower);
        if (kitItems == null) {
            MessageUtil.sendMessage(player, "Набор '&#30578C" + kitName + "&f' не найден.");
            return true;
        }

        if (!isAdmin) {
            long cooldown = kitData.getKitCooldown(kitLower);
            long lastUse = kitData.getLastUse(player.getUniqueId(), kitLower);
            long now = System.currentTimeMillis();

            if (cooldown > 0 && (now - lastUse) < cooldown * 1000) {
                long remaining = (cooldown * 1000) - (now - lastUse);
                AnimateGradientUtil.animateGradientFailTitleNoDelay(
                        player,
                        "#30578C",
                        "#DB5858",
                        "ɴᴏʀᴛʜ-ᴍᴄ",
                        "Задержка!",
                        1000
                );
                MessageUtil.sendMessage(player, "Подождите еще &#30578C" + MathUtil.formatTime(remaining) + "&f, прежде чем взять набор '&#30578C" + kitName + "&f'.");
                return true;
            }

            kitData.setLastUse(player.getUniqueId(), kitLower, now);
        }

        giveItems(player, kitItems);
        MessageUtil.sendMessage(player, "Вы получили набор '&#30578C" + kitName + "&f'.");
        AnimateGradientUtil.animateGradientTitleNoDelay(
                player,
                "#30578C",
                "#7495C1",
                "ɴᴏʀᴛʜ-ᴍᴄ",
                "Набор получен!",
                1000
        );
        return true;
    }

    private void giveItems(Player player, ItemStack[] kitItems) {
        Location dropLoc = player.getLocation();

        for (int slot = 0; slot < kitItems.length; slot++) {
            ItemStack item = kitItems[slot];
            if (item == null) continue;

            if (slot >= 36 && slot <= 39) {
                ItemStack current = player.getInventory().getItem(slot);
                if (current == null || current.getType().isAir()) {
                    player.getInventory().setItem(slot, item);
                    continue;
                }
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
                if (!left.isEmpty()) left.values().forEach(leftover -> player.getWorld().dropItemNaturally(dropLoc, leftover));
                continue;
            }

            if (slot == 40) {
                ItemStack off = player.getInventory().getItemInOffHand();
                if (off.getType().isAir()) {
                    player.getInventory().setItemInOffHand(item);
                    continue;
                }
                HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
                if (!left.isEmpty()) left.values().forEach(leftover -> player.getWorld().dropItemNaturally(dropLoc, leftover));
                continue;
            }

            HashMap<Integer, ItemStack> left = player.getInventory().addItem(item);
            if (!left.isEmpty()) left.values().forEach(leftover -> player.getWorld().dropItemNaturally(dropLoc, leftover));
        }
    }

    private boolean handleAdminCommand(CommandSender sender, String[] args) {

        if (!sender.hasPermission("gy-core.admin")) {
            MessageUtil.sendPermissionMessage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {

            case "create", "setinv" -> {
                if (args.length < 2) {
                    MessageUtil.sendUsageMessage(sender, "/kit " + sub + " [Кит]");
                    return true;
                }
                if (!(sender instanceof Player player)) return true;

                String kitLower = args[1].toLowerCase();
                kitData.saveKit(kitLower, player.getInventory().getContents());
                MessageUtil.sendMessage(sender, "Набор '&#30578C" + args[1] + "&f' " + (sub.equals("create") ? "создан." : "обновлён."));
            }

            case "preview" -> {
                if (!(sender instanceof Player player)) {
                    MessageUtil.sendMessage(sender, "§cТолько игроки могут использовать /kit preview!");
                    return true;
                }

                if (args.length != 2) {
                    MessageUtil.sendUsageMessage(sender, "/kit preview [Кит]");
                    return true;
                }

                String kitName = args[1].toLowerCase();
                ItemStack[] items = kitData.getKitItems(kitName);

                if (items == null) {
                    MessageUtil.sendMessage(player, "Набор '&#30578C" + kitName + "&f' не найден.");
                    return true;
                }

                boolean isAdmin = player.hasPermission("gy-core.admin");
                if (!isAdmin && !player.hasPermission("gy-core.kits." + kitName)) {
                    MessageUtil.sendPermissionMessage(player);
                    return true;
                }

                new KitPreviewGui().openPreview(player, kitName);
                MessageUtil.sendMessage(player, "Предпросмотр набора '&#30578C" + kitName + "&f'");
            }

            case "remove" -> {
                if (args.length < 2) {
                    MessageUtil.sendUsageMessage(sender, "/kit remove [Кит]");
                    return true;
                }
                String kitLower = args[1].toLowerCase();
                if (kitData.removeKit(kitLower)) {
                    MessageUtil.sendMessage(sender, "Набор '&#30578C" + args[1] + "&f' удалён.");
                } else {
                    MessageUtil.sendMessage(sender, "Набор '&#30578C" + args[1] + "&f' не найден.");
                }
            }

            case "give" -> {
                if (args.length != 3) {
                    MessageUtil.sendUsageMessage(sender, "/kit give [Кит] [Игрок]");
                    return true;
                }
                String kitLower = args[1].toLowerCase();
                Player target = Bukkit.getPlayer(args[2]);
                ItemStack[] items = kitData.getKitItems(kitLower);

                if (target == null) {
                    MessageUtil.sendUnknownPlayerMessage(sender, args[2]);
                    return true;
                }
                if (items == null) {
                    MessageUtil.sendMessage(sender, "Набор '&#30578C" + args[1] + "&f' не найден.");
                    return true;
                }

                giveItems(target, items);
                MessageUtil.sendMessage(sender, "Набор '&#30578C" + args[1] + "&f' выдан игроку '&#30578C" + target.getName() + "&f'.");
            }

            case "setcooldown" -> {
                if (args.length != 3) {
                    MessageUtil.sendUsageMessage(sender, "/kit setcooldown [Кит] [Секунды]");
                    return true;
                }
                try {
                    long seconds = Long.parseLong(args[2]);
                    long millis = seconds * 1000L;

                    kitData.setKitCooldown(args[1].toLowerCase(), seconds);
                    MessageUtil.sendMessage(sender, "Задержка для набора '&#30578C" + args[1] + "&f' установлена в &#30578C" + MathUtil.formatTime(millis));
                } catch (NumberFormatException e) {
                    MessageUtil.sendMessage(sender, "Укажи верный формат числа.");
                }
            }

            case "resetcooldown" -> {
                if (args.length != 3) {
                    MessageUtil.sendUsageMessage(sender, "/kit resetcooldown [Кит] [Игрок]");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    MessageUtil.sendUnknownPlayerMessage(sender, args[2]);
                    return true;
                }
                kitData.setLastUse(target.getUniqueId(), args[1].toLowerCase(), 0);
                MessageUtil.sendMessage(sender, "Задержка для набора '&#30578C" + args[1] + "&f' сброшена для игрока '&#30578C" + target.getName() + "&f'.");
            }

            case "resetallcooldown" -> {
                if (args.length != 1) {
                    MessageUtil.sendUsageMessage(sender, "/kit resetallcooldown");
                    return true;
                }
                kitData.resetAllCooldowns();
                MessageUtil.sendMessage(sender, "Все задержки для всех наборов и игроков сброшены.");
            }

            default -> MessageUtil.sendMessage(sender, "Команда не найдена.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        boolean isAdmin = sender.hasPermission("gy-core.admin");

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.addAll(kitData.getKitNames());

            if (isAdmin) {
                options.addAll(Arrays.asList(
                        "create", "setinv", "give", "remove",
                        "setcooldown", "resetcooldown",
                        "resetallcooldown", "preview"
                ));
            }

            return filterStartsWith(options, args[0]);
        }

        if (!isAdmin) return List.of();
        String sub = args[0].toLowerCase();

        if (args.length == 2 && List.of("give", "remove", "setcooldown", "resetcooldown", "preview").contains(sub)) {
            return filterStartsWith(kitData.getKitNames(), args[1]);
        }

        if (args.length == 3 && List.of("give", "resetcooldown").contains(sub)) {
            List<String> players = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
            return filterStartsWith(players, args[2]);
        }
        if (sub.equals("resetallcooldown")) return List.of();
        return List.of();
    }

    private List<String> filterStartsWith(List<String> list, String arg) {
        String lower = arg.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lower)).toList();
    }

}
