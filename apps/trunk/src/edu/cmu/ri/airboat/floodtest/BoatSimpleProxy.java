/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import edu.cmu.ri.airboat.buoytest.BuoyManager;
import edu.cmu.ri.airboat.irrigationtest.Observation;
import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.PoseListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpVehicleServer;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import robotutils.Pose3D;

/**
 * @todo Need a flag for autonomous or under human control
 * 
 * @author pscerri
 */
public class BoatSimpleProxy extends Thread {

    // @todo Needs to be configurable
    private double atWaypointTolerance = 30.0;
    private final String name;
    private final ArrayList<Marker> markers;
    private final ArrayList<Marker> ownMarkers = new ArrayList<Marker>();
    private final Material material;
    final AtomicBoolean isConnected = new AtomicBoolean(false);
    // Tasking stuff
    // private ArrayList<RoleAgent> roles = new ArrayList<RoleAgent>();
    private ProxyManager proxyManager = new ProxyManager();
    private Position currWaypoint = null;
    private Position currLoc = null;
    private UTMCoord currUtm = null;
    // private RoleAgent currRole = null;
    private int lngZone = -1;
    // Simulated
    private double fuelLevel = 1.0;
    private double fuelUsageRate = 1.0e-2;
    // ROS update
    private boolean _waypointsWereUpdated;
    PoseListener _stateListener;
    SensorListener _sensorListener;
    int _boatNo;
    UtmPose _pose;
    volatile boolean _isShutdown = false;
    // Latest image returned from this boat
    private BufferedImage latestImg = null;

    //
    // New Control variables
    //
    public enum StateEnum {

        IDLE, WAYPOINT, PATH, AREA
    };
    // Set this to false to turn off the false safe
    final boolean USE_SOFTWARE_FAIL_SAFE = true;
    UtmPose home = null;
    private StateEnum state = StateEnum.IDLE;
    final Queue<UtmPose> _waypoints = new LinkedList<UtmPose>();
    private UtmPose currentWaypoint = null;
    UdpVehicleServer _server;
    private Polygon currentArea = null;
    private Polyline currentPath = null;
    private Color color = null;
    private URI masterURI = null;
    Marker marker = null;
    Marker waypointMarker = null;
    // @todo Temporary centralized data structure, make it a listener
    public static DataDisplay dataDisplay = null;
    // @todo Temporary centralized data structure, make it a listener
    public static BuoyManager buoyManager = null;

    public BoatSimpleProxy(final String name, final ArrayList<Marker> markers, Color color, final int boatNo, InetSocketAddress addr) {

        this.masterURI = masterURI;
        this.name = name;
        this.markers = markers;
        this.material = new Material(color);
        this.color = color;

        //Initialize the boat by initalizing a proxy server for it
        // Connect to boat
        _boatNo = boatNo;

        if (addr == null) {
            System.out.println("$$$$$$$$$$$$$$$$$$$$$ addr falied");
        }

        _server = new UdpVehicleServer(addr);

        _stateListener = new PoseListener() {

            public void receivedPose(UtmPose upwcs) {
                _pose = upwcs.clone();

                if (home == null && USE_SOFTWARE_FAIL_SAFE) {
                    home = upwcs.clone();
                }

                // System.out.println("Pose: [" + _pose.pose.position.x + ", " + _pose.pose.position.y + "], zone = " + _pose.utm.zone);

                try {

                    int longZone = _pose.origin.zone;

                    // Convert hemisphere to arbitrary worldwind codes
                    String wwHemi = (_pose.origin.isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South";

                    // Fill in UTM data structure
                    // System.out.println("Converting from " + longZone + " " + wwHemi + " " + _pose.pose.position.x + " " + _pose.pose.position.y);
                    UTMCoord boatPos = UTMCoord.fromUTM(longZone, wwHemi, _pose.pose.getX(), _pose.pose.getY());

                    // UTMCoord boatPos = UTMCoord.fromLatLon(Angle.fromDegrees(14.22), Angle.fromDegrees(121.32));
                    // System.out.println("Boatpos: " + boatPos.getHemisphere() + " " + boatPos.getZone() + " " + boatPos.getLatitude() + " " + boatPos.getLongitude());

                    LatLon latlon = new LatLon(boatPos.getLatitude(), boatPos.getLongitude());

                    Position p = new Position(latlon, 0.0);

                    if (marker == null) {
                        marker = new BoatMarker(BoatSimpleProxy.this, p, new BasicMarkerAttributes(material, BasicMarkerShape.ORIENTED_SPHERE, 0.9));
                        markers.add(marker);
                    }
                    marker.setPosition(p);
                    marker.setHeading(Angle.fromRadians(Math.PI / 2.0 - _pose.pose.getRotation().toYaw()));

                    /** @todo FIX
                    UtmPose wpPose = _server.getWaypoint();
                    
                    if (wpPose != null && !(wpPose.utm.zone == 0 && wpPose.pose.position.x == 0.0)) {
                    
                    // System.out.println("wpPose is " + wpPose.utm.zone + " " + wpPose.pose.position.x);
                    
                    longZone = wpPose.utm.zone;
                    wwHemi = (wpPose.utm.isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South";
                    UTMCoord wpPos = UTMCoord.fromUTM(longZone, wwHemi, wpPose.pose.position.x, wpPose.pose.position.y);
                    
                    latlon = new LatLon(wpPos.getLatitude(), wpPos.getLongitude());
                    
                    Position pstn = new Position(latlon, 0.0);
                    
                    if (waypointMarker == null) {
                    waypointMarker = new BasicMarker(pstn, new BasicMarkerAttributes(material, BasicMarkerShape.CONE, 0.9));
                    markers.add(waypointMarker);
                    }
                    waypointMarker.setPosition(pstn);
                    
                    } else if (waypointMarker != null) {
                    markers.remove(waypointMarker);
                    waypointMarker = null;
                    }
                    
                     */
                } catch (Exception e) {
                    System.err.println("BoatSimpleProxy: Invalid pose received: " + e + " Pose: [" + _pose.pose.getX() + ", " + _pose.pose.getY() + "], zone = " + _pose.origin.zone);
                }



                // @todo Report locations commented out
                // reportLoc(_boatNo, UtmPoseToDouble(_pose.pose));
            }
        };

        _sensorListener = new SensorListener() {

            boolean first = true;

            public void receivedSensor(SensorData sd) {

                System.out.println("Received sensor");
                //Since the sensor Update is called just after state update
                //There shouldn't be too much error with regards to the
                //position of the sampling point

                if (first) {
                    // @todo Inelegant creation of the model, need to get feed names.
                    first = false;
                    DefaultComboBoxModel model = new DefaultComboBoxModel();
                    model.addElement("None");
                    for (int i = 0; i < sd.data.length; i++) {
                        model.addElement(i);
                    }
                    AutonomyPanel.dataSelectCombo.setModel(model);
                }

                // @todo Observation handling is a hack (centralized, assumes all have data display)
                try {

                    if (dataDisplay != null) {
                        for (int i = 0; i < sd.data.length; i++) {
                            Observation o = new Observation(
                                    "Sensor" + sd.type,
                                    sd.data[i],
                                    UtmPoseToDouble(_pose.pose),
                                    _pose.origin.zone, _pose.origin.isNorth);

                            System.out.println("Data " + i + " = " + sd.data[i]);

                            dataDisplay.newObservation(o, i);
                        }

                    } else {
                        // System.out.println("Nothing to do with observation");
                    }

                } catch (NullPointerException e) {
                    System.out.println("Problem in receivedSensor, null pointer " + sd + " " + _pose);
                }
            }

            // @todo Check if this is right
            double[] UtmPoseToDouble(Pose3D p) {
                double[] d = new double[3];
                d[0] = p.getX();
                d[1] = p.getY();
                d[2] = p.getZ();
                return d;
            }
        };

        System.out.println("New boat created, boat # " + _boatNo);

        //add Listeners
        _server.addPoseListener(_stateListener, null);

        // This is causing a null pointer exception
        // _server.addSensorListener(0, _sensorListener);
        // Cheating dummy data
        (new Thread() {

            Random rand = new Random();

            public void run() {
                while (true) {

                    SensorData sd = new SensorData();
                    // @todo Observation
                    // sd.type = (byte) 0;
                    sd.data = new double[4];
                    for (int i = 0; i < sd.data.length; i++) {
                        sd.data[i] = rand.nextDouble();
                    }

                    _sensorListener.receivedSensor(sd);

                    try {
                        sleep(1000L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();

    }

    private void startCamera() {

        try {
            System.out.println("SLEEPING BEFORE CAMERA START");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        System.out.println("DONE SLEEPING BEFORE CAMERA START");

        _server.addImageListener(new ImageListener() {

            public void receivedImage(byte[] ci) {
                // Take a picture, and put the resulting image into the panel
                try {
                    BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(ci));
                    // System.out.println("Got image ... ");
                    if (image != null) {
                        ImagePanel.addImage(image, _pose);

                        if (buoyManager != null) {
                            buoyManager.newImaage(image, _pose);
                        }

                        latestImg = image;
                    } else {
                        System.err.println("Failed to decode image.");
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to decode image: " + ex);
                }

            }
        }, null);

        _server.startCamera(0, 10.0, 640, 480, null);

        System.out.println("Image listener started");

    }

    void remove() {
        stopBoat();

        markers.remove(marker);
        markers.remove(waypointMarker);
    }

    public void stopBoat() {
        // @todo How to stop the boat

        _waypoints.clear();
        _server.stopWaypoints(null);

        clearRenderables();

        state = StateEnum.IDLE;
    }

    private void clearRenderables() {
        if (currentArea != null) {
            OperatorConsole.removeRenderable(currentArea);
        }
        if (currentPath != null) {
            OperatorConsole.removeRenderable(currentPath);
        }
    }

    @Override
    public void run() {

        startCamera();
    }

    public void setWaypoints(Polyline pLine) {
        clearRenderables();
        currentPath = pLine;
        pLine.setColor(color);
        setWaypoints(pLine.getPositions());
        state = StateEnum.PATH;
    }

    public StateEnum getMode() {
        return state;
    }

    public void setWaypoints(Iterable<Position> ps) {
        _waypoints.clear();

        for (Position position : ps) {
            UTMCoord utm = UTMCoord.fromLatLon(position.latitude, position.longitude);
            UtmPose pose = new UtmPose(new Pose3D(utm.getEasting(), utm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(utm.getZone(), utm.getHemisphere().contains("North")));
            _waypoints.add(pose);
        }

        // setWaypoint(_waypoints.poll());

        _server.setAutonomous(true, null);
        _server.startWaypoints(_waypoints.toArray(new UtmPose[_waypoints.size()]), null, new FunctionObserver() {

            public void completed(Object v) {
                /*
                if (state == StateEnum.AREA) {
                    System.out.println("Repeating perimeter");
                    setArea(currentArea);
                    return;
                } else {
                    System.out.println("Waypoints complete");
                }
                 * 
                 */
                System.out.println("Completed called");
            }

            public void failed(FunctionError fe) {
                // @todo Do something when start waypoints fails
                System.out.println("START WAYPOINTS FAILED");
            }
        });
    }

    public void setWaypoint(Position p) {

        ArrayList<Position> ps = new ArrayList<Position>();
        ps.add(p);
        setWaypoints(ps);
        
        /*
        clearRenderables();

        if (p == null) {
            System.out.println("Null Position waypoint provided to BoatSimpleProxy");
            return;
        }

        UTMCoord utm = UTMCoord.fromLatLon(p.latitude, p.longitude);
        UtmPose wputm = new UtmPose(new Pose3D(utm.getEasting(), utm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(utm.getZone(), utm.getHemisphere().contains("North")));

        System.out.println("Setting waypoint for " + this + " to " + wputm);        
        
        setWaypoint(wputm);

        state = StateEnum.WAYPOINT;
         * 
         */
    }

    public void setWaypoint(UtmPose wputm) {

        currentWaypoint = wputm;
        
        // ArrayList<UtmPose> 
        
        // @todo Register a waypoint listener to get the same status updates (and know at the end of the waypoints)
        _server.setAutonomous(true, null);
        _server.startWaypoints(new UtmPose[]{wputm}, null, new FunctionObserver() {

            public void completed(Object v) {
                if (state == StateEnum.AREA) {
                    System.out.println("Repeating perimeter");
                    setArea(currentArea);
                    return;
                } else {
                    System.out.println("Waypoints complete");
                }
            }

            public void failed(FunctionError fe) {
                // @todo Do something when start waypoints fails
                System.out.println("START WAYPOINTS FAILED");
            }
        });
       
    }

    public void setArea(Polygon poly) {

        if (currentArea != poly) {
            clearRenderables();
        }

        ShapeAttributes normalAttributes = new BasicShapeAttributes();
        normalAttributes.setInteriorMaterial(new Material(color));
        normalAttributes.setOutlineOpacity(0.5);
        normalAttributes.setInteriorOpacity(0.2);
        normalAttributes.setOutlineMaterial(new Material(color));
        normalAttributes.setOutlineWidth(2);
        normalAttributes.setDrawOutline(true);
        normalAttributes.setDrawInterior(true);
        normalAttributes.setEnableLighting(true);

        ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
        highlightAttributes.setOutlineMaterial(Material.WHITE);
        highlightAttributes.setOutlineOpacity(1);
        poly.setAttributes(normalAttributes);
        poly.setHighlightAttributes(highlightAttributes);

        currentArea = poly;

        ArrayList<Position> ps = new ArrayList<Position>();

        for (LatLon ll : poly.getOuterBoundary()) {
            Position pos = new Position(ll, 0.0);
            ps.add(pos);
        }

        setWaypoints(ps);

        state = StateEnum.AREA;
    }

    public static void initDataDisply(double[] ul, double[] br, Polygon pgon, final List list) {

        dataDisplay = new DataDisplay(ul, br);

        // @todo Ugly static connection between autonomy panel and boat simple proxy
        AutonomyPanel.dataSelectCombo.setEnabled(true);

        (new Thread() {

            public void run() {
                while (true) {

                    int index = 0;

                    // @todo Fix hack way of getting index
                    index = AutonomyPanel.dataSelectCombo.getSelectedIndex() - 1;

                    BufferedImage img = null;

                    if (index >= 0) {
                        img = dataDisplay.makeBufferedImage(index);
                    }

                    if (img != null) {
                        OperatorConsole.imageLayer.addImage("Data", img, list);
                    }
                    // System.out.println("Updated sensor data");

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    if (img != null) {
                        OperatorConsole.imageLayer.removeImage("Data");
                    }
                }


            }
        }).start();
    }

    public static void initBuoyDetection(ArrayList<LatLon> buoys, Polygon pgon) {
        buoyManager = new BuoyManager(buoys, pgon);

    }

    public static void showBuoyCheckFrame() {
        buoyManager.showProcessingFrame();
    }

    public Position getCurrWaypoint() {
        return currWaypoint;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public Color getColor() {
        return color;
    }

    public BufferedImage getLatestImg() {
        return latestImg;
    }

    private boolean at(Position p1, Position p2) {
        UTMCoord utm1 = UTMCoord.fromLatLon(p1.latitude, p1.longitude);
        UTMCoord utm2 = UTMCoord.fromLatLon(p2.latitude, p2.longitude);

        // @todo This only really works for short distances
        double dx = utm1.getEasting() - utm2.getEasting();
        double dy = utm1.getNorthing() - utm2.getNorthing();

        double dist = Math.sqrt(dx * dx + dy * dy);

        //System.out.println("Dist to waypoint now: " + dist);

        if (utm1.getHemisphere().equalsIgnoreCase(utm2.getHemisphere()) && utm1.getZone() == utm2.getZone()) {
            return dist < atWaypointTolerance;
        } else {
            return false;
        }

    }

    public UdpVehicleServer getVehicleServer() {
        return _server;
    }

    @Override
    public String toString() {
        return name + "@" + (masterURI == null ? "Unknown" : masterURI.toString());
    }
}
