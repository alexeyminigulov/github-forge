package net.minilex.mocapmod.state;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class StatusInventory {
    private Item mainHand;
    private Item leftHand;
    private Item helmet;
    private Item chest;
    private Item legs;
    private Item feet;
    private EquippedItem updatedItem;
    public StatusInventory(ItemStack mainHand, ItemStack leftHand, NonNullList<ItemStack> armor) {
        this.mainHand = mainHand.getItem();
        this.leftHand = leftHand.getItem();
        Object[] armorArr = armor.toArray();
        helmet = ((ItemStack)armorArr[3]).getItem();
        chest  = ((ItemStack)armorArr[2]).getItem();
        legs   = ((ItemStack)armorArr[1]).getItem();
        feet   = ((ItemStack)armorArr[0]).getItem();
    }

    public void tickUpdate(ItemStack zMainHand, ItemStack zleftHand, NonNullList<ItemStack> armor) {
        if (Item.getId(zMainHand.getItem()) != Item.getId(mainHand)) {
            int itemID = Item.getId(zMainHand.getItem());
            this.updatedItem = new EquippedItem(itemID, EquipmentSlot.MAINHAND.getFilterFlag());
            this.mainHand = updatedItem.getItem();
            return;
        }
        if (Item.getId(zleftHand.getItem()) != Item.getId(leftHand)) {
            int itemID = Item.getId(zleftHand.getItem());
            this.updatedItem = new EquippedItem(itemID, EquipmentSlot.OFFHAND.getFilterFlag());
            this.leftHand = updatedItem.getItem();
            return;
        }
        Object[] armorArray = armor.toArray();
        if (Item.getId(((ItemStack)armorArray[3]).getItem()) != Item.getId(helmet)) {
            int itemID = Item.getId(((ItemStack)armorArray[3]).getItem());
            this.updatedItem = new EquippedItem(itemID, EquipmentSlot.HEAD.getFilterFlag());
            this.helmet = updatedItem.getItem();
            return;
        }
        if (Item.getId(((ItemStack)armorArray[2]).getItem()) != Item.getId(chest)) {
            int itemID = Item.getId(((ItemStack)armorArray[2]).getItem());
            this.updatedItem = new EquippedItem(itemID, EquipmentSlot.CHEST.getFilterFlag());
            this.chest = updatedItem.getItem();
            return;
        }
        if (Item.getId(((ItemStack)armorArray[1]).getItem()) != Item.getId(legs)) {
            int itemID = Item.getId(((ItemStack)armorArray[1]).getItem());
            this.updatedItem = new EquippedItem(itemID, EquipmentSlot.LEGS.getFilterFlag());
            this.legs = updatedItem.getItem();
            return;
        }
        if (Item.getId(((ItemStack)armorArray[0]).getItem()) != Item.getId(feet)) {
            int itemID = Item.getId(((ItemStack)armorArray[0]).getItem());
            this.updatedItem = new EquippedItem(itemID, EquipmentSlot.FEET.getFilterFlag());
            this.feet = updatedItem.getItem();
        }
    }
    public EquippedItem getUpdatedItem() {
        if (this.updatedItem == null) return null;
        EquippedItem result = this.updatedItem;
        this.updatedItem = null;
        return  result;
    }
}
