/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.gulfsim.tasking.Role;
import gov.nasa.worldwind.geom.Position;

/**
 *
 * @author pscerri
 */
public class RepeatVisitRole extends Role {

    public RepeatVisitRole(String name, Position p) {
        super(name, p);
    }

    @Override
    public double getPriority() {
        return super.getPriority();
    }
    
    public String toString() {
        return "RVR @ " + posConcisely(getLocation());
    }
}
