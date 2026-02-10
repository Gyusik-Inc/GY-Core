package mc.core.placeholder;

import mc.core.GY;
import mc.core.utilites.chat.AnimatedTextPlaceholder;
import mc.core.utilites.chat.MessageUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Gyusik
 * @since 05.02.2026
 */
public class AnimatedLogo extends PlaceholderExpansion {

    private final AnimatedTextPlaceholder animatedText;

    public AnimatedLogo() {
        this.animatedText = new AnimatedTextPlaceholder(MessageUtil.colorize("ɴᴏʀᴛʜ-ᴍᴄ"), "#30578C", "#889DB8", 1);
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "northlogo";
    }

    @Override
    public String getAuthor() {
        return "Gyusik";
    }

    @Override
    public @NotNull String getVersion() {
        return GY.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return animatedText.getCurrentFrame();
    }
}
