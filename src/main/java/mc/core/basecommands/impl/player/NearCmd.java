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
            String msg;
            if (isWearingNetherite(target)) {
                msg = String.format("#3C2513%s &8» &f%.0f блоков &7(%s)",
                        target.getName(), distance, direction);
            } else {
                msg = String.format("#30578C%s &8» &f%.0f блоков &7(%s)",
                        target.getName(), distance, direction);
            }


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

    private boolean isWearingNetherite(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();

        for (ItemStack item : armor) {
            if (item == null || !item.getType().toString().contains("NETHERITE")) {
                return false;
            }
        }
        return true;
    }
}
