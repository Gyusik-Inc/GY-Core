package mc.core.chat;

import mc.core.utilites.chat.MessageUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 03.02.2026
 */

public class ChatEvent implements Listener {

    // Цвета префиксов чата
    private static final String GLOBAL_CHAT_PREFIX = "#30578CG&f";
    private static final String LOCAL_CHAT_PREFIX = "#112B4EL&f";

    // Цвета сообщений игроков
    private static final String GLOBAL_MESSAGE_COLOR = "&f";
    private static final String LOCAL_MESSAGE_COLOR = "&7";

    // Цвета для CEO
    private static final String CEO_GLOBAL_MESSAGE_COLOR = "&#30578C&l";
    private static final String CEO_LOCAL_MESSAGE_COLOR = "&#30578C";

    // Форматы
    private static final String DEFAULT_FORMAT = "{prefix} {prefixLuck}{sender} &8» {msgColor}{message}";
    private static final String CEO_FORMAT = "{prefix} {prefixLuck}{sender} &8» {msgColor}{message}";

    private static final long CHAT_COOLDOWN_TICKS = 2 * 20; // 2 сек
    private static final String BYPASS_PERMISSION = "gy-core.chat.bypass-cooldown";
    private final HashMap<UUID, Long> lastChatTime = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        event.setCancelled(true);

        if (!player.hasPermission(BYPASS_PERMISSION) && !canPlayerChat(player)) {
            MessageUtil.sendMessage(player, "Подождите &#30578C" + getRemainingCooldown(player) + " сек. &fперед следующим сообщением!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        lastChatTime.put(player.getUniqueId(), System.currentTimeMillis());

        if (message.startsWith("!")) {
            sendGlobalChat(player, message.substring(1));
        } else {
            sendLocalChat(player, message);
        }
    }

    private boolean canPlayerChat(Player player) {
        UUID uuid = player.getUniqueId();
        Long lastTime = lastChatTime.get(uuid);

        if (lastTime == null) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastTime;
        long cooldownMillis = (CHAT_COOLDOWN_TICKS * 50);

        return timePassed >= cooldownMillis;
    }

    private String getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        Long lastTime = lastChatTime.get(uuid);

        if (lastTime == null) {
            return "0";
        }

        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastTime;
        long cooldownMillis = (CHAT_COOLDOWN_TICKS * 50);
        long remaining = cooldownMillis - timePassed;

        return String.format("%.1f", remaining / 1000.0);
    }

    private void sendGlobalChat(Player sender, String message) {
        String msgColor = isCEO(sender) ? CEO_GLOBAL_MESSAGE_COLOR : GLOBAL_MESSAGE_COLOR;

        for (Player online : Bukkit.getOnlinePlayers()) {
            String prefixLuck = getLuckPermsPrefix(sender);
            String formatted = getPlayerFormat(sender)
                    .replace("{prefix}", GLOBAL_CHAT_PREFIX)
                    .replace("{prefixLuck}", prefixLuck)
                    .replace("{sender}", sender.getName())
                    .replace("{msgColor}", msgColor)
                    .replace("{message}", formatMentions(sender, message, online, msgColor));
            online.sendMessage(MessageUtil.colorize(formatted));
        }
    }

    private void sendLocalChat(Player sender, String message) {
        String msgColor = isCEO(sender) ? CEO_LOCAL_MESSAGE_COLOR : LOCAL_MESSAGE_COLOR;
        Location senderLoc = sender.getLocation();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (isInRange(senderLoc, online.getLocation())) {
                String prefixLuck = getLuckPermsPrefix(sender);
                String formatted = getPlayerFormat(sender)
                        .replace("{prefix}", LOCAL_CHAT_PREFIX)
                        .replace("{prefixLuck}", prefixLuck)
                        .replace("{sender}", sender.getName())
                        .replace("{msgColor}", msgColor)
                        .replace("{message}", formatMentions(sender, message, online, msgColor));
                online.sendMessage(MessageUtil.colorize(formatted));
            }
        }
    }

    private boolean isCEO(Player player) {
        return player.hasPermission("gy-core.chat.ceo");
    }

    private String getPlayerFormat(Player player) {
        return isCEO(player) ? CEO_FORMAT : DEFAULT_FORMAT;
    }

    private String getLuckPermsPrefix(Player player) {
        try {
            RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                LuckPerms api = provider.getProvider();
                User user = api.getUserManager().getUser(player.getUniqueId());
                if (user != null) {
                    String prefix = user.getCachedData().getMetaData().getPrefix();
                    if (prefix != null) {
                        return MessageUtil.colorize(prefix);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String formatMentions(Player sender, String message, Player recipient, String msgColor) {
        String processedMessage = message;

        if (!sender.equals(recipient) && message.toLowerCase().contains(recipient.getName().toLowerCase())) {
            String highlightedName = "#30578C&n" + recipient.getName() + msgColor;
            processedMessage = message.replaceAll("(?i)" + recipient.getName(), highlightedName);
            recipient.playSound(recipient.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.0f);
        }

        return processedMessage;
    }

    private boolean isInRange(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        return loc1.distance(loc2) <= 100;
    }
}
