package net.minilex.mocapmod.thread;

import java.io.Serial;
import java.io.Serializable;

public class Position implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public double x;
    public double y;
    public double z;
    public Position(double xx, double yy, double zz) {
        x = xx;
        y = yy;
        z = zz;
    }
    @Override
    public String toString() {
        return "X:" + x + "\nY: " + y + "\nZ: " + z;
    }
}