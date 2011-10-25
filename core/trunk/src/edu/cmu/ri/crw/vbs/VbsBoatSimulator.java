package edu.cmu.ri.crw.vbs;

import java.util.logging.Logger;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.WaypointObserver;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.vbs.ImageServerLink.ImageEvent;
import edu.cmu.ri.crw.vbs.ImageServerLink.ImageEventListener;
import robotutils.Pose3D;

/**
 * Implements a simulated vehicle in a VBS2 server.
 *
 * @see VehicleServer
 *
 * @author pkv
 */
public class VbsBoatSimulator extends AbstractVehicleServer {

    private static final Logger logger = Logger.getLogger(VbsBoatSimulator.class.getName());
    public static final int DEFAULT_RPC_PORT = 5000;
    protected final Vbs2Unit _vbsServer;
    protected final ImageServerLink _imageServer;
    UtmPose _waypoint = null;
    UtmPose _offset = new UtmPose();
    private volatile boolean _isCapturing = false;
    private volatile boolean _isNavigating = false;

    public VbsBoatSimulator(String vbsServerName, double[] position) {

        // Spawn the new vehicle
        _vbsServer = new Vbs2Unit(Vbs2Constants.Object.DOUBLEEAGLE_ROV, position);
        _vbsServer.connect(vbsServerName);

        // Connect an imageserver to the same address
        _imageServer = new ImageServerLink(vbsServerName, 5003);
        _imageServer.connect();
        _imageServer.addImageEventListener(new ImageEventListener() {

            @Override
            public void receivedImage(ImageEvent evt) {
                sendImage(_imageServer.getDirectImage());
            }
        });

        // Load up the map origin immediately after spawning
        Vbs2Unit.Origin origin = _vbsServer.origin();
        _offset.pose = new Pose3D(origin.easting, origin.northing, 0.0, 0.0, 0.0, 0.0);
        _offset.origin = new Utm(origin.zone, (origin.hemisphere == 'N' || origin.hemisphere == 'n'));

        // Add initial waypoint to stay in same spot
        _vbsServer.waypoints().add(_vbsServer.position());
    }

    @Override
    public byte[] captureImage(int width, int height) {
        logger.info("Took image @ (" + width + " x " + height + ")");
        _imageServer.takePicture();
        return _imageServer.getDirectImage();
    }

    @Override
    public int getNumSensors() {
        return 0;
    }

    @Override
    public SensorType getSensorType(int channel) {
        return null;
    }

    @Override
    public void setSensorType(int channel, SensorType type) {
        // Do nothing
    }

    @Override
    public UtmPose getState() {
        UtmPose poseMsg = new UtmPose();
        double[] pos = _vbsServer.position();
        double[] rot = _vbsServer.rotation();

        poseMsg.pose = new Pose3D(pos[0], pos[1], pos[2], rot[0], rot[1], rot[2]);
        return poseMsg;
    }

    @Override
    public void setState(UtmPose state) {
        logger.info("Ignored setState: " + state);
    }

    @Override
    public Twist getVelocity() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void setVelocity(Twist velocity) {
        logger.info("Ignored setVelocity: " + velocity);
    }

    @Override
    public void startCamera(final long numFrames, final double interval, final int width,
            final int height, final ImagingObserver obs) {

        final long captureInterval = (long) (interval * 1000.0);
        _isCapturing = true;

        new Thread(new Runnable() {

            @Override
            public void run() {
                int iFrame = 0;

                while (_isCapturing && (iFrame < numFrames)) {

                    // Every so often, take a picture 
                    _imageServer.takePicture();
                    iFrame++;

                    // Alert observer that status might have changed
                    if (obs != null) {
                        obs.imagingUpdate(CameraState.CAPTURING);
                    }

                    // Wait for a while
                    try {
                        Thread.sleep(captureInterval);
                    } catch (InterruptedException ex) {
                        obs.imagingUpdate(CameraState.CANCELLED);
                        _isCapturing = false;
                        return;
                    }
                }

                if (_isCapturing) {
                    _isCapturing = false;
                    obs.imagingUpdate(CameraState.DONE);
                } else {
                    obs.imagingUpdate(CameraState.CANCELLED);
                }
            }
        }).start();

    }

    @Override
    public void stopCamera() {
        _isCapturing = false;
    }

    @Override
    public void startWaypoint(final UtmPose waypoint, final String controller, final WaypointObserver obs) {

        // Tell VBS boat to go to specified waypoint
        double[] dest = new double[3];
        dest[0] = waypoint.pose.getX();
        dest[1] = waypoint.pose.getY();
        dest[2] = waypoint.pose.getZ();

        // TODO: handle different UTM zones
        _vbsServer.waypoints().set(0, dest);
        _vbsServer.gotoWaypoint(0);

        // Spawn monitoring thread
        new Thread(new Runnable() {

            @Override
            public void run() {

                // Loop until stopped or waypoint is reached
                while (_isNavigating && getWaypointStatus() != WaypointState.DONE) {

                    // Alert that there might be a status change
                    if (obs != null) {
                        obs.waypointUpdate(WaypointState.GOING);
                    }

                    // Wait for a while
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        obs.waypointUpdate(WaypointState.CANCELLED);
                        _isNavigating = false;
                        return;
                    }
                }

                if (_isNavigating) {
                    obs.waypointUpdate(WaypointState.DONE);
                    _isNavigating = false;
                } else {
                    obs.waypointUpdate(WaypointState.CANCELLED);
                }

            }
        }).start();
    }

    @Override
    public void stopWaypoint() {

        // Tell VBS2 to stop by setting waypoint to current position
        _vbsServer.waypoints().set(0, _vbsServer.position());
        _vbsServer.gotoWaypoint(0);

        _isNavigating = false;
    }

    @Override
    public WaypointState getWaypointStatus() {

        // Copy position of current waypoint
        double[] wpt = _vbsServer.waypoints().get(0);
        double[] pos = _vbsServer.position();

        // Measure distance to waypoint 
        double dx = wpt[0] - pos[0];
        double dy = wpt[1] - pos[1];
        double dz = wpt[2] - pos[2];

        double d = dx * dx + dy * dy + dz * dz;

        // Use distance threshold to determine if waypoint is complete
        if (d > 0.5) {
            return WaypointState.GOING;
        } else {
            return WaypointState.DONE;
        }
    }

    @Override
    public UtmPose getWaypoint() {

        // Get current waypoint from list
        double[] dest = _vbsServer.waypoints().get(0);

        // Pack as data structure
        UtmPose waypoint = new UtmPose();
        waypoint.origin = _offset.origin.clone();
        waypoint.pose = new Pose3D(dest[0], dest[1], dest[2], 0.0, 0.0, 0.0);

        return waypoint;
    }

    @Override
    public CameraState getCameraStatus() {
        if (this._isCapturing) {
            return CameraState.CAPTURING;
        } else {
            return CameraState.OFF;
        }
    }

    @Override
    public boolean isAutonomous() {
        return true;
    }

    @Override
    public void setAutonomous(boolean auto) {
        // This implementation does not support non-autonomy!
    }
}