package net.minilex.mocapmod.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.util.UUID;

public class CommandUtil {
    public static CommandUtil instance;
    public Action action;
    private String sceneName;
    private String saveSceneName;
    private ScriptObject[] scriptObjects;
    private static int idxScript = 0;
    private CommandUtil() {
        this.action = Action.STOP;
        this.sceneName = "scene_one";
        this.saveSceneName = "saved_scene";
    }
    public enum Action {
        PLAYING,
        RECORDING,
        STOP,
        EDIT
    }
    public void playing(String fileName) {
        this.sceneName = fileName;
        this.action = Action.PLAYING;
    }
    public void recording(String sceneName) {
        this.sceneName = sceneName;
        this.action = Action.RECORDING;
    }
    public void stop() {
        this.action = Action.STOP;
    }
    public void edit(String sceneName, String saveFileName) {
        this.sceneName = sceneName;
        this.saveSceneName = saveFileName;
        this.action = Action.EDIT;
    }
    public String getSceneName() {
        return this.sceneName;
    }
    public String getSaveSceneName() {
        return this.saveSceneName;
    }
    public void addScript(String scriptName) {
        this.getScriptElements(scriptName);
    }
    public ScriptObject getNextScriptElement() {
        if (this.scriptObjects == null) {
            String randomName = UUID.randomUUID().toString().substring(0, 4);
            String textureValue = "ewogICJ0aW1lc3RhbXAiIDogMTY4OTUzNDQ4NjI0NywKICAicHJvZmlsZUlkIiA6ICI1YjFiZGQxNDU1MzU0MmM3ODQ5MTMzY2VmODJmMWYwNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJnaW5hdGV0ZXN0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc1YjVmZDRmNTE1OWI5MzAxMTg3OWY5NWYyMTA0NDc4OGRmNjVlYTgwYjVjMmZmYTU4NTcxODZlMzEwODZhOWQiCiAgICB9CiAgfQp9";
            String signature = "nAUoIKXm0aPWo9CAa7kuuDdQfT9ZVM3tz42MBSD3/vUeFNAAWrOorGYjXKhHF280nfDmtkdyO43SE3ZuFPDtpZ/u3LfXl9OZkv4Jc/wNQT6qvVnQ+09cI05OlMNAozAZdbrvgz4Dp1NGTabDS6dlWEdIj1EmRGGr0IgVUZGLqdcUaTguTH7boPryiiZDNN8BzPaYe3sYmbEcXSKz6gmoQ/pq/g4FhKfFkP9NK7S6dNO0Xh8zlkTr578wU07sLaegB0E0gxOvQHc0tiXED8+bwRgZstGtMBayuDDlh+O/haru8xt6H7x6dyi1Xo8wU36IHxvKcVLNMd/nYXrqxKatjqF6YsMsUjGxTRMWGDxuQIQ6wuNC3KtapOdXudd8ayfNP2wmTzDhg7N7Ei1dxgXCxcjixXcjcO5LYvNpyFRVsbKTdKy2fwsgoInUY/woOC0K7hLBbXTjzYEptrCqIzfegxlk3aM5x2/sYLUYVUkn1ubR3XtX+eR1tqCgHOmO+TJTe2AdNhriD9ILvUM3NdFVz1iqhH1vm9oTb9PGTzgDrmTpOJ4S8qfgwLIFI2POY0os4zmepC9tje5F9j8vRprvigdej3bT2Hltb/8Ov5CnJJQ8tfYsrytO9YNdYw2YB1kSKV/iaZsP0TPiVzCs9qgOMOziJ344KbhxiY61yijCBQY=";
            boolean aiStep = false;
            ChatFormatting nameColor = ChatFormatting.WHITE;
            return new ScriptObject(randomName, 12.0f, textureValue, signature, aiStep, nameColor);
        }
        if (idxScript >= this.scriptObjects.length) idxScript = 0;
        idxScript++;
        return this.scriptObjects[idxScript-1];
    }
    private void getScriptElements(String scriptName) {
        File dir = new File(Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT) + "/mocaps/scripts");
        try {
            File file = new File(dir.getAbsolutePath() + "/" + scriptName + ".json");
            JsonElement parser = JsonParser.parseReader(new FileReader(file));
            JsonObject jsonObject = parser.getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("actors");
            this.scriptObjects = new ScriptObject[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                String name = jsonArray.get(i).getAsJsonObject().get("name").getAsString();
                float health = jsonArray.get(i).getAsJsonObject().get("health").getAsFloat();
                String skinValue = jsonArray.get(i).getAsJsonObject().get("skinValue").getAsString();
                String signature = jsonArray.get(i).getAsJsonObject().get("signature").getAsString();
                boolean aiStep = jsonArray.get(i).getAsJsonObject().get("aiStep").getAsBoolean();
                String color = jsonArray.get(i).getAsJsonObject().get("nameColor").getAsString();
                ChatFormatting nameColor = switch (color) {
                    case "white" -> ChatFormatting.WHITE;
                    case "red" -> ChatFormatting.RED;
                    case "blue" -> ChatFormatting.BLUE;
                    case "gold" -> ChatFormatting.GOLD;
                    case "yellow" -> ChatFormatting.YELLOW;
                    case "grey" -> ChatFormatting.GRAY;
                    case "green" -> ChatFormatting.DARK_GREEN;
                    default -> ChatFormatting.WHITE;
                };
                this.scriptObjects[i] = new ScriptObject(name, health, skinValue, signature, aiStep, nameColor);
            }
        } catch (FileNotFoundException e) {
        }
    }
    public static CommandUtil getInstance() {
        if (instance == null) {
            instance = new CommandUtil();
            return instance;
        }
        return instance;
    }
    public class ScriptObject {
        public String name;
        public float health;
        public String skinValue;
        public String signature;
        public boolean aiStep;
        public ChatFormatting nameColor;
        public ScriptObject(String name, float health, String skinValue, String signature, boolean aiStep, ChatFormatting nameColor) {
            this.name = name;
            this.health = health;
            this.skinValue = skinValue;
            this.signature = signature;
            this.aiStep = aiStep;
            this.nameColor = nameColor;
        }
    }
}
