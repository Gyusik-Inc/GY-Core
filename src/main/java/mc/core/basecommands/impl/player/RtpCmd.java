package mc.core.basecommands.impl.player;

import mc.core.utilites.chat.MessageUtil;
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
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            handleRtp(player);
        } else if (args[0].equalsIgnoreCase("near")) {
            handleNear(player);
        } else {
            MessageUtil.sendMessage(player, "Использование: /rtp или /rtp near");
        }

        return true;
    }

    private void handleRtp(Player player) {
        teleportRandom(player, 500);
    }

    private void handleNear(Player player) {
        World world = player.getWorld();
        List<Player> worldPlayers = new ArrayList<>(world.getPlayers());
        worldPlayers.remove(player);

        if (worldPlayers.isEmpty()) {
            return;
        }

        Player target = null;
        Location targetLoc = null;
        int selectionAttempts = 0;
        final int maxSelectionAttempts = worldPlayers.size() * 2;

        do {
            if (selectionAttempts >= maxSelectionAttempts) {
                MessageUtil.sendMessage(player, "Нет подходящих игроков.");
                player.playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS,1, 1);
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
        int attempts = 0;
        final int maxAttempts = 100;
        Location safeLocation = null;

        do {
            double distance = 50 + random.nextDouble() * 45;
            double angle = random.nextDouble() * 2 * Math.PI;

            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;

            int x = (int) (targetLoc.getX() + offsetX);
            int z = (int) (targetLoc.getZ() + offsetZ);
            int y = world.getHighestBlockYAt(x, z);

            safeLocation = new Location(world, x + 0.5, y + 1, z + 0.5);
            attempts++;
        } while (!isSafeLocation(safeLocation) && attempts < maxAttempts);

        if (attempts >= maxAttempts || safeLocation == null) {
            MessageUtil.sendMessage(player, "Не найдено безопасное место рядом с игроком.");
            player.playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS,1, 1);
            return;
        }

        if (player.getLocation().distance(target.getLocation()) <= 30) {
            MessageUtil.sendMessage(player, "Нет подходящих игроков.");
            player.playSound(player.getLocation(),Sound.BLOCK_NOTE_BLOCK_BASS,1, 1);
            return;
        }

        player.teleport(safeLocation);
        MessageUtil.sendMessage(player, "Ближайший игрок &#30578C" + target.getName());
        player.playSound(player.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_HIT,1, 1);
    }

    private void teleportRandom(Player player, int radius) {
        World world = player.getWorld();
        int attempts = 0;
        final int maxAttempts = 100;

        Location safeLocation;
        do {
            int x = random.nextInt(radius * 2) - radius;
            int z = random.nextInt(radius * 2) - radius;
            int y = world.getHighestBlockYAt(x, z);
            safeLocation = new Location(world, x + 0.5, y + 1, z + 0.5);
            attempts++;
        } while (!isSafeLocation(safeLocation) && attempts < maxAttempts);

        if (attempts >= maxAttempts) {
            MessageUtil.sendMessage(player, "Не найдено безопасное место.");
            return;
        }

        player.teleport(safeLocation);
        MessageUtil.sendMessage(player, "Успешная телепортация. &7(" + safeLocation.getX() + " " + safeLocation.getY() + " " + safeLocation.getZ() + ")");
        player.playSound(player.getLocation(),Sound.BLOCK_AMETHYST_BLOCK_HIT,1, 1);
    }
    private boolean isSafeLocation(Location loc) {
        World world = loc.getWorld();
        Block feet = world.getBlockAt(loc.getBlockX(), (int) loc.getY() - 1, loc.getBlockZ());
        Block head = world.getBlockAt(loc.getBlockX(), (int) loc.getY(), loc.getBlockZ());
        return head.getType() == Material.AIR && feet.getType() != Material.LAVA;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("near");
        }
        return List.of();
    }
}
