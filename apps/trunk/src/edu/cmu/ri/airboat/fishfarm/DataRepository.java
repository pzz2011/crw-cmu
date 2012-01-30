/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.fishfarm;

import edu.cmu.ri.airboat.general.LocationInfo;
import edu.cmu.ri.airboat.general.Observation;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.UtmPose;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import robotutils.Pose3D;

/**
 *
 * @author pscerri
 */
public class DataRepository {

    // @todo Allow setting of index and contour
    ArrayList<SensorData> rawData = new ArrayList<SensorData>(1000);
    ArrayList<Observation> observations = new ArrayList<Observation>(1000);
    ArrayList<LocationInfo[][]> locInfo = new ArrayList<LocationInfo[][]>();
    int divisions = 10;
    double dx = 1.0, dy = 1.0;
    private Hashtable<String, Integer> baseIndicies = new Hashtable<String, Integer>();
    private LatLon mins;
    private LatLon maxs;
    private UTMCoord utmMin = null;
    private UTMCoord utmMax = null;
    boolean contoursOn = true;
    double contourValue = 80.01;
    int indexOfInterest = 0;
    private DecimalFormat df = new DecimalFormat("#.###");
    private Random rand = new Random();

    public enum ImageType {

        Grid, Point, Interpolated, None;
    };
    private ImageType imgType = ImageType.Grid;

    public DataRepository(LatLon mins, LatLon maxs) {
        this.mins = mins;

        utmMin = UTMCoord.fromLatLon(mins.latitude, mins.longitude);

        this.maxs = maxs;

        utmMax = UTMCoord.fromLatLon(maxs.latitude, maxs.longitude);

        setds();
    }

    public void setMaxs(LatLon maxs) {
        utmMax = UTMCoord.fromLatLon(maxs.latitude, maxs.longitude);
        this.maxs = maxs;
        setds();
    }

    public void setMins(LatLon mins) {
        utmMin = UTMCoord.fromLatLon(mins.latitude, mins.longitude);
        this.mins = mins;
        setds();
    }

    public void setImgType(ImageType imgType) {
        this.imgType = imgType;
    }

    private void setds() {

        dx = (utmMax.getEasting() - utmMin.getEasting()) / (double) divisions;
        dy = (utmMax.getNorthing() - utmMin.getNorthing()) / (double) divisions;

        // System.out.println("Used " + utmMax.getEasting() + " " + utmMin.getEasting() + " and " + (double) divisions + " to get " + dx);
    }

    double setContourPercentOfMax(double d) {
        try {
        LocationInfo[][] model = locInfo.get(indexOfInterest);
        if (model == null) {
            // @todo What to do when contour set and no data?
        } else {
            double mean = computeMean(model);
            double extent = computeExtent(mean, model);
            contourValue = (mean - extent/2.0) + extent*d;
        }
        } catch (IndexOutOfBoundsException e) {}
        
        return contourValue;
    }

    public double getContourValue() {
        return contourValue;
    }

    public void setContourValue(double contourValue) {
        this.contourValue = contourValue;
    }     
    
    void addData(SensorData sd, UtmPose _pose) {
        rawData.add(sd);

        for (int i = 0; i < sd.data.length; i++) {
            Observation o = new Observation("Sensor" + sd.type, sd.data[i], UtmPoseToDouble(_pose.pose), _pose.origin.zone, _pose.origin.isNorth);

            newObservation(o, i);
        }
    }

    private double[] UtmPoseToDouble(Pose3D p) {
        double[] d = new double[3];
        d[0] = p.getX();
        d[1] = p.getY();
        d[2] = p.getZ();
        return d;
    }

    public void newObservation(Observation o, int index) {
        observations.add(o);

        o.index = index;

        LocationInfo[][] li = null;

        // @todo This will work because there is a loop sending the pieces of data, but too dangerous
        Integer baseI = baseIndicies.get(o.variable);
        if (baseI == null) {
            baseI = locInfo.size();
            baseIndicies.put(o.variable, baseI);
        }
        index += baseI;

        try {
            li = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
        }

        if (li == null) {
            while (locInfo.size() <= index) {
                locInfo.add(initLocInfo());
            }
            li = locInfo.get(index);
        }

        int bx = toXIndex(o.getWaypoint()[0]);
        int by = toYIndex(o.getWaypoint()[1]);

        try {
            if (li[bx][by] == null) {
                li[bx][by] = new LocationInfo();
            }

            li[bx][by].addObs(o);

            // System.out.println("Added obs to " + bx + " " + by + " mean " + li[bx][by].getMean() + " std. dev. " + li[bx][by].getStdDev() + " count " + li[bx][by].getCount());

            // Update the inverse distance interpolation values
            for (int i = 0; i < divisions; i++) {
                for (int j = 0; j < divisions; j++) {
                    double dist = Math.sqrt(((i - bx) * (i - bx)) + (j - by) * (j - by) + 1);
                    double contrib = 1.0 / dist;
                    if (li[i][j] == null) {
                        li[i][j] = new LocationInfo();
                    }
                    li[i][j].interpolationContributions += contrib;
                    li[i][j].interpolationValue += (o.value * contrib);
                }
            }


        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("OUT OF EXTENT: " + bx + " " + li.length + " " + by + " " + li[0].length);
        }
    }

    private int toXIndex(double easting) {

        // System.out.println("Using " + utmMax.getEasting() + " " + easting + " " + dx);

        return (int) Math.floor((easting - utmMin.getEasting()) / dx);
    }

    private int toYIndex(double northing) {
        return (int) Math.floor((northing - utmMin.getNorthing()) / dy);
    }

    private LocationInfo[][] initLocInfo() {
        return new LocationInfo[divisions][divisions];
    }

    /*
     *  Autonomous sensing algorithm stuff
     */
    public enum AutonomyAlgorithm {

        Random,
        Lawnmower, // Or as Balajee would say lawn-mover
        Uncertainty,
        Contour;
    }
    private AutonomyAlgorithm alg = AutonomyAlgorithm.Random;

    public void setAlg(AutonomyAlgorithm alg) {
        this.alg = alg;
    }

    public AutonomyAlgorithm getAlg() {
        return alg;
    }
    private ArrayList<FishFarmBoatProxy> autonomousProxies = new ArrayList<FishFarmBoatProxy>();

    public ArrayList<Position> getAutonomyPath(FishFarmBoatProxy proxy) {
        switch (alg) {
            case Random:
                ArrayList<Position> p = new ArrayList<Position>();
                p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
                return p;

            case Lawnmower:
                return getLawnmowerPlan(autonomousProxies.size(), autonomousProxies.indexOf(proxy));

            case Uncertainty:
                return getMaxUncertaintyPlan();

            case Contour:
                return getContourFocusPlan();

            default:
                System.out.println("Unknown autonomy algorithm: " + alg + ", using random");
                p = new ArrayList<Position>();
                p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
                return p;
        }

    }

    public void addAutonomous(FishFarmBoatProxy p) {
        autonomousProxies.add(p);

        // @todo Reassign all proxies at this point
    }

    public void removeAutonomous(FishFarmBoatProxy p) {
        autonomousProxies.remove(p);

        // @todo Reassign all proxies at this point
    }

    private ArrayList<Position> getLawnmowerPlan(int count, int index) {

        System.out.println("Index is " + index);

        int yPer = (int) Math.max(2.0, Math.ceil((double) (divisions - 1) / (double) count));

        ArrayList<Position> path = new ArrayList<Position>();

        for (int i = yPer * index + 1; i < (yPer * (index + 1)); i += 2) {

            int ri = Math.min(divisions - 2, i);

            int xb = 0;
            int xt = divisions;

            path.add(indexToPosition(xb, ri));
            path.add(indexToPosition(xt, ri));
            path.add(indexToPosition(xt, ri + 1));
            path.add(indexToPosition(xb, ri + 1));

        }

        return path;
    }

    private ArrayList<Position> getContourFocusPlan() {
        ArrayList<Position> p = new ArrayList<Position>();

        LocationInfo[][] data = locInfo.get(indexOfInterest);
        if (data == null) {
            // No data, select random point
            p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
        } else {
            double best = -1.0;
            int bestI = -1, bestJ = -1;
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    double pureVal = data[i][j].valueOfMoreObservations();
                    // Want this to be 1.0 when same, 0 when very different
                    double contourDist = Math.abs(contourValue - data[i][j].interpolationValue) / contourValue;
                    double v = pureVal * contourDist;
                    if (v > best) {
                        best = v;
                        bestI = i;
                        bestJ = j;
                    }
                }
            }
            p.add(indexToPosition(bestI, bestJ));
        }

        return p;
    }

    private ArrayList<Position> getMaxUncertaintyPlan() {
        ArrayList<Position> p = new ArrayList<Position>();

        LocationInfo[][] data = locInfo.get(indexOfInterest);
        if (data == null) {
            // No data, select random point
            p.add(indexToPosition(rand.nextInt(divisions), rand.nextInt(divisions)));
        } else {
            double best = -1.0;
            int bestI = -1, bestJ = -1;
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[0].length; j++) {
                    double v = data[i][j].valueOfMoreObservations();
                    if (v > best) {
                        best = v;
                        bestI = i;
                        bestJ = j;
                    }
                }
            }
            p.add(indexToPosition(bestI, bestJ));
        }

        return p;
    }

    /*
     *  END Autonomous sensing algorithm stuff
     */
    private Position indexToPosition(int x, int y) {
        System.out.println("Index to pos with " + x + " " + y);
        UTMCoord utm = UTMCoord.fromUTM(utmMin.getZone(), utmMin.getHemisphere(), utmMin.getEasting() + (dx * (x + 0.5)), utmMin.getNorthing() + (dy * (y + 0.5)));
        LatLon ll = new LatLon(utm.getLatitude(), utm.getLongitude());
        return new Position(ll, 0.0);
    }

    public synchronized BufferedImage makeBufferedImage(int index) {

        switch (imgType) {
            case Grid:
                return makeGridBufferedImage(index);

            case Interpolated:
                return makeInterpolatedBufferedImage(index);

            case Point:
                return makePointBufferedImage(index);

            case None:
            default:
                System.out.println("Unsupported image type: " + imgType);
                return null;
        }

    }

    private BufferedImage makePointBufferedImage(int index) {
        int width = 500;
        int height = 500;

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

        LocationInfo[][] model = null;

        try {
            model = locInfo.get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        double denomX = utmMax.getEasting() - utmMin.getEasting();
        double denomY = utmMax.getNorthing() - utmMin.getNorthing();

        double mean = computeMean(model);
        double maxExtent = computeExtent(mean, model);        
        
        for (Observation o : (Iterable<Observation>)observations.clone()) {
            
            int x = (int)(((o.waypoint[0] - utmMin.getEasting())/denomX) * width);
            int y = (int)(((o.waypoint[1] - utmMin.getNorthing())/denomY) * height);
            
            double alpha = Math.abs((mean - o.value) / maxExtent);
            alpha = Math.min(1.0, alpha);

            if (o.value < mean) {
                g2.setColor(new Color(1.0f, 0.0f, 0.0f, (float) alpha));
            } else {
                g2.setColor(new Color(0.0f, 1.0f, 0.0f, (float) alpha));
            }

            g2.fillOval(x, (int) (height - y), (int)(1 + 5 * alpha), (int)(1 + 5 * alpha));
        }
        return bimage;
    }

    double computeMean(LocationInfo[][] model) {
        int c = 0;
        double mean = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    mean += model[i][j].getMean();
                    c++;
                }
            }
        }
        mean /= (double) c;

        return mean;
    }

    double computeExtent(double mean, LocationInfo[][] model) {
        double maxExtent = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    double diff = Math.abs(mean - model[i][j].getMean());
                    if (diff > maxExtent) {
                        maxExtent = diff;
                    }
                }
            }
        }
        return maxExtent;
    }

    private BufferedImage makeGridBufferedImage(int index) {
        int width = 100;
        int height = 100;

        BufferedImage bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bimage.createGraphics();

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

        double mean = computeMean(model);

        double maxExtent = computeExtent(mean, model);

        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {

                if (model[i][j] != null && model[i][j].getCount() > 0) {

                    double v = 0.0;

                    v = model[i][j].getMean();

                    g2.setColor(Color.black);
                    g2.drawString(df.format(v), bx * i, (int) (height - by * (j + 1)));

                    double alpha = Math.abs((mean - v) / maxExtent);
                    alpha = Math.min(1.0, alpha);

                    if (v < mean) {
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

        return bimage;
    }

    private BufferedImage makeInterpolatedBufferedImage(int index) {
        int width = 100;
        int height = 100;

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

        double mean = computeMean(model);

        double maxExtent = 0.0;
        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {
                if (model[i][j] != null && model[i][j].getCount() > 0) {
                    double diff = Math.abs(mean - model[i][j].getMean());
                    if (diff > maxExtent) {
                        maxExtent = diff;
                    }
                }
            }
        }

        int[][] marchingSquares = new int[model.length][model.length];

        for (int i = 0; i < model.length; i++) {
            for (int j = 0; j < model[0].length; j++) {

                if (model[i][j] != null) {

                    double v = model[i][j].getInterpolatedValue();

                    if (v > contourValue) {
                        marchingSquares[i][j] = 1;
                    } else {
                        marchingSquares[i][j] = 0;
                    }

                    g2.setColor(Color.black);
                    g2.drawString(df.format(v), bx * i, (int) (height - by * (j + 1)));

                    double alpha = Math.abs((mean - v) / maxExtent);
                    alpha = Math.min(1.0, alpha);

                    if (v < mean) {
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

        if (contoursOn) {

            // g2.setStroke(new BasicStroke(2.0f));
            g2.setColor(Color.BLUE);

            // Notice the indicies on this are a bit quirky because counts is really the corners            
            int[][] counts = new int[model.length - 1][model.length - 1];
            for (int i = 0; i < counts.length; i++) {
                for (int j = 0; j < counts[i].length; j++) {

                    counts[i][j] += 1 * marchingSquares[i][j + 1];
                    counts[i][j] += 2 * marchingSquares[i + 1][j + 1];
                    counts[i][j] += 4 * marchingSquares[i + 1][j];
                    counts[i][j] += 8 * marchingSquares[i][j];

                }
            }

            for (int i = 0; i < counts.length; i++) {
                for (int j = 0; j < counts[i].length; j++) {
                    int lineNo = counts[i][j];
                    switch (lineNo) {
                        case 0:
                        case 15:
                            break;

                        default:
                            // g2.setColor(colors[lineNo]);
                            g2.drawLine((int) (bx * (i + lines[lineNo][0])), (int) (height - (by * (j + 1.5 - lines[lineNo][1]))),
                                    (int) (bx * (i + lines[lineNo][2])), (int) (height - (by * (j + 1.5 - lines[lineNo][3]))));
                    }
                }
            }

            // g2.setStroke(new BasicStroke(1.0f));
        }

        return bimage;
    }
    
    double[][] lines = {
        {0.0, 0.0, 0.0, 0.0}, // zero
        {0.0, 0.5, 0.5, 0.0},
        {0.5, 0.0, 1.0, 0.5},
        {0.0, 0.5, 1.0, 0.5},
        {0.5, 1.0, 1.0, 0.5}, // four
        {0.0, 0.0, 0.0, 0.0},
        {0.5, 0.0, 0.5, 1.0},
        {0.0, 0.5, 0.5, 1.0},
        {0.0, 0.5, 0.5, 1.0}, // eight
        {0.5, 0.0, 0.5, 1.0},
        {0.0, 0.0, 0.0, 0.0},
        {0.5, 1.0, 1.0, 0.5},
        {0.0, 0.5, 1.0, 0.5}, // twelve
        {0.5, 0.0, 1.0, 0.5},
        {0.0, 0.5, 0.5, 0.0},
        {0.0, 0.0, 0.0, 0.0}};
    Color[] colors = {
        Color.BLACK, // 0
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA, // 4
        Color.ORANGE,
        Color.PINK, // 6
        Color.RED,
        Color.RED,
        Color.PINK, // 9      
        Color.ORANGE,
        Color.MAGENTA, // 11
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.BLACK // 15
    };
}