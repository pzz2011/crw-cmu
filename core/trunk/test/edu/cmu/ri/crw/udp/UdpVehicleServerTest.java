/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CameraListener;
import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.PoseListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VelocityListener;
import edu.cmu.ri.crw.WaypointListener;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpServer.Request;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UdpVehicleServerTest {
    
    UdpVehicleService service;
    SimpleBoatSimulator sbs;
    
    public UdpVehicleServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        sbs = new SimpleBoatSimulator();
        service = new UdpVehicleService(sbs);
    }
    
    @After
    public void tearDown() {
        service.shutdown();
        sbs.shutdown();
    }

    /**
     * Test of shutdown method, of class UdpVehicleServer.
     */
    /*@Test
    public void testShutdown() {
        System.out.println("shutdown");
        UdpVehicleServer instance = new UdpVehicleServer(service.getSocketAddress());
        instance.shutdown();
        
        // Make sure the UDP socket was closed
        assertTrue(instance._udpServer._socket.isClosed());
        
        // Make sure the timer processes are shut down
        try {
            instance._timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // Do nothing
                }
            }, 0);
            fail("Timer was not shut down.");
        } catch (IllegalStateException e) {
            
        }
    }*/

    /**
     * Test of setVehicleService method, of class UdpVehicleServer.
     */
    @Test
    public void testSetVehicleService() {
        System.out.println("setVehicleService");
        
        UdpVehicleServer instance = new UdpVehicleServer();
        
        VehicleServer server = AsyncVehicleServer.Util.toSync(instance);
        assertEquals("Server reports connected to null service", 
                false, server.isConnected());
        
        instance.setVehicleService(service.getSocketAddress());
        assertEquals("Server resports not connected to service",
                true, server.isConnected());
        
        instance.shutdown();
    }

    /**
     * Test of getVehicleService method, of class UdpVehicleServer.
     */
    @Test
    public void testGetVehicleService() {
        System.out.println("getVehicleService");
        UdpVehicleServer instance = new UdpVehicleServer();
        
        instance.setVehicleService(service.getSocketAddress());
        assertEquals("SocketAddress was not set correctly", 
                service.getSocketAddress(), instance.getVehicleService());
        
        instance.shutdown();
    }

    /**
     * Test of addStateListener method, of class UdpVehicleServer.
     */
    @Test
    public void testAddStateListener() {
        System.out.println("addStateListener");
        final CountDownLatch latch = new CountDownLatch(1);
        
        // Register a new pose listener on this server
        UdpVehicleServer instance = new UdpVehicleServer(service.getSocketAddress());
        VehicleServer server = AsyncVehicleServer.Util.toSync(instance);
        server.addStateListener(new PoseListener() {
            @Override
            public void receivedState(UtmPose state) {
                latch.countDown();
            }
        });
        
        // If we haven't received a pose in a full second, something is wrong
        try {
            assertTrue("Did not receive pose update.", latch.await(1, TimeUnit.SECONDS));
        } catch(InterruptedException e) {
            fail("Did not receive pose update.");
        }
        
        instance.shutdown();
    }

    /**
     * Test of removeStateListener method, of class UdpVehicleServer.
     */
    @Test
    public void testRemoveStateListener() {
        System.out.println("removeStateListener");
        PoseListener l = null;
        //FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.removeStateListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setState method, of class UdpVehicleServer.
     */
    @Test
    public void testSetState() {
        System.out.println("setState");
        UtmPose state = null;
        //FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.setState(state, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getState method, of class UdpVehicleServer.
     */
    @Test
    public void testGetState() {
        System.out.println("getState");
        //FunctionObserver<UtmPose> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getState(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addImageListener method, of class UdpVehicleServer.
     */
    @Test
    public void testAddImageListener() {
        System.out.println("addImageListener");
        ImageListener l = null;
        //FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.addImageListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeImageListener method, of class UdpVehicleServer.
     */
    @Test
    public void testRemoveImageListener() {
        System.out.println("removeImageListener");
        ImageListener l = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.removeImageListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of captureImage method, of class UdpVehicleServer.
     */
    @Test
    public void testCaptureImage() {
        System.out.println("captureImage");
        int width = 0;
        int height = 0;
        FunctionObserver<byte[]> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.captureImage(width, height, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addCameraListener method, of class UdpVehicleServer.
     */
    @Test
    public void testAddCameraListener() {
        System.out.println("addCameraListener");
        CameraListener l = null;
        //FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.addCameraListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeCameraListener method, of class UdpVehicleServer.
     */
    @Test
    public void testRemoveCameraListener() {
        System.out.println("removeCameraListener");
        CameraListener l = null;
        //FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.removeCameraListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of startCamera method, of class UdpVehicleServer.
     */
    @Test
    public void testStartCamera() {
        System.out.println("startCamera");
        int numFrames = 0;
        double interval = 0.0;
        int width = 0;
        int height = 0;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.startCamera(numFrames, interval, width, height, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopCamera method, of class UdpVehicleServer.
     */
    @Test
    public void testStopCamera() {
        System.out.println("stopCamera");
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.stopCamera(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCameraStatus method, of class UdpVehicleServer.
     */
    @Test
    public void testGetCameraStatus() {
        System.out.println("getCameraStatus");
        FunctionObserver<CameraState> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getCameraStatus(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addSensorListener method, of class UdpVehicleServer.
     */
    @Test
    public void testAddSensorListener() {
        System.out.println("addSensorListener");
        int channel = 0;
        SensorListener l = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.addSensorListener(channel, l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeSensorListener method, of class UdpVehicleServer.
     */
    @Test
    public void testRemoveSensorListener() {
        System.out.println("removeSensorListener");
        int channel = 0;
        SensorListener l = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.removeSensorListener(channel, l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setSensorType method, of class UdpVehicleServer.
     */
    @Test
    public void testSetSensorType() {
        System.out.println("setSensorType");
        int channel = 0;
        SensorType type = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.setSensorType(channel, type, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSensorType method, of class UdpVehicleServer.
     */
    @Test
    public void testGetSensorType() {
        System.out.println("getSensorType");
        int channel = 0;
        FunctionObserver<SensorType> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getSensorType(channel, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumSensors method, of class UdpVehicleServer.
     */
    @Test
    public void testGetNumSensors() {
        System.out.println("getNumSensors");
        FunctionObserver<Integer> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getNumSensors(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addVelocityListener method, of class UdpVehicleServer.
     */
    @Test
    public void testAddVelocityListener() {
        System.out.println("addVelocityListener");
        VelocityListener l = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.addVelocityListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeVelocityListener method, of class UdpVehicleServer.
     */
    @Test
    public void testRemoveVelocityListener() {
        System.out.println("removeVelocityListener");
        VelocityListener l = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.removeVelocityListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setVelocity method, of class UdpVehicleServer.
     */
    @Test
    public void testSetVelocity() {
        System.out.println("setVelocity");
        Twist velocity = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.setVelocity(velocity, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVelocity method, of class UdpVehicleServer.
     */
    @Test
    public void testGetVelocity() {
        System.out.println("getVelocity");
        FunctionObserver<Twist> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getVelocity(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addWaypointListener method, of class UdpVehicleServer.
     */
    @Test
    public void testAddWaypointListener() {
        System.out.println("addWaypointListener");
        WaypointListener l = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.addWaypointListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeWaypointListener method, of class UdpVehicleServer.
     */
    @Test
    public void testRemoveWaypointListener() {
        System.out.println("removeWaypointListener");
        WaypointListener l = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.removeWaypointListener(l, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of startWaypoints method, of class UdpVehicleServer.
     */
    @Test
    public void testStartWaypoints() {
        System.out.println("startWaypoints");
        UtmPose[] waypoints = null;
        String controller = "";
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.startWaypoints(waypoints, controller, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stopWaypoints method, of class UdpVehicleServer.
     */
    @Test
    public void testStopWaypoints() {
        System.out.println("stopWaypoints");
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.stopWaypoints(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWaypoints method, of class UdpVehicleServer.
     */
    @Test
    public void testGetWaypoints() {
        System.out.println("getWaypoints");
        FunctionObserver<UtmPose[]> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getWaypoints(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWaypointStatus method, of class UdpVehicleServer.
     */
    @Test
    public void testGetWaypointStatus() {
        System.out.println("getWaypointStatus");
        FunctionObserver<WaypointState> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getWaypointStatus(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isConnected method, of class UdpVehicleServer.
     */
    @Test
    public void testIsConnected() {
        System.out.println("isConnected");
        FunctionObserver<Boolean> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.isConnected(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isAutonomous method, of class UdpVehicleServer.
     */
    @Test
    public void testIsAutonomous() {
        System.out.println("isAutonomous");
        FunctionObserver<Boolean> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.isAutonomous(obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAutonomous method, of class UdpVehicleServer.
     */
    @Test
    public void testSetAutonomous() {
        System.out.println("setAutonomous");
        boolean auto = false;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.setAutonomous(auto, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGains method, of class UdpVehicleServer.
     */
    @Test
    public void testSetGains() {
        System.out.println("setGains");
        int axis = 0;
        double[] gains = null;
        FunctionObserver<Void> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.setGains(axis, gains, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGains method, of class UdpVehicleServer.
     */
    @Test
    public void testGetGains() {
        System.out.println("getGains");
        int axis = 0;
        FunctionObserver<double[]> obs = null;
        //UdpVehicleServer instance = new UdpVehicleServer();
        //instance.getGains(axis, obs);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
