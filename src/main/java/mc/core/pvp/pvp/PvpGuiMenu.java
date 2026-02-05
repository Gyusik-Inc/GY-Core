/* Decompiler 56ms, total 1136ms, lines 107 */
package mc.core.pvp.pvp;

import mc.core.basecommands.impl.player.PvpCmd;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PvpGuiMenu {
    private static final String GUI_TITLE = "§cPvP Меню";
    private static ItemStack queueSword;
    private static ItemStack barrierItem;
    private static ItemStack netheriteChestplate;

    public static Inventory createPvpMenu(Player player) {
        Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, "§cPvP Меню");
        setSlots(inv, List.of(0, 1, 9, 17, 25, 26), Material.BLACK_STAINED_GLASS_PANE);
        setSlots(inv, List.of(new Integer[]{2, 3, 6, 7, 8, 11, 15, 18, 19, 20, 23, 24}), Material.BLUE_STAINED_GLASS_PANE);
        setSlots(inv, List.of(4, 5, 12, 14, 21, 22), Material.WHITE_STAINED_GLASS_PANE);
        if (PvpCmd.isInAnyQueue(player.getUniqueId())) {
            inv.setItem(10, barrierItem);
        } else {
            inv.setItem(10, queueSword);
        }

        ItemStack gearItem;
        ItemMeta meta;
        if (PvpCmd.isInAnyQueue(player.getUniqueId())) {
            gearItem = barrierItem.clone();
            meta = gearItem.getItemMeta();
            meta.setDisplayName("§cВыйти из очереди");
            gearItem.setItemMeta(meta);
        } else {
            PvpCmd.ArmorSetType playerSet = PvpCmd.getPlayerArmorSet(player);
            if (playerSet == PvpCmd.ArmorSetType.NONE) {
                gearItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                meta = gearItem.getItemMeta();
                meta.setDisplayName("§cНужен минимум 2 алмаз/незерит");
                meta.setLore(Arrays.asList("§7Нужны минимум 2 предмета", "§7алмазной или незеритовой брони"));
                gearItem.setItemMeta(meta);
            } else {
                gearItem = netheriteChestplate.clone();
                meta = gearItem.getItemMeta();
                meta.setLore(Arrays.asList("§7Ваш тип: §f" + playerSet.name(), "§7Подбор по большинству предметов", "", "§8§lНезерит ↔ Незерит", "§8§lАлмаз ↔ Алмаз"));
                gearItem.setItemMeta(meta);
            }
        }

        inv.setItem(16, gearItem);
        ItemStack infoItem = new ItemStack(Material.PAPER);
        meta = infoItem.getItemMeta();
        meta.setDisplayName("§6Информация о PvP");
        List<String> lore = new ArrayList();
        lore.add("§7Обычная: §f" + PvpCmd.getPvpQueueSize());
        lore.add("§7Бронесеты: §f" + PvpCmd.getGearQueueSize());
        lore.add("");
        lore.add("§7Правила:");
        lore.add("§f• §715 блоков расстояния");
        lore.add("§f• §7Без воды/лавы");
        lore.add("§f• §7Свечение 10 сек");
        meta.setLore(lore);
        infoItem.setItemMeta(meta);
        inv.setItem(13, infoItem);
        return inv;
    }

    private static void setSlots(Inventory inv, List<Integer> slots, Material mat) {
        ItemStack item = new ItemStack(mat);
        Iterator var4 = slots.iterator();

        while(var4.hasNext()) {
            int slot = (Integer)var4.next();
            inv.setItem(slot, item);
        }

    }

    static {
        queueSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta swordMeta = queueSword.getItemMeta();
        swordMeta.setDisplayName("§eВойти в очередь PvP");
        swordMeta.setLore(Arrays.asList("§7Нажмите, чтобы присоединиться к очереди PvP", "§7Когда наберется 2 игрока,", "§7вы будете телепортированы на арену"));
        swordMeta.addEnchant(Enchantment.SHARPNESS, 1, true);
        queueSword.setItemMeta(swordMeta);
        barrierItem = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrierItem.getItemMeta();
        barrierMeta.setDisplayName("§cВыйти из очереди PvP");
        barrierMeta.setLore(Arrays.asList("§7Нажмите, чтобы покинуть очередь PvP", "§7§oВы уже находитесь в очереди"));
        barrierItem.setItemMeta(barrierMeta);
        netheriteChestplate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta chestMeta = netheriteChestplate.getItemMeta();
        chestMeta.setDisplayName("§6Очередь бронесетов");
        chestMeta.setLore(Arrays.asList("§7Подбор по типу брони (минимум 2)!", "", "§8§lНезерит ↔ Незерит", "§8§lАлмаз ↔ Алмаз"));
        chestMeta.addEnchant(Enchantment.PROTECTION, 1, true);
        netheriteChestplate.setItemMeta(chestMeta);
    }
}