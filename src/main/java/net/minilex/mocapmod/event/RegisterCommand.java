package net.minilex.mocapmod.event;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.commands.ClearRecordingCommand;
import net.minilex.mocapmod.thread.PreLoadParams;
import net.minilex.mocapmod.thread.RecordThread;

@Mod.EventBusSubscriber(modid = MocapMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommand
{
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event)
    {
        PreLoadParams.getInstance().registry(event);
        new ClearRecordingCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
