package mc.core;

import lombok.Getter;
import mc.core.command.PluginsCommand;
import mc.core.event.Events;
import mc.core.utilites.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class GY extends JavaPlugin {
    @Getter
    private static GY instance;
    private static final String RESET = "\u001B[0m", GRAY = "\u001B[90m", RED = "\u001B[91m", BLUE = "\u001B[94m";

    @Override
    public void onEnable() {
        instance = this;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("gy-core.admin")) {
                player.sendMessage("");
                MessageUtil.sendMessage(player, "&8(Админ)&f Плагин &aвключён.");
                player.sendMessage("");
            }
        }

        logState(BLUE, "включён");

        getServer().getPluginManager().registerEvents(new Events(), this);
        Objects.requireNonNull(getCommand("plugins")).setExecutor(new PluginsCommand());
    }

    @Override
    public void onLoad() {
        logState(GRAY, "загружается");
    }

    @Override
    public void onDisable() {
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
