/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.enduser;

import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.airboat.general.BoatProxyListener;
import edu.cmu.ri.airboat.general.Observation;
import edu.cmu.ri.airboat.general.ProxyManagerListener;
import edu.cmu.ri.airboat.general.ProxyManager;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.data.SensorData;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Polygon;
import java.util.ArrayList;

/**
 * Singleton
 *
 * @todo Stop the autonomy
 *
 * @author pscerri
 */
public class IntelligenceAlgorithms implements ProxyManagerListener, BoatProxyListener {

    private static BoatProxy selectedProxy;

    public static enum Algorithm {

        Grid, Entropy
    };
    private static Algorithm currAlg = Algorithm.Grid;
    private static ArrayList<BoatProxy> proxies = new ArrayList<BoatProxy>();
    private static Polygon area = null;
    private static Angle minLat = Angle.ZERO, maxLat = Angle.ZERO, minLon = Angle.ZERO, maxLon = Angle.ZERO;
    private static Angle currLat = Angle.ZERO;
    private boolean autonomous = false, allAutonomous = false;
    private static boolean listening = false;
    private static DataDisplay data = null;

    public IntelligenceAlgorithms() {
        if (!listening) {
            (new ProxyManager()).addListener(this);
            listening = true;
        }
    }

    public void proxyAdded(final BoatProxy bp) {

        // System.out.println("INTELLIGENCE INFORMED");

        proxies.add(bp);
        bp.addListener(this);
        for (int i = 0; i < 3; i++) {
            bp.addSensorListener(i, new SensorListener() {
                public void receivedSensor(SensorData sd) {
                    if (data != null) {
                        for (int i = 0; i < sd.data.length; i++) {
                            Position p = bp.getCurrLoc();
                            UTMCoord c = UTMCoord.fromLatLon(p.latitude, p.longitude);
                            Observation obs = new Observation(sd.type.toString(), sd.data[0], new double [] {c.getEasting(), c.getNorthing()}, c.getZone(), c.getHemisphere().equalsIgnoreCase("N"));
                            data.newObservation(obs, i);
                        }
                    }
                }
            });
        }
    }

    public Algorithm getCurrAlg() {
        return currAlg;
    }

    public void setCurrAlg(Algorithm currAlg) {
        IntelligenceAlgorithms.currAlg = currAlg;
    }

    void setArea(Polygon pgon) {
        area = pgon;

        // Compute the bounding box
        minLat = Angle.POS360;
        maxLat = Angle.NEG360;
        minLon = Angle.POS360;
        maxLon = Angle.NEG360;

        for (LatLon latLon : pgon.getOuterBoundary()) {

            if (latLon.latitude.degrees > maxLat.degrees) {
                maxLat = latLon.latitude;
            } else if (latLon.latitude.degrees < minLat.degrees) {
                minLat = latLon.latitude;
            }

            if (latLon.longitude.degrees > maxLon.degrees) {
                maxLon = latLon.longitude;
            } else if (latLon.longitude.degrees < minLon.degrees) {
                minLon = latLon.longitude;
            }
        }

        currLat = minLat;

        UTMCoord utmUl = UTMCoord.fromLatLon(maxLat, minLon);
        double[] ul = new double[2];
        ul[0] = utmUl.getEasting();
        ul[1] = utmUl.getNorthing();

        UTMCoord utmLr = UTMCoord.fromLatLon(minLat, maxLon);
        double[] lr = new double[2];
        lr[0] = utmLr.getEasting();
        lr[1] = utmLr.getNorthing();

        System.out.println("DATA DISPLAY IS CREATED");
        data = new DataDisplay(ul, lr, pgon);

    }

    public void startAutonomyProxy() {

        System.out.println("Starting autonomy for proxy: " + selectedProxy);

        autonomous = true;

        if (selectedProxy != null) {
            generatePathFor(selectedProxy);
        }
    }

    public void startAutonomyAll() {

        allAutonomous = true;
        autonomous = true;

        System.out.println("Starting autonomy for all");

        for (BoatProxy boatProxy : proxies) {
            generatePathFor(boatProxy);
        }
    }

    void setSelectedProxy(BoatProxy selectedProxy) {
        this.selectedProxy = selectedProxy;
    }

    public void poseUpdated() {
        // @todo IntelligenceAlgorithms got a pose update
    }   

    // 
    // Path planning stuff
    // 
    public void waypointsComplete() {
        // @todo IntelligenceAlgorithms got a waypoint complete
        if (autonomous && !allAutonomous) {
            if (selectedProxy.getCurrWaypoint() == null) {
                generatePathFor(selectedProxy);
            }
        } else if (allAutonomous) {
            for (BoatProxy boatProxy : proxies) {
                if (boatProxy.getCurrWaypoint() == null) {
                    generatePathFor(boatProxy);
                }
            }
        }
    }

    private void generatePathFor(BoatProxy bp) {

        if (allAutonomous) {
            switch (currAlg) {
                case Grid:
                    bp.setWaypoints(getLawnmowerPath(1, 1));
                    break;

                case Entropy:
                    // @todo Handle entropy autonomy case
                    break;
            }
        } else {

            switch (currAlg) {
                case Grid:
                    bp.setWaypoints(getLawnmowerPath(1, 1));
                    break;

                case Entropy:
                    // @todo Handle entropy autonomy case
                    break;
            }

        }
    }
    // For most of the interesting part of the planet, 1 degree latitude is something like 110,000m
    // Longtitude varies a bit more, but 90,000m is a decent number for the purpose of this calculation
    // See http://www.csgnetwork.com/degreelenllavcalc.html
    static double lonDiff = 1.0 / 90000.0 * 10.0;
    static double latDiff = 1.0 / 110000.0 * 10.0;

    // @todo Need to stay in area
    // @todo Grid width needs to be configurable
    // @todo Watch out for maximimum path length
    private ArrayList<Position> getLawnmowerPath(int no, int of) {

        ArrayList<Position> path = new ArrayList<Position>();

        for (int i = 0; i < 3; i++) {

            currLat = currLat.addDegrees(latDiff);

            if (currLat.degrees > maxLat.degrees) {
                currLat = minLat;
            }

            path.add(new Position(new LatLon(currLat, getMinLonAt(minLon, currLat)), 0.0));
            path.add(new Position(new LatLon(currLat, getMaxLonAt(maxLon, currLat)), 0.0));

            currLat = currLat.addDegrees(latDiff);

            if (currLat.degrees > maxLat.degrees) {
                currLat = minLat;
            }

            path.add(new Position(new LatLon(currLat, getMaxLonAt(maxLon, currLat)), 0.0));
            path.add(new Position(new LatLon(currLat, getMinLonAt(minLon, currLat)), 0.0));
        }

        /*
         path.add(new Position(new LatLon(minLat, minLon), 0.0));
         path.add(new Position(new LatLon(maxLat, maxLon), 0.0));
         */

        return path;
    }

    private static Angle getMinLonAt(Angle minLon, Angle lat) {
        Angle mL = minLon;
        LatLon l1 = new LatLon(lat, mL);
        while (!isLocationInside(l1, (ArrayList<LatLon>) area.getOuterBoundary())) {
            mL = mL.addDegrees(lonDiff);
            l1 = new LatLon(lat, mL);
        }
        return mL;
    }

    private static Angle getMaxLonAt(Angle minLon, Angle lat) {
        Angle mL = minLon;
        LatLon l1 = new LatLon(lat, mL);
        while (!isLocationInside(l1, (ArrayList<LatLon>) area.getOuterBoundary())) {
            mL = mL.addDegrees(-lonDiff);
            l1 = new LatLon(lat, mL);
        }
        return mL;
    }

    public static boolean isLocationInside(LatLon point, ArrayList<? extends LatLon> positions) {
        if (point == null) {
            throw new IllegalArgumentException("isLocationInside failed");
        }
        boolean result = false;
        LatLon p1 = positions.get(0);
        for (int i = 1; i < positions.size(); i++) {
            LatLon p2 = positions.get(i);

// Developped for clarity
//            double lat = point.getLatitude().degrees;
//            double lon = point.getLongitude().degrees;
//            double lat1 = p1.getLatitude().degrees;
//            double lon1 = p1.getLongitude().degrees;
//            double lat2 = p2.getLatitude().degrees;
//            double lon2 = p2.getLongitude().degrees;
//            if ( ((lat2 <= lat && lat < lat1) || (lat1 <= lat && lat < lat2))
//                    && (lon < (lon1 - lon2) * (lat - lat2) / (lat1 - lat2) + lon2) )
//                result = !result;

            if (((p2.getLatitude().degrees <= point.getLatitude().degrees
                    && point.getLatitude().degrees < p1.getLatitude().degrees)
                    || (p1.getLatitude().degrees <= point.getLatitude().degrees
                    && point.getLatitude().degrees < p2.getLatitude().degrees))
                    && (point.getLongitude().degrees < (p1.getLongitude().degrees - p2.getLongitude().degrees)
                    * (point.getLatitude().degrees - p2.getLatitude().degrees)
                    / (p1.getLatitude().degrees - p2.getLatitude().degrees) + p2.getLongitude().degrees)) {
                result = !result;
            }

            p1 = p2;
        }
        return result;
    }

    void showDataDisplay() {
        DataDisplayPopup popup = new DataDisplayPopup(data);
        popup.setVisible(true);
    }
}