package net.minilex.mocapmod.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minilex.mocapmod.mixin.LivingEntityMixin;
import net.minilex.mocapmod.state.BuildBlock;
import net.minilex.mocapmod.state.RecordingState;
import net.minilex.mocapmod.thread.FakePlayer;
import net.minilex.mocapmod.thread.Position;
import net.minilex.mocapmod.thread.RecordThread;
import net.minilex.mocapmod.util.CommandUtil;
import net.minilex.mocapmod.util.EntityData;
import net.minilex.mocapmod.util.SceneUtil;

public class PlayerHandler {
    public static int tickCount = 0;
    private RecordThread recordThread;
    private static PlayerHandler instance;
    private Minecraft minecraft;
    private LivingEntity entity;

    public PlayerHandler() {
        minecraft = Minecraft.getInstance();
        recordThread = RecordThread.getInstance();
    }
    public void handle() {
        tickCount = 0;
        if (recordThread.getState() == RecordingState.EMPTY) {
            minecraft.player.sendSystemMessage(Component.literal("Pressed a Key!!! "));
            recordThread.changeState(RecordingState.RECORDING);
        } else if (recordThread.getState() == RecordingState.RECORDING) {
            recordThread.changeState(RecordingState.STOP);
        } else if (recordThread.getState() == RecordingState.STOP) {
            entity = recordThread.fakePlayer;
            recordThread.changeState(RecordingState.PLAYING);
        } else if (recordThread.getState() == RecordingState.PLAYING) {
            recordThread.changeState(RecordingState.STOP);
        }
    }
    public void handleScene() {
        tickCount = 0;
        if (CommandUtil.Action.PLAYING == CommandUtil.getInstance().action) {
            recordThread.changeState(RecordingState.PLAYING_SCENE);
        }
        else if (CommandUtil.Action.RECORDING == CommandUtil.getInstance().action) {
            recordThread.changeState(RecordingState.RECORDING_SCENE);
        }
        else if (CommandUtil.Action.STOP == CommandUtil.getInstance().action) {
            recordThread.changeState(RecordingState.STOP_SCENE);
        }
        else if (CommandUtil.Action.EDIT == CommandUtil.getInstance().action) {
            recordThread.changeState(RecordingState.EDIT_SCENE);
        }
    }
    public void changedActor() {
        if (recordThread.getState() == RecordingState.STOP) {
            tickCount = 0;
            recordThread.changedActor();
        }
    }
    public void tick() {
        if (recordThread.getState() == RecordingState.RECORDING) {
            tickCount++;
            if (tickCount % 25 == 0) {
                recordThread.run();
            }
        }
        if (recordThread.getState() == RecordingState.PLAYING) {
            tickCount++;
            LivingEntity fakePlayer = recordThread.fakePlayer;
            if (fakePlayer != null) {
                if (tickCount % 25 == 0) {
                    Position[] position = recordThread.result.toArray(new Position[recordThread.result.size()]);
                    fakePlayer.setPos(
                            position[recordThread.positionIndex].x,
                            position[recordThread.positionIndex].y,
                            position[recordThread.positionIndex].z);
                    fakePlayer.setXRot(position[recordThread.positionIndex].rotX);
                    fakePlayer.setYRot(position[recordThread.positionIndex].rotY);
                    fakePlayer.setYBodyRot(position[recordThread.positionIndex].yBodyRot);
                    fakePlayer.setYHeadRot(position[recordThread.positionIndex].yHeadRot);
                    recordThread.setLoot(position[recordThread.positionIndex]);
                    ((LivingEntityMixin)fakePlayer).callDetectEquipmentUpdates();
                    if (position[recordThread.positionIndex].swinging) fakePlayer.swing(InteractionHand.MAIN_HAND);
                    if (position[recordThread.positionIndex].isBowPulling) {
                        EntityData.LIVING_ENTITY_FLAGS.set(fakePlayer, (byte)1);
                    } else if (fakePlayer.getUseItemRemainingTicks() == 0
                            && position[recordThread.positionIndex].looseArrowStrength != 0) {
                        BowItem bowItem = (BowItem) Items.BOW;
                        ((FakePlayer)fakePlayer).getAbilities().instabuild = true;
                        bowItem.releaseUsing(new ItemStack(bowItem),
                                Minecraft.getInstance().getSingleplayerServer().overworld(), fakePlayer,
                                position[recordThread.positionIndex].looseArrowStrength);
                        ((FakePlayer)fakePlayer).getAbilities().instabuild = false;
                        EntityData.LIVING_ENTITY_FLAGS.set(fakePlayer, (byte)0);
                    }
                    if (position[recordThread.positionIndex].tossItem != null) {
                        ((FakePlayer) fakePlayer).drop(new ItemStack(Item.byId(position[recordThread.positionIndex].tossItem.itemID)), true);
                    }
                    if (position[recordThread.positionIndex].buildBlock != null) {
                        if (position[recordThread.positionIndex].buildBlock.getAction() == BuildBlock.Action.PLACE)
                            position[recordThread.positionIndex].buildBlock.placeBlock();
                        else position[recordThread.positionIndex].buildBlock.breakBlock();
                    }
                    recordThread.positionIndex++;
                    if(recordThread.positionIndex == position.length) {
                        recordThread.positionIndex = 0;
                        recordThread.clearMap();
                    }
                }
            }
        }
        if (recordThread.getState() == RecordingState.PLAYING_SCENE) {
            tickCount++;
            if (tickCount % 25 == 0) {
                SceneUtil.getInstance().runScene();
            }
        }
        if (recordThread.getState() == RecordingState.RECORDING_SCENE) {
            tickCount++;
            if (tickCount % 25 == 0) {
                SceneUtil.getInstance().tickRecord();
            }
        }
        if (recordThread.getState() == RecordingState.EDIT_SCENE) {
            tickCount++;
            if (tickCount % 25 == 0) {
                SceneUtil.getInstance().editSceneTick();
            }
        }
    }
    public RecordThread getRecordThread() {
        return recordThread;
    }
    public static PlayerHandler getInstance() {
        if (instance == null) {
            instance = new PlayerHandler();
            return instance;
        }
        return instance;
    }
}
