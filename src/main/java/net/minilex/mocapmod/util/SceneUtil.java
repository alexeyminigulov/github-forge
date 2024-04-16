package net.minilex.mocapmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.state.EquippedItem;
import net.minilex.mocapmod.state.SceneData;
import net.minilex.mocapmod.state.StatusInventory;
import net.minilex.mocapmod.state.TossItem;
import net.minilex.mocapmod.thread.Position;
import net.minilex.mocapmod.thread.RecordThread;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class SceneUtil {
    private Player mainPlayer;
    public Set<SceneData> scene;
    private SceneData recordingMainPlayer;
    public Data dataForMainPlayer = new Data();
    private StatusInventory statusInventory;
    private boolean isLootSet = true;
    public boolean speakerIcon = false;
    public boolean ignoreAttack = false;
    private FileOutputStream file;
    private ObjectOutputStream o;
    private static SceneUtil instance;
    private SceneUtil(){
        mainPlayer = Minecraft.getInstance().player;
    }

    public static SceneUtil getInstance() {
        if (instance == null) {
            instance = new SceneUtil();
        }
        return instance;
    }
    public void runScene() {
        for (SceneData sceneData : scene) {
            sceneData.run();
        }
        SceneData.tickCount++;
    }
    public void editScene() {
        this.initScene();
        this.initFile(CommandUtil.getInstance().getSaveSceneName());
    }
    public void editSceneTick() {
        for (SceneData sceneData : scene) {
            sceneData.run();
        }
        this.tickRecord();
        SceneData.tickCount++;
    }
    public void stopScene() {
        clearMap();
    }
    public void startRecord() {
        this.initFile(CommandUtil.getInstance().getSceneName());
    }
    public void tickRecord() {
        try {
            this.trackAndWriteMovement();
        } catch (Exception e) {

        }
    }
    public void saveScene() {
        Set<SceneData> result = new HashSet<SceneData>();
        result.add(recordingMainPlayer);
        this.saveFile(result);
        this.clearMap();
    }
    public void saveSceneEdit() {
        Set<SceneData> result = new HashSet<SceneData>();
        result.add(recordingMainPlayer);
        for (SceneData sceneData : scene) {
            result.add(sceneData.getEditSceneData());
        }
        this.saveFile(result);
        this.clearMap();
    }
    public boolean initScene() {
        scene = this.getScene();
        if (scene == null) {
            String msg = "Scene " + CommandUtil.getInstance().getSceneName() + " don't exist";
            Minecraft.getInstance().player.sendSystemMessage(Component.literal(msg));
            return false;
        }
        for (SceneData sceneData : scene) {
            sceneData.init();
        }
        return true;
    }
    public boolean isPlayerSpeak(UUID id) {
        if (scene == null || scene.isEmpty()) return false;
        for (SceneData sceneData : scene) {
            if (sceneData.fakePlayer.getUUID() == id && sceneData.speak) return true;
        }
        return false;
    }
    private void initFile(String capname) {
        try {
            File dir = new File(Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT) + "/" + "mocaps");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new FileOutputStream(dir.getAbsolutePath() + "/" + capname + ".mocap");
            o = new ObjectOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void saveFile(Set<SceneData> dataSet) {
        isLootSet = true;
        try {
            for (SceneData sceneData : dataSet) {
                o.writeObject(sceneData);
            }
            file.close();
            o.close();
        } catch (IOException e) {
            System.out.println("Can't close file");
        }
    }
    public Set<SceneData> getScene() {
        Set<SceneData> result = new HashSet<SceneData>();
        File dir = new File(Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT) + "/" + "mocaps");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(dir.getAbsolutePath() + "/" + CommandUtil.getInstance().getSceneName() + ".mocap");
        if(!f.exists()) {
            return null;
        }
        try {
            FileInputStream fi = new FileInputStream(dir.getAbsolutePath() + "/" + CommandUtil.getInstance().getSceneName() + ".mocap");
            ObjectInputStream oi = new ObjectInputStream(fi);
            try {
                for (;;) {
                    result.add((SceneData) oi.readObject());
                }
            } catch (EOFException e) {
                // End of stream
            }
            oi.close();
            fi.close();
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
        return result;
    }
    private void trackAndWriteMovement() throws IOException {
        if (recordingMainPlayer == null) {
            recordingMainPlayer = new SceneData(new LinkedHashSet<>());
        }
        Vec3 entityPos = mainPlayer.position();
        Vec2 entityRot = mainPlayer.getRotationVector();
        Position pos = new Position(entityPos.x, entityPos.y, entityPos.z,
                entityRot.x, entityRot.y, mainPlayer.getVisualRotationYInDegrees(), mainPlayer.yHeadRot);
        if (RecordThread.getInstance().buildBlock != null) {
            pos.buildBlock = RecordThread.getInstance().buildBlock;
            RecordThread.getInstance().buildBlock = null;
        }
        if (isLootSet) {
            mainPlayer.getInventory().armor.forEach((ItemStack is) -> {
                int index = mainPlayer.getInventory().armor.indexOf(is);
                EquipmentSlot slot = switch (index) {
                    case 0 -> EquipmentSlot.FEET;
                    case 1 -> EquipmentSlot.LEGS;
                    case 2 -> EquipmentSlot.CHEST;
                    case 3 -> EquipmentSlot.HEAD;
                    default -> null;
                };
                EquippedItem item = new EquippedItem(Item.getId(is.getItem()), slot.getFilterFlag());
                pos.addEquippedItem(item);
            });

            Item offhand = ((ItemStack)mainPlayer.getInventory().offhand.toArray()[0]).getItem();
            pos.addEquippedItem(new EquippedItem(Item.getId(offhand), EquipmentSlot.OFFHAND.getFilterFlag()));

            try {
                Field nameField = mainPlayer.getClass().getSuperclass().getSuperclass().getDeclaredField("lastItemInMainHand");
                nameField.setAccessible(true);
                Item itemMainHand = ((ItemStack)nameField.get(mainPlayer)).getItem();
                pos.addEquippedItem(new EquippedItem(Item.getId(itemMainHand), EquipmentSlot.MAINHAND.getFilterFlag()));
            } catch (Exception e) {

            }
            statusInventory = new StatusInventory(mainPlayer.getMainHandItem(), ((ItemStack)mainPlayer.getInventory().offhand.toArray()[0]), mainPlayer.getInventory().armor);

            isLootSet = false;
        }
        statusInventory.tickUpdate(mainPlayer.getMainHandItem(), ((ItemStack)mainPlayer.getInventory().offhand.toArray()[0]), mainPlayer.getInventory().armor);
        EquippedItem equippedItem = statusInventory.getUpdatedItem();
        if (equippedItem != null) {
            pos.addEquippedItem(equippedItem);
        }
        if (mainPlayer.swinging && mainPlayer.swingTime == 0) pos.swinging = true;
        if (Item.getId(Items.BOW) == Item.getId(mainPlayer.getUseItem().getItem()) && mainPlayer.isUsingItem()) {
            pos.isBowPulling = true;
        }
        if (!dataForMainPlayer.isArrowLooseEmpty()) {
            pos.looseArrowStrength = dataForMainPlayer.getArrowLooseEvent().getCharge();
        }
        if (!dataForMainPlayer.isTossItemEmpty()) {
            pos.tossItem = new TossItem(dataForMainPlayer.getTossItemEvent().getEntity());
        }
        if (speakerIcon) {
            pos.speakerIcon = true;
        }
        if (ignoreAttack) {
            pos.ignoreAttack = true;
        }

        this.recordingMainPlayer.addPosition(pos);
    }
    private void clearMap() {
        if (scene != null) {
            for(SceneData sceneData : scene) {
                sceneData.fakePlayer.remove(Entity.RemovalReason.KILLED);
            }
            scene = null;
        }
        if (recordingMainPlayer != null) {
            recordingMainPlayer.clearMap();
            recordingMainPlayer = null;
        }
    }
}
