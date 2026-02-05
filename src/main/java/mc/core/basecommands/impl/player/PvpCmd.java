/* Decompiler 283ms, total 1403ms, lines 362 */
package mc.core.basecommands.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import mc.core.GY;
import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.pvp.pvp.PvpGuiMenu;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@BaseCommandInfo(
        name = "pvp",
        permission = "gy-core.pvp",
        cooldown = 30
)
public class PvpCmd implements BaseCommand {
    private static final List<UUID> pvpQueue = new ArrayList();
    private static final List<UUID> gearQueue = new ArrayList();
    private static final Map<UUID, Integer> queueTasks = new HashMap();
    private static final Map<UUID, Integer> gearQueueTasks = new HashMap();
    private static final Map<UUID, PvpCmd.ArmorSetType> playerArmorTypes = new HashMap();
    private static final Random random = new Random();

    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            player.openInventory(PvpGuiMenu.createPvpMenu(player));
            return true;
        } else {
            MessageUtil.sendMessage(sender, "§cТолько для игроков!");
            return true;
        }
    }

    public static int getPvpQueueSize() {
        return pvpQueue.size();
    }

    public static int getGearQueueSize() {
        return gearQueue.size();
    }

    public static PvpCmd.ArmorSetType getPlayerArmorSet(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] armor = new ItemStack[]{inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()};
        int diamondCount = 0;
        int netheriteCount = 0;
        int totalArmorCount = 0;
        ItemStack[] var6 = armor;
        int var7 = armor.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            ItemStack piece = var6[var8];
            if (piece != null && piece.getType() != Material.AIR) {
                ++totalArmorCount;
                Material type = piece.getType();
                if (type != Material.DIAMOND_HELMET && type != Material.DIAMOND_CHESTPLATE && type != Material.DIAMOND_LEGGINGS && type != Material.DIAMOND_BOOTS) {
                    if (type == Material.NETHERITE_HELMET || type == Material.NETHERITE_CHESTPLATE || type == Material.NETHERITE_LEGGINGS || type == Material.NETHERITE_BOOTS) {
                        ++netheriteCount;
                    }
                } else {
                    ++diamondCount;
                }
            }
        }

        if (totalArmorCount < 2) {
            return PvpCmd.ArmorSetType.NONE;
        } else if (netheriteCount >= diamondCount) {
            return netheriteCount >= 1 ? PvpCmd.ArmorSetType.NETHERITE : PvpCmd.ArmorSetType.NONE;
        } else {
            return diamondCount >= 1 ? PvpCmd.ArmorSetType.DIAMOND : PvpCmd.ArmorSetType.NONE;
        }
    }

    public static synchronized boolean isInAnyQueue(UUID playerId) {
        return isInQueue(playerId) || isInGearQueue(playerId);
    }

    public static synchronized boolean isInQueue(UUID playerId) {
        return pvpQueue.contains(playerId);
    }

    public static synchronized boolean isInGearQueue(UUID playerId) {
        return gearQueue.contains(playerId);
    }

    public static void handleMenuClick(Player player, int slot) {
        UUID playerId = player.getUniqueId();
        if (isInAnyQueue(playerId)) {
            removePlayerFromQueue(playerId);
            MessageUtil.sendMessage(player, "§cВы вышли из очереди PvP");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
            player.openInventory(PvpGuiMenu.createPvpMenu(player));
        } else {
            if (slot == 10) {
                addToQueue(playerId, player);
                MessageUtil.sendMessage(player, "§aВы встали в обычную очередь PvP");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 2.0F);
            } else if (slot == 16) {
                PvpCmd.ArmorSetType playerSet = getPlayerArmorSet(player);
                if (playerSet == PvpCmd.ArmorSetType.NONE) {
                    MessageUtil.sendMessage(player, "§cНужны минимум 2 предмета алмазной или незеритовой брони!");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 0.5F);
                    player.openInventory(PvpGuiMenu.createPvpMenu(player));
                    return;
                }

                addToGearQueue(playerId, player, playerSet);
                MessageUtil.sendMessage(player, "§aВы встали в очередь бронесетов (" + playerSet.name() + ")");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.5F);
            }

            player.openInventory(PvpGuiMenu.createPvpMenu(player));
        }
    }

    private static synchronized void addToQueue(UUID playerId, Player player) {
        if (!isInQueue(playerId)) {
            pvpQueue.add(playerId);
            int taskId = Bukkit.getScheduler().runTaskTimer(GY.getInstance(), PvpCmd::checkQueue, 20L, 20L).getTaskId();
            queueTasks.put(playerId, taskId);
            checkQueue();
        }

    }

    private static synchronized void addToGearQueue(UUID playerId, Player player, PvpCmd.ArmorSetType armorType) {
        if (!isInGearQueue(playerId)) {
            gearQueue.add(playerId);
            playerArmorTypes.put(playerId, armorType);
            int taskId = Bukkit.getScheduler().runTaskTimer(GY.getInstance(), PvpCmd::checkGearQueue, 20L, 20L).getTaskId();
            gearQueueTasks.put(playerId, taskId);
            checkGearQueue();
        }

    }

    private static synchronized void removeFromQueue(UUID playerId) {
        if (isInQueue(playerId)) {
            pvpQueue.remove(playerId);
            if (queueTasks.containsKey(playerId)) {
                Bukkit.getScheduler().cancelTask((Integer)queueTasks.remove(playerId));
            }

            broadcastQueueUpdate();
        }

    }

    private static synchronized void removeFromGearQueue(UUID playerId) {
        if (isInGearQueue(playerId)) {
            gearQueue.remove(playerId);
            playerArmorTypes.remove(playerId);
            if (gearQueueTasks.containsKey(playerId)) {
                Bukkit.getScheduler().cancelTask((Integer)gearQueueTasks.remove(playerId));
            }

            broadcastGearQueueUpdate();
        }

    }

    private static void checkQueue() {
        if (pvpQueue.size() >= 2) {
            UUID p1 = (UUID)pvpQueue.get(0);
            UUID p2 = (UUID)pvpQueue.get(1);
            Player player1 = Bukkit.getPlayer(p1);
            Player player2 = Bukkit.getPlayer(p2);
            if (player1 != null && player2 != null && player1.isOnline() && player2.isOnline()) {
                teleportToArena(player1, player2);
                removeFromQueue(p1);
                removeFromQueue(p2);
            }
        }

    }

    private static void checkGearQueue() {
        if (gearQueue.size() >= 2) {
            UUID p1 = (UUID)gearQueue.get(0);
            UUID p2 = (UUID)gearQueue.get(1);
            Player player1 = Bukkit.getPlayer(p1);
            Player player2 = Bukkit.getPlayer(p2);
            if (player1 != null && player2 != null && player1.isOnline() && player2.isOnline()) {
                PvpCmd.ArmorSetType set1 = (PvpCmd.ArmorSetType)playerArmorTypes.get(p1);
                PvpCmd.ArmorSetType set2 = (PvpCmd.ArmorSetType)playerArmorTypes.get(p2);
                if (set1 == set2 && set1 != PvpCmd.ArmorSetType.NONE && getPlayerArmorSet(player1) == set1 && getPlayerArmorSet(player2) == set2) {
                    teleportToArena(player1, player2);
                    removeFromGearQueue(p1);
                    removeFromGearQueue(p2);
                    return;
                }
            }

            if (player1 == null || !player1.isOnline() || getPlayerArmorSet(player1) != playerArmorTypes.get(p1)) {
                removeFromGearQueue(p1);
                if (player1 != null) {
                    MessageUtil.sendMessage(player1, "§cБроня изменилась! Сняты с очереди.");
                }
            }

            if (player2 == null || !player2.isOnline() || getPlayerArmorSet(player2) != playerArmorTypes.get(p2)) {
                removeFromGearQueue(p2);
                if (player2 != null) {
                    MessageUtil.sendMessage(player2, "§cБроня изменилась! Сняты с очереди.");
                }
            }
        }

    }

    private static void teleportToArena(Player p1, Player p2) {
        World world = p1.getWorld();
        int attempts = 0;

        Location loc1;
        Location loc2;
        double dist;
        do {
            Location center = getRandomLocation(world, 1000);
            dist = random.nextDouble() * 2.0D * 3.141592653589793D;
            double distance = 15.0D;
            loc1 = center.clone().add(Math.cos(dist) * distance / 2.0D, 0.0D, Math.sin(dist) * distance / 2.0D);
            loc1.setY((double)(world.getHighestBlockYAt(loc1.getBlockX(), loc1.getBlockZ()) + 1));
            loc2 = center.clone().add(Math.cos(dist + 3.141592653589793D) * distance / 2.0D, 0.0D, Math.sin(dist + 3.141592653589793D) * distance / 2.0D);
            loc2.setY((double)(world.getHighestBlockYAt(loc2.getBlockX(), loc2.getBlockZ()) + 1));
            ++attempts;
        } while(attempts < 100 && (!isSafeLocation(loc1) || !isSafeLocation(loc2) || loc1.distance(loc2) < 14.5D || loc1.distance(loc2) > 15.5D));

        loc1 = findSafeLocation(loc1);
        loc2 = findSafeLocation(loc2);
        p1.teleport(loc1);
        p2.teleport(loc2);
        p1.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, true, false));
        p2.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, true, false));
        dist = loc1.distance(loc2);
        String var10001 = p2.getName();
        MessageUtil.sendMessage(p1, "§6§lPvP ДУЭЛЬ §7| §f" + var10001 + " §7| §f" + String.format("%.1f", dist) + "м");
        var10001 = p1.getName();
        MessageUtil.sendMessage(p2, "§6§lPvP ДУЭЛЬ §7| §f" + var10001 + " §7| §f" + String.format("%.1f", dist) + "м");
        Iterator var12 = Bukkit.getOnlinePlayers().iterator();

        while(var12.hasNext()) {
            Player onlinePlayer = (Player)var12.next();
            if (onlinePlayer != p1 && onlinePlayer != p2) {
                String rivalName = onlinePlayer.equals(p1) ? p2.getName() : p1.getName();
                MessageUtil.sendMessage(onlinePlayer, "§6§lPvP §7| Ваш соперник §f" + rivalName);
            }
        }

    }

    private static Location getRandomLocation(World world, int radius) {
        int x = random.nextInt(radius * 2) - radius;
        int z = random.nextInt(radius * 2) - radius;
        return new Location(world, (double)x + 0.5D, (double)(world.getHighestBlockYAt(x, z) + 1), (double)z + 0.5D);
    }

    private static Location findSafeLocation(Location loc) {
        World world = loc.getWorld();

        for(int i = 0; i < 50; ++i) {
            if (isSafeLocation(loc)) {
                return loc;
            }

            double angle = random.nextDouble() * 2.0D * 3.141592653589793D;
            loc.add(Math.cos(angle) * 5.0D, 0.0D, Math.sin(angle) * 5.0D);
            loc.setY((double)(world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1));
        }

        return loc;
    }

    private static boolean isSafeLocation(Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        Block feet = world.getBlockAt(x, y - 1, z);
        return feet.getType().isSolid() && !feet.getType().toString().contains("WATER") && !feet.getType().toString().contains("LAVA") && world.getBlockAt(x, y, z).getType() == Material.AIR && world.getBlockAt(x, y + 1, z).getType() == Material.AIR;
    }

    private static void broadcastQueueUpdate() {
        Iterator var0 = pvpQueue.iterator();

        while(var0.hasNext()) {
            UUID id = (UUID)var0.next();
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                MessageUtil.sendActionBar(p, "§fОбычная: §e" + pvpQueue.size(), true);
            }
        }

    }

    private static void broadcastGearQueueUpdate() {
        Iterator var0 = gearQueue.iterator();

        while(var0.hasNext()) {
            UUID id = (UUID)var0.next();
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                MessageUtil.sendActionBar(p, "§fБронесеты: §e" + gearQueue.size(), true);
            }
        }

    }

    public static void checkArmorChange(Player player) {
        UUID playerId = player.getUniqueId();
        if (isInGearQueue(playerId)) {
            PvpCmd.ArmorSetType current = getPlayerArmorSet(player);
            PvpCmd.ArmorSetType expected = (PvpCmd.ArmorSetType)playerArmorTypes.get(playerId);
            if (current != expected) {
                removeFromGearQueue(playerId);
                MessageUtil.sendMessage(player, "§cВы изменили броню! Сняты с очереди бронесетов.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 0.5F);
            }
        }

    }

    public static synchronized void removePlayerFromQueue(UUID playerId) {
        removeFromQueue(playerId);
        removeFromGearQueue(playerId);
    }

    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    public static enum ArmorSetType {
        NETHERITE,
        DIAMOND,
        NONE;

        // $FF: synthetic method
        private static PvpCmd.ArmorSetType[] $values() {
            return new PvpCmd.ArmorSetType[]{NETHERITE, DIAMOND, NONE};
        }
    }
}