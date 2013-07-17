/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.gulfsim.UtmUtils;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.util.ArrayList;
import java.util.Random;

/**
 * Singleton
 * 
 * @author pscerri
 */
public class CentralizedGAPollutionModel implements LearnedDataModel {

    private static _Model model = null;
    private Random rand = new Random();

    public CentralizedGAPollutionModel() {
        if (model == null) {
            model = new _Model();
        }
    }

    public void addListener(DataModelListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addObservation(double v, UTMCoord utm, long t) {
        model.addObservation(v, utm, t);
    }

    @Override
    public double getValue(UTMCoord utm, long t) {
        return model.getValue(utm, t);
    }

    // @todo Make these parameters
    UTMCoord ll = UTMCoord.fromLatLon(Angle.fromDegrees(22.0), Angle.fromDegrees(-95));
    UTMCoord ur = UTMCoord.fromLatLon(Angle.fromDegrees(27.0), Angle.fromDegrees(-86));

    private class _Model {

        ArrayList<Observation> obs = new ArrayList<Observation>();
        ArrayList<Chromosome> population = new ArrayList<Chromosome>();
        Chromosome best = new Chromosome();

        public void addObservation(double v, UTMCoord utm, long t) {
            Observation o = new Observation(v, utm, t);
            obs.add(o);
            System.out.println("No. obs now: " + obs.size());
        }

        public double getValue(UTMCoord utm, long t) {
            return best.getValue(utm, t);
        }
    }

    private class Observation {

        private final double v;
        private final UTMCoord utm;
        private final long t;

        public Observation(double v, UTMCoord utm, long t) {
            this.v = v;
            this.utm = utm;
            this.t = t;
        }
    }

    private class Chromosome {

        ArrayList<Gene> genes = null;

        public Chromosome() {
            genes = new ArrayList<Gene>();
            int i = rand.nextInt(5) + 10;
            for (int j = 0; j < i; j++) {                 
                genes.add(new Gene());
            }
        }

        public double computeFitness(ArrayList<Observation> obs) {
            return 0.0;
        }

        public double getValue(UTMCoord utm, long t) {
            double v = 0;
            for (Gene gene : genes) {
                v += gene.getValue(utm, t);
            }
            return v;
        }
    }

    private class Gene {
        UTMCoord loc = null;
        long time = 0;
        double magnitude = 0.0;
        
        public Gene () {
            loc = UtmUtils.randLocationIn(ll, ur);            
            // @todo Smarter on time
            time = (long)rand.nextInt(100);
            magnitude = rand.nextDouble() * 10.0;            
            System.out.println("New gene: " + loc + " with magnitude " + magnitude);
        }
        
        public double getValue(UTMCoord utm, long t) {
            double dist = UtmUtils.dist(utm, loc);
            
            double value = PollutionModel.computeDissipatedValueAt(dist, magnitude, time, t);
            
            return value;
        }
    }
}
