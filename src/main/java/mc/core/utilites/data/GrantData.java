package mc.core.utilites.data;

import lombok.Getter;
import lombok.Setter;
import mc.core.GY;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class GrantData {

    private UUID playerUuid;
    private String playerName;
    private String group;
    private Map<String, Integer> remainingGrants;
    private Map<String, GrantHistory> grantHistory;

    private static final Map<UUID, GrantData> cache = new ConcurrentHashMap<>();
    private static File dataFolder;

    public GrantData(UUID playerUuid, String playerName, String group) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.group = group;
        this.remainingGrants = new LinkedHashMap<>(); // LinkedHashMap для сохранения порядка
        this.grantHistory = new LinkedHashMap<>();
        setGrantsByGroup(group);
    }

    public void setGrantsByGroup(String group) {
        remainingGrants.clear();

        switch (group.toLowerCase()) {
            case "north":
                remainingGrants.put("cunt", 9);
                remainingGrants.put("erbus", 7);
                remainingGrants.put("warden", 5);
                remainingGrants.put("strider", 3);
                remainingGrants.put("merchant", 1);
                break;
            case "noctir":
                remainingGrants.put("cunt", 7);
                remainingGrants.put("erbus", 5);
                remainingGrants.put("warden", 3);
                remainingGrants.put("strider", 1);
                break;
            case "crux":
                remainingGrants.put("cunt", 5);
                remainingGrants.put("erbus", 3);
                remainingGrants.put("warden", 1);
                break;
            default:
                break;
        }
    }

    public boolean hasGrant(String grantName) {
        return remainingGrants.containsKey(grantName) && remainingGrants.get(grantName) > 0;
    }

    public void useGrant(String grantName) {
        if (hasGrant(grantName)) {
            remainingGrants.put(grantName, remainingGrants.get(grantName) - 1);
        }
    }

    public void addToHistory(String grantName, String targetPlayer, long timestamp) {
        GrantHistory history = new GrantHistory(grantName, targetPlayer, timestamp);
        String key = grantName + "_" + targetPlayer + "_" + timestamp;
        grantHistory.put(key, history);
    }

    // ========== СОХРАНЕНИЕ И ЗАГРУЗКА ==========

    public static void init(File folder) {
        dataFolder = new File(folder, "grantdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        loadAll();
    }

    public void save() {
        if (dataFolder == null) return;

        File file = new File(dataFolder, playerUuid.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("uuid", playerUuid.toString());
        config.set("name", playerName);
        config.set("group", group);

        // Сохраняем оставшиеся выдачи
        for (Map.Entry<String, Integer> entry : remainingGrants.entrySet()) {
            config.set("grants." + entry.getKey(), entry.getValue());
        }

        // Сохраняем историю
        int i = 0;
        for (GrantHistory history : grantHistory.values()) {
            config.set("history." + i + ".grant", history.getGrantName());
            config.set("history." + i + ".target", history.getTargetPlayer());
            config.set("history." + i + ".time", history.getTimestamp());
            i++;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            GY.getInstance().getLogger().warning("Не удалось сохранить GrantData для " + playerName + ": " + e.getMessage());
        }
    }

    public static GrantData load(UUID uuid) {
        if (dataFolder == null) return null;

        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String name = config.getString("name");
        String group = config.getString("group");

        GrantData data = new GrantData(uuid, name, group);
        data.getRemainingGrants().clear();

        // Загружаем оставшиеся выдачи
        if (config.contains("grants")) {
            for (String key : config.getConfigurationSection("grants").getKeys(false)) {
                int value = config.getInt("grants." + key);
                data.getRemainingGrants().put(key, value);
            }
        } else {
            // Если нет сохраненных данных, используем стандартные
            data.setGrantsByGroup(group);
        }

        // Загружаем историю
        if (config.contains("history")) {
            for (String key : config.getConfigurationSection("history").getKeys(false)) {
                String grant = config.getString("history." + key + ".grant");
                String target = config.getString("history." + key + ".target");
                long time = config.getLong("history." + key + ".time");
                data.addToHistory(grant, target, time);
            }
        }

        cache.put(uuid, data);
        return data;
    }

    public static void loadAll() {
        if (dataFolder == null) return;

        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                String fileName = file.getName().replace(".yml", "");
                UUID uuid = UUID.fromString(fileName);
                load(uuid);
            } catch (IllegalArgumentException e) {
                // Не UUID имя файла, пропускаем
            }
        }
    }

    public static void saveAll() {
        for (GrantData data : cache.values()) {
            data.save();
        }
    }

    public static GrantData get(Player player) {
        GrantData data = cache.get(player.getUniqueId());
        if (data == null) {
            data = load(player.getUniqueId());
            if (data == null) {
                String group = "default"; // тут нужно получить группу из LP
                data = new GrantData(player.getUniqueId(), player.getName(), group);
            }
            cache.put(player.getUniqueId(), data);
        }
        return data;
    }

    public static void removeFromCache(UUID uuid) {
        cache.remove(uuid);
    }

    @Getter
    @Setter
    public static class GrantHistory {
        private String grantName;
        private String targetPlayer;
        private long timestamp;

        public GrantHistory(String grantName, String targetPlayer, long timestamp) {
            this.grantName = grantName;
            this.targetPlayer = targetPlayer;
            this.timestamp = timestamp;
        }
    }
}