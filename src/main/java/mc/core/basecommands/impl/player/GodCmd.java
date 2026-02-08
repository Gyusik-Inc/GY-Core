package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@BaseCommandInfo(name = "god", permission = "gy-core.god", cooldown = 3)
public class GodCmd implements BaseCommand {

    private static final Set<String> godPlayers = new HashSet<>();

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length > 0) {
            if (!player.hasPermission("gy-core.admin")) {
                MessageUtil.sendPermissionMessage(player);
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtil.sendUnknownPlayerMessage(sender, args[0]);
                return true;
            }

            return toggleGodMode(player, target);
        }

        return toggleGodMode(player, player);
    }

    private boolean toggleGodMode(Player sender, Player target) {
        String uuid = target.getUniqueId().toString();
        boolean isGod = godPlayers.contains(uuid);

        if (isGod) {
            godPlayers.remove(uuid);
            target.setAllowFlight(false);
            target.setFlying(false);
        } else {
            godPlayers.add(uuid);
            target.setHealth(target.getMaxHealth());
        }

        String status = isGod ? "&cвыключен." : "&aвключён.";

        if (target == sender) {
            MessageUtil.sendMessage(target, "Режим бога " + status);
        } else {
            MessageUtil.sendMessage(sender, "Режим бога для &#30578C" + target.getName() + " " + status);
            MessageUtil.sendMessage(target, "Режим бога " + status + " &7(§e" + sender.getName() + "&7)");
        }

        return true;
    }

    public static boolean isGod(Player player) {
        return godPlayers.contains(player.getUniqueId().toString());
    }

    public static void disableGod(Player player) {
        String uuid = player.getUniqueId().toString();
        if (godPlayers.remove(uuid)) {
            player.setAllowFlight(false);
            player.setFlying(false);
            MessageUtil.sendMessage(player, "Режим бога &cвыключен.");
        }
    }

    public static void disableAllGod() {
        for (String uuidStr : new HashSet<>(godPlayers)) {
            Player player = Bukkit.getPlayer(java.util.UUID.fromString(uuidStr));
            if (player != null && player.isOnline()) {
                disableGod(player);
            }
            godPlayers.remove(uuidStr);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("gy-core.admin")) {
            Player player = sender instanceof Player ? (Player) sender : null;
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))
                    .map(Player::getName)
                    .filter(name -> args[0].isEmpty() || name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
