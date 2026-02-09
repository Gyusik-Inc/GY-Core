package mc.core.basecommands.impl.player;

import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FixCmd implements TabExecutor {

    private static final long FIX_HAND_COOLDOWN = 30_000L;
    private static final long FIX_ALL_COOLDOWN = 60_000L;

    private final Map<UUID, Long> fixHandCooldowns = new HashMap<>();
    private final Map<UUID, Long> fixAllCooldowns = new HashMap<>();

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("gy-core.fix")) {
                MessageUtil.sendPermissionMessage(player);
                return true;
            }
            handleFixHand(player);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            if (!player.hasPermission("gy-core.fix.other")) {
                MessageUtil.sendPermissionMessage(player);
                return true;
            }
            handleFixAll(player, player, false);
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("all")) {
            if (!player.hasPermission("gy-core.fix.admin")) {
                MessageUtil.sendPermissionMessage(player);
                return true;
            }
            Player target = player.getServer().getPlayerExact(args[1]);
            if (target == null) {
                MessageUtil.sendUnknownPlayerMessage(sender, args[1]);
                return true;
            }
            handleFixAll(player, target, true);
            return true;
        }

        MessageUtil.sendMessage(player, "Использование: /fix, /fix all");
        return true;
    }

    private void handleFixHand(Player player) {
        UUID uuid = player.getUniqueId();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            MessageUtil.sendMessage(player, "Этот предмет нельзя починить");
            return;
        }

        if (!(item.getItemMeta() instanceof Damageable dmg)) {
            MessageUtil.sendMessage(player, "Этот предмет нельзя починить");
            return;
        }

        if (dmg.getDamage() == 0) {
            MessageUtil.sendMessage(player, "Этот предмет нельзя починить");
            return;
        }

        long now = System.currentTimeMillis();
        long lastUse = fixHandCooldowns.getOrDefault(uuid, 0L);
        long left = FIX_HAND_COOLDOWN - (now - lastUse);

        if (left > 0 && !player.hasPermission("gy-core.fix.admin")) {
            long sec = left / 1000;
            MessageUtil.sendMessage(player, "Подождите &#30578C" + sec + " сек. перед следующим использованием.");
            return;
        }

        dmg.setDamage(0);
        item.setItemMeta(dmg);

        fixHandCooldowns.put(uuid, now);
        MessageUtil.sendMessage(player, "Предмет успешно починен.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
    }

    private void handleFixAll(Player executor, Player target, boolean isAdmin) {
        UUID uuid = executor.getUniqueId();
        long now = System.currentTimeMillis();

        if (!isAdmin) {
            long lastUse = fixAllCooldowns.getOrDefault(uuid, 0L);
            long left = FIX_ALL_COOLDOWN - (now - lastUse);
            if (left > 0) {
                long sec = left / 1000;
                MessageUtil.sendMessage(executor, "Подождите &#30578C" + sec + " сек. перед следующим использованием.");
                return;
            }
        }

        PlayerInventory inv = target.getInventory();
        boolean repairedSomething = false;

        for (int i = 0; i < inv.getSize(); i++) {
            if (repairItem(inv.getItem(i))) {
                repairedSomething = true;
            }
        }

        ItemStack[] armor = inv.getArmorContents();
        for (ItemStack itemStack : armor) {
            if (repairItem(itemStack)) {
                repairedSomething = true;
            }
        }
        inv.setArmorContents(armor);

        ItemStack off = inv.getItemInOffHand();
        if (repairItem(off)) {
            repairedSomething = true;
        }
        inv.setItemInOffHand(off);

        if (!isAdmin && !repairedSomething) {
            MessageUtil.sendMessage(executor, "Вещи для починки не найдены.");
            return;
        }

        if (!isAdmin) {
            fixAllCooldowns.put(uuid, now);
        }

        if (executor.equals(target)) {
            MessageUtil.sendMessage(executor, "Весь ваш инвентарь был починен.");
            executor.playSound(executor.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
        } else {
            MessageUtil.sendMessage(executor, "Вы починили инвентарь игрока &#30578C" + target.getName());
            MessageUtil.sendMessage(target, "Ваш инвентарь был починен игроком &#30578C" + executor.getName());
            target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
        }
    }

    private boolean repairItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (!(item.getItemMeta() instanceof Damageable dmg)) return false;
        if (dmg.getDamage() == 0) return false;

        dmg.setDamage(0);
        item.setItemMeta(dmg);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player)) {
            return List.of();
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return List.of("all").stream()
                    .filter(s -> s.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("gy-core.fix.admin")) {
                return List.of();
            }
            String prefix = args[1].toLowerCase();
            return sender.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
