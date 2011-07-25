package edu.cmu.ri.crw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

import edu.cmu.ri.crw.QuaternionUtils;

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

	private volatile boolean _isCapturing = false;
	private volatile boolean _isNavigating = false;
	private volatile boolean _isAutonomous = true;
	
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
		return _waypoint;
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
		_isNavigating = true;
		_waypoint = waypoint;

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (_isNavigating) {

					// Pause for a while
					try {
						Thread.sleep(UPDATE_INTERVAL_MS);
						obs.waypointUpdate(WaypointState.GOING);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					// If we are not set in autonomous mode, don't try to drive!
					if (!_isAutonomous)
						continue;
					
					// Get the position of the vehicle and the waypoint
					UtmPoseWithCovarianceStamped state = getState();
					Pose pose = state.pose.pose.pose;

					UtmPose waypointState = getWaypoint();
					Pose waypoint = waypointState.pose;

					// TODO: handle different UTM zones!

					// Compute the distance and angle to the waypoint
					double distance = Math.sqrt(Math.pow(
							(waypoint.position.x - pose.position.x), 2)
							+ Math.pow((waypoint.position.y - pose.position.y),
									2));
					double angle = Math.atan2(
							(waypoint.position.y - pose.position.y),
							(waypoint.position.x - pose.position.x))
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
						_velocity.linear.x = 0.0;
						_velocity.angular.z = 0.0;
						
						obs.waypointUpdate(WaypointState.DONE);
						_isNavigating = false;
						return;
					}
				}
				
				// If we broke out of the loop, it means someone cancelled us
				obs.waypointUpdate(WaypointState.CANCELLED);
			}
		}).start();
	}

	@Override
	public void stopWaypoint() {
		// Stop the thread that is doing the "navigation"
		_isNavigating = false;
		_waypoint = null;
	}

	@Override
	public void startCamera(final long numFrames, final double interval,
			final int width, final int height, final ImagingObserver obs) {
		_isCapturing = true;

		new Thread(new Runnable() {

			@Override
			public void run() {
				int iFrame = 0;

				while (_isCapturing && (iFrame < numFrames)) {

					// Every so often, send out a random picture
					sendImage(captureImage(width, height));
					iFrame++;

					if (obs != null) {
						obs.imagingUpdate(CameraState.CAPTURING);
					}

					// Wait for a while
					try {
						Thread.sleep((long) (interval * 1000.0));
					} catch (InterruptedException ex) {
						obs.imagingUpdate(CameraState.CANCELLED);
						_isCapturing = false;
						return;
					}
				}

				obs.imagingUpdate(CameraState.DONE);
				_isCapturing = false;
			}
		}).start();
	}

	@Override
	public void stopCamera() {
		// Stop the thread that sends out images
		_isCapturing = false;
	}

	public WaypointState getWaypointStatus() {
		if (this._isNavigating)
			return WaypointState.GOING;
		else
			return WaypointState.DONE;
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

	@Override
	public CameraState getCameraStatus() {
		if (this._isCapturing) {
			return CameraState.CAPTURING;
		} else {
			return CameraState.OFF;
		}
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
		return _isAutonomous;
	}

	@Override
	public void setAutonomous(boolean auto) {
		_isAutonomous = auto;
		_velocity = new Twist(); // Reset velocity when changing modes
	}
}
