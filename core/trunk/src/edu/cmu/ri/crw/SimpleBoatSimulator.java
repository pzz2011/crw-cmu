package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import robotutils.Pose3D;
import robotutils.Quaternion;

/**
 * A simple simulation of an unmanned boat.
 * 
 * The vehicle is fixed on the ground (Z = 0.0), and can only turn along the
 * Z-axis and move along the X-axis (a unicycle motion model). Imagery and
 * sensor are simulated using simple artificial generator functions that produce
 * recognizable basic patterns.
 * 
 * Implementation of RosVehicleActionServer and RosVehicleActionClient
 * 
 * @author pkv
 * @author kss
 * 
 */
public class SimpleBoatSimulator extends AbstractVehicleServer {

    private static final Logger logger = Logger.getLogger(SimpleBoatSimulator.class.getName());
    public static final int UPDATE_INTERVAL_MS = 100;
    
    public final SensorType[] _sensorTypes = new SensorType[3];
    public UtmPose _pose = new UtmPose();
    public Twist _velocity = new Twist();
    public UtmPose[] _waypoints = new UtmPose[0];
    
    protected final Object _captureLock = new Object();
    protected TimerTask _captureTask = null;
    
    protected final Object _navigationLock = new Object();
    protected TimerTask _navigationTask = null;
    
    protected final AtomicBoolean _isAutonomous = new AtomicBoolean(true);
    protected final Timer _timer = new Timer();
    
    /**
     * Current navigation controller
     */
    SimpleBoatController _controller = SimpleBoatController.POINT_AND_SHOOT;

    public SimpleBoatSimulator() {
        final double dt = UPDATE_INTERVAL_MS / 1000.0;

        new Thread(new Runnable() {

            @Override
            public void run() {

                while (true) {
                    try {
                        Thread.sleep(UPDATE_INTERVAL_MS);
                    } catch (InterruptedException e) {
                    }

                    // Send out pose updates
                    UtmPose pose = _pose.clone();
                    sendState(pose);

                    // Send out velocity updates
                    Twist velocity = _velocity.clone();
                    sendVelocity(velocity);

                    // Move in an arc with given velocity over time interval
                    double yaw = pose.pose.getRotation().toYaw();
                    double x = _pose.pose.getX() + _velocity.dx() * Math.cos(yaw) * dt;
                    double y = _pose.pose.getY() + _velocity.dx() * Math.sin(yaw) * dt;
                    Quaternion q = Quaternion.fromEulerAngles(0, 0, yaw + _velocity.drz() * dt);
                    _pose.pose = new Pose3D(x, y, _pose.pose.getZ(), q);

                    // Generate spurious sensor data
                    SensorData reading = new SensorData();
                    reading.data = new double[3];
                    reading.type = SensorType.TE;

                    Random random = new Random();
                    reading.data[0] = (_pose.pose.getX()) + 10 * random.nextGaussian();
                    reading.data[1] = (_pose.pose.getY());
                    reading.data[2] = (_pose.pose.getZ());

                    sendSensor(0, reading);
                }
            }
        }).start();
    }

    @Override
    public byte[] captureImage(int width, int height) {

        // Create an image and fill it with a random color
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setPaint(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

        return toCompressedImage(image);
    }

    @Override
    public SensorType getSensorType(int channel) {
        return _sensorTypes[channel];
    }

    @Override
    public UtmPose[] getWaypoints() {
        synchronized (_navigationLock) {
            return _waypoints;
        }
    }

    @Override
    public void setSensorType(int channel, SensorType type) {
        _sensorTypes[channel] = type;
    }

    @Override
    public void setState(UtmPose state) {
        _pose = state.clone();
    }

    @Override
    public void startWaypoints(final UtmPose[] waypoints, final String controller) {

        logger.log(Level.INFO, "Starting waypoints: {0}", Arrays.toString(waypoints));
        
        synchronized (_navigationLock) {
            // Change waypoints to new set of waypoints
            _waypoints = Arrays.copyOf(waypoints, waypoints.length);

            // Cancel any previous navigation tasks
            _navigationTask.cancel();
            
            // Create a waypoint navigation task
            _navigationTask = new TimerTask() {
                final double dt = (double) UPDATE_INTERVAL_MS / 1000.0;

                @Override
                public void run() {
                    synchronized (_navigationLock) {
                        if (!_isAutonomous.get()) {
                            // If we are not autonomous, do nothing
                            sendWaypointUpdate(WaypointState.PAUSED);
                            return;
                        } else if (_waypoints.length == 0) {
                            // If we are finished with waypoints, stop in place
                            sendWaypointUpdate(WaypointState.OFF);
                            setVelocity(new Twist());
                            this.cancel();
                        } else {
                            // If we are still executing waypoints, use a 
                            // controller to figure out how to get to waypoint
                            // TODO: measure dt directly instead of approximating
                            _controller.controller.update(SimpleBoatSimulator.this, dt);
                        }
                    }
                }
            };
            
            // Schedule this task for execution
            _timer.scheduleAtFixedRate(_navigationTask, 0, UPDATE_INTERVAL_MS);
        }
    }

    @Override
    public void stopWaypoints() {

        // Stop the thread that is doing the "navigation" by terminating its
        // navigation flag and then removing the reference to the old flag.
        System.out.println("STOP!!!!");
        synchronized (_navigationLock) {
            _navigationTask.cancel();
            _navigationTask = null;
            _waypoints = new UtmPose[0];
        }
    }

    @Override
    public WaypointState getWaypointStatus() {
        synchronized (_navigationLock) {
            if (_waypoints.length > 0) {
                return WaypointState.GOING;
            } else {
                return WaypointState.OFF;
            }
        }
    }

    @Override
    public void startCamera(final int numFrames, final double interval, final int width, final int height) {

        logger.log(Level.INFO, "Starting capture: {0} ({1}x{2}) frames @ {3}s ", new Object[]{numFrames, width, height, interval});
        
        synchronized (_captureLock) {
            // Cancel any previous capture tasks
            _captureTask.cancel();
            
            // Create a camera capture task
            _captureTask = new TimerTask() {
                int nFrames = numFrames;

                @Override
                public void run() {
                    // Take a new image and send it out
                    sendImage(captureImage(width, height));
                    
                    // Decrement the number of frames left
                    if (nFrames > 0)
                        nFrames--;
                    
                    // If we hit exactly 0, that means we just finished
                    if (nFrames == 0) {
                        sendCameraUpdate(CameraState.DONE);
                        this.cancel();
                    } else {
                        sendCameraUpdate(CameraState.CAPTURING);
                    }
                }
            };
            
            // Schedule this task for execution
            _timer.scheduleAtFixedRate(_captureTask, 0, (long)(interval * 1000.0));
        }
    }

    @Override
    public void stopCamera() {
        // Stop the thread that sends out images by terminating its
        // navigation flag and then removing the reference to the old flag.
        synchronized (_captureLock) {
            _captureTask.cancel();
            _captureTask = null;
        }
    }

    @Override
    public CameraState getCameraStatus() {
        synchronized (_captureLock) {
            if (_captureTask != null) {
                return CameraState.CAPTURING;
            } else {
                return CameraState.OFF;
            }
        }
    }

    @Override
    public UtmPose getState() {
        return _pose.clone();
    }

    @Override
    public int getNumSensors() {
        return _sensorTypes.length;
    }

    @Override
    public void setVelocity(Twist velocity) {
        _velocity = velocity.clone();
    }

    @Override
    public Twist getVelocity() {
        return _velocity.clone();
    }

    /**
     * Takes an angle and shifts it to be in the range -Pi to Pi.
     * 
     * @param angle
     *            an angle in radians
     * @return the same angle as given, normalized to the range -Pi to Pi.
     */
    public static double normalizeAngle(double angle) {
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    @Override
    public boolean isAutonomous() {
        return _isAutonomous.get();
    }

    @Override
    public void setAutonomous(boolean auto) {
        _isAutonomous.set(auto);
        _velocity = new Twist(); // Reset velocity when changing modes
    }

    @Override
    public boolean isConnected() {
        // The simulated vehicle will always be connected
        return true;
    }
}
