package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Gyusik
 * @since 03.02.2026
 */

@BaseCommandInfo(name = "near", permission = "gy-core.near", cooldown = 60)
public class NearCmd implements BaseCommand {

    private static final String[] DIRECTIONS = {
            "⬅", "⬉", "⬆", "⬈", "➡", "⬊", "⬇", "⬋"
    };

    private static final double DEFAULT_RADIUS = 100.0;
    private static final double DONATOR_RADIUS = 150.0;
    private static final String DONATOR_PERMISSION = "gy-core.near.vip";
    private static final String NETHERITE_PERMISSION = "gy-core.near.netherite";
    private static final String ADMIN_PERMISSION = "gy-core.admin";

    public enum ArmorType {
        NETHERITE, DIAMOND, NONE
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        Location pLoc = player.getLocation();
        double maxRadius = player.hasPermission(DONATOR_PERMISSION) ? DONATOR_RADIUS : DEFAULT_RADIUS;
        boolean foundAny = false;
        boolean isAdminViewer = player.hasPermission(ADMIN_PERMISSION);

        MessageUtil.sendMessage(player, "&7Окружающие игроки: ");

        for (Player target : player.getWorld().getPlayers()) {

            if (target == player ||
                    target.getGameMode().equals(GameMode.SPECTATOR) ||
                    VanishCmd.isVanished(target)) {
                continue;
            }

            // Админы видят всех, обычные игроки НЕ видят игнор
            if (!isAdminViewer && target.hasPermission("gy-core.near.ignore")) {
                continue;
            }

            // Проверка на нехеритовую броню только для обычных игроков
            if (!isAdminViewer && target.hasPermission(NETHERITE_PERMISSION) && !isWearingNetherite(target)) {
                continue;
            }

            Location tLoc = target.getLocation();
            double dx = tLoc.getX() - pLoc.getX();
            double dz = tLoc.getZ() - pLoc.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            // Радиус только для обычных игроков
            if (!isAdminViewer && distance > maxRadius) continue;

            float angle = (float) Math.toDegrees(Math.atan2(dz, dx) - Math.toRadians(pLoc.getYaw()));
            if (angle < 0) angle += 360;

            String direction = DIRECTIONS[(int) ((angle + 22.5f) / 45f) % 8];
            ArmorType armorType = getDominantArmor(target);
            boolean isIgnored = target.hasPermission("gy-core.near.ignore");

            // Компонент статуса скрытости (только для админов)
            Component hiddenMark = Component.empty();
            if (isAdminViewer && isIgnored) {
                hiddenMark = Component.text(" ")
                        .append(Component.text("Скрыт", NamedTextColor.GRAY));
            }

            // компонент сообщения
            Component msg = switch (armorType) {
                case NETHERITE -> Component.text()
                        .append(Component.text("\uD83D\uDEE1", TextColor.fromHexString("#574e57"))
                                .append(Component.text(" ", NamedTextColor.DARK_GRAY)))
                        .append(Component.text(target.getName(), TextColor.fromHexString("#B1B7BE")))
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(String.format("%.0f блоков", distance), NamedTextColor.WHITE))
                        .append(Component.text(" (", NamedTextColor.GRAY)
                                .append(Component.text(direction, NamedTextColor.WHITE))
                                .append(Component.text(")", NamedTextColor.GRAY)))
                        .append(getInventoryButton(player, target))
                        .append(hiddenMark)
                        .build();

                case DIAMOND -> Component.text()
                        .append(Component.text("\uD83D\uDEE1", TextColor.fromHexString("#42968d"))
                                .append(Component.text(" ", NamedTextColor.DARK_GRAY)))
                        .append(Component.text(target.getName(), TextColor.fromHexString("#B1B7BE")))
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(String.format("%.0f блоков", distance), NamedTextColor.WHITE))
                        .append(Component.text(" (", NamedTextColor.GRAY)
                                .append(Component.text(direction, NamedTextColor.WHITE))
                                .append(Component.text(")", NamedTextColor.GRAY)))
                        .append(getInventoryButton(player, target))
                        .append(hiddenMark)
                        .build();

                default -> Component.text()
                        .append(Component.text(target.getName(), TextColor.fromHexString("#B1B7BE")))
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text(String.format("%.0f блоков", distance), NamedTextColor.WHITE))
                        .append(Component.text(" (", NamedTextColor.GRAY)
                                .append(Component.text(direction, NamedTextColor.WHITE))
                                .append(Component.text(")", NamedTextColor.GRAY)))
                        .append(getInventoryButton(player, target))
                        .append(hiddenMark)
                        .build();
            };

            player.sendMessage(msg);
            foundAny = true;
        }

        if (!foundAny) {
            MessageUtil.sendMessage(player, "Игроки не найдены.");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    private ArmorType getDominantArmor(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        int netheriteCount = 0, diamondCount = 0;

        for (ItemStack item : armor) {
            if (item == null) continue;

            String material = item.getType().toString().toUpperCase();
            if (material.contains("NETHERITE")) netheriteCount++;
            else if (material.contains("DIAMOND")) diamondCount++;
        }

        if (netheriteCount > diamondCount || (netheriteCount == diamondCount && netheriteCount >= 2))
            return ArmorType.NETHERITE;
        else if (diamondCount >= 2) return ArmorType.DIAMOND;
        return ArmorType.NONE;
    }

    private boolean isWearingNetherite(Player player) {
        return getDominantArmor(player) == ArmorType.NETHERITE;
    }

    private Component getInventoryButton(Player viewer, Player target) {
        Component base = Component.empty();

        if (viewer.hasPermission("gy-core.invsee")) {
            base = Component.text(" [")
                    .color(TextColor.fromHexString("#B1B7BE"))
                    .append(Component.text("Инвентарь", TextColor.fromHexString("#30578C"))
                            .clickEvent(ClickEvent.runCommand("/invsee " + target.getName()))
                            .hoverEvent(HoverEvent.showText(
                                    Component.text("Открыть инвентарь ", NamedTextColor.GRAY)
                                            .append(Component.text(target.getName(), TextColor.fromHexString("#30578C")))
                            )))
                    .append(Component.text("]"))
                    .color(TextColor.fromHexString("#B1B7BE"));
        }

        return base;
    }
}
