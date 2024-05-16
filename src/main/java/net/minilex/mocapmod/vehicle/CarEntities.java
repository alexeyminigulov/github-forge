package net.minilex.mocapmod.vehicle;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.vehicle.custom.SimpleCarEntity;

public class CarEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MocapMod.MODID);
    public static final RegistryObject<EntityType<SimpleCarEntity>> SIMPLE_CAR =
            ENTITY_TYPES.register("simple_car", () -> EntityType.Builder.of(SimpleCarEntity::new, MobCategory.CREATURE)
                    .sized(.5f, .5f).build("simple_car"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
