/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.vbs;

import edu.cmu.ri.airboat.server.AirboatVbs;
import java.util.Arrays;

/**
 * Please don't use this script for anything important, it is just a simple
 * testing script for internal development.
 * 
 * @author pkv
 */
public class TestScript {
    static double[] startingPos = {2572.28, 2068.4, 1.15486};
    static double[] endingPos = {2572.28, 1268.4, 1.15486};

    public static void main(String[] args) throws Exception {
        AirboatVbs boat = new AirboatVbs("usar-laptop.cimds.ri.cmu.edu", endingPos);

        System.out.println("Zone: " + boat.getUTMZone() + (boat.isUTMHemisphereNorth() ? "North" : "South"));
        System.out.println("Wpt: " + Arrays.toString(boat.getWaypoint()));
        System.out.println("Pose: " + Arrays.toString(boat.getPose()));

        System.out.println("Setting waypoint to current pose");
        boat.setWaypoint(boat.getPose());
        Thread.sleep(1000);
        System.out.println("Wpt: " + Arrays.toString(boat.getWaypoint()));

        System.out.println("Setting waypoint to origin");
        boat.setWaypoint(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0} );
        Thread.sleep(1000);
        System.out.println("Wpt: " + Arrays.toString(boat.getWaypoint()));

        System.out.println("Setting boat pose to origin");
        boat.setPose(new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0} );
        Thread.sleep(1000);
        System.out.println("Pose: " + Arrays.toString(boat.getPose()));

        System.out.println("Watching boat move to " + Arrays.toString(boat.getWaypoint()));
        while(true) {
            Thread.sleep(1000);
            System.out.println("Pose: " + Arrays.toString(boat.getPose()));
        }
    }
}
