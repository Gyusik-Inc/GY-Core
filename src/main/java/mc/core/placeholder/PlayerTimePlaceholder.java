package mc.core.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerTimePlaceholder extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "playtime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Gyusik";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "0ч 0м";

        String formatted = PlaceholderAPI.setPlaceholders(player, "%statistic_time_played%");
        formatted = formatted
                .replace("d", "д")
                .replace("h", "ч")
                .replace("m", "м")
                .replaceAll("\\d+s", "")
                .trim();

        return formatted;
    }
}
