package mc.core.basecommands.impl;

import mc.core.basecommands.impl.player.*;
import mc.core.pvp.command.PvpCmd;
import mc.core.utilites.chat.MessageUtil;
import mc.core.GY;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EventCommand implements Listener {
    private final HealCmd healCmd;
    private final GodCmd godCmd;
    private final InvseeCmd invseeCmd;

    public EventCommand() {
        this.healCmd = new HealCmd();
        this.godCmd = new GodCmd();
        this.invseeCmd = new InvseeCmd();

        Bukkit.getScheduler().runTaskTimer(GY.getInstance(), () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (InvseeCmd.isViewing(viewer)) {
                    Player target = InvseeCmd.getTarget(viewer);
                    if (target != null && target.isOnline()) {
                        Inventory invsee = viewer.getOpenInventory().getTopInventory();
                        if (invsee != null) {
                            InvseeCmd.updateInvsee(invsee, target);
                        }
                    }
                }
            }
        }, 0L, 10L);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage().toLowerCase();
        if (message.startsWith("/heal")) {
            e.setCancelled(true);
            String[] args = message.split(" ");
            String[] cmdArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
            healCmd.execute(e.getPlayer(), "heal", cmdArgs);
        }
        if (message.startsWith("/god")) {
            e.setCancelled(true);
            String[] args = message.split(" ");
            String[] cmdArgs = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
            godCmd.execute(e.getPlayer(), "god", cmdArgs);
        }
    }

    @EventHandler
    public void onInvseeClick(InventoryClickEvent e) {
        Player viewer = (Player) e.getWhoClicked();
        if (!InvseeCmd.isViewing(viewer)) return;
        if (!viewer.hasPermission("gy-core.admin")) {
            int slot = e.getRawSlot();
            if (slot >= 36 && slot <= 44 || slot >= 50 && slot <= 53) {
                e.setCancelled(true);
                return;
            }
            e.setCancelled(true);
            return;
        }

        Player target = InvseeCmd.getTarget(viewer);
        if (target == null || !target.isOnline()) {
            viewer.closeInventory();
            InvseeCmd.closeSession(viewer);
            return;
        }

        int slot = e.getRawSlot();
        if (slot >= 36 && slot <= 44 || slot >= 50 && slot <= 53) {
            e.setCancelled(true);
            return;
        }

        Bukkit.getScheduler().runTaskLater(GY.getInstance(), () -> {
            syncInvseeToReal(viewer, target);
            InvseeCmd.updateInvsee(viewer.getOpenInventory().getTopInventory(), target);
        }, 1L);
    }



    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player viewer = (Player) e.getPlayer();
        if (InvseeCmd.isViewing(viewer)) {
            InvseeCmd.closeSession(viewer);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player target = e.getPlayer();

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (InvseeCmd.isViewing(viewer) && InvseeCmd.getTarget(viewer) == target) {
                viewer.closeInventory();
                InvseeCmd.closeSession(viewer);
            }
        }

        TpaCmd.teleportTasks.remove(e.getPlayer().getUniqueId());
        TpaCmd.removeRequest(e.getPlayer().getUniqueId());
        PvpCmd.removePlayerFromQueue(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        TpCmd.updateLastLocation(e.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && GodCmd.isGod(player)) {
            e.setCancelled(true);
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
    public void onInteract(PlayerInteractEvent e) {
        if (GodCmd.isGod(e.getPlayer()) && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            e.setCancelled(true);
            MessageUtil.sendActionBar(e.getPlayer(), "Запрещено в &#30578C/god", true);
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    private void syncInvseeToReal(Player viewer, Player target) {
        Inventory invsee = viewer.getOpenInventory().getTopInventory();
        if (invsee == null) return;

        ItemStack[] realContents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            realContents[i] = invsee.getItem(i);
        }
        target.getInventory().setContents(realContents);

        ItemStack[] realArmor = new ItemStack[4];
        realArmor[0] = invsee.getItem(45);
        realArmor[1] = invsee.getItem(46);
        realArmor[2] = invsee.getItem(47);
        realArmor[3] = invsee.getItem(48);
        target.getInventory().setArmorContents(realArmor);

        target.getInventory().setItemInOffHand(invsee.getItem(49));
    }
}
