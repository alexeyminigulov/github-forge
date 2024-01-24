package net.minilex.mocapmod.thread;

//import net.minecraft.nbt.NBTTagCompound;

import java.awt.*;

public class MocapAction {
    public MocapAction(byte chat) {
        super();
        this.type = chat;
        //itemData = new NBTTagCompound();
    }

    byte type;

    /* Chat */
    public String message;

    /* Equip */
    public int armorId;
    public int armorSlot;
    public int armorDmg;

    /* Drop, Equip */
    //NBTTagCompound itemData;
    List itemData;

    /* Arrow Shoot */
    public int arrowCharge;

    /* Place Block */
    public int xCoord;
    public int yCoord;
    public int zCoord;
}
