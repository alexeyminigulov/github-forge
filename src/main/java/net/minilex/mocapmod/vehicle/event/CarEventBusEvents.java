package net.minilex.mocapmod.vehicle.event;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.vehicle.CarEntities;
import net.minilex.mocapmod.vehicle.custom.SimpleCarEntity;

@Mod.EventBusSubscriber(modid = MocapMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CarEventBusEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(CarEntities.SIMPLE_CAR.get(), SimpleCarEntity.createAttributes().build());
    }
}