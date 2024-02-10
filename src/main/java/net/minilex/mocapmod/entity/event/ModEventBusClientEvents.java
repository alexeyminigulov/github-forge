package net.minilex.mocapmod.entity.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.entity.client.ModModelLayers;
import net.minilex.mocapmod.entity.client.VillagerTwoModel;

@Mod.EventBusSubscriber(modid = MocapMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.VILLAGER_TWO_LAYER, VillagerTwoModel::createBodyLayer);
    }
}
