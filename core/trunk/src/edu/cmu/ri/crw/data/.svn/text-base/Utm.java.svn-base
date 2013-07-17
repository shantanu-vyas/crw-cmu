package edu.cmu.ri.crw.data;

import java.io.Serializable;

/**
 * Represents a UTM frame, but NOT a location in that frame.
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class Utm implements Serializable, Cloneable {
    // TODO: make this externalizable
    public final int zone;
    public final boolean isNorth;
    
    public Utm(int zone, boolean isNorth) {
        this.zone = zone;
        this.isNorth = isNorth;
    }
    
    @Override
    public Utm clone() {
        return new Utm(zone, isNorth);
    }
    
    @Override
    public String toString() {
        return zone + (isNorth ? "North" : "South");
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.zone;
        hash = 59 * hash + (this.isNorth ? 1 : 0);
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
        final Utm other = (Utm) obj;
        if (this.zone != other.zone) {
            return false;
        }
        if (this.isNorth != other.isNorth) {
            return false;
        }
        return true;
    }
}
