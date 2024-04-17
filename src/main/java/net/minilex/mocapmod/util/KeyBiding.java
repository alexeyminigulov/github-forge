package net.minilex.mocapmod.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBiding {
    public static final String KEY_CATEGORY_TUTORIAL = "key.category.mocapmod.tutorial";
    public static final String KEY_IGNORE_ATTACK = "key.mocapmod.ignore_attack";
    public static final String KEY_EXECUTE_COMMAND = "key.mocapmod.execute_command";
    public static final String KEY_SPEAKER_ICON = "key.mocapmod.speaker_icon";
    public static final String KEY_GET_DAMAGE = "key.mocapmod.get_damage";

    public static final KeyMapping IGNORE_ATTACK_KEY = new KeyMapping(KEY_IGNORE_ATTACK, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY_TUTORIAL);
    public static final KeyMapping EXECUTE_COMMAND_KEY = new KeyMapping(KEY_EXECUTE_COMMAND, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY_TUTORIAL);
    public static final KeyMapping SPEAKER_ICON = new KeyMapping(KEY_SPEAKER_ICON, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY_TUTORIAL);
    public static final KeyMapping GET_DAMAGE_KEY = new KeyMapping(KEY_GET_DAMAGE, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY_TUTORIAL);
}
