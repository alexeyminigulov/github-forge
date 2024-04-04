package net.minilex.mocapmod.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBiding {
    public static final String KEY_CATEGORY_TUTORIAL = "key.category.mocapmod.tutorial";
    public static final String KEY_DRINK_WATER = "key.mocapmod.drink_water";
    public static final String KEY_CHANGE_ACTOR = "key.mocapmod.change_actor";
    public static final String KEY_SPEAKER_ICON = "key.mocapmod.speaker_icon";

    public static final KeyMapping DRINKING_KEY = new KeyMapping(KEY_DRINK_WATER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY_TUTORIAL);
    public static final KeyMapping CHANGE_ACTOR = new KeyMapping(KEY_CHANGE_ACTOR, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_I, KEY_CATEGORY_TUTORIAL);
    public static final KeyMapping SPEAKER_ICON = new KeyMapping(KEY_SPEAKER_ICON, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY_TUTORIAL);
}
