package net.minilex.mocapmod.state;

import java.io.Serializable;

public class DeathState implements Serializable {
    public double force;
    public double x;
    public double z;
    public DeathState(double force, double x, double z) {
        this.force = force;
        this.x = x;
        this.z = z;
    }
}
