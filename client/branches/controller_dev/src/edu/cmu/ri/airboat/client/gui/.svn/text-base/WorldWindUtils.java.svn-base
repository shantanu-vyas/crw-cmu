/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client.gui;

/**
 * Conversion utility class for WorldWind data structures.
 *
 * @author pkv
 */
public class WorldWindUtils {

    /**
     * Takes a WorldWind hemisphere string and returns whether it represents
     * the northern hemisphere or not.
     *
     * @param hemisphere a WorldWind hemisphere string, e.g. "gov.nasa.worldwind.avkey.North"
     * @return true if the hemisphere represents the northern hemisphere
     */
    public static boolean isNorth(String hemisphere) {
        return hemisphere.contains("North");
    }

    /**
     * Returns the appropriate WorldWind hemisphere string for the northern or
     * southern hemisphere.
     *
     * @param isNorth true if the hemisphere represents the northern hemisphere
     * @return a WorldWind hemisphere string, e.g. "gov.nasa.worldwind.avkey.North"
     */
    public static String toHemisphere(boolean isNorth) {
        return (isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South";
    }

}
