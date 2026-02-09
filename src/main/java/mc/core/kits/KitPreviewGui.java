package mc.core.kits;

import lombok.Getter;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.KitData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUI для просмотра содержимого китов без возможности взаимодействия, с поддержкой страниц и стрелок
 */
public class KitPreviewGui implements Listener {

    private final KitData kitData = new KitData();

    @Getter
    private static KitPreviewGui instance;

    private static final int[] BLACK_SLOTS = {9, 18, 27, 36, 17, 26, 35, 44};
    private static final int[] LIGHT_BLUE_SLOTS = {0, 2, 3, 5, 6, 8, 45, 47, 50, 51, 53};
    private static final int[] WHITE_SLOTS = {1, 4, 7, 48, 49};
    private static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public KitPreviewGui() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, mc.core.GY.getInstance());
    }

    public void openPreview(Player player, String kitName) {
        ItemStack[] kitItems = kitData.getKitItems(kitName.toLowerCase());
        if (kitItems == null) {
            MessageUtil.sendMessage(player, "Набор '&#30578C" + kitName + "&f' не найден.");
            return;
        }

        openPreviewPage(player, kitName, kitItems, 0);
    }

    private void openPreviewPage(Player player, String kitName, ItemStack[] kitItems, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, MessageUtil.colorize("&#30578CПревью набора " + kitName));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack lightBluePane = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

        for (int slot : BLACK_SLOTS) inv.setItem(slot, blackPane);
        for (int slot : LIGHT_BLUE_SLOTS) inv.setItem(slot, lightBluePane);
        for (int slot : WHITE_SLOTS) inv.setItem(slot, whitePane);

        List<ItemStack> contentItems = new ArrayList<>();
        List<Integer> prioritySlots = Arrays.asList(36, 37, 38, 39, 40, 41);
        for (int i : prioritySlots) if (i < kitItems.length && kitItems[i] != null && !kitItems[i].getType().isAir())
            contentItems.add(kitItems[i]);
        for (int i = 0; i < kitItems.length; i++) {
            if (!prioritySlots.contains(i) && kitItems[i] != null && !kitItems[i].getType().isAir())
                contentItems.add(kitItems[i]);
        }

        int maxPerPage = CONTENT_SLOTS.length;
        int startIndex = page * maxPerPage;
        int endIndex = Math.min(startIndex + maxPerPage, contentItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            ItemStack item = contentItems.get(i).clone();
            inv.setItem(CONTENT_SLOTS[i - startIndex], item);
        }
        int totalPages = (int) Math.ceil((double) contentItems.size() / maxPerPage);

        if (totalPages > 1) {
            ItemStack arrowPrev = new ItemStack(Material.SPECTRAL_ARROW);
            ItemMeta metaPrev = arrowPrev.getItemMeta();
            metaPrev.setDisplayName(MessageUtil.colorize("&#30578CПредыдущая"));
            arrowPrev.setItemMeta(metaPrev);

            ItemStack arrowNext = new ItemStack(Material.SPECTRAL_ARROW);
            ItemMeta metaNext = arrowNext.getItemMeta();
            metaNext.setDisplayName(MessageUtil.colorize("&#30578CСледующая"));
            arrowNext.setItemMeta(metaNext);


            if (page > 0) inv.setItem(46, arrowPrev);
            else inv.setItem(46, whitePane);

            if (page < totalPages - 1) inv.setItem(52, arrowNext);
            else inv.setItem(52, whitePane);
        } else {
            inv.setItem(46, whitePane);
            inv.setItem(52, whitePane);
        }

        inv.setItem(48, whitePane);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (isPreviewInventory(e)) {
            e.setCancelled(true);

            int slot = e.getRawSlot();
            if (slot != 46 && slot != 52) return;

            Player player = (Player) e.getWhoClicked();
            String title = e.getView().getTitle();
            String kitName = title.replace(MessageUtil.colorize("&#30578CПревью набора "), "");

            ItemStack[] kitItems = kitData.getKitItems(kitName.toLowerCase());
            if (kitItems == null) return;

            List<ItemStack> contentItems = new ArrayList<>();
            List<Integer> prioritySlots = Arrays.asList(36, 37, 38, 39, 40, 41);
            for (int i : prioritySlots) if (i < kitItems.length && kitItems[i] != null && !kitItems[i].getType().isAir())
                contentItems.add(kitItems[i]);
            for (int i = 0; i < kitItems.length; i++) {
                if (!prioritySlots.contains(i) && kitItems[i] != null && !kitItems[i].getType().isAir())
                    contentItems.add(kitItems[i]);
            }

            int maxPerPage = CONTENT_SLOTS.length;
            int totalPages = (int) Math.ceil((double) contentItems.size() / maxPerPage);

            int firstIndexInKit = 0;
            outer:
            for (int i = 0; i < contentItems.size(); i++) {
                for (int contentSlot : CONTENT_SLOTS) {
                    ItemStack guiItem = e.getInventory().getItem(contentSlot);
                    if (guiItem != null && guiItem.isSimilar(contentItems.get(i))) {
                        firstIndexInKit = i;
                        break outer;
                    }
                }
            }

            int currentPage = firstIndexInKit / maxPerPage;
            if (slot == 46 && currentPage > 0) currentPage--;
            if (slot == 52 && currentPage < totalPages - 1) currentPage++;

            openPreviewPage(player, kitName, kitItems, currentPage);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (isPreviewInventory(e)) e.setCancelled(true);
    }

    private boolean isPreviewInventory(InventoryClickEvent e) {
        return e.getView().getTitle().startsWith(MessageUtil.colorize("&#30578CПревью набора "));
    }

    private boolean isPreviewInventory(InventoryDragEvent e) {
        return e.getView().getTitle().startsWith(MessageUtil.colorize("&#30578CПревью набора "));
    }
}
