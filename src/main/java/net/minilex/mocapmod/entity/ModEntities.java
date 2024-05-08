package net.minilex.mocapmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.entity.custom.RhinoEntity;
import net.minilex.mocapmod.entity.custom.VillagerEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MocapMod.MODID);

    public static final RegistryObject<EntityType<VillagerEntity>> VILLAGER_TWO =
            ENTITY_TYPES.register("villager_two", () -> EntityType.Builder.of(VillagerEntity::new, MobCategory.CREATURE)
                    .sized(1f, 1f)
                    .build("villager_two"));
    public static final RegistryObject<EntityType<RhinoEntity>> RHINO =
            ENTITY_TYPES.register("rhino", () -> EntityType.Builder.of(RhinoEntity::new, MobCategory.CREATURE)
                    .sized(2.5f, 2.5f).build("rhino"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
