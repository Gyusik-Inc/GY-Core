package mc.core.pvp.command;

import java.util.*;
import mc.core.GY;
import mc.core.basecommands.base.*;
import mc.core.pvp.antirelog.AntiRelog;
import mc.core.utilites.chat.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@BaseCommandInfo(name = "pvp", permission = "gy-core.pvp", cooldown = 30)
public class PvpCmd implements BaseCommand {
    private static final List<UUID> pvpQueue = new ArrayList<>();
    private static final List<UUID> gearQueue = new ArrayList<>();
    private static final Map<UUID, Integer> queueTasks = new HashMap<>();
    private static final Map<UUID, Integer> gearTasks = new HashMap<>();
    private static final Map<UUID, ArmorSetType> armorTypes = new HashMap<>();
    private static final Random rand = new Random();

    public boolean execute(CommandSender s, String l, String[] a) {
        if (s instanceof Player p) p.openInventory(PvpGuiMenu.createPvpMenu(p));
        return true;
    }

    public static int getPvpQueueSize() { return pvpQueue.size(); }
    public static int getGearQueueSize() { return gearQueue.size(); }

    public static ArmorSetType getPlayerArmorSet(Player p) {
        ItemStack[] armor = {p.getInventory().getHelmet(), p.getInventory().getChestplate(),
                p.getInventory().getLeggings(), p.getInventory().getBoots()};
        int diamond = 0, netherite = 0;

        for (ItemStack piece : armor) {
            if (piece != null && piece.getType() != Material.AIR) {
                Material m = piece.getType();
                if (isDiamond(m)) diamond++;
                else if (isNetherite(m)) netherite++;
            }
        }

        if (totalArmor(armor) < 2) return ArmorSetType.NONE;
        return netherite >= diamond && netherite >= 1 ? ArmorSetType.NETHERITE :
                diamond >= 1 ? ArmorSetType.DIAMOND : ArmorSetType.NONE;
    }

    private static int totalArmor(ItemStack[] armor) {
        return (int) Arrays.stream(armor).filter(i -> i != null && i.getType() != Material.AIR).count();
    }

    private static boolean isDiamond(Material m) {
        return m == Material.DIAMOND_HELMET || m == Material.DIAMOND_CHESTPLATE ||
                m == Material.DIAMOND_LEGGINGS || m == Material.DIAMOND_BOOTS;
    }

    private static boolean isNetherite(Material m) {
        return m == Material.NETHERITE_HELMET || m == Material.NETHERITE_CHESTPLATE ||
                m == Material.NETHERITE_LEGGINGS || m == Material.NETHERITE_BOOTS;
    }

    public static synchronized boolean isInAnyQueue(UUID id) {
        return pvpQueue.contains(id) || gearQueue.contains(id);
    }

    public static void handleMenuClick(Player p, int slot) {
        UUID id = p.getUniqueId();
        if (isInAnyQueue(id)) {
            removePlayerFromQueue(id);
            MessageUtil.sendMessage(p, "Вы вышли из очереди PvP");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        } else if (slot == 10) {
            addToQueue(id, p);
            Bukkit.broadcast(MessageUtil.getGYString("Игрок &#30578C" + p.getName() + "&f ищет противника, &7обычного&f уровня. &7(/pvp)"), "");
            MessageUtil.sendMessage(p, "Поиск &7обычного &fсоперника.");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
        } else if (slot == 16) {
            ArmorSetType set = getPlayerArmorSet(p);
            if (set == ArmorSetType.NONE) {
                MessageUtil.sendMessage(p, "Ваше снаряжение не подходит.");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
                return;
            }
            addToGearQueue(id, p, set);
            Bukkit.broadcast(MessageUtil.getGYString("Игрок &#30578C" + p.getName() + "&f ищет противника, &#B1B7BEэлитного&f уровня! &7(/pvp)"), "");
            MessageUtil.sendMessage(p, "Поиск &#B1B7BEэлитного &fсоперника: &#30578C(" + set + ")");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.5f);
        }
        p.openInventory(PvpGuiMenu.createPvpMenu(p));
    }

    private static synchronized void addToQueue(UUID id, Player p) {
        if (pvpQueue.contains(id)) return;
        pvpQueue.add(id);
        queueTasks.put(id, Bukkit.getScheduler().runTaskTimer(GY.getInstance(), PvpCmd::checkQueue, 20L, 20L).getTaskId());
        checkQueue();
    }

    private static synchronized void addToGearQueue(UUID id, Player p, ArmorSetType type) {
        if (gearQueue.contains(id)) return;
        gearQueue.add(id);
        armorTypes.put(id, type);
        gearTasks.put(id, Bukkit.getScheduler().runTaskTimer(GY.getInstance(), PvpCmd::checkGearQueue, 20L, 20L).getTaskId());
        checkGearQueue();
    }

    public static synchronized void removePlayerFromQueue(UUID id) {
        if (pvpQueue.remove(id)) cancelTask(queueTasks.remove(id));
        if (gearQueue.remove(id)) {
            armorTypes.remove(id);
            cancelTask(gearTasks.remove(id));
        }
        broadcastQueues();
    }

    private static void cancelTask(Integer taskId) {
        if (taskId != null) Bukkit.getScheduler().cancelTask(taskId);
    }

    private static void checkQueue() {
        if (pvpQueue.size() < 2) return;
        Player[] players = getOnlinePlayers(pvpQueue.get(0), pvpQueue.get(1));
        if (players[0] != null && players[1] != null) {
            teleportToArena(players[0], players[1]);
            removePlayerFromQueue(pvpQueue.removeFirst());
            removePlayerFromQueue(pvpQueue.removeFirst());
        }
    }

    private static void checkGearQueue() {
        if (gearQueue.size() < 2) return;
        UUID id1 = gearQueue.get(0), id2 = gearQueue.get(1);
        Player[] players = getOnlinePlayers(id1, id2);
        if (players[0] != null && players[1] != null) {
            ArmorSetType set1 = armorTypes.get(id1), set2 = armorTypes.get(id2);
            if (set1 == set2 && set1 != ArmorSetType.NONE &&
                    getPlayerArmorSet(players[0]) == set1 && getPlayerArmorSet(players[1]) == set2) {
                teleportToArena(players[0], players[1]);
                removePlayerFromQueue(gearQueue.remove(0));
                removePlayerFromQueue(gearQueue.remove(0));
                return;
            }
        }
        validateGearQueue();
    }

    private static Player[] getOnlinePlayers(UUID... ids) {
        return Arrays.stream(ids).map(Bukkit::getPlayer)
                .filter(Objects::nonNull).filter(Player::isOnline).toArray(Player[]::new);
    }

    private static void validateGearQueue() {
        gearQueue.removeIf(id -> {
            Player p = Bukkit.getPlayer(id);
            if (p == null || !p.isOnline() || getPlayerArmorSet(p) != armorTypes.get(id)) {
                if (p != null) MessageUtil.sendMessage(p, "Вы изменили броню! Выход из очереди.");
                removePlayerFromQueue(id);
                return true;
            }
            return false;
        });
    }

    private static void teleportToArena(Player p1, Player p2) {
        Location[] locs = findArenaLocs(p1.getWorld());
        p1.teleport(locs[0]); p2.teleport(locs[1]);
        AntiRelog.addPlayer(p1, p2);

        applyEffects(p1, p2);
        double dist = locs[0].distance(locs[1]);
        MessageUtil.sendMessage(p1, "Противник: &#30578C" + p2.getName() + "&f, расстояние: &#30578C" + String.format("%.1f", dist) + "м.");
        MessageUtil.sendMessage(p2, "Противник: &#30578C" + p1.getName() + "&f, расстояние: &#30578C" + String.format("%.1f", dist) + "м.");

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p != p1 && p != p2)
                .forEach(p -> {
                    String rival = p.equals(p1) ? p2.getName() : p1.getName();
                    MessageUtil.sendMessage(p, "Ваш соперник: &#30578C" + rival);
                    AnimateGradientUtil.animateGradientTitle(p, "#30578C", "#7495C1", "ɴᴏʀᴛʜ-ᴍᴄ", "Соперник: " + rival, 500);
                });
    }

    private static Location[] findArenaLocs(World w) {
        for (int i = 0; i < 100; i++) {
            Location center = getRandomLocation(w, 1000);
            double angle = rand.nextDouble() * Math.PI * 2;
            double dist = 15;
            Location loc1 = center.clone().add(Math.cos(angle) * dist / 2, 0, Math.sin(angle) * dist / 2);
            loc1.setY(w.getHighestBlockYAt(loc1.getBlockX(), loc1.getBlockZ()) + 1);
            Location loc2 = center.clone().add(Math.cos(angle + Math.PI) * dist / 2, 0, Math.sin(angle + Math.PI) * dist / 2);
            loc2.setY(w.getHighestBlockYAt(loc2.getBlockX(), loc2.getBlockZ()) + 1);

            if (isSafeLocation(loc1) && isSafeLocation(loc2) &&
                    loc1.distance(loc2) >= 14.5 && loc1.distance(loc2) <= 15.5) {
                return new Location[]{findSafeLocation(loc1), findSafeLocation(loc2)};
            }
        }
        return new Location[]{findSafeLocation(getRandomLocation(w, 1000)),
                findSafeLocation(getRandomLocation(w, 1000))};
    }

    private static void applyEffects(Player... players) {
        for (Player p : players)
            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 200, 0, true, false));
    }

    private static Location getRandomLocation(World w, int r) {
        int x = rand.nextInt(r * 2) - r, z = rand.nextInt(r * 2) - r;
        return new Location(w, x + 0.5, w.getHighestBlockYAt(x, z) + 1, z + 0.5);
    }

    private static Location findSafeLocation(Location loc) {
        for (int i = 0; i < 50; i++) {
            if (isSafeLocation(loc)) return loc;
            double angle = rand.nextDouble() * Math.PI * 2;
            loc.add(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);
            loc.setY(loc.getWorld().getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()) + 1);
        }
        return loc;
    }

    private static boolean isSafeLocation(Location loc) {
        World w = loc.getWorld();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
        Block feet = w.getBlockAt(x, y - 1, z);
        return feet.getType().isSolid() && !feet.getType().toString().contains("WATER")
                && !feet.getType().toString().contains("LAVA")
                && w.getBlockAt(x, y, z).getType() == Material.AIR
                && w.getBlockAt(x, y + 1, z).getType() == Material.AIR;
    }

    private static void broadcastQueues() {
        pvpQueue.forEach(id -> {
            Player p = Bukkit.getPlayer(id);
            if (p != null) MessageUtil.sendActionBar(p, "&7Обычная: &7" + pvpQueue.size(), true);
        });
        gearQueue.forEach(id -> {
            Player p = Bukkit.getPlayer(id);
            if (p != null) MessageUtil.sendActionBar(p, "&#B1B7BEЭлитная: &#30578C" + gearQueue.size(), true);
        });
    }

    public static void checkArmorChange(Player p) {
        UUID id = p.getUniqueId();
        if (gearQueue.contains(id) && getPlayerArmorSet(p) != armorTypes.get(id)) {
            removePlayerFromQueue(id);
            MessageUtil.sendMessage(p, "Вы изменили броню! Выход из очереди.");
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }

    public List<String> tabComplete(CommandSender s, String a, String[] args) { return List.of(); }

    public enum ArmorSetType { NETHERITE, DIAMOND, NONE }
}
