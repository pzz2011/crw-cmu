package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
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

	public static final int UPDATE_INTERVAL_MS = 100;

	public final SensorType[] _sensorTypes = new SensorType[3];
	public UtmPose _pose = new UtmPose();
	public Twist _velocity = new Twist();
	public UtmPose _waypoint = null;

	protected final Object _captureLock = new Object();
	protected AtomicBoolean _isCapturing = new AtomicBoolean(false);

	protected final Object _navigationLock = new Object();
	protected AtomicBoolean _isNavigating = null;

	protected final AtomicBoolean _isAutonomous = new AtomicBoolean(true);
	
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
					reading.data[0] = (_pose.pose.getX()) + 10*random.nextGaussian();
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
		graphics.setPaint(new Color((float) Math.random(), (float) Math
				.random(), (float) Math.random()));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

		return toCompressedImage(image);
	}

	@Override
	public SensorType getSensorType(int channel) {
		return _sensorTypes[channel];
	}

	@Override
	public UtmPose getWaypoints() {
		synchronized (_navigationLock) {
			return _waypoint;
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
	public void startWaypoints(final UtmPose waypoint, final String controller, final WaypointListener obs) {

		final double dt = (double)UPDATE_INTERVAL_MS / 1000.0;
		
		// Keep a reference to the navigation flag for THIS waypoint
		final AtomicBoolean isNavigating = new AtomicBoolean(true);
		System.out.println("\nStart Waypoint to " + waypoint);
		// Set this to be the current navigation flag
		synchronized (_navigationLock) {
			if (_isNavigating != null)
				_isNavigating.set(false);
			_isNavigating = isNavigating;
			_waypoint = waypoint.clone();
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (isNavigating.get()) {
					// If we are not set in autonomous mode, don't try to drive!
					if (!_isAutonomous.get()) {
						// TODO: probably should add a "paused" state
						if (obs != null) 
							obs.waypointUpdate(WaypointState.OFF);
					} else {
						// Report our status as moving toward target
						if (obs != null)
							obs.waypointUpdate(WaypointState.GOING);
						
						// Get the position of the vehicle and the waypoint
						// TODO: fix threading issue (fast stop/start)
						Pose3D pose = _pose.pose;
						Pose3D wpPose = _waypoint.pose;
	
						// TODO: handle different UTM zones!
						// Figure out how to drive to waypoint
						_controller.controller.update(SimpleBoatSimulator.this, dt);
						
						// TODO: measure dt directly instead of approximating
						
						// Check for termination condition
						// TODO: termination conditions tested by controller
						double dist = pose.getEuclideanDistance(wpPose);
						
						if (dist <= 6.0) 
							{
								System.out.println("Should reach a waypoint now.");
								break;
							}
					}
					
					// Pause for a while
					try { 
						Thread.sleep(UPDATE_INTERVAL_MS); 
					} catch (InterruptedException e) {}
				}
				
				// Stop the vehicle
				_velocity = new Twist();
				
				// Upon completion, report status 
				// (if isNavigating is still true, we completed on our own)
				if (isNavigating.getAndSet(false)) {
					if (obs != null)
					{
						obs.waypointUpdate(WaypointState.DONE);
						System.out.println("Should report reaching a waypoint now.");
						
					}
				} else {
					if (obs != null){
						obs.waypointUpdate(WaypointState.CANCELLED);
						System.out.println("Should report cancelling a waypoint now.");
					}
				}
			}
		}).start();
	}

	@Override
	public void stopWaypoints() {

		// Stop the thread that is doing the "navigation" by terminating its
		// navigation flag and then removing the reference to the old flag.
		System.out.println("STOP!!!!");
		synchronized (_navigationLock) {
			if (_isNavigating != null) {
				_isNavigating.set(false);
				_isNavigating = null;
			}
			_waypoint = null;
		}
	}

	public WaypointState getWaypointStatus() {
		synchronized (_navigationLock) {
			if (_isNavigating == null) {
				return WaypointState.OFF;
			} else if (_isNavigating.get()) {
				return WaypointState.GOING;
			} else {
				return WaypointState.DONE;
			}
		}
	}

	@Override
	public void startCamera(final long numFrames, final double interval,
			final int width, final int height, final CameraListener obs) {

		// Keep a reference to the capture flag for THIS capture process
		final AtomicBoolean isCapturing = new AtomicBoolean(true);

		// Set this to be the current capture flag
		synchronized (_captureLock) {
			if (_isCapturing != null)
				_isCapturing.set(false);
			_isCapturing = isCapturing;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				int iFrame = 0;
				long int_ms = (long) (interval * 1000.0);

				while (isCapturing.get()
						&& (numFrames <= 0 || iFrame < numFrames)) {

					// Every so often, send out a random picture
					sendImage(captureImage(width, height));
					iFrame++;

					if (obs != null)
						obs.imagingUpdate(CameraState.CAPTURING);

					// Wait for a while
					try {
						Thread.sleep(int_ms);
					} catch (InterruptedException ex) {
						if (obs != null)
							obs.imagingUpdate(CameraState.CANCELLED);
						isCapturing.set(false);
						return;
					}
				}

				if (obs != null)
					obs.imagingUpdate(CameraState.DONE);
				isCapturing.set(false);
			}
		}).start();
	}

	@Override
	public void stopCamera() {
		// Stop the thread that sends out images by terminating its
		// navigation flag and then removing the reference to the old flag.
		synchronized (_captureLock) {
			if (_isCapturing != null) {
				_isCapturing.set(false);
				
			}
		}
	}

	@Override
	public CameraState getCameraStatus() {
		synchronized (_captureLock) {
			if (_isCapturing.get()) {
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
		while (angle > Math.PI)
			angle -= 2 * Math.PI;
		while (angle < -Math.PI)
			angle += 2 * Math.PI;
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

}
