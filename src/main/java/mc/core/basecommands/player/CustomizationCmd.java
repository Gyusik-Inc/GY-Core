package mc.core.basecommands.player;

import mc.core.GY;
import mc.north.commands.basecommands.BaseCommand;
import mc.north.commands.basecommands.BaseCommandInfo;

import mc.core.utilites.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@BaseCommandInfo(name = "customization", permission = "gy-core.customization", cooldown = 5)
public class CustomizationCmd implements BaseCommand {

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length != 2) {
            GY.msg.sendMessage(player, "Использование: &#30578C/customization <msg|tp|pay|scoreboard|mention|damage-text|enchant-text|modern-eco> <enable|disable>");
            return true;
        }

        String feature = args[0].toLowerCase();
        String action = args[1].toLowerCase();

        PlayerData data = new PlayerData(player.getUniqueId());
        boolean enable;

        if (action.equals("enable")) enable = true;
        else if (action.equals("disable")) enable = false;
        else {
            GY.msg.sendMessage(player, "&cДоступные действия: enable, disable");
            return true;
        }

        switch (feature) {
            case "msg" -> {
                boolean current = data.isMsgEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Личные сообщения уже " + (enable ? "&aвключены." : "&cотключены."));
                } else {
                    data.setMsgEnabled(enable);
                    GY.msg.sendMessage(player, "Личные сообщения " + (enable ? "&aвключены." : "&cотключены."));
                }
            }
            case "modern-eco" -> {
                boolean current = data.isModernEcoEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Современный дизайн экономики уже " + (enable ? "&aвключен." : "&cотключен."));
                } else {
                    data.setModernEcoEnabled(enable);
                    GY.msg.sendMessage(player, "Современный дизайн экономики " + (enable ? "&aвключен." : "&cотключен."));
                }
            }
            case "damage-text" -> {
                boolean current = data.isDamageTextEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Текст с уроном при ударе уже " + (enable ? "&aвключен." : "&cотключен."));
                } else {
                    data.setDamageTextEnabled(enable);
                    GY.msg.sendMessage(player, "Текст с уроном при ударе " + (enable ? "&aвключен." : "&cотключен."));
                }
            }
            case "enchant-text" -> {
                boolean current = data.isEnchantTextEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Текст зачарований уже " + (enable ? "&aвключен." : "&cотключен."));
                } else {
                    data.setEnchantTextEnabled(enable);
                    GY.msg.sendMessage(player, "Текст зачарований " + (enable ? "&aвключен." : "&cотключен."));
                }
            }
            case "tp" -> {
                boolean current = data.isTpEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Запросы на телепортацию уже " + (enable ? "&aвключены." : "&cотключены."));
                } else {
                    data.setTpEnabled(enable);
                    GY.msg.sendMessage(player, "Запросы на телепортацию " + (enable ? "&aвключены." : "&cотключены."));
                }
            }
            case "pay" -> {
                boolean current = data.isPayEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Переводы денег уже " + (enable ? "&aвключены." : "&cотключены."));
                } else {
                    data.setPayEnabled(enable);
                    GY.msg.sendMessage(player, "Переводы денег " + (enable ? "&aвключены." : "&cотключены."));
                }
            }
            case "scoreboard" -> {
                boolean current = data.isScoreboardEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Scoreboard уже " + (enable ? "&aвключён." : "&cотключён."));
                } else {
                    data.setScoreboardEnabled(enable);
                    GY.msg.sendMessage(player, "Scoreboard " + (enable ? "&aвключён." : "&cотключён."));
                    Bukkit.getServer().dispatchCommand(player, "sb");
                }
            }
            case "mention" -> {
                boolean current = data.isMentionSoundEnabled();
                if (enable == current) {
                    GY.msg.sendMessage(player, "Звук при упоминании в чате уже " + (enable ? "&aвключён." : "&cотключён."));
                } else {
                    data.setMentionSoundEnabled(enable);
                    GY.msg.sendMessage(player, "Звук при упоминании в чате " + (enable ? "&aвключён." : "&cотключён."));
                }
            }
            default -> GY.msg.sendMessage(player, "&cДоступные функции: msg, tp, pay, scoreboard, mention, damage-text, enchant-text, modern-eco");
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) return List.of("msg", "tp", "pay", "scoreboard", "mention", "damage-text", "enchant-text", "modern-eco");
        if (args.length == 2) return List.of("enable", "disable");
        return List.of();
    }
}