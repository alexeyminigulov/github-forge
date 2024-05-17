package net.minilex.mocapmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.handler.PlayerHandler;
import net.minilex.mocapmod.item.ModItems;
import net.minilex.mocapmod.state.RecordingState;
import net.minilex.mocapmod.state.SceneData;
import net.minilex.mocapmod.util.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = MocapMod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        private static PlayerHandler playerHandler;
        public static float f = 0f;
        public static boolean fire = false;
        private static Set<ItemEntity> myTossItem = new HashSet<ItemEntity>();;
        private static Set<TossedItem> myTossItemList = new HashSet<TossedItem>();;
        private static boolean microphone = false;
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
            if(KeyBiding.IGNORE_ATTACK_KEY.consumeClick()) {
                if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
                if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                        playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                    SceneUtil.getInstance().ignoreAttack = !SceneUtil.getInstance().ignoreAttack;
                }
            }
            if(KeyBiding.EXECUTE_COMMAND_KEY.consumeClick()) {
                if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
                playerHandler.handleScene();
            }
            if(KeyBiding.SPEAKER_ICON.consumeClick()) {
                if (microphone) {
                    if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                            playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                        SceneUtil.getInstance().speakerIcon = false;
                    }
                    microphone = false;
                } else {
                    if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                            playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                        SceneUtil.getInstance().speakerIcon = true;
                    }
                    microphone = true;
                }
            }
            if(KeyBiding.GET_DAMAGE_KEY.consumeClick()) {
                DamageMainPlayer.getInstance().handle();
            }
        }

        @SubscribeEvent
        public static void onTick(TickEvent tick) {
            if (playerHandler != null)  playerHandler.tick();

            pickItemFakePlayer();
        }

        @SubscribeEvent
        public static void onArrowLooseEvent(ArrowLooseEvent event) {
            if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
            if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                    playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                SceneUtil.getInstance().dataForMainPlayer.setArrowLoose(event);
            }
        }

        @SubscribeEvent
        public static void onItemTossEvent(ItemTossEvent event) {
            if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
            if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                    playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                SceneUtil.getInstance().dataForMainPlayer.setTossItem(event);
            }
            if (playerHandler.getRecordThread().getState() == RecordingState.PLAYING_SCENE
                    && event.getPlayer().getUUID().compareTo(Minecraft.getInstance().player.getUUID()) == 0) {
                myTossItem.add(event.getEntity());
                myTossItemList.add(new TossedItem(event.getEntity()));
            }
        }
        @SubscribeEvent
        public static void onEntityItemPickupEvent(EntityItemPickupEvent event) {
            if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
            if (playerHandler.getRecordThread().getState() == RecordingState.PLAYING_SCENE) {
                if (myTossItem.isEmpty()) return;
                myTossItem.remove(event.getItem());
            }
        }

        @SubscribeEvent
        public static void onRenderName(RenderNameTagEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            boolean isSpeakPlayer = SceneUtil.getInstance().isPlayerSpeak(event.getEntity().getUUID());
            if (!isSpeakPlayer) return;
            SpeakerIconRender.instance().renderSpeakerIcon(event.getEntity(), event.getContent(), event.getPoseStack(), event.getMultiBufferSource());
        }
        @SubscribeEvent
        public static void renderHeldGun(RenderPlayerEvent.Pre event)
        {
            if (event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getItem().getDescriptionId().compareTo(ModItems.SAPPHIRE_STAFF.get().getDescriptionId()) == 0) {
                PlayerRenderer render = event.getRenderer();
                PlayerModel<AbstractClientPlayer> model = render.getModel();
                model.rightArmPose= HumanoidModel.ArmPose.BOW_AND_ARROW;
                model.leftArmPose= HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }
        @SubscribeEvent
        public static void onRenderHUD(RenderGuiEvent event) {
            if (microphone) MicrophoneIconRender.instance().onRenderHUD(event.getPoseStack());
        }

        private static void pickItemFakePlayer() {
            // the Fakeplayer can pick up an item that you have thrown
            if (!myTossItem.isEmpty() && isAllPlayerCreated(SceneUtil.getInstance().scene)) {
                Set<SceneData> scene = SceneUtil.getInstance().scene;
                for (SceneData sceneData : scene) {
                    if (anyNearTossItem(myTossItem, sceneData.fakePlayer)) {
                        System.out.println("Ai Step");
                        sceneData.fakePlayer.aiStep();
                    }
                }
                checkTossItem();
            }
        }
        private static void checkTossItem() {
            if (myTossItemList.isEmpty()) return;
            for (TossedItem item : myTossItemList) {
                if (item.oldAge != 0 && item.tick > 0 && item.item.getAge() == item.oldAge && !item.deprecated) {
                    item.tick--;
                    if (item.tick == 0) {
                        item.deprecated = true;
                    }
                } else if (!item.deprecated) {
                    item.tick = 100;
                    item.oldAge = item.item.getAge();
                }
            }
        }
        private static boolean anyNearTossItem(Set<ItemEntity> items, LivingEntity player) {
            if (items.isEmpty()) return false;
            for (ItemEntity item : items) {
                double d = distance(item.position(), player.position());
                if (d < 1.5d && !isblackedList(item)) {
                    return true;
                }
            }
            return false;
        }
        private static boolean isblackedList(ItemEntity entity) {
            if (myTossItemList.isEmpty()) return false;
            for (TossedItem item : myTossItemList) {
                if (item.item.getUUID().compareTo(entity.getUUID()) == 0 && item.deprecated) return true;
            }
            return false;
        }
        private static boolean isAllPlayerCreated(Set<SceneData> scene) {
            if (scene == null || scene.isEmpty()) return false;
            int size = scene.size();
            List<SceneData> sceneList = new ArrayList<>(scene);
            return sceneList.get(size-1).fakePlayer != null;
        }
        public static double distance(Vec3 entity1, Vec3 entity2) {
            double ac = Math.abs(entity2.z - entity1.z);
            double cb = Math.abs(entity2.x - entity1.x);
            return Math.hypot(ac, cb);
        }
    }

    @Mod.EventBusSubscriber(modid = MocapMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBiding.IGNORE_ATTACK_KEY);
        }
    }
}
