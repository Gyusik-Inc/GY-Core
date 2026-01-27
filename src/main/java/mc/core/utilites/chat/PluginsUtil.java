package mc.core.utilites.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 27.01.2026
 */

public class PluginsUtil {
    private static final List<String> SPECIAL_AUTHORS = Arrays.asList("Gyusik", "YouLow_Skill");

    public static void showPlugins(CommandSender sender) {
        sender.sendMessage("");
        MessageUtil.sendMessage(sender, "&8Плагины сервера: ");
        sender.sendMessage("");

        List<String> normalPlugins = new ArrayList<>();
        List<String> specialPlugins = new ArrayList<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String name = plugin.getName();
            List<String> authors = plugin.getPluginMeta().getAuthors();
            boolean isSpecial = false;

            for (String auth : authors) {
                if (SPECIAL_AUTHORS.contains(auth)) {
                    isSpecial = true;
                    specialPlugins.add(plugin.isEnabled() ? "&a• &#30578C" + name + " &8(" + String.join(", ", authors) + ")"
                            : "&c• &#30578C" + name + " &8(" + String.join(", ", authors) + ")");
                    break;
                }
            }

            if (!isSpecial) {
                String nameColor = plugin.isEnabled() ? "&f" : "&c";
                normalPlugins.add(nameColor + name);
            }
        }

        specialPlugins.sort(Comparator.comparingInt(String::length));

        if (!specialPlugins.isEmpty()) {
            MessageUtil.sendMessage(sender, "&#30578CGY-Studio &7(" + specialPlugins.size() + ")");
            for (String plugin : specialPlugins) {
                MessageUtil.sendMessage(sender, "  " + plugin);
            }
            MessageUtil.sendMessage(sender, "");
        }

        if (!normalPlugins.isEmpty()) {
            MessageUtil.sendMessage(sender, "&7Обычные (" + normalPlugins.size() + "&7)");
            for (int i = 0; i < normalPlugins.size(); i += 7) {
                String line = String.join(MessageUtil.colorize("&8, "),
                        normalPlugins.subList(i, Math.min(i + 7, normalPlugins.size())));
                MessageUtil.sendMessage(sender, "  " + line);
            }
        }

        sender.sendMessage("");
    }
}