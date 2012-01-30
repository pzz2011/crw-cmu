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

    public double interpolationValue = 0.0;
    public double interpolationContributions = 0.0;
    
    public LocationInfo() {        
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
    
}
