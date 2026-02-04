package mc.core.basecommands.impl.player;

import mc.core.basecommands.base.BaseCommand;
import mc.core.basecommands.base.BaseCommandInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.inventory.InventoryType;

import java.util.List;

@BaseCommandInfo(name = "invsee", permission = "gy-core.invsee", cooldown = 15)
public class InvseeCmd implements BaseCommand {

    // Открывает меню с синхронизацией
    public static void openInvseeMenu(Player viewer, Player target) {
        Inventory invsee = Bukkit.createInventory(viewer, 54, "Инвентарь " + target.getName());

        updateInvsee(invsee, target);

        viewer.openInventory(invsee);
    }

    // Обновляет invsee на основе реального инвентаря target
    public static void updateInvsee(Inventory invsee, Player target) {
        // Основной инвентарь (0–35)
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            invsee.setItem(i, contents[i]);
        }

        // Броня (45–48)
        ItemStack[] armor = target.getInventory().getArmorContents();
        invsee.setItem(45, armor[0]); // ботинки
        invsee.setItem(46, armor[1]); // поножи
        invsee.setItem(47, armor[2]); // нагрудник
        invsee.setItem(48, armor[3]); // шлем

        // Оффхенд (49)
        invsee.setItem(49, target.getInventory().getItemInOffHand());

        // Фоновые панели (не трогаем)
        ItemStack blueGlass = createGlassPane(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int i = 36; i <= 44; i++) invsee.setItem(i, blueGlass);

        ItemStack blackGlass = createGlassPane(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 50; i <= 53; i++) invsee.setItem(i, blackGlass);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок может использовать эту команду.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§cИспользование: /invsee <ник>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§cИгрок не найден!");
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
            meta.setDisplayName("");
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
