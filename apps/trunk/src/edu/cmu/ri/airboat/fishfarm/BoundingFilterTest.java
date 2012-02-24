/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.fishfarm;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class BoundingFilterTest {

    int size = 10;
    int noBoats = 1;
    int moveTime = 10;
    int totalTime = 10000;
    double sensorDelta = 0.01;
    double maWeight = 0.9;
    
    double[][] realValues = null;
    double[][] upperBounds = null;
    double[][] lowerBounds = null;
    double[][] averages = null;
    double lower = 0.0, upper = 1.0;
    Boat[] boats = null;
    DecimalFormat df = new DecimalFormat("#.###");
    Random rand = new Random(0L);

    public BoundingFilterTest() {

        realValues = new double[size][size];
        for (int i = 0; i < realValues.length; i++) {
            for (int j = 0; j < realValues.length; j++) {
                realValues[i][j] = rand.nextDouble();
            }
        }

        System.out.println("Real values");
        printArray(realValues);
        
        upperBounds = initArray(upper);
        lowerBounds = initArray(lower);
        averages = initArray(lower + 0.5 * (upper-lower));
        
        boats = new Boat[noBoats];
        for (int i = 0; i < boats.length; i++) {
            boats[i] = new Boat();            
        }
        
        for (int i = 0; i < totalTime; i++) {
            for (int j = 0; j < boats.length; j++) {
                boats[j].step();                
                
                /*
                System.out.println("Real values");
                printArray(realValues);
                System.out.println("Avg: ");
                printArray(averages);
                System.out.println("Diff: " + comp(averages));
                System.out.println("Lower: ");
                printArray(lowerBounds);
                System.out.println("Diff: " + comp(lowerBounds));
                System.out.println("Upper: ");
                printArray(upperBounds);
                System.out.println("Diff: " + comp(upperBounds));
                System.out.println("Mids: " + compMids(lowerBounds, upperBounds));
                */
                
                if (i % 20 == 0) System.out.println(comp(averages) + "\t" + comp(lowerBounds) + "\t" + comp(upperBounds) + "\t" + compMids(lowerBounds, upperBounds));
            }
            
        }
    }

    private double comp(double [][] a) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                s += Math.abs(a[i][j] - realValues[i][j]);
            }
        }
        return s;
    }
    
    private double compMids(double [][] l, double [][] u) {
        double s = 0.0;
        for (int i = 0; i < l.length; i++) {
            for (int j = 0; j < l.length; j++) {
                s += Math.abs(((u[i][j]+l[i][j])/2.0) - realValues[i][j]);
            }
        }
        return s;
    }
    
    private double[][] initArray(double v) {
        double[][] a = new double[size][size];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                a[i][j] = v;
            }
        }
        return a;
    }

    private void printArray(double[][] a) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                System.out.print(df.format(a[i][j]) + "\t");
            }
            System.out.println("");
        }
    }

    public static void main(String argv[]) {
        new BoundingFilterTest();
    }

    static int count = 0;
    private class Boat {
        
        int id = count++;
        int x = 0, y = 0;
        int moveTimeRemaining = 0;
        double currMeasure = 0.0;
        double gradient = 0.0;
        
        void step() {
            
            // Boat movement
            if (--moveTimeRemaining <= 0) {
                x += rand.nextBoolean() ? -1 : 1;
                y += rand.nextBoolean() ? -1 : 1;
                x = Math.max(0, Math.min(x, size - 1));
                y = Math.max(0, Math.min(y, size - 1));
                moveTimeRemaining = moveTime;
                // System.out.println("Boat " + id + " moved to " + x + " " + y);
            }
            
            // Update sensor reading
            double change = 0.0;
            if (currMeasure < realValues[x][y]) {
                change = Math.min(sensorDelta, Math.abs(currMeasure - realValues[x][y]));
            } else if (currMeasure > realValues[x][y]) {
                change = -Math.min(sensorDelta, Math.abs(currMeasure - realValues[x][y]));
            }
            currMeasure += change;
            gradient = change;
            // System.out.println("New curr sensor: " + df.format(currMeasure) + " gradient " + gradient + " real " + realValues[x][y]);
            
            // update the filters
            averages[x][y] = maWeight * averages[x][y] + (1.0 - maWeight) * currMeasure;
            if (gradient < 0.0) {
                upperBounds[x][y] = Math.min(upperBounds[x][y], currMeasure);
            } else if (gradient > 0.0) {
                lowerBounds[x][y] = Math.max(lowerBounds[x][y], currMeasure);
            }
        }
    }
}
