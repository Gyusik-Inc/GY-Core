package mc.core.basecommands.impl.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RtpFallProtection implements Listener {

    private static final Set<UUID> noFall = new HashSet<>();

    public static void give(Player p) {
        noFall.add(p.getUniqueId());
    }

    public static void remove(Player p) {
        noFall.remove(p.getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        if (e.getCause() == EntityDamageEvent.DamageCause.FALL && noFall.contains(p.getUniqueId())) {
            e.setCancelled(true);
            remove(p);
        }
    }
}
