package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@BaseCommandInfo(name = "invsee", permission = "gy-core.invsee", cooldown = 15)
public class InvseeCmd implements BaseCommand {

    private static final Map<UUID, UUID> INVSEE_SESSIONS = new HashMap<>();

    public static void openInvseeMenu(Player viewer, Player target) {
        INVSEE_SESSIONS.put(viewer.getUniqueId(), target.getUniqueId());
        Inventory invsee = Bukkit.createInventory(viewer, 54, MessageUtil.colorize("Инвентарь &#30578C" + target.getName()));
        updateInvsee(invsee, target);
        viewer.openInventory(invsee);
    }

    public static boolean isViewing(Player viewer) {
        return INVSEE_SESSIONS.containsKey(viewer.getUniqueId());
    }

    public static Player getTarget(Player viewer) {
        UUID targetUUID = INVSEE_SESSIONS.get(viewer.getUniqueId());
        return targetUUID != null ? Bukkit.getPlayer(targetUUID) : null;
    }

    public static void closeSession(Player viewer) {
        INVSEE_SESSIONS.remove(viewer.getUniqueId());
    }

    public static void updateInvsee(Inventory invsee, Player target) {
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            invsee.setItem(i, i < contents.length ? contents[i] : null);
        }

        ItemStack[] armor = target.getInventory().getArmorContents();
        invsee.setItem(45, armor.length > 0 ? armor[0] : null); // ботинки
        invsee.setItem(46, armor.length > 1 ? armor[1] : null); // поножи
        invsee.setItem(47, armor.length > 2 ? armor[2] : null); // нагрудник
        invsee.setItem(48, armor.length > 3 ? armor[3] : null); // шлем
        invsee.setItem(49, target.getInventory().getItemInOffHand());

        ItemStack blueGlass = createGlassPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int i = 36; i <= 44; i++) {
            invsee.setItem(i, blueGlass);
        }

        ItemStack blackGlass = createGlassPane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 50; i <= 53; i++) {
            invsee.setItem(i, blackGlass);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length != 1) {
            MessageUtil.sendMessage(player, "Использование: /invsee <ник>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            MessageUtil.sendMessage(player, "Игрок не найден.");
            return true;
        }

        if (target == player) {
            MessageUtil.sendMessage(player, "Нельзя смотреть свой инвентарь.");
            return true;
        }

        openInvseeMenu(player, target);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    private static ItemStack createGlassPane(Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
