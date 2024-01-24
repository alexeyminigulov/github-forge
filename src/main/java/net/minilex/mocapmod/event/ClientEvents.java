package net.minilex.mocapmod.event;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.util.KeyBiding;
import net.minilex.mocapmod.thread.RecordThread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = MocapMod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {

        private static RecordThread recordThread;
        private static int tickCount = 0;

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if(KeyBiding.DRINKING_KEY.consumeClick()) {
                if (recordThread == null) {
                    recordThread = RecordThread.getInstance();
                    Path path = Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT);
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Pressed a Key!!!  " + path.toString()));
                    if (!recordThread.capture) {
                        recordThread.capture = true;
                        System.out.println("Start recording");
                    }
                } else if (recordThread.capture) {
                    recordThread.capture = false;
                    System.out.println("Stop recording");
                    //recordThread.stop();
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("The End!!!  "));
                    FileOutputStream file = recordThread.getFile();
                    recordThread.stop();
                    recordThread.read();
                }
            }
        }

        @SubscribeEvent
        public static void onTick(TickEvent tick) {
            if (recordThread != null && recordThread.capture) {
                tickCount++;
                if (tickCount % 100 == 0) {
                    System.out.println("Tick");
                    recordThread.run();
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
