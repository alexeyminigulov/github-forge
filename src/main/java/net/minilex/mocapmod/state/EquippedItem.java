package net.minilex.mocapmod.state;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.io.Serializable;

public class EquippedItem implements Serializable {
    public int itemId;
    public int flagSlot;
    public EquippedItem(int item, int slot) {
        this.itemId = item;
        this.flagSlot = slot;
    }

    public Item getItem() {
        return Item.byId(itemId);
    }

    public EquipmentSlot getSlot() {
        EquipmentSlot slot = switch (flagSlot) {
            case 0 -> EquipmentSlot.MAINHAND;
            case 1 -> EquipmentSlot.FEET;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.CHEST;
            case 4 -> EquipmentSlot.HEAD;
            case 5 -> EquipmentSlot.OFFHAND;
            default -> null;
        };
        return slot;
    }
    @Override
    public String toString() {
        return "itemId:" + itemId + "\nflagSlot: " + flagSlot;
    }
}
