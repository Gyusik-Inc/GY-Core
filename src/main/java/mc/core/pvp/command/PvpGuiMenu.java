package mc.core.pvp.command;

import lombok.Getter;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class PvpGuiMenu {

    @Getter
    private static final String TITLE = MessageUtil.colorize("&#30578CБоевая очередь");

    private static final ItemStack QUEUE_SWORD = sword();
    private static final ItemStack BARRIER = barrier();
    private static final ItemStack NETHERITE_CHESTPLATE = gear();

    private static final List<Integer> BLACK = List.of(0,1,9,17,25,26);
    private static final List<Integer> BLUE  = List.of(2,3,6,7,8,11,15,18,19,20,23,24);
    private static final List<Integer> WHITE = List.of(4,5,12,14,21,22);

    public static Inventory createPvpMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        setSlots(inv, BLACK, Material.BLACK_STAINED_GLASS_PANE, MessageUtil.colorize("&x&5&5&7&5&A&0play.north-mc.su"));
        setSlots(inv, BLUE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, MessageUtil.colorize("&x&5&5&7&5&A&0play.north-mc.su"));
        setSlots(inv, WHITE, Material.WHITE_STAINED_GLASS_PANE, MessageUtil.colorize("&x&5&5&7&5&A&0play.north-mc.su"));

        inv.setItem(10, PvpCmd.isInAnyQueue(p.getUniqueId()) ? BARRIER : QUEUE_SWORD);
        inv.setItem(16, PvpCmd.isInAnyQueue(p.getUniqueId()) ? barrierExit() : gearItem(p));
        inv.setItem(13, info());

        return inv;
    }

    private static void hideAll(ItemStack item) {
        item.addItemFlags(
                ItemFlag.HIDE_ATTRIBUTES,
                ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_UNBREAKABLE,
                ItemFlag.HIDE_DESTROYS,
                ItemFlag.HIDE_PLACED_ON
        );

        item.getItemMeta().setHideTooltip(true);
    }

    private static ItemStack barrierExit() {
        ItemStack b = BARRIER.clone();
        b.editMeta(m -> m.setDisplayName(MessageUtil.colorize("&cВыйти из очереди")));
        hideAll(b);
        return b;
    }

    private static ItemStack gearItem(Player p) {
        var set = PvpCmd.getPlayerArmorSet(p);
        if (set == PvpCmd.ArmorSetType.NONE) return noArmor();

        ItemStack item = NETHERITE_CHESTPLATE.clone();
        hideAll(item);
        return item;
    }

    private static ItemStack info() {
        ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(m -> {
            m.setDisplayName(MessageUtil.colorize("&#30578C&nИнформация:"));
            m.setLore(List.of(
                    " ",
                    MessageUtil.colorize("&7☁ &7Обычная: &7" + PvpCmd.getPvpQueueSize()),
                    MessageUtil.colorize("&#30578C★ &#B1B7BEЭлитная: &#30578C" + PvpCmd.getGearQueueSize()),
                    " "
            ));
        });
        hideAll(item);
        return item;
    }

    private static ItemStack noArmor() {
        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        item.editMeta(m -> {
            m.setDisplayName(MessageUtil.colorize("&cНевозможно начать поиск"));
            m.setLore(Arrays.asList(
                    "",
                    MessageUtil.colorize(" &7Ваше снаряжение &cне подходит "),
                    MessageUtil.colorize(" &7для &#B1B7BE&nэлитного&7 подбора. "),
                    ""
            ));
        });
        hideAll(item);
        return item;
    }

    private static void setSlots(Inventory inv, List<Integer> slots, Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        item.editMeta(m -> {
            m.setDisplayName(MessageUtil.colorize(name));
            m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        });
        slots.forEach(s -> inv.setItem(s, item));
    }


    private static ItemStack sword() {
        ItemStack s = new ItemStack(Material.FIRE_CHARGE);
        s.editMeta(m -> {
            m.setDisplayName(MessageUtil.colorize("&#30578CВойти в очередь"));
            m.setLore(List.of(
                    "",
                    MessageUtil.colorize("  &7Обычная очередь, без &#30578Cсортировки "),
                    MessageUtil.colorize("  &7игроков по &#30578Cснаряжению. "),
                    "",
                    MessageUtil.colorize(" &#30578C➥ Нажмите, &fчтобы войти."),
                    ""));
            m.addEnchant(Enchantment.SHARPNESS, 1, true);
            m.addItemFlags(ItemFlag.values());
        });
        return s;
    }

    private static ItemStack barrier() {
        ItemStack b = new ItemStack(Material.BARRIER);
        b.editMeta(m -> m.setDisplayName(MessageUtil.colorize("&cВыйти из очереди")));
        hideAll(b);
        return b;
    }

    private static ItemStack gear() {
        ItemStack c = new ItemStack(Material.AMETHYST_SHARD);
        c.editMeta(m -> {
            m.setDisplayName(MessageUtil.colorize("&#B1B7BE☄ &nЭлитная очередь"));
            m.setLore(List.of(
                    "",
                    MessageUtil.colorize("  &7Особенность &#B1B7BEэлитной &7очереди, заключается "),
                    MessageUtil.colorize("  &7в том, что в неё могут &#B1B7BEвступить&7 только игроки,"),
                    MessageUtil.colorize("  &7с &#B1B7BEалмазной &7бронёй и выше "),
                    "",
                    MessageUtil.colorize(" &#30578C➥ Нажмите, &fчтобы войти."),
                    ""
            ));
            m.addEnchant(Enchantment.PROTECTION, 1, true);
        });
        hideAll(c);
        return c;
    }
}
