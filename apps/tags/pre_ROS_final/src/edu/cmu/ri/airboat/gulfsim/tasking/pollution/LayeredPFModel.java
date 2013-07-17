/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.gulfsim.UtmUtils;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 *
 * @author pscerri
 */
public class LayeredPFModel implements LearnedDataModel {

    private Sector sector = null;
    private Random rand = new Random();
    private final int noParticles = 100;
    private Particle bestParticle = null;
    private ArrayList<Observation> obs = new ArrayList<Observation>();

    public LayeredPFModel() {
        for (int i = 0; i < noParticles; i++) {
            Particle p = new Particle();
            particles.add(p);
        }
        bestParticle = particles.get(0);
    }

    public void setSector(Sector s) {
        sector = s;
    }

    public void addObservation(double v, UTMCoord utm, long t) {

        obs.add(new Observation(v, utm, t));

        // Perhaps add some quarks, based on this reading
        int noToAdd = 10;
        if (allQuarks.size() > 100) {
            noToAdd = 0; // rand.nextBoolean() ? 0 : rand.nextInt(10);
        }
        for (int i = 0; i < noToAdd; i++) {
            Quark q = new Quark(utm, v, 1000.0 + ((rand.nextDouble() * 1000.0) - 500.0));
            allQuarks.add(q);
        }

        // Reweight quarks 
        // @todo should reweight *some* quarks
        /*
        for (Quark quark : allQuarks) {
        
        // What is a good quark?
        // Should not suggest a higher value than observation, but could suggest lower since other quarks may add
        // Higher "value" is better since it provides more information, so if quark underestimates and is high, then good
        // Intuitively close numbers close by are good, but this isn't completely clear
        
        double prev = quark.weight;
        double est = quark.getValueAt(utm, t);
        double diff = v - est;
        if (Double.isNaN(est)) {
        //System.out.println("Est is NaN: " + quark.decayPow + " " + quark.value);
        } else if (v < 0.0) {                
        quark.weight -= 0.01;
        } else {
        quark.weight += est/100.0;
        }
        
        // Bound to 0-1
        quark.weight = Math.min(1.0, Math.max(0.0, quark.weight));
        // System.out.println("For est = " + est + " diff = " + diff + " weight now " + quark.weight + " from " + prev);            
        }
         * 
         */

        // Allow particles to adjust
        //
        // Thought: Might some of Robin's SIV work here?  Particles shared wins and losses and somehow agree on a better particle (ala smart GA)
        // If we think about quarks as "mini-hypotheses", then perhaps the agents/particles are coming to conclusions about those hypotheses, this
        // would then imply that perhaps quark weights are not even updated with obs, only with conclusions.  [Perhaps obs don't even come in 
        // everywhere?]
        //
        // @todo Add quarks, GA, 

        double totQuality = 0.0;
        for (Particle particle : particles) {

            // Let the particle change
            particle.update();

            // Check for new best value
            double nv = particle.incUpdateQuality();
            if (particle != bestParticle && nv < bestParticle.quality) {
                System.out.println("Changing best particle quality goes from " + bestParticle.quality + " to " + nv);
                bestParticle = particle;
            }

            totQuality += nv;
        }

        updateListeners();
        System.out.println("Best: " + bestParticle + " with quality " + bestParticle.quality + " (over " + bestParticle.evaluations + " evaluations)");
        System.out.println("Average quality: " + totQuality / particles.size());
        doGTEvaluation(bestParticle);
        System.out.println("***********************\n\n");
    }
    // Ground truth eval of filter
    double minLatD = 41.06;
    double maxLatD = 41.13;
    double minLonD = 16.74;
    double maxLonD = 16.90;
    double dLat = maxLatD - minLatD;
    double dLon = maxLonD - minLonD;
    UTMCoord center = UTMCoord.fromLatLon(Angle.fromDegrees(41.11), Angle.fromDegrees(16.85));

    double avg = 0.0;
    int gtEvals = 0;
    private void doGTEvaluation(Particle p) {

        int samples = 100;
        double ss = 0.0;
        double avgDelta = 0.0;
        for (int i = 0; i < samples; i++) {
            UTMCoord loc = UTMCoord.fromLatLon(Angle.fromDegrees(minLatD + (rand.nextDouble() * dLat)), Angle.fromDegrees(minLonD + (rand.nextDouble() * dLon)));
            double dist = Math.min(10.0, 1.0 / (UtmUtils.dist(loc, center) / 10000.0));
            
            double est = p.getValue(loc, 0);
            
            // System.out.println("Comparing " + dist + " and " + est);
            
            ss += (dist-est)*(dist-est);
            avgDelta += (dist-avg)*(dist-avg);
        }
                
        avg = (avg*gtEvals + Math.sqrt(avgDelta/samples))/(gtEvals + 1);
        gtEvals++;
        System.out.println("Ground truth error estimate " + Math.sqrt(ss/samples) + " versus avg " + avg);

    }
    // End GT eval

    public double getValue(UTMCoord utm, long t) {

        return getBestEstimate().getValue(utm, t);
    }
    private Vector<DataModelListener> listeners = new Vector<DataModelListener>();

    public void addListener(DataModelListener l) {
        listeners.add(l);
    }

    private void updateListeners() {
        for (DataModelListener l : listeners) {
            l.dataModelUpdate();
        }
    }
    // Filter implementation
    private ArrayList<Particle> particles = new ArrayList<Particle>();

    {
        // Initialize with a single particle
        particles.add(new Particle());
    }
    private ArrayList<Quark> allQuarks = new ArrayList<Quark>();

    private Particle getBestEstimate() {
        if (bestParticle == null) {
            return particles.get(0);
        } else {
            return bestParticle;
        }
    }

    private class Quark {

        private final UTMCoord center;
        private final double value;
        private final double distDiv;
        private final double decayPow = 10.0;
        // @todo What is the correct initial weight?
        // private double weight = 0.5;
        ArrayList<Particle> particles = new ArrayList<Particle>();

        public Quark(UTMCoord center, double value, double distDiv) {
            this.center = center;
            this.value = value;
            this.distDiv = distDiv;
        }
        Random rand = new Random();

        public double getValueAt(UTMCoord loc, long t) {

            // @todo Make this function nicer
            double d = Math.min(10.0, UtmUtils.dist(center, loc) / distDiv);

            // UtmUtils.dist is return NaN when same location
            if (Double.isNaN(d)) {
                return value;
            }

            //System.out.println("Dist is " + d);

            double decay = Math.pow(decayPow, -d);
            double e = value * decay;

            //if (rand.nextDouble() < 0.001) System.out.println("e = " + e + " from " + d + " and " + value + " and " + decay);
            if (Double.isNaN(e)) {
                System.out.println("e = " + e + " from " + d + " and " + value + " and " + decay + " " + distDiv + " " + center + " " + loc);
            }

            return e;
        }
    }

    private class Particle {

        // @todo perhaps inefficient to create here
        ArrayList<Quark> quarks = new ArrayList<Quark>();
        Hashtable<Quark, Double> qh = new Hashtable<Quark, Double>();
        // Quality is RMS (i.e., lower is better)
        double quality = 0.0;
        private double sumSquares = 0.0;
        int evaluations = 0;

        public Particle() {
        }

        public void addQuark(Quark q) {
            quarks.add(q);
            q.particles.add(this);

            // @todo Consider alternative priors
            // This puts the "prior" on the hypothesis for the agent at 0.5
            qh.put(q, 0.5);
        }

        // @todo More intelligently update
        // @todo Need to fix the evaluation after a change 
        // @todo Should be more likely to change bad particles
        public void update() {
            
            processCommunication();
            
            // @todo Somehow this needs to be focused on problems with the current model
            // Randomly add some new quark
            if (quarks.size() < 20.0 && rand.nextDouble() < 0.2) {
                Quark toAdd = allQuarks.get(rand.nextInt(allQuarks.size() - 1));
                
                // The second clause here biases the particle to pick quarks that have higher weight
                if (!quarks.contains(toAdd) && (!qh.containsKey(toAdd) || qh.get(toAdd) > rand.nextDouble())) {
                    addQuark(toAdd);
                }
            }

            // Occasionally think about deleting some bad quarks
            if (rand.nextDouble() < 0.1) {
                double worst = 1.0;
                Quark toDelete = null;
                for (Quark quark : qh.keySet()) {
                    if (qh.get(quark) < worst && rand.nextBoolean()) {
                        toDelete = quark;
                        worst = qh.get(quark);
                    }
                }
                if (toDelete != null) {
                    // Notice that it is not deleted from the hashtable to allow for knowledge to be kept
                    quarks.remove(toDelete);
                    toDelete.particles.remove(this);
                }
            }
        }

        public double getValue(UTMCoord utm, long t) {

            double v = 0;
            for (Quark quark : quarks) {
                // @todo what is the right combination function? (taking into account weights)
                v += quark.getValueAt(utm, t);
            }

            return v;
        }

        public double incUpdateQuality() {

            int c = 0;

            double q = quality;

            while (c < 5 || (evaluations < 100 && obs.size() > 1000)) {
                singleUpdateQuality();
                c++;
                evaluations++;
            }

            quality = Math.sqrt(sumSquares / evaluations);
            // System.out.println("Before " + q + " after " + quality);

            // Should this be being bounded in some way?  Old particles seem to have a better chance.

            return quality;
        }

        private void singleUpdateQuality() {

            // @todo Evaluation should be biased towards most recent
            // Not done here because it is called a bunch of times on particle creation
            Observation o = obs.get(rand.nextInt(obs.size()));

            double v = 0;
            for (Quark quark : qh.keySet()) {
                double dv = quark.getValueAt(o.utm, o.t);
                double qw = qh.get(quark);
                double oldQw = qw;
                
                if (dv > o.v) {
                    // Over-estimating on its own, unlikely
                    qw = qw / 1.1;                    
                } else {
                    // Underestimate, so possible, but the bigger the better (since presumably more information)
                    dv /= 10.0;
                    qw = (qw + dv) / (1.0 + dv);
                }
                
                // Update local knowledge
                qh.put(quark, qw);                                

                if (oldQw < threshold && qw > threshold) {
                    //System.out.println("Communicating positively: " + oldQw + " to " + qw);
                    Message m = new Message(this, particles.get(rand.nextInt(particles.size())), quark, Ternary.True);
                    outgoingCommunicate(m);
                } else if (oldQw > 1.0 - threshold && qw < 1.0 - threshold) {
                    System.out.println("Communicating negatively: " + oldQw + " to " + qw);
                    Message m = new Message(this, particles.get(rand.nextInt(particles.size())), quark, Ternary.False);
                    outgoingCommunicate(m);
                }
                
                v += dv;
            }


            double diff = Math.abs(o.v - v);

            sumSquares += diff * diff;
        }
        
        // Communication stuff
        
        ArrayList<Message> incoming = new ArrayList<Message>();
        double cp = 0.55;
        double threshold = 0.8;
        
        private void outgoingCommunicate(Message m) {
            m.dest.incomingCommunicate(m);
        }
        
        private void incomingCommunicate(Message m) {
            incoming.add(m);
        }
        
        private void processCommunication() {
            // Not using fore because of update problems
            while (!incoming.isEmpty()) {
                Message m = incoming.remove(0);
                
                // @todo Make this a Bayesian update
                double v = 0.5;
                double ov = v;
                if (qh.contains(m.q)) {
                    v = qh.get(m.q);
                    System.out.println("Updating information about known thing");
                } else {
                    // System.out.println("New information: " + m.q + " in " + qh.keySet());
                }
                
                // @todo Bayes rule
                if (m.m == Ternary.True) {
                    v = (v + cp)/(1.0 + cp); 
                } else if (m.m == Ternary.False) {
                    v = (v + (1.0 - cp))/(1.0 + cp); 
                }
                qh.put(m.q, v);
                
                //System.out.println("Communication changed from : " + ov + " to " + v);
                
                if (ov < threshold && v > threshold) {
                    System.out.println("Cascading positively: " + ov + " to " + v);
                    Message om = new Message(this, particles.get(rand.nextInt(particles.size())), m.q, Ternary.True);
                    outgoingCommunicate(om);
                } else if (ov > 1.0 - threshold && v < 1.0 - threshold) {
                    System.out.println("Cascading negatively: " + ov + " to " + v);
                    Message om = new Message(this, particles.get(rand.nextInt(particles.size())), m.q, Ternary.False);
                    outgoingCommunicate(om);
                } else {
                    System.out.println("Comm didn't change anything: " + ov + " to " + v);
                }
            }
        }
    }

    enum Ternary { True, False, Unknown };
    
    class Message {
        private final Particle source;
        private final Particle dest;
        private final Quark q;
        private final Ternary m;
        
        public Message(Particle source, Particle dest, Quark q, Ternary m) {
            this.source = source;
            this.dest = dest;
            this.q = q;
            this.m = m;            
        }
    }
    
    class Observation {

        private final double v;
        private final UTMCoord utm;
        private final long t;

        public Observation(double v, UTMCoord utm, long t) {
            this.v = v;
            this.utm = utm;
            this.t = t;

        }
    }
}
