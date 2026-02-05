package mc.core.utilites.data;

import mc.core.GY;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PlayerData {

    private static final File DATA_FOLDER = new File(GY.getInstance().getDataFolder(), "playerdata");
    private static final File COUNTER_FILE = new File(DATA_FOLDER, "total_new_players.yml");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static final FileConfiguration counterConfig;

    static {
        if (!DATA_FOLDER.exists()) DATA_FOLDER.mkdirs();
        counterConfig = YamlConfiguration.loadConfiguration(COUNTER_FILE);
    }

    private final UUID playerUUID;
    private File playerFile;
    private FileConfiguration playerConfig;

    public PlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        loadData();
    }

    private void loadData() {
        this.playerFile = new File(DATA_FOLDER, playerUUID + ".yml");
        this.playerConfig = YamlConfiguration.loadConfiguration(playerFile);
    }

    public void setFirstJoin() {
        String now = LocalDateTime.now().format(DATE_FORMAT);
        int playerNumber = getTotalNewPlayers() + 1;

        playerConfig.set("first_join", now);
        playerConfig.set("join_number", playerNumber);
        incrementTotalNewPlayers();

        saveData();
    }

    public String getFirstJoin() {
        return playerConfig.getString("first_join", "Неизвестно");
    }

    public int getJoinNumber() {
        return playerConfig.getInt("join_number", -1);
    }

    public boolean hasPlayedBefore() {
        return playerConfig.contains("first_join");
    }

    public static int getTotalNewPlayers() {
        return counterConfig.getInt("total", 0);
    }

    private void incrementTotalNewPlayers() {
        int current = getTotalNewPlayers();
        counterConfig.set("total", current + 1);
        saveCounter();
    }

    private static void saveCounter() {
        try {
            counterConfig.save(COUNTER_FILE);
        } catch (IOException e) {
            GY.getInstance().getLogger().severe("Не удалось сохранить счётчик игроков");
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            GY.getInstance().getLogger().severe("Не удалось сохранить данные игрока " + playerUUID);
            e.printStackTrace();
        }
    }

    public static void recordFirstJoin(UUID playerUUID) {
        PlayerData data = new PlayerData(playerUUID);
        if (!data.hasPlayedBefore()) {
            data.setFirstJoin();
        }
    }

    // --- Customization flags ---
    public boolean isMsgEnabled() {
        return playerConfig.getBoolean("customization.msg_enabled", true);
    }

    public void setMsgEnabled(boolean enabled) {
        playerConfig.set("customization.msg_enabled", enabled);
        saveData();
    }

    public boolean isTpEnabled() {
        return playerConfig.getBoolean("customization.tp_enabled", true);
    }

    public void setTpEnabled(boolean enabled) {
        playerConfig.set("customization.tp_enabled", enabled);
        saveData();
    }

    public boolean isPayEnabled() {
        return playerConfig.getBoolean("customization.pay_enabled", true);
    }

    public void setPayEnabled(boolean enabled) {
        playerConfig.set("customization.pay_enabled", enabled);
        saveData();
    }

    public boolean isScoreboardEnabled() {
        return playerConfig.getBoolean("customization.scoreboard_enabled", true);
    }

    public void setScoreboardEnabled(boolean enabled) {
        playerConfig.set("customization.scoreboard_enabled", enabled);
        saveData();
    }
}
