package mc.core.event;

import com.viaversion.viaversion.api.Via;
import lombok.Getter;
import mc.core.GY;
import mc.core.command.PluginsCommand;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.PlayerData;
import mc.core.utilites.data.SpawnData;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.*;
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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Events implements Listener {
    @Getter
    private static Events instance;

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
        instance = this;
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
        PlayerData data = new PlayerData(player.getUniqueId());

        if (data.isDamageTextEnabled()) {
            spawnDamageText(target, damage, player);
        }
    }

    private final Set<Entity> activeDisplays = ConcurrentHashMap.newKeySet();

    private void spawnDamageText(Entity target, double damage, Player damager) {
        int protocolVersion = Via.getAPI().getPlayerVersion(damager.getUniqueId());
        boolean useTextDisplay = protocolVersion >= 762;

        int side = Math.random() < 0.5 ? -1 : 1;
        Vector lookDir = damager.getLocation().getDirection().setY(0).normalize();
        Vector perp = new Vector(-lookDir.getZ(), 0, lookDir.getX()).normalize().multiply(side * 0.7);

        double heightOffset = target.getHeight() + (Math.random() - 0.5) * 0.4;
        Location spawnLoc = target.getLocation().clone().add(perp).add(0, heightOffset, 0);
        double currentHealth = (target instanceof LivingEntity le) ? le.getHealth() : 20.0;
        double healthPercent = (damage / currentHealth) * 100.0;

        String color;
        if (healthPercent < 5) {
            color = MessageUtil.colorize("#87C68E");
        } else if (healthPercent < 15) {
            color = MessageUtil.colorize("#C6AB87");
        } else {
            color = MessageUtil.colorize("#D97676");
        }

        String formattedDamage = String.format("%.1f", damage);
        String text = color + "-" + formattedDamage;

        Entity displayEntity;

        if (useTextDisplay) {
            displayEntity = target.getWorld().spawn(spawnLoc, TextDisplay.class, d -> {
                d.setText(text);
                d.setBillboard(Display.Billboard.CENTER);
                d.setAlignment(TextDisplay.TextAlignment.CENTER);
                d.setShadowed(true);
                d.setGlowing(true);
                d.setPersistent(false);
                d.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                d.setSeeThrough(true);
                d.setDisplayWidth(1.2f);
                d.setDisplayHeight(0.3f);
            });
        } else {
        Location farLoc = spawnLoc.clone().add(0, 200, 0);

        displayEntity = target.getWorld().spawnEntity(farLoc, EntityType.ARMOR_STAND);
        ArmorStand as = (ArmorStand) displayEntity;
        as.setVisible(false);
        as.setCustomName(text);
        as.setCustomNameVisible(true);
        as.setMarker(true);
        as.setGravity(false);
        as.setInvulnerable(true);
        as.setSmall(true);
        as.setCollidable(false);
        as.setSilent(true);
        as.teleport(spawnLoc);
    }

        activeDisplays.add(displayEntity);

        Bukkit.getScheduler().runTaskLater(GY.getInstance(), () -> {
            if (!displayEntity.isDead()) {
                displayEntity.remove();
            }
            activeDisplays.remove(displayEntity);
        }, 30L);
    }


    public void onDisable() {
        for (Entity entity : activeDisplays) {
            if (entity != null && !entity.isDead()) {
                entity.remove();
            }
        }
        activeDisplays.clear();
    }
}
