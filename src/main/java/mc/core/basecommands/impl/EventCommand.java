package mc.core.basecommands.impl;

import mc.core.basecommands.impl.player.GodCmd;
import mc.core.basecommands.impl.player.HealCmd;
import mc.core.basecommands.impl.player.TpCmd;
import mc.core.basecommands.impl.player.TpaCmd;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventCommand implements Listener {
    private final HealCmd healCmd;
    private final GodCmd godCmd;

    public EventCommand() {
        this.healCmd = new HealCmd();
        this.godCmd = new GodCmd();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage();
        if (message.toLowerCase().startsWith("/heal")) {
            e.setCancelled(true);
            String[] args = message.split(" ");
            String[] cmdArgs = args.length > 1 ?
                    java.util.Arrays.copyOfRange(args, 1, args.length) :
                    new String[0];
            healCmd.execute(e.getPlayer(), "heal", cmdArgs);
        }
        if (message.toLowerCase().startsWith("/god")) {
            e.setCancelled(true);
            String[] args = message.split(" ");
            String[] cmdArgs = args.length > 1 ?
                    java.util.Arrays.copyOfRange(args, 1, args.length) :
                    new String[0];
            godCmd.execute(e.getPlayer(), "god", cmdArgs);
        }
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
            Player player = e.getPlayer();
            e.setCancelled(true);
            MessageUtil.sendActionBar(player, "Запрещено в &#30578C/god", true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (GodCmd.isGod(e.getPlayer())) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            MessageUtil.sendActionBar(player, "Запрещено в &#30578C/god", true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
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
            Player player = e.getPlayer();
            e.setCancelled(true);
            MessageUtil.sendActionBar(player, "Запрещено в &#30578C/god", true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

}