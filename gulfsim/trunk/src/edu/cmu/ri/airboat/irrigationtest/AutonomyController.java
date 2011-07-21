/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class AutonomyController implements IrrigationTestInterface.IrrigationTestInterfaceListener {

    IrrigationTestInterface boats = new InterfaceTester();    
    
    String algorithm = "Lawnmower";        
    Hashtable<Integer, double[]> locations = new Hashtable<Integer, double[]>();
    ArrayList<Observation> observations = new ArrayList<Observation>();
    LocationInfo[][] locInfo = null;
    double[] ul = null;
    double[] lr = null;
    private int xCount;
    private int yCount;
    double dx = 0;
    double dy = 0;
    Random rand = new Random();

    public AutonomyController(double[] ul, double[] lr, int xCount, int yCount) {
        boats.setExtent(ul, lr);
        boats.addListener(this);

        this.xCount = xCount;
        this.yCount = yCount;

        setExtent(ul, lr);

        boatDone(0);
    }

    public double getWidth() {
        return lr[0] - ul[0];
    }

    public double getHeight() {
        return ul[1] - lr[1];
    }

    public LocationInfo[][] getLocInfo() {
        return locInfo;
    }

    public void setxCount(int xCount) {
        this.xCount = xCount;
        setExtent(ul, lr);
    }

    public void setyCount(int yCount) {
        this.yCount = yCount;
        setExtent(ul, lr);
    }

    public void setExtent(double[] ul, double[] lr) {
        boats.setExtent(ul, lr);
        this.ul = ul;
        this.lr = lr;

        // Compute box widths and heights (assumes northern hemisphere lr is > on x-axis)
        dx = (lr[0] - ul[0]) / ((double) xCount);
        dy = (ul[1] - lr[1]) / ((double) yCount);

        locInfo = new LocationInfo[xCount][yCount];
        
        ArrayList<Observation> cobs = (ArrayList<Observation>)observations.clone();
        observations.clear();
        for (Observation obs : cobs) {
            newObservation(obs);
        }
    }

    public void newObservation(Observation o) {
        observations.add(o);

        int bx = (int) ((o.getWaypoint()[0] - ul[0]) / dx);
        int by = (int) ((o.getWaypoint()[1] - lr[1]) / dy);

        try {
            if (locInfo[bx][by] == null) {
                locInfo[bx][by] = new LocationInfo();
            }

            // System.out.println("Added obs to " + bx + " " + by);

            locInfo[bx][by].addObs(o);

            notifyListeners();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("OUT OF EXTENT");
        }
    }

    public void newBoatPosition(int boatNo, double[] pose) {
        locations.put(boatNo, pose);
        notifyListeners();
    }

    public void setAlgorithm(String algorithm) {

        this.algorithm = algorithm;
        // @todo Only changes algorithm for boat 0
        boatDone(0);
    }

    public void boatDone(int boatNo) {

        System.out.println("Boat planning");

        if (ul == null) {
            System.out.println("No extents, no plan");

            return;
        }

        double[][] poses = null;

        if (algorithm.equalsIgnoreCase("Lawnmower")) {

            poses = new double[(yCount * 2) - 1][2];

            boolean left = true, same = true;
            double y = lr[1];
            for (int i = 0; i < poses.length; i++) {
                if (!same) {
                    left = !left;
                    same = true;
                } else {
                    same = false;
                }
                if (left) {
                    poses[i][0] = ul[0];
                } else {
                    poses[i][0] = lr[0];
                }
                if (same) {
                    y += dy;
                }
                poses[i][1] = y;
            }
        } else if (algorithm.equalsIgnoreCase("Max Uncertainty")) {
            
        }



        boats.setWaypoints(boatNo, poses);
    }

    public double randLon() {
        return (rand.nextDouble() * (lr[0] - ul[0])) + ul[0];
    }

    public double randLat() {
        return (rand.nextDouble() * (lr[1] - ul[1])) + ul[1];
    }

    public double[] getBoatLocation(int i) {
        return locations.get(i);
    }

    public Hashtable<Integer, double[]> getLocations() {
        return locations;
    }

    private void notifyListeners() {
        for (AutonomyEventListener autonomyEventListener : listeners) {
            autonomyEventListener.changed();
        }
    }
    ArrayList<AutonomyEventListener> listeners = new ArrayList<AutonomyEventListener>();

    public void addListener(AutonomyEventListener l) {
        listeners.add(l);
    }

    public interface AutonomyEventListener {

        public void changed();
    }

    public class LocationInfo {

        double mean = 0.0;
        double tot = 0.0;
        int count = 0;
        ArrayList<Observation> obs = new ArrayList<Observation>();

        public void addObs(Observation o) {
            obs.add(o);
            tot += o.getValue();
            count++;
            mean = tot / count;
        }

        public double getMean() {
            return mean;
        }
                
        public double getStdDev() {
            
            double ss = 0.0;
            
            //for (Observation observation : obs) {
            ListIterator<Observation> lo = obs.listIterator();
            while (lo.hasNext()) {
                Observation observation = lo.next();
                ss += (mean - observation.getValue()) * (mean - observation.getValue());
            }
            
            ss /= obs.size();
            
            return Math.sqrt(ss);
        }
    }
}
