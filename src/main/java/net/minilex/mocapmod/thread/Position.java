package net.minilex.mocapmod.thread;

import net.minilex.mocapmod.state.BuildBlock;

import java.io.Serial;
import java.io.Serializable;

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
    public BuildBlock buildBlock;
    public Position(double xx, double yy, double zz, float rotXX, float rotYY, float yyBodyRot, float yyHeadRot) {
        x = xx;
        y = yy;
        z = zz;
        rotX = rotXX;
        rotY = rotYY;
        yBodyRot = yyBodyRot;
        yHeadRot = yyHeadRot;
    }
    @Override
    public String toString() {
        return "X:" + x + "\nY: " + y + "\nZ: " + z + "\nrotX: " + rotX + "\nrotY: " + rotY + "\n" + buildBlock;
    }
}