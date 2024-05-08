package net.minilex.mocapmod.entity.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.entity.ModEntities;
import net.minilex.mocapmod.entity.custom.RhinoEntity;
import net.minilex.mocapmod.entity.custom.VillagerEntity;

@Mod.EventBusSubscriber(modid = MocapMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.VILLAGER_TWO.get(), VillagerEntity.createAttributes().build());
        event.put(ModEntities.RHINO.get(), RhinoEntity.createAttributes().build());
    }
}
