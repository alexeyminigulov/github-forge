package net.minilex.mocapmod.state;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

import java.io.Serializable;

public class TossItem implements Serializable {
    public int itemID;
    public TossItem(ItemEntity itemEntity) {
        itemID = Item.getId(itemEntity.getItem().getItem());
    }
}