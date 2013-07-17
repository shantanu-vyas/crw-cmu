/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.gulfsim.UtmUtils;
import edu.cmu.ri.airboat.gulfsim.UtmUtils.UTM;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class PollutionModel implements DataModel {

    private final static double moveRate = 5000.0;
    private final static double dissapateRate = 0.9;
    private final static double distAlpha = 1.0e-6;

    public double getValue(UTMCoord utm, long t) {
        return model.getValue(utm, t);
    }
    _PollutionModel model = null;

    public PollutionModel() {
        if (model == null) {
            model = new _PollutionModel();
        }
    }

    public void addListener(DataModelListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }    
   
    private class _PollutionModel {

        ArrayList<Source> sources = new ArrayList<Source>();

        public double getValue(UTMCoord utm, long t) {
            double v = 0;
            for (Source source : sources) {
                v += source.getContribAt(utm, t);
            }
            return v;
        }

        public _PollutionModel() {

            System.out.println("Creating test pollution model");

            UTMCoord c = UTMCoord.fromLatLon(Angle.fromDegreesLatitude(22.5), Angle.fromDegreesLongitude(-94.0));
            Source s = new Source(c, 0, 10.0);
            sources.add(s);

            UTMCoord c2 = UTMCoord.fromLatLon(Angle.fromDegreesLatitude(23.5), Angle.fromDegreesLongitude(-93.0));
            Source s2 = new Source(c2, 0, 7.0);
            sources.add(s2);

        }
    }

    private class Source {

        public Source(UTMCoord utm, long t, double magnitude) {
            this.utm = utm;
            this.t = t;
            this.magnitude = magnitude;
        }
        public UTMCoord utm;
        long t;
        double magnitude;

        public double getContribAt(UTMCoord p, long time) {
            /*
             * if (p.getZone() != utm.getZone()) {
            UTM p1 = UtmUtils.convertZone(utm, p);
            p = UTMCoord.fromUTM(p1.longZone, p1.latZone, p1.easting, p1.northing);
            }
            
            double dx = p.getEasting() - utm.getEasting();
            double dy = p.getNorthing() - utm.getNorthing();
            
            double dist = Math.sqrt(dx * dx + dy * dy);
             */

            double dist = UtmUtils.dist(p, utm);

            return computeDissipatedValueAt(dist, magnitude, t, time);
        }
    }

    public static double computeDissipatedValueAt(double dist, double magnitude, long srcTime, long time) {
        double val = 0.0;
        if (dist / moveRate < time - srcTime) {
            double d2 = dist * dist;
            val = magnitude / (distAlpha * d2);
            if (val > magnitude) {
                val = magnitude;
            }
            //System.out.println("Magnitude at " + dist + " is " + val);

        } else {
            //System.out.println("Source could not reach " + dist + " in " + (time - t) + " requires " + (int)(dist / moveRate));
        }

        return val;
    }
}
