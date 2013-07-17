/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general;

import edu.cmu.ri.airboat.general.Observation;
import java.util.ArrayList;
import java.util.ListIterator;

/**
 *
 * @author pscerri
 */
public class LocationInfo {

    double mean = 0.0;
    double tot = 0.0;
    int count = 0;
    ArrayList<Observation> obs = new ArrayList<Observation>();
    double lowerBound = 0.0;
    double upperBound = 0.0;
    public double interpolationValue = 0.0;
    public double interpolationContributions = 0.0;

    public LocationInfo(double l, double u) {
        lowerBound = l;
        upperBound = u;
    }

    public double getInterpolatedValue() {
        if (interpolationContributions > 0) {
            return interpolationValue / interpolationContributions;
        } else {
            return 0.0;
        }
    }

    public void addObs(Observation o) {
        obs.add(o);
        tot += o.getValue();
        count++;
        mean = tot / count;
        // System.out.println("Count now " + count + " and mean " + mean);

        // ABHINAV COMMENT IN System.out.print(lowerBound + " - " + upperBound + " " + o.getValue() + " " + o.getGradient());
        if (o.getGradient() > 0.0) {
            if (o.getValue() > lowerBound) {
                lowerBound = o.getValue();
            }
            
            if (o.getValue() > upperBound) {
                // System.out.println(">>>>>>>>>> Reassessing upper bound " + upperBound + " to " + o.getValue());
                upperBound = o.getValue();
            }
        } else if (o.getGradient() < 0.0) {
            if (o.getValue() < upperBound) {
                upperBound = o.getValue();
            }
            
            if (o.getValue() < lowerBound) {
                // System.out.println(">>>>>>>>>> Reassessing lower bound " + lowerBound + " to " + o.getValue());
                lowerBound = o.getValue();
            }
        }/* else if (o.getGradient() == 0.0) {
            upperBound = o.getValue();
            lowerBound = o.getValue();
        }*/
        // ABHINAV COMMENT IN System.out.println(" " + lowerBound + " " + upperBound);
        // @todo Notice we do nothing with 0.0 gradient, which could help a lot
    }

    public double getMean() {
        return mean;
    }

    public int getCount() {
        return count;
    }

    public ArrayList<Observation> getObs() {
        return obs;
    }

    public double getStdDev() {
        double ss = 0.0;
        //for (Observation observation : obs) {
        ListIterator<Observation> lo = obs.listIterator();
        while (lo.hasNext()) {
            try {
                Observation observation = lo.next();
                ss += (mean - observation.getValue()) * (mean - observation.getValue());
            } catch (NullPointerException e) {
                System.out.println("NULL pointer in DataDisplay, ignored");
            }
        }
        ss /= obs.size();
        return Math.sqrt(ss);
    }

    public double valueOfMoreObservations() {
        double d = 0.0;
        double s = getStdDev();
        
        if (count == 0 || count == 1) {
            d = Double.MAX_VALUE;
        } else {
            d = (s * s) * Math.pow(0.99, count);
        }
        return d;
    }
    
    public double interpolatedValueOfMoreObservations() {
        return 100.0 / interpolationContributions;
    }

    public double getBoundsMidpoint() {
        return (lowerBound + upperBound) / 2.0;
    }

    public double getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }

    public void userChange(boolean up) {
        double userContrib = 0.001;
        double value = getInterpolatedValue();
        
        if (up) value *= 1.1;
        else value *= 0.9;
        
        // System.out.print("Before " + getInterpolatedValue());
        interpolationValue += value * userContrib;
        interpolationContributions += userContrib;
        // System.out.println("  after " + getInterpolatedValue());
    }
}
