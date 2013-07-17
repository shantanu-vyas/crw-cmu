/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.mrc.widgets;

import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.airboat.general.SkeletonBoatProxy;
import edu.cmu.ri.airboat.general.widgets.Core;
import edu.cmu.ri.airboat.general.widgets.ProxyListener;
import edu.cmu.ri.airboat.general.widgets.WidgetInterface;
import edu.cmu.ri.airboat.mrc.widgets.WorldWindWidget.WWMarkerLayer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

/**
 *
 * @author pscerri
 */
public class WWLocationMarkerLayer implements WidgetInterface, WWMarkerLayer, ProxyListener {

    ArrayList<Marker> markers = new ArrayList<Marker>();
    HashMap<SkeletonBoatProxy, BoatMarker> map = new HashMap<SkeletonBoatProxy, BoatMarker>();
    
    MarkerLayer ml = new MarkerLayer();
    {
        ml.setOverrideMarkerElevation(true);
        ml.setKeepSeparated(false);
        ml.setElevation(10d);
        //ml.setMarkers(markers);
        ml.setPickEnabled(true);
    }   
    
    public WWLocationMarkerLayer() {
        (new Core()).addListener(this);
    }

    public JPanel getPanel() {
        return null;
    }

    public JPanel getControl() {
        return null;
    }

    public String getName() {
        return "Markers";
    }

    /**
     * Next is to work out how to get the updates of boat locations
     */
    
    public void proxyUpdated(SkeletonBoatProxy p) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Boat added", this);
    }

    public void proxyAdded(SkeletonBoatProxy p) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Adding marker at " + p.getCurrLoc(), this);
        Position pos = p.getCurrLoc();
       
        if (pos == null) {
            pos = new Position(new LatLon(Angle.fromDegreesLatitude(0.0), Angle.fromDegreesLongitude(0.0)), 0.0);
        }
        BoatMarker bm = new BoatMarker(p, pos, new BasicMarkerAttributes());
        markers.add(bm);        
        map.put(p, bm);
        
        ml.setMarkers(markers);
    }

    public MarkerLayer getMarkerLayer() {

        return ml;
    }

    public class BoatMarker extends BasicMarker {

        private final SkeletonBoatProxy proxy;
        private Angle heading = Angle.ZERO;

        public BoatMarker(SkeletonBoatProxy proxy, Position p, BasicMarkerAttributes attr) {
            super(p, attr);
            this.proxy = proxy;
            setHeading(heading);
        }

        public SkeletonBoatProxy getProxy() {
            return proxy;
        }
    }
}
