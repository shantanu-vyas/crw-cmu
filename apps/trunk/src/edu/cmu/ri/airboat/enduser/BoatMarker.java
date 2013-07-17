/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.general.BoatProxy;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

/**
 * 
 * @author pscerri 
 */
public class BoatMarker extends BasicMarker {
    private final BoatProxy proxy;
    private Angle heading = Angle.ZERO;
    
    public BoatMarker(BoatProxy proxy, Position p, BasicMarkerAttributes attr) {
        super(p, attr);
        this.proxy = proxy;
        setHeading(heading);
    }

    public BoatProxy getProxy() {
        return proxy;
    }        
        
}
