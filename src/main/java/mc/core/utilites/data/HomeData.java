// HomeData.java
package mc.core.utilites.data;

import lombok.Getter;
import mc.core.GY;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeData {
    private static final Map<UUID, Map<String, HomeEntry>> homes = new HashMap<>();
    private static final FileConfiguration config = GY.getInstance().getConfig();

    @Getter
    public static class HomeEntry {
        private final Location location;
        private final long timestamp;

        public HomeEntry(Location location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }

    }

    public static void loadHomes() {
        if (!config.contains("homes")) return;

        for (String key : config.getConfigurationSection("homes").getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            Map<String, HomeEntry> playerHomes = new HashMap<>();

            for (String homeName : config.getConfigurationSection("homes." + key).getKeys(false)) {
                String path = "homes." + key + "." + homeName;
                String worldName = config.getString(path + ".world");
                double x = config.getDouble(path + ".x");
                double y = config.getDouble(path + ".y");
                double z = config.getDouble(path + ".z");
                float yaw = (float) config.getDouble(path + ".yaw");
                float pitch = (float) config.getDouble(path + ".pitch");
                long timestamp = config.getLong(path + ".timestamp");

                playerHomes.put(homeName, new HomeEntry(
                        new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch),
                        timestamp
                ));
            }

            homes.put(playerId, playerHomes);
        }
    }

    public static void saveHomes() {
        config.set("homes", null);

        homes.forEach((uuid, playerHomes) -> {
            playerHomes.forEach((homeName, entry) -> {
                String path = "homes." + uuid.toString() + "." + homeName;
                config.set(path + ".world", entry.getLocation().getWorld().getName());
                config.set(path + ".x", entry.getLocation().getX());
                config.set(path + ".y", entry.getLocation().getY());
                config.set(path + ".z", entry.getLocation().getZ());
                config.set(path + ".yaw", entry.getLocation().getYaw());
                config.set(path + ".pitch", entry.getLocation().getPitch());
                config.set(path + ".timestamp", entry.getTimestamp());
            });
        });
        GY.getInstance().saveConfig();
    }

    public static void setHome(UUID playerId, String name, Location location) {
        long now = System.currentTimeMillis();
        homes.computeIfAbsent(playerId, k -> new HashMap<>())
                .put(name, new HomeEntry(location, now));
        saveHomes();
    }

    public static HomeEntry getHome(UUID playerId, String name) {
        return homes.getOrDefault(playerId, Map.of()).get(name);
    }

    public static boolean hasHome(UUID playerId, String name) {
        return homes.containsKey(playerId) && homes.get(playerId).containsKey(name);
    }

    public static Map<String, HomeEntry> getHomes(UUID playerId) {
        return homes.getOrDefault(playerId, Map.of());
    }

    public static boolean removeHome(UUID playerId, String name) {
        if (!homes.containsKey(playerId)) return false;
        Map<String, HomeEntry> playerHomes = homes.get(playerId);
        if (!playerHomes.containsKey(name)) return false;

        playerHomes.remove(name);

        if (playerHomes.isEmpty()) {
            homes.remove(playerId);
        }

        saveHomes();
        return true;
    }
}
