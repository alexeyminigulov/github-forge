package net.minilex.mocapmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minilex.mocapmod.handler.PlayerHandler;
import net.minilex.mocapmod.util.CommandUtil;

public class SceneCommand {
    private PlayerHandler playerHandler;
    public SceneCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mc").then(Commands.literal("scene_play")
                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                .executes((command) -> {
                                    return playScene(command.getSource(), StringArgumentType.getString(command, "scene_name"));
                                }))));
        dispatcher.register(
                Commands.literal("mc").then(Commands.literal("scene_play")
                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                .then(Commands.argument("script_name", StringArgumentType.string())
                                        .executes((command) -> {
                                            return playScene(command.getSource(), StringArgumentType.getString(command, "scene_name"), StringArgumentType.getString(command, "script_name"));
                                        })
                                )
                        )));
        dispatcher.register(
                Commands.literal("mc").then(Commands.literal("scene_play")
                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                .then(Commands.argument("script_name", StringArgumentType.string())
                                        .then(Commands.argument("get_damage", StringArgumentType.string())
                                                .executes((command) -> {
                                                    return playScene(command.getSource(), StringArgumentType.getString(command, "scene_name"), StringArgumentType.getString(command, "script_name"), StringArgumentType.getString(command, "get_damage"));
                                                })
                                        )
                                )
                        )));

        dispatcher.register(
                Commands.literal("mc").then(Commands.literal("scene_record")
                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                .executes((command) -> {
                                    return recordScene(command.getSource(), StringArgumentType.getString(command, "scene_name"));
                                }))));
        dispatcher.register(
                Commands.literal("mc").then(Commands.literal("scene_edit")
                        .then(Commands.argument("scene_name", StringArgumentType.string())
                                .then(Commands.argument("save_name", StringArgumentType.string())
                                        .executes((command) -> {
                                            return editScene(command.getSource(), StringArgumentType.getString(command, "scene_name"), StringArgumentType.getString(command, "save_name"));
                                        })
                                )
                        )));


        dispatcher.register(Commands.literal("mc").then(Commands.literal("scene_stop").executes((command) -> {
            return stopScene(command.getSource());
        })));
    }

    private int playScene(CommandSourceStack source, String sceneName, String... scriptName) throws CommandSyntaxException {
        //if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
        CommandUtil.getInstance().playing(sceneName);
        if (scriptName.length >= 1) CommandUtil.getInstance().addScript(scriptName[0]);
        if (scriptName.length == 2) CommandUtil.getInstance().setDamage(scriptName[1]);
        return 1;
    }
    private int recordScene(CommandSourceStack source, String sceneName) throws CommandSyntaxException {
        //if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
        CommandUtil.getInstance().recording(sceneName);
        return 1;
    }
    private int editScene(CommandSourceStack source, String sceneName, String saveName) throws CommandSyntaxException {
        CommandUtil.getInstance().edit(sceneName, saveName);
        return 1;
    }
    private int stopScene(CommandSourceStack source) throws CommandSyntaxException {
        CommandUtil.getInstance().stop();
        return 1;
    }
}
