package edu.cmu.ri.crw.data;

import java.io.Serializable;
import robotutils.Pose3D;

/**
 * Represents a location in 6D pose and UTM origin.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UtmPose implements Serializable, Cloneable {
 
    public static final Pose3D DEFAULT_POSE = new Pose3D(0.0,0.0,0.0,0.0,0.0,0.0);
    public static final Utm DEFAULT_ORIGIN = new Utm(17, true);
    
    // TODO: make this externalizable and cloneable
    public Pose3D pose;
    public Utm origin;
    
    public UtmPose() {
        this.pose = DEFAULT_POSE;
        this.origin = DEFAULT_ORIGIN;
    }
    
    public UtmPose(Pose3D pose, Utm origin) {
        this.pose = pose;
        this.origin = origin;
    }
    
    @Override
    public UtmPose clone() {
        return new UtmPose(pose, origin);
    }
    
    @Override
    public String toString() {
        return pose.toString() + " @ " + origin.toString();
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.pose != null ? this.pose.hashCode() : 0);
        hash = 53 * hash + (this.origin != null ? this.origin.hashCode() : 0);
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
        final UtmPose other = (UtmPose) obj;
        if (this.pose != other.pose && (this.pose == null || !this.pose.equals(other.pose))) {
            return false;
        }
        if (this.origin != other.origin && (this.origin == null || !this.origin.equals(other.origin))) {
            return false;
        }
        return true;
    }
}
