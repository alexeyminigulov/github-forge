package net.minilex.mocapmod.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.particle.ModParticles;
import net.minilex.mocapmod.particle.custom.CitrineParticles;

@Mod.EventBusSubscriber(modid = MocapMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        Minecraft.getInstance().particleEngine.register(ModParticles.CITRINE_PARTICLES.get(),
                CitrineParticles.Provider::new);
    }
}
