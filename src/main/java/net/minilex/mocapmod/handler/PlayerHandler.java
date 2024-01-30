package net.minilex.mocapmod.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
import net.minilex.mocapmod.state.RecordingState;
import net.minilex.mocapmod.thread.FakePlayer;
import net.minilex.mocapmod.thread.Position;
import net.minilex.mocapmod.thread.RecordThread;

public class PlayerHandler {
    public static int tickCount = 0;
    private RecordThread recordThread;
    private static PlayerHandler instance;
    private Minecraft minecraft;

    public PlayerHandler() {
        minecraft = Minecraft.getInstance();
        recordThread = RecordThread.getInstance();
    }
    public void handle() {
        tickCount = 0;
        if (recordThread.getState() == RecordingState.IDLE) {
            minecraft.player.sendSystemMessage(Component.literal("Pressed a Key!!! "));
            recordThread.changeState(RecordingState.RECORDING);
        } else if (recordThread.getState() == RecordingState.RECORDING) {
            recordThread.changeState(RecordingState.STOP);
        } else if (recordThread.getState() == RecordingState.STOP) {
            recordThread.changeState(RecordingState.PLAYING);
        } else if (recordThread.getState() == RecordingState.PLAYING) {
            recordThread.changeState(RecordingState.IDLE);
        }
    }
    public void tick() {
        //if (recordThread != null) tickCount++;
        if (recordThread.getState() == RecordingState.RECORDING) {
            tickCount++;
            if (tickCount % 25 == 0) {
                recordThread.run();
            }
        }
        if (recordThread.getState() == RecordingState.PLAYING) {
            tickCount++;
            FakePlayer fakePlayer = recordThread.fakePlayer;
            if (fakePlayer != null) {
                if (tickCount % 25 == 0) {
                    Position[] position = recordThread.result.toArray(new Position[recordThread.result.size()]);
                    fakePlayer.setPos(
                            position[recordThread.positionIndex].x,
                            position[recordThread.positionIndex].y,
                            position[recordThread.positionIndex].z);
                    fakePlayer.setXRot(position[recordThread.positionIndex].rotX);
                    fakePlayer.setYRot(position[recordThread.positionIndex].rotY);
                    fakePlayer.setYHeadRot(position[recordThread.positionIndex].rotY);
                    System.out.println(position[recordThread.positionIndex].toString());
                    recordThread.positionIndex++;
                    if(recordThread.positionIndex == position.length) recordThread.positionIndex = 0;
                }
            }
        }
    }
    public static PlayerHandler getInstance() {
        if (instance == null) {
            instance = new PlayerHandler();
            return instance;
        }
        return instance;
    }
}
