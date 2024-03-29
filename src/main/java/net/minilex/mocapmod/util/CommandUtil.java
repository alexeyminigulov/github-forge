package net.minilex.mocapmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;
import net.minilex.mocapmod.handler.PlayerHandler;
import net.minilex.mocapmod.state.SceneData;
import net.minilex.mocapmod.thread.RecordThread;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class CommandUtil {
    public static CommandUtil instance;
    public Action action;
    private String sceneName;
    private String saveSceneName;
    private CommandUtil() {
        this.action = Action.PLAYING;
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
    public static CommandUtil getInstance() {
        if (instance == null) {
            instance = new CommandUtil();
            return instance;
        }
        return instance;
    }
}
