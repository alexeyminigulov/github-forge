package net.minilex.mocapmod.util;

import net.minecraft.world.entity.item.ItemEntity;

public class TossedItem {
    public ItemEntity item;
    public int oldAge = 0;
    public int tick = 100;
    public boolean deprecated = false;
    public TossedItem(ItemEntity entity) {
        item = entity;
    }
}
