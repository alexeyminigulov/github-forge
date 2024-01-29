package net.minilex.mocapmod.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.handler.PlayerHandler;
import net.minilex.mocapmod.util.KeyBiding;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = MocapMod.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        private static PlayerHandler playerHandler;
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if(KeyBiding.DRINKING_KEY.consumeClick()) {
                if (playerHandler == null) playerHandler = PlayerHandler.getInstance();
                playerHandler.handle();
            }
        }

        @SubscribeEvent
        public static void onTick(TickEvent tick) {
            if (playerHandler != null) playerHandler.tick();
        }

        @SubscribeEvent
        public static void onStartedServer(ServerStartedEvent event) {
           // playerHandler = PlayerHandler.getInstance();
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
