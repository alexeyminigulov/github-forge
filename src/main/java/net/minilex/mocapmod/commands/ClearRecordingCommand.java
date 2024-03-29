package net.minilex.mocapmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minilex.mocapmod.state.RecordingState;
import net.minilex.mocapmod.thread.RecordThread;

public class ClearRecordingCommand {
    private RecordThread recordThread;
    public ClearRecordingCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mc").then(Commands.literal("clear").executes((command) -> {
            return clearRecord(command.getSource());
        })));
    }

    private int clearRecord(CommandSourceStack source) throws CommandSyntaxException {
        if (recordThread == null) recordThread = RecordThread.getInstance();
        recordThread.changeState(RecordingState.EMPTY);
        /*Player player = Minecraft.getInstance().player;
        Minecraft.getInstance().level.addParticle(ModParticles.CITRINE_PARTICLES.get(),
                player.position().x, player.position().y + 2.5d, player.position().z,
                0, 0.001d, 0);*/
        return 1;
    }
}
