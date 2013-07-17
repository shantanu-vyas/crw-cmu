/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;

/**
 *
 * @author pscerri
 */
public class OilRig {
    private final String name;
    
    private final double lat;
    private final double lon;
    private Position pos = null;
    
    public OilRig(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;

        pos = Position.fromDegrees(lat, lon);
        
        UTMCoord utm = UTMCoord.fromLatLon(Angle.fromDegrees(lat), Angle.fromDegrees(lon));

        System.out.println("Translated " + lat + " " + lon + " to " + utm);        
        System.out.println("Position: " + pos);
    }
}
