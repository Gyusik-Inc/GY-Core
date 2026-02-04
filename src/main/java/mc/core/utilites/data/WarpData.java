package mc.core.utilites.data;

import mc.core.GY;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpData {

    public record WarpEntry(Location location, long timestamp, UUID owner) {}

    private static final Map<String, WarpEntry> warps = new HashMap<>();
    private static final Map<String, WarpEntry> adminWarps = new HashMap<>();

    private static File warpFile, adminFile;
    private static FileConfiguration warpConfig, adminConfig;

    public static void init() {
        warpFile = new File(GY.getInstance().getDataFolder(), "warps.yml");
        adminFile = new File(GY.getInstance().getDataFolder(), "admin_warps.yml");

        try {
            if (!warpFile.exists()) warpFile.createNewFile();
            if (!adminFile.exists()) adminFile.createNewFile();
        } catch (IOException e) { e.printStackTrace(); }

        warpConfig = YamlConfiguration.loadConfiguration(warpFile);
        adminConfig = YamlConfiguration.loadConfiguration(adminFile);

        load(warpConfig, warps, false);
        load(adminConfig, adminWarps, true);
    }

    private static void load(FileConfiguration config, Map<String, WarpEntry> map, boolean admin) {
        if (!config.contains("warps")) return;

        for (String name : config.getConfigurationSection("warps").getKeys(false)) {
            String path = "warps." + name;

            Location loc = new Location(
                    Bukkit.getWorld(config.getString(path + ".world")),
                    config.getDouble(path + ".x"),
                    config.getDouble(path + ".y"),
                    config.getDouble(path + ".z"),
                    (float) config.getDouble(path + ".yaw"),
                    (float) config.getDouble(path + ".pitch")
            );

            long time = config.getLong(path + ".timestamp");
            UUID owner = admin ? null : UUID.fromString(config.getString(path + ".owner"));

            map.put(name, new WarpEntry(loc, time, owner));
        }
    }

    private static void save(FileConfiguration config, File file, Map<String, WarpEntry> map, boolean admin) {
        config.set("warps", null);

        map.forEach((name, entry) -> {
            String path = "warps." + name;
            config.set(path + ".world", entry.location().getWorld().getName());
            config.set(path + ".x", entry.location().getX());
            config.set(path + ".y", entry.location().getY());
            config.set(path + ".z", entry.location().getZ());
            config.set(path + ".yaw", entry.location().getYaw());
            config.set(path + ".pitch", entry.location().getPitch());
            config.set(path + ".timestamp", entry.timestamp());

            if (!admin) config.set(path + ".owner", entry.owner().toString());
        });

        try { config.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public static void setWarp(String name, Location loc, UUID owner, boolean admin) {
        WarpEntry entry = new WarpEntry(loc, System.currentTimeMillis(), admin ? null : owner);

        if (admin) {
            adminWarps.put(name, entry);
            save(adminConfig, adminFile, adminWarps, true);
        } else {
            warps.put(name, entry);
            save(warpConfig, warpFile, warps, false);
        }
    }

    public static WarpEntry getWarp(String name) {
        return warps.get(name);
    }

    public static WarpEntry getAdminWarp(String name) {
        return adminWarps.get(name);
    }

    public static boolean deleteWarp(String name, UUID requester, boolean isAdmin) {

        if (adminWarps.containsKey(name)) {
            if (!isAdmin) return false;
            adminWarps.remove(name);
            save(adminConfig, adminFile, adminWarps, true);
            return true;
        }

        WarpEntry entry = warps.get(name);
        if (entry == null) return false;

        if (!isAdmin && !entry.owner().equals(requester)) return false;

        warps.remove(name);
        save(warpConfig, warpFile, warps, false);
        return true;
    }

    public static int getWarpCount() {
        return warps.size();
    }

    public static boolean isPlayerWarp(String name) {
        return warps.containsKey(name);
    }

    public static int getPlayerWarpCount(UUID playerId) {
        return (int) warps.values().stream()
                .filter(w -> w.owner() != null && w.owner().equals(playerId))
                .count();
    }

    public static Map<String, WarpEntry> getWarps() {
        return new HashMap<>(warps);
    }

    public static Map<String, WarpEntry> getAdminWarps() {
        return new HashMap<>(adminWarps);
    }

}
