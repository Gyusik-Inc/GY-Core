package mc.core.basecommands.impl.player;

import mc.core.GY;
import mc.core.basecommands.impl.world.RtpFallProtection;
import mc.core.utilites.chat.AnimateGradientUtil;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RtpCmd implements TabExecutor {

    private final Random random = new Random();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            handleRtp(player);
        } else if (args[0].equalsIgnoreCase("near")) {
            handleNear(player);
        } else {
            MessageUtil.sendUsageMessage(sender, "/rtp, /rtp near");
        }

        return true;
    }

    private void handleRtp(Player player) {
        World world = player.getWorld();
        Location safeLocation = findSafeLocation(world, 0, 0, 500);

        if (safeLocation == null) {
            MessageUtil.sendMessage(player, "Не найдено безопасное место в радиусе 500 блоков.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        AnimateGradientUtil.animateGradientTitleNoDelay(
                player,
                "#30578C",
                "#7495C1",
                "ɴᴏʀᴛʜ-ᴍᴄ",
                "Успешная телепортация!",
                1000
        );

        Location finalSafeLocation = safeLocation;
        Bukkit.getScheduler().runTaskLater(GY.getInstance(), () -> {
            teleportWithSkyFall(player, finalSafeLocation);
            MessageUtil.sendMessage(player,
                    "Успешная телепортация! §7(" +
                            (int) finalSafeLocation.getX() + ", " +
                            (int) finalSafeLocation.getY() + ", " +
                            (int) finalSafeLocation.getZ() + ")");
            player.playSound(finalSafeLocation, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        }, 1L);
    }

    private void handleNear(Player player) {
        World world = player.getWorld();
        List<Player> worldPlayers = new ArrayList<>(world.getPlayers());
        worldPlayers.remove(player);

        if (worldPlayers.isEmpty()) {
            MessageUtil.sendMessage(player, "В этом мире нет других игроков.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        Player target = null;
        Location targetLoc = null;
        int selectionAttempts = 0;
        final int maxSelectionAttempts = worldPlayers.size() * 2;

        do {
            if (selectionAttempts >= maxSelectionAttempts) {
                MessageUtil.sendMessage(player, "Нет подходящих игроков поблизости.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                return;
            }

            target = worldPlayers.get(random.nextInt(worldPlayers.size()));
            targetLoc = target.getLocation();
            Location playerLoc = player.getLocation();

            if (playerLoc.distance(targetLoc) > 10) {
                break;
            }

            selectionAttempts++;
        } while (true);

        Location safeLocation = findSafeNearLocation(world, targetLoc, 50, 95);
        if (safeLocation == null) {
            MessageUtil.sendMessage(player, "Не найдено безопасное место рядом с игроком.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        if (distance2D(player.getLocation(), target.getLocation()) <= 30) {
            MessageUtil.sendMessage(player, "Игрок слишком близко.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        AnimateGradientUtil.animateGradientTitle(
                player,
                "#30578C",
                "#7495C1",
                "ɴᴏʀᴛʜ-ᴍᴄ",
                "Телепорт к игроку!",
                500
        );

        Location finalSafeLocation = safeLocation;
        Player finalTarget = target;
        Bukkit.getScheduler().runTaskLater(GY.getInstance(), () -> {
            teleportWithSkyFall(player, finalSafeLocation);

            MessageUtil.sendMessage(player,
                    "Ближайший игрок §f&#30578C" + finalTarget.getName() +
                            " §7(" + (int) finalSafeLocation.getX() + ", " + (int) finalSafeLocation.getY() + ", " + (int) finalSafeLocation.getZ() + ")");
            player.playSound(finalSafeLocation, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
        }, 10L);
    }

    private Location findSafeLocation(World world, int centerX, int centerZ, int radius) {
        int attempts = 0;
        final int maxAttempts = 200;

        do {
            int x = centerX + random.nextInt(radius * 2) - radius;
            int z = centerZ + random.nextInt(radius * 2) - radius;

            Location candidate = findSafeLanding(world, x, z);
            if (candidate != null) {
                return candidate;
            }
            attempts++;
        } while (attempts < maxAttempts);

        return null;
    }

    private Location findSafeNearLocation(World world, Location center, double minRadius, double maxRadius) {
        int attempts = 0;
        final int maxAttempts = 100;

        do {
            double distance = minRadius + random.nextDouble() * (maxRadius - minRadius);
            double angle = random.nextDouble() * 2 * Math.PI;

            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;

            int x = (int) (center.getX() + offsetX);
            int z = (int) (center.getZ() + offsetZ);

            Location candidate = findSafeLanding(world, x, z);
            if (candidate != null) {
                return candidate;
            }
            attempts++;
        } while (attempts < maxAttempts);

        return null;
    }

    private Location findSafeLanding(World world, int x, int z) {
        if (world == null) return null;

        int surfaceY = world.getHighestBlockYAt(x, z);
        Location landing = new Location(world, x + 0.5, surfaceY + 1, z + 0.5);

        // ✅ Проверяем посадочную точку на поверхности
        Block feet = world.getBlockAt(landing.getBlockX(), surfaceY, landing.getBlockZ());   // Под ноги
        Block head = world.getBlockAt(landing.getBlockX(), surfaceY + 1, landing.getBlockZ()); // Голова

        // ❌ Вода/лава под ногами = НЕТ
        if (isLiquid(feet.getType())) return null;

        // ❌ Не твердый блок под ногами = НЕТ
        if (!feet.getType().isSolid()) return null;

        // ❌ Голова не в воздухе = НЕТ
        if (head.getType() != Material.AIR) return null;

        // ❌ Вода рядом = НЕТ
        if (hasWaterNearby(world, landing.getBlockX(), surfaceY, landing.getBlockZ())) return null;

        return landing;
    }

    private boolean isLiquid(Material material) {
        return material == Material.WATER ||
                material == Material.LAVA;
    }

    private boolean hasWaterNearby(World world, int x, int y, int z) {
        // Проверяем 3x3 вокруг под ногами на воду
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                Block nearby = world.getBlockAt(x + dx, y, z + dz);
                if (nearby.getType() == Material.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    private void teleportWithSkyFall(Player player, Location groundLoc) {
        Location skyLoc = new Location(
                groundLoc.getWorld(),
                groundLoc.getX(),
                200,
                groundLoc.getZ(),
                groundLoc.getYaw(),
                groundLoc.getPitch()
        );

        RtpFallProtection.give(player);
        player.teleport(skyLoc);
        player.setFallDistance(0);
        player.setVelocity(player.getVelocity().setY(0));
    }

    private double distance2D(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("near");
        }
        return List.of();
    }
}
