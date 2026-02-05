package mc.core.basecommands.impl;

import mc.core.GY;
import mc.core.basecommands.impl.player.*;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EventCommand implements Listener {
    private final HealCmd healCmd;
    private final GodCmd godCmd;

    public EventCommand() {
        this.healCmd = new HealCmd();
        this.godCmd = new GodCmd();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage().toLowerCase();
        if (message.startsWith("/heal")) {
            e.setCancelled(true);
            String[] args = message.split(" ");
            String[] cmdArgs = args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0];
            healCmd.execute(e.getPlayer(), "heal", cmdArgs);
        }
        if (message.startsWith("/god")) {
            e.setCancelled(true);
            String[] args = message.split(" ");
            String[] cmdArgs = args.length > 1 ? java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0];
            godCmd.execute(e.getPlayer(), "god", cmdArgs);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInvseeClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;

        String title = e.getView().getTitle();
        if (!title.startsWith("Инвентарь ")) return;

        Player target = Bukkit.getPlayerExact(title.substring(9));
        if (target == null || !target.isOnline()) return;

        int slot = e.getRawSlot();
        Inventory invsee = e.getView().getTopInventory();

        // Блокируем изменение панелей
        if ((slot >= 36 && slot <= 44) || (slot >= 50 && slot <= 53)) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(true); // Отменяем стандартный клик

        ItemStack cursor = e.getCursor();
        ItemStack current = e.getCurrentItem();

        // 1. Основной инвентарь (0–35)
        if (slot >= 0 && slot < 36) {
            target.getInventory().setItem(slot, cursor);
            invsee.setItem(slot, cursor);
        }

        // 2. Броня (45–48)
        else if (slot >= 45 && slot <= 48) {
            switch (slot) {
                case 45 -> target.getInventory().setBoots(cursor);
                case 46 -> target.getInventory().setLeggings(cursor);
                case 47 -> target.getInventory().setChestplate(cursor);
                case 48 -> target.getInventory().setHelmet(cursor);
            }
            invsee.setItem(slot, cursor);
        }

        // 3. Оффхенд (49)
        else if (slot == 49) {
            target.getInventory().setItemInOffHand(cursor);
            invsee.setItem(slot, cursor);
        }

        // 4. Обновляем весь invsee после изменения
        InvseeCmd.updateInvsee(invsee, target);
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TpaCmd.teleportTasks.remove(e.getPlayer().getUniqueId());
        TpaCmd.removeRequest(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        TpCmd.updateLastLocation(e.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && GodCmd.isGod(player)) {
            e.setCancelled(true);
            MessageUtil.sendActionBar(player, "Запрещено в &#30578C/god", true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (GodCmd.isGod(e.getPlayer())) {
            e.setCancelled(true);
            MessageUtil.sendActionBar(e.getPlayer(), "Запрещено в &#30578C/god", true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (GodCmd.isGod(e.getPlayer())) {
            e.setCancelled(true);
            MessageUtil.sendActionBar(e.getPlayer(), "Запрещено в &#30578C/god", true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player && GodCmd.isGod(player)) {
            e.setCancelled(true);
            MessageUtil.sendActionBar(player, "Запрещено в &#30578C/god", true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    @EventHandler
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent e) {
        if (GodCmd.isGod(e.getPlayer()) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
            MessageUtil.sendActionBar(e.getPlayer(), "Запрещено в &#30578C/god", true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity var3 = event.getWhoClicked();
        if (var3 instanceof Player) {
            Player player = (Player)var3;
            if (event.getView().getTitle().equals("§cPvP Меню")) {
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
        if (var3 instanceof Player) {
            Player player = (Player)var3;
            Bukkit.getScheduler().runTaskLater(GY.getInstance(), () -> {
                PvpCmd.checkArmorChange(player);
            }, 1L);
        }
    }
}
