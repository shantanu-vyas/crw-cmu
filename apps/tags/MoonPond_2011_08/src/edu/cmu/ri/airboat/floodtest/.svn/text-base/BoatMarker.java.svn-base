/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.floodtest;

import edu.cmu.ri.airboat.floodtest.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

/**
 * @todo Directional
 * 
 * @author pscerri 
 */
public class BoatMarker extends BasicMarker {
    private final BoatSimpleProxy proxy;
    private Angle heading = Angle.ZERO;
    
    public BoatMarker(BoatSimpleProxy proxy, Position p, BasicMarkerAttributes attr) {
        super(p, attr);
        this.proxy = proxy;
        setHeading(heading);
    }

    public BoatSimpleProxy getProxy() {
        return proxy;
    }        
        
}
