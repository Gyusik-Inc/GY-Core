package mc.core.utilites.data;

import mc.core.GY;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class KitData {

    private final File file;
    private final YamlConfiguration config;

    public KitData() {
        this.file = new File(GY.getInstance().getDataFolder(), "kits.yml");

        if (!file.exists()) {
            try {
                GY.getInstance().getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveKit(String name, ItemStack[] contents) {
        String path = "kits." + name.toLowerCase();
        config.set(path + ".items", null);

        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType().isAir()) continue;
            config.set(path + ".items." + slot, item);
        }

        save();
    }

    public ItemStack[] getKitItems(String name) {
        String path = "kits." + name.toLowerCase() + ".items";
        if (!config.contains(path)) return null;

        ItemStack[] contents = new ItemStack[41];

        for (String key : config.getConfigurationSection(path).getKeys(false)) {
            int slot = Integer.parseInt(key);
            contents[slot] = config.getItemStack(path + "." + key);
        }
        return contents;
    }

    public boolean removeKit(String name) {
        String path = "kits." + name.toLowerCase();
        if (!config.contains(path)) return false;

        config.set(path, null);
        save();
        return true;
    }

    public List<String> getKitNames() {
        if (!config.contains("kits")) return new ArrayList<>();
        return new ArrayList<>(Objects.requireNonNull(config.getConfigurationSection("kits")).getKeys(false));
    }

    public void setKitCooldown(String kit, long seconds) {
        config.set("kits." + kit.toLowerCase() + ".cooldown", seconds);
        save();
    }

    public long getKitCooldown(String kit) {
        return config.getLong("kits." + kit.toLowerCase() + ".cooldown", 0);
    }

    public void setLastUse(UUID uuid, String kit, long time) {
        config.set("usage." + uuid + "." + kit.toLowerCase(), time);
        save();
    }

    public long getLastUse(UUID uuid, String kit) {
        return config.getLong("usage." + uuid + "." + kit.toLowerCase(), 0);
    }

    public void resetAllCooldowns() {
        config.set("usage", null); // удаляем всю секцию usage
        save();
    }


    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
