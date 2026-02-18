package mc.core;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaManager;
import lombok.Getter;
import mc.core.autorestart.AutoRestart;
import mc.core.basecommands.base.CommandManager;
import mc.core.basecommands.impl.EventCommand;
import mc.core.basecommands.impl.player.FixCmd;
import mc.core.basecommands.impl.player.RtpCmd;
import mc.core.basecommands.impl.player.VanishCmd;
import mc.core.basecommands.impl.world.RtpFallProtection;
import mc.core.chat.ChatEvent;
import mc.core.chat.JoinEvent;
import mc.core.command.PluginsCommand;
import mc.core.event.Events;
import mc.core.event.HideMessages;
import mc.core.placeholder.AnimatedLogo;
import mc.core.placeholder.PlayerTimePlaceholder;
import mc.core.pvp.antirelog.AntiRelog;
import mc.core.pvp.antirelog.AntiRelogEvent;
import mc.core.pvp.command.PvpMenuEvent;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.HomeData;
import mc.core.utilites.data.PlayerData;
import mc.core.utilites.data.SpawnData;
import mc.core.utilites.data.WarpData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GY extends JavaPlugin {

    @Getter
    private static GY instance;

    @Getter
    private CommandManager commandManager;

    private static final String RESET = "\u001B[0m";
    private static final String GRAY = "\u001B[90m";
    private static final String RED = "\u001B[91m";
    private static final String BLUE = "\u001B[94m";

    @Getter
    private AutoRestart autoRestart;

    @Getter
    private ViaManager versionManager;

    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    public static PlayerData getPlayerData(UUID uuid) {
        if (instance == null) {
            throw new IllegalStateException("GY-Core plugin instance is not initialized yet");
        }
        return instance.playerDataCache.computeIfAbsent(uuid, PlayerData::new);
    }

    public static PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public static void refreshPlayerData(UUID uuid) {
        if (instance == null) {
            throw new IllegalStateException("GY-Core plugin instance is not initialized yet");
        }
        instance.playerDataCache.remove(uuid);
        getPlayerData(uuid);
    }

    public static void refreshPlayerData(Player player) {
        refreshPlayerData(player.getUniqueId());
    }

    @Override
    public void onEnable() {
        instance = this;
        commandManager = new CommandManager();
        autoRestart = new AutoRestart();
        versionManager = Via.getManager();

        AntiRelog.init();
        HomeData.loadHomes();
        SpawnData.init();
        WarpData.init();

        new AnimatedLogo().register();
        new PlayerTimePlaceholder().register();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("gy-core.admin")) {
                player.sendMessage("");
                MessageUtil.sendMessage(player, "&8(Админ)&f Плагин &aвключён.");
                player.sendMessage("");
            }
        }

        logState(BLUE, "включён");

        getServer().getPluginManager().registerEvents(new Events(), this);
        getServer().getPluginManager().registerEvents(new ChatEvent(), this);
        getServer().getPluginManager().registerEvents(new JoinEvent(), this);
        getServer().getPluginManager().registerEvents(new HideMessages(), this);
        getServer().getPluginManager().registerEvents(new EventCommand(), this);
        getServer().getPluginManager().registerEvents(new PvpMenuEvent(), this);
        getServer().getPluginManager().registerEvents(new AntiRelogEvent(), this);
        getServer().getPluginManager().registerEvents(new RtpFallProtection(), this);

        Objects.requireNonNull(getCommand("plugins")).setExecutor(new PluginsCommand());
        Objects.requireNonNull(getCommand("rtp")).setExecutor(new RtpCmd());
        Objects.requireNonNull(getCommand("fix")).setExecutor(new FixCmd());
    }

    @Override
    public void onLoad() {
        logState(GRAY, "загружается");
    }

    @Override
    public void onDisable() {
        VanishCmd.removeAllVanishes();
        HomeData.saveHomes();
        AntiRelog.shutdown();
        Events.getInstance().onDisable();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("gy-core.admin")) {
                player.sendMessage("");
                MessageUtil.sendMessage(player, "&8(Админ)&f Плагин &cвыключен.");
                player.sendMessage("");
            }
        }

        logState(RED, "выключен");
    }

    private void logState(String color, String state) {
        printLogo(color);
        String version = getPluginMeta().getVersion();
        getLogger().info(color + "GY-Core " + state + "." + RESET);
        getLogger().info("Ветка: " + color + "Development" + RESET);
        getLogger().info("Версия: " + color + version + RESET);
    }

    private void printLogo(String color) {
        String[] lines = {
                "\n",
                "    _____ __ __ ",
                "   |   __|  |  |",
                "   |  |  |_   _|",
                "   |_____| |_|  ",
                "  ___             ",
                " / __|___ _ _ ___ ",
                "| (__/ _ \\ '_/ -_)",
                " \\___\\___/_| \\___|\n",
        };
        for (String line : lines) {
            getLogger().info(color + line + RESET);
        }
    }
}