/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

/**
 *
 * @author pscerri
 */
public class Observation {

    public double[] waypoint = null; // new double[6];
    public int waypointZone;
    public boolean waypointHemisphereNorth;
    
    public String variable = null;
    public double value = 0.0;
    
    public Observation (String variable, double value, double [] waypoint, int waypointZone, boolean waypointHemisphereNorth) {
        this.variable = variable;
        this.value = value;
        this.waypoint = waypoint;
        this.waypointZone = waypointZone;
        this.waypointHemisphereNorth = waypointHemisphereNorth;        
    }

    public double getValue() {
        return value;
    }

    public String getVariable() {
        return variable;
    }

    public double[] getWaypoint() {
        return waypoint;
    }

    public boolean isWaypointHemisphereNorth() {
        return waypointHemisphereNorth;
    }

    public int getWaypointZone() {
        return waypointZone;
    }
    
    
}
