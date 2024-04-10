package net.minilex.mocapmod.thread;

import net.minilex.mocapmod.state.BuildBlock;
import net.minilex.mocapmod.state.EquippedItem;
import net.minilex.mocapmod.state.TossItem;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Position implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public double x;
    public double y;
    public double z;
    public float rotX;
    public float rotY;
    public float yBodyRot;
    public float yHeadRot;
    public boolean swinging = false;
    public boolean isBowPulling = false;
    public int looseArrowStrength = 0;
    public boolean hurtAnim = false;
    public boolean speakerIcon = false;
    public boolean ignoreAttack = false;
    public boolean dead = false;
    public TossItem tossItem;
    public BuildBlock buildBlock;
    private List<EquippedItem> equippedItem = null;
    public Position(double xx, double yy, double zz, float rotXX, float rotYY, float yyBodyRot, float yyHeadRot) {
        x = xx;
        y = yy;
        z = zz;
        rotX = rotXX;
        rotY = rotYY;
        yBodyRot = yyBodyRot;
        yHeadRot = yyHeadRot;
    }
    public void addEquippedItem(EquippedItem item) {
        if (equippedItem == null) equippedItem = new ArrayList<EquippedItem>();
        equippedItem.add(item);
    }
    public List<EquippedItem> getEquippedItem() {
        return equippedItem;
    }
    @Override
    public String toString() {
        return "X:" + x + "\nY: " + y + "\nZ: " + z + "\nrotX: " + rotX + "\nrotY: " + rotY + "\n" + buildBlock;
    }
}