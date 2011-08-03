package edu.cmu.ri.crw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

import edu.cmu.ri.crw.VehicleServer.WaypointState;

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
	public UtmPose _state = new UtmPose();
	public Twist _velocity = new Twist();
	public UtmPose _waypoint = null;

	private final Object _captureLock = new Object();
	private AtomicBoolean _isCapturing = null;
	
	private final Object _navigationLock = new Object();
	private AtomicBoolean _isNavigating = null;
	
	private final AtomicBoolean _isAutonomous = new AtomicBoolean(true);
	
	public SimpleBoatSimulator() {
		final double dt = UPDATE_INTERVAL_MS / 1000.0;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while(true) {
					try { Thread.sleep(UPDATE_INTERVAL_MS); } catch (InterruptedException e) {}

					// Send out pose updates 
					UtmPoseWithCovarianceStamped state = new UtmPoseWithCovarianceStamped();
					state.pose.pose.pose = _state.pose.clone();
					state.utm = _state.utm.clone();
					sendState(state);
					
					// Send out velocity updates
					TwistWithCovarianceStamped velocity = new TwistWithCovarianceStamped();
					velocity.twist.twist = _velocity.clone();
					sendVelocity(velocity);
					
					// Move in an arc with given velocity over time interval
					double yaw = QuaternionUtils.toYaw(_state.pose.orientation);
					_state.pose.position.x += _velocity.linear.x * Math.cos(yaw) * dt;
					_state.pose.position.y += _velocity.linear.x * Math.sin(yaw) * dt;
					_state.pose.orientation = QuaternionUtils.fromEulerAngles(0, 0, yaw + _velocity.angular.z * dt);
				}
			}
		}).start();
	}
	
	@Override
	public CompressedImage captureImage(int width, int height) {

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
	public UtmPose getWaypoint() {
		synchronized(_navigationLock) {
			return _waypoint;
		}
	}

	@Override
	public void setSensorType(int channel, SensorType type) {
		_sensorTypes[channel] = type;
	}

	@Override
	public void setState(UtmPose state) {
		_state.setTo(state); // TODO: I have no idea what setTo does!
	}

	@Override
	public void startWaypoint(final UtmPose waypoint, final WaypointObserver obs) {
		
		// Keep a reference to the navigation flag for THIS waypoint
		final AtomicBoolean isNavigating = new AtomicBoolean(true);
		
		// Set this to be the current navigation flag
		synchronized(_navigationLock) {
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
					if (_isAutonomous.get()) {

						// Get the position of the vehicle and the waypoint
						UtmPoseWithCovarianceStamped state = getState();
						Pose pose = state.pose.pose.pose;
						Pose wpPose = waypoint.pose;
		
						// TODO: handle different UTM zones!
	
						// Compute the distance and angle to the waypoint
						// TODO: compute distance more efficiently
						double distance = Math.sqrt(Math.pow(
								(wpPose.position.x - pose.position.x), 2)
								+ Math.pow((wpPose.position.y - pose.position.y),
										2));
						double angle = Math.atan2(
								(wpPose.position.y - pose.position.y),
								(wpPose.position.x - pose.position.x))
								- QuaternionUtils.toYaw(pose.orientation);
						angle = normalizeAngle(angle);
	
						// Choose driving behavior depending on direction and
						// where we are
						if (Math.abs(angle) > 1.0) {
	
							// If we are facing away, turn around first
							_velocity.linear.x = 0.5;
							_velocity.angular.z = Math.max(Math.min(angle / 1.0, 1.0),
									-1.0);
						} else if (distance >= 3.0) {
	
							// If we are far away, drive forward and turn
							_velocity.linear.x = Math.min(distance / 10.0, 1.0);
							_velocity.angular.z = Math.max(Math.min(angle / 10.0, 1.0),
									-1.0);
						} else  /*(distance < 3.0)*/ {
							break;
						}
					}
					
					// Pause for a while
					try {
						Thread.sleep(UPDATE_INTERVAL_MS);
						if (obs != null)
							obs.waypointUpdate(WaypointState.GOING);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				// Stop the vehicle
				_velocity.linear.x = 0.0;
				_velocity.angular.z = 0.0;
				
				// Upon completion, report status 
				// (if isNavigating is still true, we completed on our own)
				if (isNavigating.getAndSet(false)) {
					if (obs != null)
						obs.waypointUpdate(WaypointState.DONE);
				} else {
					if (obs != null)
						obs.waypointUpdate(WaypointState.CANCELLED);
				}
			}
		}).start();
	}

	@Override
	public void stopWaypoint() {
		
		// Stop the thread that is doing the "navigation" by terminating its
		// navigation flag and then removing the reference to the old flag.
		synchronized(_navigationLock) {
			if (_isNavigating != null) {
				_isNavigating.set(false);
				_isNavigating = null;
			}
			_waypoint = null;
		}
	}

	public WaypointState getWaypointStatus() {
		synchronized(_navigationLock) {
			if (_isNavigating.get()) {
				return WaypointState.GOING;
			} else {
				return WaypointState.DONE;
			}
		}
	}

	@Override
	public void startCamera(final long numFrames, final double interval,
			final int width, final int height, final ImagingObserver obs) {

		// Keep a reference to the capture flag for THIS capture process
		final AtomicBoolean isCapturing = new AtomicBoolean(true);
		
		// Set this to be the current capture flag
		synchronized(_captureLock) {
			if (_isCapturing != null)
				_isCapturing.set(false);		
			_isCapturing = isCapturing;
		}
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				int iFrame = 0;
				long int_ms = (long)(interval * 1000.0);

				while (isCapturing.get() && (numFrames <= 0 || iFrame < numFrames)) {

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
		synchronized(_captureLock) {
			if (_isCapturing != null) {
				_isCapturing.set(false);
				_isCapturing = null;
			}
		}
	}

	@Override
	public CameraState getCameraStatus() {
		synchronized(_captureLock) {
			if (_isCapturing.get()) {
				return CameraState.CAPTURING;
			} else {
				return CameraState.OFF;
			}
		}
	}

	double distToGoal(Pose x, Pose y) {
		double x1 = x.position.x, x2 = x.position.y, x3 = x.position.z;
		double y1 = y.position.x, y2 = y.position.y, y3 = y.position.z;

		return Math.sqrt(Math.pow(x1 - y1, 2) + Math.pow(x2 - y2, 2)
				+ Math.pow(x3 - y3, 2));
	}

	@Override
	public UtmPoseWithCovarianceStamped getState() {
		UtmPoseWithCovarianceStamped stateMsg = new UtmPoseWithCovarianceStamped();
		stateMsg.utm = _state.utm.clone();
		stateMsg.pose.pose.pose = _state.pose;
		return stateMsg;
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
	public TwistWithCovarianceStamped getVelocity() {
		TwistWithCovarianceStamped velMsg = new TwistWithCovarianceStamped();
		velMsg.twist.twist = _velocity.clone();
		return velMsg;
	}

	/**
	 * Takes an angle and shifts it to be in the range -Pi to Pi.
	 * 
	 * @param angle an angle in radians
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
