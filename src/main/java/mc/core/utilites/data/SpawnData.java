package mc.core.utilites.data;

import mc.core.GY;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SpawnData {

    private static Location spawn;
    private static File file;
    private static FileConfiguration config;

    public static void init() {
        file = new File(GY.getInstance().getDataFolder(), "spawn.yml");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        loadSpawn();
    }

    private static void loadSpawn() {
        if (!config.contains("spawn.world")) return;

        World world = Bukkit.getWorld(config.getString("spawn.world"));
        if (world == null) return;

        spawn = new Location(
                world,
                config.getDouble("spawn.x"),
                config.getDouble("spawn.y"),
                config.getDouble("spawn.z"),
                (float) config.getDouble("spawn.yaw"),
                (float) config.getDouble("spawn.pitch")
        );
    }

    public static void setSpawn(Location loc) {
        spawn = loc.clone();

        config.set("spawn.world", loc.getWorld().getName());
        config.set("spawn.x", loc.getX());
        config.set("spawn.y", loc.getY());
        config.set("spawn.z", loc.getZ());
        config.set("spawn.yaw", loc.getYaw());
        config.set("spawn.pitch", loc.getPitch());

        save();
    }

    public static Location getSpawn() {
        return spawn == null ? null : spawn.clone();
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteSpawn() {
        spawn = null;

        config.set("spawn", null);
        save();
    }

}
