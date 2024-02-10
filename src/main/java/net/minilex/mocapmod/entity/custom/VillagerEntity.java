package net.minilex.mocapmod.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minilex.mocapmod.entity.ModEntities;

import javax.annotation.Nullable;

public class VillagerEntity extends Villager {
    public VillagerEntity(EntityType<? extends Villager> p_35381_, Level p_35382_) {
        super(p_35381_, p_35382_);
    }
}
