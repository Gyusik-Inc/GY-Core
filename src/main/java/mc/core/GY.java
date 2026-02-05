package mc.core;

import lombok.Getter;
import mc.core.autorestart.AutoRestart;
import mc.core.basecommands.base.CommandManager;
import mc.core.basecommands.impl.EventCommand;
import mc.core.basecommands.impl.player.FixCmd;
import mc.core.basecommands.impl.player.RtpCmd;
import mc.core.basecommands.impl.player.VanishCmd;
import mc.core.chat.ChatEvent;
import mc.core.chat.JoinEvent;
import mc.core.command.PluginsCommand;
import mc.core.event.Events;
import mc.core.event.HideMessages;
import mc.core.placeholder.AnimatedLogo;
import mc.core.pvp.antirelog.AntiRelog;
import mc.core.pvp.antirelog.AntiRelogEvent;
import mc.core.pvp.command.PvpMenuEvent;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.data.HomeData;
import mc.core.utilites.data.SpawnData;
import mc.core.utilites.data.WarpData;
import me.clip.placeholderapi.PlaceholderAPI;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GY extends JavaPlugin {
    @Getter
    private static GY instance;
    @Getter
    private CommandManager commandManager;
    private static final String RESET = "\u001B[0m", GRAY = "\u001B[90m", RED = "\u001B[91m", BLUE = "\u001B[94m";
    @Getter
    private static Economy econ = null;
    @Getter
    private AutoRestart autoRestart;

    @Override
    public void onEnable() {
        instance = this;
        commandManager = new CommandManager();
        autoRestart = new AutoRestart();
        AntiRelog.init();
        HomeData.loadHomes();
        SpawnData.init();
        WarpData.init();

        new AnimatedLogo().register();

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
        Events.onDisable();
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

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
