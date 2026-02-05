package mc.core.pvp.antirelog;

import mc.core.basecommands.impl.player.GodCmd;
import mc.core.basecommands.impl.player.VanishCmd;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AntiRelogEvent implements Listener {

    private static final List<String> WHITELIST = List.of(
            "shop", "hub", "m", "msg", "tell", "helper"
    );

    private static final List<ItemCooldown> COOLDOWN_ITEMS = List.of(
            new ItemCooldown(Material.ENDER_PEARL, 15),
            new ItemCooldown(Material.CHORUS_FRUIT, 5),
            new ItemCooldown(Material.TOTEM_OF_UNDYING, 10),
            new ItemCooldown(Material.GOLDEN_APPLE, 3),
            new ItemCooldown(Material.ENCHANTED_GOLDEN_APPLE, 60)
    );

    private static final Set<Material> FOOD_ITEMS = Set.of(
            Material.GOLDEN_APPLE,
            Material.ENCHANTED_GOLDEN_APPLE
    );

    private static final Map<UUID, Map<Material, Long>> cooldowns = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker) ||
                !(e.getEntity() instanceof Player target)) return;

        if (attacker == target) return;
        if (VanishCmd.isVanished(attacker) || VanishCmd.isVanished(target)) {
            e.setCancelled(true);
            return;
        }

        if (GodCmd.isGod(attacker) || GodCmd.isGod(target)) {
            GodCmd.disableGod(attacker);
            GodCmd.disableGod(target);
        }

        target.setAllowFlight(false);
        attacker.setAllowFlight(false);
        AntiRelog.addPlayer(attacker, target);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (AntiRelog.isInPvp(player)) {
            player.setHealth(0);
            Bukkit.broadcast(MessageUtil.getGYString("Игрок &#30578C" + player.getName() + "&f покинул игру, во время боя."), "");
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        if (!AntiRelog.isInPvp(player)) return;
        if (player.hasPermission("gy-core.admin")) return;

        String msg = e.getMessage().toLowerCase();
        String cmd = msg.split(" ")[0].substring(1);

        if (WHITELIST.stream().noneMatch(cmd::equalsIgnoreCase)) {
            e.setCancelled(true);
            MessageUtil.sendMessage(player, "Команды запрещены во время боя.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    @EventHandler
    public void onItemEat(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        if (!AntiRelog.isInPvp(p)) return;
        if (p.hasPermission("gy-core.admin")) return;

        ItemStack item = e.getItem();
        if (!FOOD_ITEMS.contains(item.getType())) return;
        ItemCooldown cooldown = COOLDOWN_ITEMS.stream()
                .filter(c -> c.material == item.getType())
                .findFirst().orElse(null);
        if (cooldown == null) return;

        long now = System.currentTimeMillis();
        Map<Material, Long> playerCooldowns = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>());
        Long lastUse = playerCooldowns.get(item.getType());
        long delayMs = cooldown.delay * 1000L;

        if (lastUse != null && now < (lastUse + delayMs)) {
            e.setCancelled(true);
            long remainMs = (lastUse + delayMs) - now;
            MessageUtil.sendMessage(p, "Задержка: &#30578C" + MathUtil.formatTime(remainMs));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        playerCooldowns.put(item.getType(), now);
    }



    @EventHandler
    public void onItemUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!AntiRelog.isInPvp(p)) return;
        if (p.hasPermission("gy-core.admin")) return;

        if (e.getAction() != Action.RIGHT_CLICK_AIR &&
                e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (item == null) return;
        if (FOOD_ITEMS.contains(item.getType())) return;

        ItemCooldown cooldown = COOLDOWN_ITEMS.stream()
                .filter(c -> c.material == item.getType())
                .findFirst().orElse(null);
        if (cooldown == null) return;

        long now = System.currentTimeMillis();
        Map<Material, Long> playerCooldowns = cooldowns.computeIfAbsent(p.getUniqueId(), k -> new HashMap<>());
        Long lastUse = playerCooldowns.get(item.getType());
        long delayMs = cooldown.delay * 1000L;

        if (lastUse != null && now < (lastUse + delayMs)) {
            e.setCancelled(true);
            long remainMs = (lastUse + delayMs) - now;
            MessageUtil.sendMessage(p, "Задержка: &#30578C" + MathUtil.formatTime(remainMs));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        playerCooldowns.put(item.getType(), now);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1f);
    }

    public static void clearCooldowns(UUID uuid) {
        cooldowns.remove(uuid);
    }

    private record ItemCooldown(Material material, int delay) {}
}
