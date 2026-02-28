package mc.core.basecommands;

import mc.core.GY;
import mc.core.autorestart.AutoRestartCmd;
import mc.core.basecommands.player.*;
import mc.core.basecommands.world.*;
import mc.core.basecommands.world.NightCmd;
import mc.core.basecommands.world.StormCmd;
import mc.core.basecommands.world.SunCmd;
import mc.core.kits.KitCmd;
import mc.core.pvp.command.PvpCmd;
import mc.north.commands.basecommands.CommandManager;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 28.02.2026
 */

public class CommandRegistry {

    public static void registerBaseCommands() {
        CommandManager cm = GY.getInstance().getCommandManager();

        cm.registerCommand(DayCmd.class);
        cm.registerCommand(NearCmd.class);
        cm.registerCommand(NightCmd.class);
        cm.registerCommand(SunCmd.class);
        cm.registerCommand(StormCmd.class);

        cm.registerCommand(TpaCmd.class);
        cm.registerCommand(TpacceptCmd.class);
        cm.registerCommand(TpaDenyCmd.class);
        cm.registerCommand(TpCmd.class);
        cm.registerCommand(TphereCmd.class);

        cm.registerCommand(SetHomeCmd.class);
        cm.registerCommand(HomeCmd.class);
        cm.registerCommand(DelHomeCmd.class);

        cm.registerCommand(VanishCmd.class);
        cm.registerCommand(EcCmd.class);

        cm.registerCommand(GamemodeCmd.class);
        cm.registerCommand(HealCmd.class);
        cm.registerCommand(FeedCmd.class);
        cm.registerCommand(FlyCmd.class);
        cm.registerCommand(GodCmd.class);
        cm.registerCommand(ClearCmd.class);
        cm.registerCommand(PvpCmd.class);

        cm.registerCommand(SetSpawnCmd.class);
        cm.registerCommand(SpawnCmd.class);
        cm.registerCommand(DelSpawnCmd.class);

        cm.registerCommand(SetWarp.class);
        cm.registerCommand(WarpCmd.class);
        cm.registerCommand(DelWarp.class);

        cm.registerCommand(InvseeCmd.class);
        cm.registerCommand(HelperCmd.class);

        cm.registerCommand(BaltopCmd.class);
        cm.registerCommand(BroadcastCmd.class);

        cm.registerCommand(MsgCmd.class);
        cm.registerCommand(ReplyCmd.class);

        cm.registerCommand(CustomizationCmd.class);
        cm.registerCommand(AutoRestartCmd.class);

        cm.registerCommand(KitCmd.class);
        cm.registerCommand(BalanceCmd.class);
        cm.registerCommand(PingCmd.class);
    }
}