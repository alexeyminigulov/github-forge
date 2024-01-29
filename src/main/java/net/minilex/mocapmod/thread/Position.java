package net.minilex.mocapmod.thread;

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
    public Position(double xx, double yy, double zz, float rotXX, float rotYY) {
        x = xx;
        y = yy;
        z = zz;
        rotX = rotXX;
        rotY = rotYY;
    }
    @Override
    public String toString() {
        return "X:" + x + "\nY: " + y + "\nZ: " + z + "\nrotX: " + rotX + "\nrotY: " + rotY;
    }
}