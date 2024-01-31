package net.minilex.mocapmod.event;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.commands.ChangeActorTypeCommand;
import net.minilex.mocapmod.commands.ClearRecordingCommand;
import net.minilex.mocapmod.thread.PreLoadParams;

@Mod.EventBusSubscriber(modid = MocapMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterCommand
{
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event)
    {
        PreLoadParams.getInstance().registry(event);
        new ClearRecordingCommand(event.getDispatcher());
        new ChangeActorTypeCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
