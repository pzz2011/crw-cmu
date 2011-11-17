/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import edu.cmu.ri.airboat.irrigationtest.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;

/**
 *
 * @author pscerri
 */
public class DataDisplay {

    // AutonomyController autonomy = null;
    private Dimension minSize = new Dimension(500, 500);
    private boolean showMean = true;
    // Hack version for keeping mean value for drawing
    double mean = 0.0;
    double maxExtent = 1.0;
    DecimalFormat df = new DecimalFormat("#.###");
    ArrayList<double[]> prev = new ArrayList<double[]>();
    double width = 100.0;
    double height = 100.0;
    ArrayList<Observation> observations = new ArrayList<Observation>();
    ArrayList<LocationInfo[][]> locInfo = null;
    double[] ul = null;
    double[] lr = null;
    private int xCount = 10;
    private int yCount = 10;
    double dx = 10.0;
    double dy = 10.0;

    /**
     * Some of these numbers are confusing, ul and lr only work in one hemisphere, translation should fix it.
     * 
     * @param ul
     * @param lr 
     */
    public DataDisplay(double[] ul, double[] lr) {
        this.ul = ul;
        this.lr = lr;

        // Initialize model
        locInfo = new ArrayList<LocationInfo[][]>();
                
        if (ul[0] > lr[0]) {
            double d = ul[0];
            ul[0] = lr[0];
            lr[0] = d;
        }
        dx = (lr[0] - ul[0])/xCount;
        
        if (ul[1] < lr[1]) {
            double d = ul[1];
            ul[1] = lr[1];
            lr[1] = d;
        }
        dy = (ul[1] - lr[1])/yCount;        
    }

    private LocationInfo[][] initLocInfo() {
        return new LocationInfo[xCount][yCount];
    }
    
    public int getxCount() {
        return xCount;
    }

    public void setxCount(int xCount) {
        // @todo put the data into the right boxes when this changes
        this.xCount = xCount;        
    }

    public int getyCount() {
        return yCount;
    }

    public void setyCount(int yCount) {
        // @todo put the data into the right boxes when this changes
        this.yCount = yCount;
    }

    /**
     * x and y are normalized so that 0,0 is bottom, left and 1,1 is top, right.
     * @param x
     * @param y
     * @param index
     * @return 
     */
    public double getValueAt(double x, double y, int index) {
        
        int xi = (int)Math.floor(x * xCount);
        int yi = (int)Math.floor(y * yCount);

        try {
            return locInfo.get(index)[xi][yi].mean;
        } catch (NullPointerException e) {
            return Double.NaN;
        }
        
    }
    
    public BufferedImage makeBufferedImage(int index) {

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

        // @todo Work out scaling

        // double dx = width / autonomy.getWidth();
        // double dy = height / autonomy.getHeight();
        double dx = 1.0;
        double dy = 1.0;

        g2.clearRect(0, 0, (int) width, (int) height);

        LocationInfo[][] model = null;
        
        try {
            model = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        int bx = (int) (width / model.length);
        int by = (int) (height / model[0].length);

        double prevMean = mean;
        double prevMaxExtent = maxExtent;

        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {

                if (model[i][j] != null) {

                    double v = 0.0;

                    if (showMean) {
                        v = model[i][j].getMean();
                    } else {
                        v = model[i][j].getStdDev();
                    }

                    g2.setColor(Color.black);
                    g2.drawString(df.format(v), bx * i, (int) (height - by * (j + 1)));

                    mean = (0.99 * mean) + (0.01 * v);

                    if (maxExtent < Math.abs(mean - v)) {
                        maxExtent *= 1.01;
                    } else {
                        maxExtent *= 0.999;
                    }

                    double alpha = Math.abs((prevMean - v) / prevMaxExtent);
                    alpha = Math.min(1.0, alpha);

                    if (v < prevMean) {
                        g2.setColor(new Color(1.0f, 0.0f, 0.0f, (float) alpha));
                    } else {
                        g2.setColor(new Color(0.0f, 1.0f, 0.0f, (float) alpha));
                    }

                    // System.out.println("v = " + v + " mean = " + mean + " maxExtent=" + maxExtent + " alpha=" + alpha);

                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                }

                g2.fillRect(bx * i, (int) (height - by * (j + 1)), bx, by);

            }
        }

        // g2.setColor(Color.black);

        /*
        // @todo Not sure what these should be
        // double left = autonomy.ul[0];
        // double bottom = autonomy.lr[1];

        double left = 0.0;
        double bottom = 0.0;

        for (double[] p : prev) {
            g2.fillOval((int) ((p[0] - left) * dx), (int) (height - ((p[1] - bottom) * dy)), 5, 5);
        }
         */
        
        /*
        // Draw boats
        for (Integer boat : autonomy.getLocations().keySet()) {
        double[] pose = autonomy.getBoatLocation(boat);
        
        int x = (int) ((pose[0] - left) * dx);
        int y = (int) ((pose[1] - bottom) * dy);
        
        // System.out.println("Drawing at " + x + " " + y + " based on " + pose[1] + " " + dy + " " + height + " " + autonomy.getHeight());
        
        g2.drawString("B" + boat, x, (int) (height - y));
        
        double[][] plan = autonomy.getPlans().get(boat);
        
        if (plan != null) {
        
        // g2.drawLine(x, (int)(height - y), (int) (plan[0][0] * dx), (int) (height - plan[0][1] * dy));
        
        for (int i = 1; i < plan.length; i++) {
        double[] ds = plan[i - 1];
        double[] de = plan[i];
        g2.drawLine((int) ((ds[0] - left) * dx), (int) (height - (ds[1] - bottom) * dy), (int) ((de[0] - left) * dx), (int) (height - (de[1] - bottom) * dy));
        }
        }
        
        prev.add(pose);
        if (prev.size() > 200) {
        prev.remove(0);
        }
        
        }
         */
        return bimage;
    }

    public void setShowMean(boolean showMean) {
        this.showMean = showMean;
    }

    public void newObservation(Observation o, int index) {
        observations.add(o);

        LocationInfo[][] li = null; 
        
        try {
            li = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {}
        
        if (li == null) {
            while(locInfo.size() <= index) {                
                locInfo.add(initLocInfo());
            }
            li = locInfo.get(index);
        }
        
        int bx = (int) ((o.getWaypoint()[0] - ul[0]) / dx);
        int by = (int) ((o.getWaypoint()[1] - lr[1]) / dy);

        try {
            if (li[bx][by] == null) {
                li[bx][by] = new LocationInfo();
            }

            System.out.println("Added obs to " + bx + " " + by);

            li[bx][by].addObs(o);

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("OUT OF EXTENT: " + bx + " " + li.length + " " + by + " " + li[0].length);
        }
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

        public double valueOfMoreObservations() {
            double d = 0.0;

            double s = getStdDev();

            if (count == 0 || count == 1) {
                d = Double.MAX_VALUE;
            } else {
                d = (s * getStdDev()) * Math.pow(0.99, count);
            }

            return d;
        }
    }
}
