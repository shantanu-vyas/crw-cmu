/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.vbs;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A utility class for VBS2 command generation and parsing
 * @author pkv
 */
public class Vbs2Utils {

    // Patterns used to match against VBS2 coordinates
    private static Pattern coordsPattern = Pattern.compile("[\\[\\],]+");
    private static Pattern originPattern = Pattern.compile("[\"\\[\\], ]+");
    
    public static double[] parseArray(String array) {
        String[] str = coordsPattern.split(array);
        double[] coords = new double[str.length-1];

        for (int i = 1; i < str.length; i++) {
            try {
                coords[i-1] = Double.parseDouble(str[i]);
            } catch (NumberFormatException e) {
                coords[i-1] = Double.NaN;
            }
        }

        return coords;
    }

    public static double[] parseCoord(String array) {
        // System.out.println("Parsing origin from: " + array);
        double[] ret = new double[2];
        String[] str = originPattern.split(array);
        if(str[2].equalsIgnoreCase("N")) {
            ret[0] = Double.valueOf(str[1])*Math.PI/180.0;
        }
        else {
            ret[0] = -1*Double.valueOf(str[1])*Math.PI/180.0;
        }
        if(str[4].equalsIgnoreCase("E")) {
            ret[1] = Double.valueOf(str[3])*Math.PI/180.0;
        }
        else {
            ret[1] = -1*Double.valueOf(str[3])*Math.PI/180.0;
        }
        return ret;
    }

    /**
     * Utility class that represents VBS2 waypoints for a unit as a Java
     * list object.
     *
     * VBS2 waypoint lists include a dummy waypoint for the initial location
     * of the group.  All of the indexing used by this waypoint list is shifted
     * to ignore this dummy waypoint and emulate a zero-indexed list of
     * destination waypoints.
     */
    protected static class WaypointList extends AbstractList<double[]> {

        protected final Vbs2Link _link;
        protected final String _group;

        protected WaypointList(final Vbs2Link link, final String groupName) {
            _link = link;
            _group = groupName;
        }

        @Override
        public double[] get(int i) {

            // >> waypointPosition [_grp, 1]
            return parseArray(_link.evaluate("waypointPosition [" + _group + "," + (i+1) + "];"));
        }

        @Override
        public double[] set(int index, double[] element) {

            // >> waypointPosition [_grp, 1]
            double[] pos = parseArray(_link.evaluate("waypointPosition [" + _group + "," + (index+1) + "];"));

            // >> waypoint setWaypointPosition [center, radius]
            _link.send("[" + _group + "," + (index+1) + "] setWaypointPosition [" + Arrays.toString(element) + ",0];");

            return pos;
        }

        @Override
        public void add(int index, double[] element) {

            // TODO: fix insertions into center of list
            // >> _grp addWaypoint [center, radius, index]
            //_link.send(_group + " addWaypoint [" + Arrays.toString(element) + ",0," + index + "];");
            _link.send(_group + " addWaypoint [" + Arrays.toString(element) + ",0];");
        }

        @Override
        public double[] remove(int index) {

            // >> waypointPosition [_grp, 1]
            double[] pos = parseArray(_link.evaluate("waypointPosition [" + _group + "," + (index+1) + "];"));

            // >> deleteWaypoint [_grp, 2]
            _link.send("deleteWaypoint [" + _group + "," + (index+1) + "];");

            return pos;
        }

        @Override
        public int size() {

            // >> _wpcount = nWaypoints _grp
            String wpcount = _link.evaluate("nWaypoints " + _group + ";");
            return (int)Double.parseDouble(wpcount) - 1;
        }
    }
}
