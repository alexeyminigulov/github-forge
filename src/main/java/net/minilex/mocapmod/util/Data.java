package net.minilex.mocapmod.util;

import net.minecraftforge.event.entity.player.ArrowLooseEvent;

public class Data {
    private ArrowLooseEvent ARROW_LOOSE;

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
}
