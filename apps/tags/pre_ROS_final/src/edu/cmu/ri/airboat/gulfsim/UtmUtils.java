/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.util.Random;

/**
 * Utility functions for dealing with UTM coordinate frames.
 *
 * @author pkv
 */
public class UtmUtils {

    public static double EQUATORIAL_RADIUS_EARTH_M = 6378137.0;
    public static double POLAR_RADIUS_EARTH_M = 6356752.3;

    /**
     * Container class representing a UTM location with appropriate zone info.
     * We need a custom data structure to return out-of-range UTM coordinates.
     */
    public static class UTM {
        public final double northing;
        public final double easting;
        public final int longZone;
        public final String latZone;

        public UTM(int longZone, String latZone, double easting, double northing) {
            this.northing = northing;
            this.easting = easting;
            this.longZone = longZone;
            this.latZone = latZone;
        }

        @Override
        public String toString() {
            return "UTM[" + longZone + ", " + latZone + ", " + northing + ", " + easting + "]";
        }
    }

    /**
     * Performs an approximate UTM zone conversion.  The resulting UTM
     * coordinates may not always be accepted as valid by other UTM parsers, as
     * they can technically be out-of-range by normal UTM standards.
     *
     * More precisely, this function takes a destination location and
     * approximately projects it into the UTM zone of a specified source
     * location.  This is useful when locations need to be specified to a system
     * using a particular UTM zone as a local coordinate frame.
     *
     * To perform the conversion, the two locations are converted to geodetic
     * coordinates, and the ellipsoid azimuth and distance are computed.  The
     * azimuth and distance are then applied to the source location within
     * the UTM zone coordinates to yield an approximate projection of the
     * destination relative to the source location.
     *
     * @param src the origin of the projection, in the desired zone
     * @param dst the location that will be projected to the source zone
     * @return the projection of the destination location in the source zone.
     */
    public static UTM convertZone(UTMCoord src, LatLon dst) {

        // Convert UTM coordinates to zone
        LatLon srcPoint = LatLon.fromDegrees(src.getLatitude().degrees, src.getLongitude().degrees);
        LatLon dstPoint = LatLon.fromDegrees(dst.getLatitude().degrees, dst.getLongitude().degrees);

        // Compute angle and bearing to destination
        Angle azimuth = LatLon.greatCircleAzimuth(srcPoint, dstPoint);
        double distance = LatLon.ellipsoidalDistance(srcPoint, dstPoint, EQUATORIAL_RADIUS_EARTH_M, POLAR_RADIUS_EARTH_M);

        // Apply this angle and bearing into source UTM zone
        double dEast = distance*Math.sin(azimuth.getRadians());
        double dNorth = distance*Math.cos(azimuth.getRadians());
        
        return new UTM(src.getZone(), (src.getHemisphere().equalsIgnoreCase("gov.nasa.worldwind.avkey.North") ? "North" : "South"), src.getEasting() + dEast, src.getNorthing() + dNorth);
    }

    /**
     * Performs an approximate UTM zone conversion.  The resulting UTM
     * coordinates may not always be accepted as valid by other UTM parsers, as
     * they can technically be out-of-range by normal UTM standards.
     *
     * More precisely, this function takes a destination location and
     * approximately projects it into the UTM zone of a specified source
     * location.  This is useful when locations need to be specified to a system
     * using a particular UTM zone as a local coordinate frame.
     *
     * To perform the conversion, the two locations are converted to geodetic
     * coordinates, and the ellipsoid azimuth and distance are computed.  The
     * azimuth and distance are then applied to the source location within
     * the UTM zone coordinates to yield an approximate projection of the
     * destination relative to the source location.
     *
     * @param src the origin of the projection, in the desired zone
     * @param dst the location that will be projected to the source zone
     * @return the projection of the destination location in the source zone.
     */
    public static UTM convertZone(UTMCoord src, UTMCoord dst) {

        // Convert UTM coordinates to zone
        LatLon srcPoint = LatLon.fromDegrees(src.getLatitude().degrees, src.getLongitude().degrees);
        LatLon dstPoint = LatLon.fromDegrees(dst.getLatitude().degrees, dst.getLongitude().degrees);

        // Compute angle and bearing to destination
        Angle azimuth = LatLon.greatCircleAzimuth(srcPoint, dstPoint);
        double distance = LatLon.ellipsoidalDistance(srcPoint, dstPoint, EQUATORIAL_RADIUS_EARTH_M, POLAR_RADIUS_EARTH_M);

        // Apply this angle and bearing into source UTM zone
        double dEast = distance*Math.sin(azimuth.getRadians());
        double dNorth = distance*Math.cos(azimuth.getRadians());

        return new UTM(src.getZone(), (src.getHemisphere().equalsIgnoreCase("gov.nasa.worldwind.avkey.North") ? "North" : "South"), src.getEasting() + dEast, src.getNorthing() + dNorth);
    }

    public static double dist(UTMCoord a, UTMCoord b) {
        LatLon srcPoint = LatLon.fromDegrees(a.getLatitude().degrees, a.getLongitude().degrees);
        LatLon dstPoint = LatLon.fromDegrees(b.getLatitude().degrees, b.getLongitude().degrees);

        // Compute distance
        double distance = LatLon.ellipsoidalDistance(srcPoint, dstPoint, EQUATORIAL_RADIUS_EARTH_M, POLAR_RADIUS_EARTH_M);        
        
        return distance;
    }
    
    public static UTMCoord randLocationIn(UTMCoord ll, UTMCoord ur) {
        Random rand = new Random();
        
        LatLon srcPoint = LatLon.fromDegrees(ll.getLatitude().degrees, ll.getLongitude().degrees);
        LatLon dstPoint = LatLon.fromDegrees(ur.getLatitude().degrees, ur.getLongitude().degrees);

        Angle lat = Angle.fromDegrees(srcPoint.latitude.degrees + rand.nextDouble()*(dstPoint.latitude.degrees - srcPoint.latitude.degrees));
        Angle lon = Angle.fromDegrees(srcPoint.longitude.degrees + rand.nextDouble()*(dstPoint.longitude.degrees - srcPoint.longitude.degrees));
        
        return UTMCoord.fromLatLon(lat, lon);        
    }
    
    /**
     * Simple test script to verify that out-of-range conversion produces
     * somewhat reasonable output.
     * 
     * @param args ignored
     */
    public static void main(String args[]) {

        System.out.println("Testing out-of-range UTM coordinate conversion.");

        LatLon pittsburgh = LatLon.fromDegrees(40.35, -79.93);
        LatLon destination = LatLon.fromDegrees(30.552181, -20.292);

        UTMCoord pghUtm = UTMCoord.fromLatLon(pittsburgh.latitude, pittsburgh.longitude);
        UTMCoord londonUtm = UTMCoord.fromLatLon(destination.latitude, destination.longitude);

        System.out.println("UTM SOURCE:" + pghUtm);
        System.out.println("UTM DESTINATION:" + londonUtm);
        UTM fakeLondonUtm = convertZone(pghUtm, londonUtm);
        System.out.println("UTM OUT:" + fakeLondonUtm);

        try {
            UTMCoord out = UTMCoord.fromUTM(fakeLondonUtm.longZone, fakeLondonUtm.latZone.equals("North") ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South", fakeLondonUtm.easting, fakeLondonUtm.northing);
            LatLon newLondon = LatLon.fromDegrees(out.getLatitude().degrees, out.getLongitude().degrees);
            System.out.println(destination + " -> " + newLondon);
        } catch (IllegalArgumentException e) {
            System.err.println("UTM conversion is too out of range for Worldwind.");
        }
    }
}

