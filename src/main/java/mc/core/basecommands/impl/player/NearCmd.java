package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 03.02.2026
 */

@BaseCommandInfo(name = "near", permission = "gy-core.near", cooldown = 60)
public class NearCmd implements BaseCommand {

    private static final String[] DIRECTIONS = {
            "⬅",
            "⬉",
            "⬆",
            "⬈",
            "➡",
            "⬊",
            "⬇",
            "⬋"
    };

    private static final double DEFAULT_RADIUS = 100.0;
    private static final double DONATOR_RADIUS = 150.0;
    private static final String DONATOR_PERMISSION = "gy-core.near.vip";
    private static final String NETHERITE_PERMISSION = "gy-core.near.netherite";

    public enum ArmorType {
        NETHERITE, DIAMOND, NONE
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        Location pLoc = player.getLocation();
        double maxRadius = player.hasPermission(DONATOR_PERMISSION) ? DONATOR_RADIUS : DEFAULT_RADIUS;
        boolean foundAny = false;

        MessageUtil.sendMessage(player, "&7Окружающие игроки: ");

        for (Player target : player.getWorld().getPlayers()) {
            if (target == player ||
                    target.getGameMode().equals(GameMode.SPECTATOR) ||
                    target.hasPermission("gy-core.near.ignore") ||
                    VanishCmd.isVanished(target)) {
                continue;
            }

            if (target.hasPermission(NETHERITE_PERMISSION) && !isWearingNetherite(target)) {
                continue;
            }

            Location tLoc = target.getLocation();
            double dx = tLoc.getX() - pLoc.getX();
            double dz = tLoc.getZ() - pLoc.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);

            if (distance > maxRadius) {
                continue;
            }

            float angle = (float) Math.toDegrees(Math.atan2(dz, dx) - Math.toRadians(pLoc.getYaw()));
            if (angle < 0) angle += 360;

            String direction = MessageUtil.colorize(DIRECTIONS[(int) ((angle + 22.5f) / 45f) % 8]);
            ArmorType armorType = getDominantArmor(target);
            String msg = switch (armorType) {
                case NETHERITE -> String.format("#574e57\uD83D\uDEE1 &#B1B7BE%s &8» &f%.0f блоков &7(%s)",
                        target.getName(), distance, direction);
                case DIAMOND -> String.format("#42968d\uD83D\uDEE1 &#B1B7BE%s &8» &f%.0f блоков &7(%s)",
                        target.getName(), distance, direction);
                default -> String.format("&#B1B7BE%s &8» &f%.0f блоков &7(%s)",
                        target.getName(), distance, direction);
            };

            player.sendMessage(MessageUtil.colorize(msg));
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
        int netheriteCount = 0;
        int diamondCount = 0;

        for (ItemStack item : armor) {
            if (item == null) continue;

            String material = item.getType().toString().toUpperCase();
            if (material.contains("NETHERITE")) {
                netheriteCount++;
            } else if (material.contains("DIAMOND")) {
                diamondCount++;
            }
        }

        if (netheriteCount > diamondCount || (netheriteCount == diamondCount && netheriteCount >= 2)) {
            return ArmorType.NETHERITE;
        } else if (diamondCount >= 2) {
            return ArmorType.DIAMOND;
        }
        return ArmorType.NONE;
    }

    private boolean isWearingNetherite(Player player) {
        return getDominantArmor(player) == ArmorType.NETHERITE;
    }
}
