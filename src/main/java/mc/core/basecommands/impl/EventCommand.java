package mc.core.basecommands.impl;

import mc.core.basecommands.impl.player.TpCmd;
import mc.core.basecommands.impl.player.TpaCmd;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventCommand implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        TpaCmd.teleportTasks.remove(e.getPlayer().getUniqueId());
        TpaCmd.removeRequest(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        TpCmd.updateLastLocation(e.getPlayer());
    }
}
