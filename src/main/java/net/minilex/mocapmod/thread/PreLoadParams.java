package net.minilex.mocapmod.thread;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;

import javax.annotation.Nullable;

public class PreLoadParams {
    private static PreLoadParams instance;
    public CommandDispatcher<CommandSourceStack> dispatcher;
    public CommandBuildContext cbc;

    public CommandSourceStack commandSource;

    public PreLoadParams() {
    }

    public void registry(RegisterCommandsEvent event) {
        dispatcher = event.getDispatcher();
        cbc = event.getBuildContext();
    }

    public static PreLoadParams getInstance() {
        if (instance == null) {
            instance = new PreLoadParams();
            return instance;
        }
        return instance;
    }
}
