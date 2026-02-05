package mc.core.pvp.command;

import mc.core.GY;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 05.02.2026
 */

public class PvpMenuEvent implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity var3 = event.getWhoClicked();
        if (var3 instanceof Player player) {
            if (event.getView().getTitle().equals(PvpGuiMenu.getTITLE())) {
                event.setCancelled(true);
                int slot = event.getRawSlot();
                if ((slot == 10 || slot == 16) && (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT)) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.0F);
                    PvpCmd.handleMenuClick(player, slot);
                }

            }
        }
    }

    @EventHandler
    public void onArmorChange(InventoryClickEvent event) {
        HumanEntity var3 = event.getWhoClicked();
        if (var3 instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(GY.getInstance(), () -> {
                PvpCmd.checkArmorChange(player);
            }, 1L);
        }
    }
}