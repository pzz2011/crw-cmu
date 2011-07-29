/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleImageListener;
import edu.cmu.ri.crw.VehicleSensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VehicleStateListener;
import edu.cmu.ri.crw.VehicleVelocityListener;
import edu.cmu.ri.crw.WaypointObserver;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;

/**
 *
 * @author pscerri
 */
public class InterfaceTester implements IrrigationTestInterface {

    public static double speed = 1.0;
    public static long sleepTime = 1000;
    private Random rand = new Random();
    Hashtable<Integer, VehicleServer> boats = new Hashtable<Integer, VehicleServer>();
    ArrayList<IrrigationTestInterfaceListener> listeners = new ArrayList<IrrigationTestInterfaceListener>();
    double[] ul = null;
    double[] lr = null;
    protected List<VehicleSensorListener> _sensorListeners = new ArrayList<VehicleSensorListener>();
    protected List<VehicleImageListener> _imageListeners = new ArrayList<VehicleImageListener>();
    protected List<VehicleStateListener> _stateListeners = new ArrayList<VehicleStateListener>();
    protected final List<UtmPose> boatPoses = new ArrayList<UtmPose>();
    protected final List<Observation> observers = new ArrayList<Observation>();

    public void setWaypoints(final int boatNo, double[][] poses) {

        if (ul == null || lr == null) {
            System.out.println("SET EXTENT FIRST!");
            return;
        }
        //TODO This current implementation should be run as a separate thread
        // for each boatNumber, currently is a blocking function call.
        VehicleServer boat = boats.get(boatNo);
        if (boat == null) {               //i.e. if no Vehicle Server exists
            //TODO: Create a separate boat initializer contatining this stuff for each boat
            VehicleStateListener state = new VehicleStateListener() {

                public void receivedState(UtmPoseWithCovarianceStamped upwcs) {
                    boatPoses.get(boatNo).pose = upwcs.pose.pose.pose.clone();
                    boatPoses.get(boatNo).utm = upwcs.utm.clone();

                }
            };
            VehicleSensorListener sensor = new VehicleSensorListener() {

                public void receivedSensor(SensorData sd) {
                    //TODO Perform Sensor value assignment correctly

                    //Since the sensor Update is called just after state update
                    observers.get(boatNo).value = sd.data[0];
                    observers.get(boatNo).variable = "Sensor" + sd.type;
                    observers.get(boatNo).waypoint = UtmPoseToDouble(boatPoses.get(boatNo).pose);
                    observers.get(boatNo).waypointHemisphereNorth = boatPoses.get(boatNo).utm.isNorth;
                    observers.get(boatNo).waypointZone = boatPoses.get(boatNo).utm.zone;
                }
            };

            boat = new SimpleBoatSimulator();//Create a Sim server
            boats.put(boatNo, boat);

            System.out.println("New boat created, boat # " + boatNo);
        } else //Cancel all current waypoints
        {
            boat.stopWaypoint();
        }
        //In a loop, get the boat to start moving along given waypoints
        for (int i = 0;
                i < poses.length;
                i++) {
            //Start the ith Waypoint
            boat.startWaypoint(DoubleToUtmPose(poses[i]), new WaypointObserver() {

                public void waypointUpdate(WaypointState ws) {
                    System.out.print("STATUS::");
                    switch (ws) {
                        case DONE:
                            System.out.println("Performed waypoint");
                        case GOING:
                            System.out.println("Goint to Waypoint");
                        case CANCELLED:
                            System.out.println("Cancelled");
                        default:
                            System.out.println("ERROR!!!!");
                    }
                }
            });

            //Ideally should be handled better
            boolean exit = false;
            do {
                switch (boat.getWaypointStatus()) {
                    case DONE:
                        exit = true;
                        System.out.println("Waypoint " + (i + 1) + " achieved!");
                        break;
                    case CANCELLED:
                        System.out.println("Waypoint " + (i + 1) + " cancelled!");
                        exit = true;
                        break;
                    default:
                        //TODO Inform about current status
                        continue;

                }
            } while (!exit);
            //TODO If I get a cancel, should I terminate this method?
        }
        //Reaching here normally implies that all Waypoints have been achieved

        System.out.println("All Waypoints Done!");

        reportDone(boatNo);
    }

    public void addListener(IrrigationTestInterfaceListener l) {
        listeners.add(l);




    }

    private void reportLoc(int no, double[] pose) {
        for (IrrigationTestInterfaceListener l : listeners) {
            l.newBoatPosition(no, pose);




        }
    }

    private void reportObs(Observation o) {
        for (IrrigationTestInterfaceListener l : listeners) {
            l.newObservation(o);

        }
    }

    private void reportDone(int no) {
        for (IrrigationTestInterfaceListener l : listeners) {
            l.boatDone(no);

        }
    }

    public void setExtent(double[] ul, double[] lr) {
        this.ul = ul;
        this.lr = lr;
    }

    public double randLon() {
        return (rand.nextDouble() * (lr[0] - ul[0])) + ul[0];
    }

    public double randLat() {
        return (rand.nextDouble() * (lr[1] - ul[1])) + ul[1];
    }
    boolean created = false;

    /*private class SimBoat extends Thread {

    private final int no;
    private double lon;
    private double lat;
    final double baseline = 100.0;
    double[][] poses = null;
    int waypointIndex = 0;
    final VehicleServer[] servers;

    public SimBoat(int no, double lon, double lat) {
    this.no = no;
    this.lon = lon;
    this.lat = lat;
    servers = new SimpleBoatSimulator[no];
    start();
    }

    public void setWaypoints(double[][] poses) {
    System.out.println("New waypoints: " + poses.length);
    this.poses = poses;
    for(int i=0;i<poses.length;i++)
    {
    servers[i].startWaypoint(DoubleToUtmPose(poses[i]), null);
    }
    waypointIndex = 0;
    doneReported = false;
    }

    // No need to report that the boat starts with nothing
    volatile boolean doneReported = true;

    @Override
    public void run() {

    while (true) {
    if (poses != null && waypointIndex < poses.length) {
    double pose[] = poses[waypointIndex];
    double dx = pose[0] - lon;
    double dy = pose[1] - lat;
    double dist = Math.sqrt(dx * dx + dy * dy);
    if (dist > speed) {
    double d = dist / speed;
    dx /= d;
    dy /= d;
    } else {
    waypointIndex++;
    }

    lat += dy;
    lon += dx;

    double[] p = new double[2];
    p[0] = lon;
    p[1] = lat;

    reportLoc(no, p);

    double v = baseline;
    for (Gaussian2D g : model) {
    v += g.getValueAt(p[0], p[1]);
    }

    // System.out.println("Obs value = " + v);
    Observation o = new Observation("Blah", v, p, 0, true);
    reportObs(o);

    } else {
    System.out.println("Boat " + no + " idle, " + poses + " " + waypointIndex + " " + doneReported);
    if (!doneReported) {
    doneReported = true;
    reportDone(no);
    }
    }

    try {
    sleep(sleepTime);
    } catch (Exception e) {
    }

    }

    }


    }

    public class Gaussian2D {

    double x, y, v, decay;

    public Gaussian2D(double x, double y, double v, double decay) {
    this.x = x;
    this.y = y;
    this.v = v;
    this.decay = decay;
    }

    public double getValueAt(double x2, double y2) {
    double dx = x2 - x;
    double dy = y2 - y;

    double dist = Math.sqrt(dx * dx + dy * dy);

    double ret = (0.1 * rand.nextGaussian()) + 1.0;

    ret *= v * Math.exp((dist / Math.floor(dist)) * decay);

    // System.out.println("Returning " + ret + " for v = " + v + ", decay = " + decay + " and dist = " + dist);

    return ret;

    }

    public String toString() {
    return "Gaussian @ " + x + "," + y + " v= " + v + " decay = " + decay;
    }
    }*/
    public static UtmPose DoubleToUtmPose(double[] pose) {
        if (pose.length != 7) {
            return null;




        }
        UtmPose _pose = new UtmPose();
        _pose.pose.position.x = pose[0];
        _pose.pose.position.y = pose[1];
        _pose.pose.position.z = pose[2];

        _pose.pose.orientation.w = pose[3];
        _pose.pose.orientation.x = pose[4];
        _pose.pose.orientation.y = pose[5];
        _pose.pose.orientation.z = pose[6];





        return _pose;





    }

    public static double[] UtmPoseToDouble(Pose pose) {

        double[] _pose = new double[7];
        _pose[0] = pose.position.x;
        _pose[1] = pose.position.y;
        _pose[2] = pose.position.z;

        _pose[3] = pose.orientation.w;
        _pose[4] = pose.orientation.x;
        _pose[5] = pose.orientation.y;
        _pose[6] = pose.orientation.z;





        return _pose;



    }
}
