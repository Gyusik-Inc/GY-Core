package mc.core.event;

import mc.core.GY;
import mc.core.command.PluginsCommand;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.SpawnData;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Events implements Listener {

    public Events() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : GY.getInstance().getServer().getOnlinePlayers()) {
                    if (hasPotions(player)) {
                        checkPlayerPotions(player);
                    }
                }
            }
        }.runTaskTimer(GY.getInstance(), 0L, 20L);
    }

    private boolean hasPotions(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().contains("POTION")) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase().trim();
        if (command.equals("/pl") || command.equals("/plugins") || command.equals("/plugin")) {
            event.setCancelled(true);

            PluginsCommand pluginsCmd = new PluginsCommand();
            pluginsCmd.onCommand(event.getPlayer(), null, "plugins", new String[0]);
        }
    }

    @EventHandler
    public void onPickupPotion(PlayerPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (isPotion(item)) setPotionStackSize(item);
        e.getItem().setItemStack(item);
    }

    @EventHandler
    public void onInventoryAdd(InventoryPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (isPotion(item)) setPotionStackSize(item);
        e.getItem().setItemStack(item);
    }

    private void checkPlayerPotions(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isPotion(item)) {
                setPotionStackSize(item);
            }
        }
    }

    private boolean isPotion(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.contains("POTION");
    }

    private void setPotionStackSize(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setMaxStackSize(8);
            item.setItemMeta(meta);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Location spawn = SpawnData.getSpawn();
        if (spawn != null) {
            e.setRespawnLocation(spawn);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        Entity target = e.getEntity();
        double damage = e.getFinalDamage();
        spawnDamageText(target, damage, player);
    }

    private static final List<TextDisplay> activeDisplays = new ArrayList<>();

    private void spawnDamageText(Entity target, double damage, Player damager) {
        int side = Math.random() < 0.5 ? -1 : 1;
        Vector lookDir = damager.getLocation().getDirection().setY(0).normalize();
        Vector perp = new Vector(-lookDir.getZ(), 0, lookDir.getX()).normalize().multiply(side * 0.7);
        double height = target.getHeight() + (Math.random() - 0.5);
        Location loc = target.getLocation().clone().add(perp).add(0, height, 0);

        String color;
        if (damage < 1) color = MessageUtil.colorize("#87C68E");
        else if (damage < 3) color = MessageUtil.colorize("#C6AB87");
        else color = MessageUtil.colorize("#D97676");

        String formattedDamage = String.format("%.1f", damage);
        TextDisplay display = target.getWorld().spawn(loc, TextDisplay.class, d -> {
            d.setText(color + "-" + formattedDamage);
            d.setBillboard(Display.Billboard.CENTER);
            d.setAlignment(TextDisplay.TextAlignment.CENTER);
            d.setShadowed(true);
            d.setGlowing(true);
            d.setPersistent(false);
            d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        });

        activeDisplays.add(display);

        Bukkit.getScheduler().runTaskLater(GY.getInstance(), () -> {
            display.remove();
            activeDisplays.remove(display);
        }, 30L);
    }

    public static void onDisable() {
        for (TextDisplay display : activeDisplays) {
            if (!display.isDead()) display.remove();
        }
        activeDisplays.clear();
    }
}
