package mc.core.event;

import mc.core.GY;
import mc.core.command.PluginsCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

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


}
