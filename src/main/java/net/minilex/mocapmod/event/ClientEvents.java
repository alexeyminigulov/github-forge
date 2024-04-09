package net.minilex.mocapmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.handler.PlayerHandler;
import net.minilex.mocapmod.particle.ModParticles;
import net.minilex.mocapmod.particle.custom.CitrineParticles;
import net.minilex.mocapmod.state.RecordingState;
import net.minilex.mocapmod.thread.FakePlayer;
import net.minilex.mocapmod.util.KeyBiding;
import net.minilex.mocapmod.util.SceneUtil;
import net.minilex.mocapmod.util.SpeakerIconRender;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = MocapMod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        private static PlayerHandler playerHandler;
        public static float f = 0f;
        public static boolean fire = false;
        public static ItemEntity myTossItem;
        private static CitrineParticles particle;
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
            if(KeyBiding.DRINKING_KEY.consumeClick()) {
                /*if (fire) {
                    EntityData.LIVING_ENTITY_FLAGS.set(playerHandler.getRecordThread().fakePlayer, (byte)1);
                    return;
                }*/

                if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
                playerHandler.handle();
            }
            if(KeyBiding.CHANGE_ACTOR.consumeClick()) {
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Pressed key I "));
                if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
                playerHandler.handleScene();
            }
            if(KeyBiding.SPEAKER_ICON.consumeClick()) {
                if (particle != null) {
                    particle.remove();
                    particle = null;
                    SceneUtil.getInstance().speakerIcon = false;
                    return;
                }
                Player player = Minecraft.getInstance().player;
                particle = (CitrineParticles) Minecraft.getInstance().particleEngine.createParticle(ModParticles.CITRINE_PARTICLES.get(),
                        player.position().x, player.position().y + 2.5d, player.position().z,
                        0, 0.0d, 0);
                if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                        playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                    SceneUtil.getInstance().speakerIcon = true;
                }
            }
        }

        @SubscribeEvent
        public static void onTick(TickEvent tick) {
            if (playerHandler != null)  playerHandler.tick();

            pickItemFakePlayer();
            /*if (fire) {
                f += 0.01f;
                if (f > 40.0f) {
                    FakePlayer fakePlayer = (FakePlayer)playerHandler.getRecordThread().fakePlayer;
                    BowItem bowItem = (BowItem) fakePlayer.getUseItem().getItem();
                    //fakePlayer.startUsingItem(fakePlayer.getUsedItemHand());
                    fakePlayer.getAbilities().instabuild = true;
                    bowItem.releaseUsing(fakePlayer.getUseItem(), Minecraft.getInstance().getSingleplayerServer().overworld(), fakePlayer, 71986);
                    EntityData.LIVING_ENTITY_FLAGS.set(fakePlayer, (byte)0);
                    f = 0;
                    fakePlayer.getAbilities().instabuild = false;
                }
            }*/
        }

        @SubscribeEvent
        public static void onArrowLooseEvent(ArrowLooseEvent event) {
            if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
            if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING) {
                playerHandler.getRecordThread().data.setArrowLoose(event);
            }
            if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                    playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                SceneUtil.getInstance().dataForMainPlayer.setArrowLoose(event);
            }
        }

        @SubscribeEvent
        public static void onItemTossEvent(ItemTossEvent event) {
            if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
            if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING) {
                playerHandler.getRecordThread().data.setTossItem(event);
            }
            if (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE ||
                    playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE) {
                SceneUtil.getInstance().dataForMainPlayer.setTossItem(event);
            }
            if (playerHandler.getRecordThread().getState() == RecordingState.PLAYING
                    && event.getPlayer().getUUID().compareTo(Minecraft.getInstance().player.getUUID()) == 0) {
                myTossItem = event.getEntity();
            }
        }

        @SubscribeEvent
        public static void onRenderName(RenderNameTagEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            boolean isSpeakPlayer = SceneUtil.getInstance().isPlayerSpeak(event.getEntity().getUUID());
            if (!isSpeakPlayer) return;
            SpeakerIconRender.instance().renderSpeakerIcon(event.getEntity(), event.getContent(), event.getPoseStack(), event.getMultiBufferSource());
        }

        private static void pickItemFakePlayer() {
            // the Fakeplayer can pick up an item that you have thrown
            if (myTossItem != null && playerHandler.getRecordThread().fakePlayer != null) {
                if (myTossItem != null && myTossItem.getAge() > 15) {
                    FakePlayer player = ((FakePlayer) playerHandler.getRecordThread().fakePlayer);
                    player.aiStep();
                    if (myTossItem != null && myTossItem.getAge() == 40) {
                        myTossItem = null;
                    }
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MocapMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(KeyBiding.DRINKING_KEY);
        }
    }
}
