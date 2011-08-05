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
import edu.cmu.ri.crw.ros.RosVehicleProxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    protected List<VehicleSensorListener> sensorListeners = new ArrayList<VehicleSensorListener>();
    protected List<VehicleStateListener> stateListeners = new ArrayList<VehicleStateListener>();
    protected final List<UtmPose> boatPoses = new ArrayList<UtmPose>();
    protected final List<Observation> observers = new ArrayList<Observation>();
    private final String MASTER_URI = "http://localhost:11311";

    public void setWaypoints(final int boatNo, double[][] poses) {

        if (ul == null || lr == null) {
            System.out.println("SET EXTENT FIRST!");
            return;
        }
        //TODO This current implementation should be run as a separate thread
        // for each boatNumber, currently is a blocking function call.
        VehicleServer boat = boats.get(boatNo);
        if (boat == null) {               //i.e. if no Vehicle Server exists
            try {
                //i.e. if no Vehicle Server exists, intialize one
                intializeBoat(boatNo);
                boat = new RosVehicleProxy(new URI(MASTER_URI), "vehicle" + boatNo);

                //add Listeners
                boat.addStateListener(stateListeners.get(boatNo));
                boat.addSensorListener(boatNo, sensorListeners.get(boatNo));

                //Add this to the list
                boats.put(boatNo, boat);
            } catch (URISyntaxException ex) {
                Logger.getLogger(InterfaceTester.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    private void intializeBoat(final int boatNo) {
        //Initialize the boat by initalizing a proxy server for it
        VehicleStateListener state = stateListeners.get(boatNo);
        state = new VehicleStateListener() {

            public void receivedState(UtmPoseWithCovarianceStamped upwcs) {
                UtmPose pose = new UtmPose();
                pose.pose = upwcs.pose.pose.pose.clone();
                pose.utm = upwcs.utm.clone();
                boatPoses.set(boatNo, pose);

                reportLoc(boatNo, UtmPoseToDouble(boatPoses.get(boatNo).pose));
            }
        };

        VehicleSensorListener sensor = sensorListeners.get(boatNo);
        sensor = new VehicleSensorListener() {

            public void receivedSensor(SensorData sd) {
                //TODO Perform Sensor value assignment correctly

                //Since the sensor Update is called just after state update
                //There shouldn't be too much error with regards to the
                //position of the sampling point
                Observation o = new Observation(
                        "Sensor" + sd.type,
                        sd.data[0],
                        UtmPoseToDouble(boatPoses.get(boatNo).pose),
                        boatPoses.get(boatNo).utm.zone,
                        boatPoses.get(boatNo).utm.isNorth);

                observers.set(boatNo, o);
                reportObs(observers.get(boatNo));
            }
        };

        System.out.println("New boat created, boat # " + boatNo);

    }
}
