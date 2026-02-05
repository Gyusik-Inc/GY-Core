package mc.core.basecommands.base;

import lombok.Getter;
import mc.core.GY;
import mc.core.basecommands.impl.player.*;
import mc.core.basecommands.impl.world.*;
import mc.core.pvp.command.PvpCmd;
import mc.core.utilites.chat.MessageUtil;
import mc.core.utilites.math.MathUtil;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommandManager {
    @Getter
    private final Map<String, Long> cooldowns = new HashMap<>();

    public CommandManager() {
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(DayCmd.class);
        registerCommand(NearCmd.class);
        registerCommand(NightCmd.class);
        registerCommand(SunCmd.class);
        registerCommand(StormCmd.class);

        registerCommand(TpaCmd.class);
        registerCommand(TpacceptCmd.class);
        registerCommand(TpaDenyCmd.class);
        registerCommand(TpCmd.class);
        registerCommand(TphereCmd.class);

        registerCommand(SetHomeCmd.class);
        registerCommand(HomeCmd.class);
        registerCommand(DelHomeCmd.class);

        registerCommand(VanishCmd.class);
        registerCommand(EcCmd.class);

        registerCommand(GamemodeCmd.class);
        registerCommand(HealCmd.class);
        registerCommand(FeedCmd.class);
        registerCommand(FlyCmd.class);
        registerCommand(GodCmd.class);
        registerCommand(ClearCmd.class);
        registerCommand(PvpCmd.class);

        registerCommand(SetSpawnCmd.class);
        registerCommand(SpawnCmd.class);
        registerCommand(DelSpawnCmd.class);

        registerCommand(SetWarp.class);
        registerCommand(WarpCmd.class);
        registerCommand(DelWarp.class);

        registerCommand(InvseeCmd.class);
        registerCommand(HelperCmd.class);

        registerCommand(BaltopCmd.class);
        registerCommand(BroadcastCmd.class);

        registerCommand(MsgCmd.class);
        registerCommand(ReplyCmd.class);
        registerCommand(PayCmd.class);
    }

    private void registerCommand(Class<? extends BaseCommand> commandClass) {
        BaseCommandInfo annotation = commandClass.getAnnotation(BaseCommandInfo.class);
        if (annotation == null) return;

        String commandName = annotation.name();

        CommandExecutor executor = new CommandExecutorWrapper(GY.getInstance(), commandClass);
        TabCompleter tabCompleter = (sender, command, alias, args) -> {
            try {
                if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
                    return List.of();
                }

                BaseCommand cmd = commandClass.getDeclaredConstructor().newInstance();
                return cmd.tabComplete(sender, alias, args);
            } catch (Exception e) {
                e.printStackTrace();
                return List.of();
            }
        };


        if (GY.getInstance().getCommand(commandName) != null) {
            Objects.requireNonNull(GY.getInstance().getCommand(commandName)).setExecutor(executor);
            Objects.requireNonNull(GY.getInstance().getCommand(commandName)).setTabCompleter(tabCompleter);
        }
    }


    private static class CommandExecutorWrapper implements CommandExecutor {

        private final GY plugin;
        private final Class<? extends BaseCommand> commandClass;
        private final BaseCommandInfo annotation;

        public CommandExecutorWrapper(GY plugin, Class<? extends BaseCommand> commandClass) {
            this.plugin = plugin;
            this.commandClass = commandClass;
            this.annotation = commandClass.getAnnotation(BaseCommandInfo.class);
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
                MessageUtil.sendMessage(sender, "Нет прав на использование.");
                if (sender instanceof Player player) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                }
                return true;
            }

            if (annotation.cooldown() > 0) {
                String key = getCooldownKey(sender);
                Long lastUse = plugin.getCommandManager().getCooldowns().get(key);

                if (lastUse != null) {
                    long now = System.currentTimeMillis();
                    long cooldownMs = annotation.cooldown() * 1000L;
                    if (now - lastUse < cooldownMs && sender instanceof Player player && !player.hasPermission("gy-core.admin")) {
                        double remaining = (cooldownMs - (now - lastUse)) / 1000.0;
                        MessageUtil.sendMessage(sender, "Подождите ещё &#30578C" + MathUtil.formatTime(remaining));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);

                        return true;
                    }
                }

                plugin.getCommandManager().getCooldowns().put(key, System.currentTimeMillis());
            }

            try {
                BaseCommand cmd = commandClass.getDeclaredConstructor().newInstance();
                return cmd.execute(sender, label, args);
            } catch (Exception e) {
                sender.sendMessage(MessageUtil.colorize("&cОшибка команды!"));
                e.printStackTrace();
            }

            return false;
        }

        private String getCooldownKey(CommandSender sender) {
            return (sender instanceof org.bukkit.entity.Player ?
                    ((org.bukkit.entity.Player) sender).getUniqueId().toString() :
                    sender.getName()) + "_" + annotation.name();
        }
    }
}
