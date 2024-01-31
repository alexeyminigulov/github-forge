package net.minilex.mocapmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minilex.mocapmod.state.ActorType;
import net.minilex.mocapmod.thread.RecordThread;

public class ChangeActorTypeCommand {
    private RecordThread recordThread;
    public ChangeActorTypeCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mc").then(Commands.literal("actor")
                .then(Commands.argument("type", StringArgumentType.string())
                .executes((command) -> {
                    return clearRecord(command.getSource(), StringArgumentType.getString(command, "type"));
                }))));
    }

    private int clearRecord(CommandSourceStack source, String actorType) throws CommandSyntaxException {
        if (recordThread == null) recordThread = RecordThread.getInstance();
        if (actorType.equalsIgnoreCase("villager")) recordThread.changeActorType(ActorType.VILLAGER);
        if (actorType.equalsIgnoreCase("zombie")) recordThread.changeActorType(ActorType.ZOMBIE);
        if (actorType.equalsIgnoreCase("fox")) recordThread.changeActorType(ActorType.FOX);
        if (actorType.equalsIgnoreCase("rabbit")) recordThread.changeActorType(ActorType.RABBIT);
        return 1;
    }
}
