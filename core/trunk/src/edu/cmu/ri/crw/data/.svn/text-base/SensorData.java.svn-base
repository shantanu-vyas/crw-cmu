package edu.cmu.ri.crw.data;

import edu.cmu.ri.crw.VehicleServer.SensorType;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Structure for holding sensor data.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class SensorData implements Cloneable, Serializable  {
    public int channel;
    public double[] data;
    public SensorType type;
    
    @Override
    public String toString() {
        return "Sensor" + channel + "(" + type + ")" + Arrays.toString(data);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.channel;
        hash = 97 * hash + Arrays.hashCode(this.data);
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
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
        final SensorData other = (SensorData) obj;
        if (this.channel != other.channel) {
            return false;
        }
        if (!Arrays.equals(this.data, other.data)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
}
