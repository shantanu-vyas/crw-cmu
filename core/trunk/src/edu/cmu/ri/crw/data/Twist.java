package edu.cmu.ri.crw.data;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Implements a mutable class for representing velocity in 6D space.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class Twist implements Cloneable, Serializable {
    final double[] velocity = new double[6];
    
    public Twist() {}
    
    public Twist(Twist t) {
        this(t.velocity);
    }
    
    public Twist(double... velocities) {
        System.arraycopy(velocities, 0, velocity, 0, Math.min(velocity.length, velocities.length));
    }
    
    public final double dx() { return velocity[0]; };
    public final double dy() { return velocity[1]; };
    public final double dz() { return velocity[2]; };
    public final double drx() { return velocity[3]; };
    public final double dry() { return velocity[4]; };
    public final double drz() { return velocity[5]; };
    
    public final void dx(double dx) { velocity[0] = dx; };
    public final void dy(double dy) { velocity[1] = dy; };
    public final void dz(double dz) { velocity[2] = dz; };
    public final void drx(double drx) { velocity[3] = drx; };
    public final void dry(double dry) { velocity[4] = dry; };
    public final void drz(double drz) { velocity[5] = drz; };
    
    @Override
    public Twist clone() {
        return new Twist(velocity);
    }
    
    @Override
    public String toString() {
        return "Twist" + Arrays.toString(velocity);
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Arrays.hashCode(this.velocity);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Twist other = (Twist) obj;
        if (!Arrays.equals(this.velocity, other.velocity)) {
            return false;
        }
        return true;
    }
}
