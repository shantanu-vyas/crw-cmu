/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;

/**
 *
 * @author pscerri 
 */
public class BoatMarker extends BasicMarker {
    private final BoatSimpleProxy proxy;

    public BoatMarker(BoatSimpleProxy proxy, Position p, BasicMarkerAttributes attr) {
        super(p, attr);
        this.proxy = proxy;
    }

    public BoatSimpleProxy getProxy() {
        return proxy;
    }
        
}
