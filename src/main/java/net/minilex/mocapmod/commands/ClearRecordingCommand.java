package net.minilex.mocapmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ClearRecordingCommand {
    public ClearRecordingCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mc").then(Commands.literal("clear").executes((command) -> {
            return clearRecord(command.getSource());
        })));
    }

    private int clearRecord(CommandSourceStack source) throws CommandSyntaxException {
        System.out.println("============cleared==========");
        return 1;
    }
}
