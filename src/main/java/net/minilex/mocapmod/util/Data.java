package net.minilex.mocapmod.util;

import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;

public class Data {
    private ArrowLooseEvent ARROW_LOOSE;
    private ItemTossEvent TOSS_ITEM;

    public void setArrowLoose(ArrowLooseEvent event) {
        ARROW_LOOSE = event;
    }

    public ArrowLooseEvent getArrowLooseEvent() {
        ArrowLooseEvent ev = ARROW_LOOSE;
        ARROW_LOOSE = null;
        return ev;
    }

    public boolean isArrowLooseEmpty() {
        return ARROW_LOOSE == null;
    }

    public void setTossItem(ItemTossEvent event) {
        TOSS_ITEM = event;
    }

    public ItemTossEvent getTossItemEvent() {
        ItemTossEvent ev = TOSS_ITEM;
        TOSS_ITEM = null;
        return ev;
    }

    public boolean isTossItemEmpty() {
        return TOSS_ITEM == null;
    }
}
