package edu.cmu.ri.airboat.server;

import java.util.ArrayList;
import java.util.List;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Pose;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

import com.google.code.microlog4android.LoggerFactory;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.VehicleFilter;
import edu.cmu.ri.crw.WaypointObserver;

/**
 * Contains the actual implementation of vehicle functionality, accessible as a
 * singleton that is updated and maintained by a background service.
 * 
 * @author pkv
 * @author kss
 * 
 */
public class AirboatImpl extends AbstractVehicleServer {

	private static final com.google.code.microlog4android.Logger logger = LoggerFactory
			.getLogger();

	public static final int UPDATE_INTERVAL_MS = 100;

	public final SensorType[] _sensorTypes = new SensorType[3];
	public final double[] _state = new double[7];
	public UtmPose _waypoint = null;
	//public UtmPoseWithCovarianceStamped _pose = new UtmPoseWithCovarianceStamped();

	private volatile boolean _isCapturing = false;
	private volatile boolean _isNavigating = false;

	private static final String logTag = AirboatImpl.class.getName();

	/**
	 * Defines the PID gains that will be returned if there is an error.
	 */
	public static final double[] NAN_GAINS = new double[] { Double.NaN,
			Double.NaN, Double.NaN };

	// Define Amarino function control codes
	public static final char GET_GYRO_FN = 'g';
	public static final char GET_RUDDER_FN = 'r';
	public static final char GET_THRUST_FN = 't';
	public static final char GET_TE_FN = 's';
	public static final char SET_VELOCITY_FN = 'v';
	public static final char GET_GAINS_FN = 'l';
	public static final char SET_GAINS_FN = 'k';

	// Set timeout for asynchronous Amarino calls
	public static final int RESPONSE_TIMEOUT_MS = 250; // was 200 earlier

	// Status information
	boolean _isConnected = true;
	boolean _isAutonomous = false;

	// UTM zone information
	boolean _utmHemiNorth = true;
	int _utmZone = 17;

	// Internal data structures for Amarino callbacks
	final Context _context;
	final String _arduinoAddr;
	final List<String> _partialCommand = new ArrayList<String>(10);
	final Object _velocityGainLock = new Object();
	double[] _velocityGain = new double[3];
	double _velocityGainAxis = -1;

	/**
	 * Inertial state vector, currently containing a 6D pose estimate:
	 * [x,y,z,roll,pitch,yaw]
	 */
	// double[] _pose = new double[6];

	/**
	 * Filter used internally to update the current pose estimate
	 */
	VehicleFilter filter = new SimpleFilter();

	/**
	 * Inertial velocity vector, containing a 6D angular velocity estimate: [rx,
	 * ry, rz, rPhi, rPsi, rOmega]
	 */
	Twist _velocities = new Twist();

	/**
	 * Raw gyroscopic readings, as reported from the Arduino.
	 */
	double[] _gyroReadings = new double[3];

	/**
	 * Creates a new instance of the vehicle implementation. This function
	 * should only be used internally when the corresponding vehicle service is
	 * started and stopped.
	 * 
	 * @param context the application context to use
	 * @param addr the bluetooth address of the vehicle controller
	 */

	protected AirboatImpl(Context context, String addr) {
		_context = context;
		_arduinoAddr = addr;

	}

	/**
	 * Internal update function called at regular intervals to process command
	 * and control events.
	 * 
	 * @param dt the elapsed time since the last update call (in seconds)
	 */
	protected void update(double dt) {

		// Do an intelligent state prediction update here

		// TODO Talk to the filter here
		// _pose = filter.pose(System.currentTimeMillis());
		// logger.info("POSE: " + Arrays.toString(_pose));

		// If the vehicle is currently autonomous and has a valid navigation
		// controller, update the controller to get new commanded velocities
		/*
		 * if (_isAutonomous) { if (_controller != null) {
		 * _controller.controller.update(this, this, dt); } else { Log.w(logTag,
		 * "No controller update."); } }
		 */
		// Call Amarino with new velocities here
		Amarino.sendDataToArduino(_context, _arduinoAddr, SET_VELOCITY_FN,
				new float[] { 
					(float) _velocities.linear.x, 
					(float) _velocities.linear.y,
					(float) _velocities.linear.z, 
					(float) _velocities.angular.x,
					(float) _velocities.angular.y, 
					(float) _velocities.angular.z 
				});
		// Yes, I know this looks silly, but Amarino doesn't handle doubles
		
		if(_isNavigating)
			AirboatControllerLibrary.POINT_AND_SHOOT.controller.update(this, dt);
	}

	public double[] getPID(int axis) {

		// Call Amarino here
		Amarino.sendDataToArduino(_context, _arduinoAddr, GET_GAINS_FN, axis);

		// Wait for response here (only if connected)
		if (_isConnected) {
			synchronized (_velocityGainLock) {

				// Clear any old return value
				_velocityGainAxis = -1;

				try {
					// Wait for the correct axis to be filled in,
					// but if we start getting too backed up, drop calls
					for (int i = 0; i < 3 && _velocityGainAxis != axis; ++i)
						_velocityGainLock.wait(RESPONSE_TIMEOUT_MS);
				} catch (InterruptedException ex) {
					Log.w(logTag, "Interrupted function: " + GET_GAINS_FN, ex);
				}

				// If got the appropriate axis, make a copy immediately
				if (_velocityGainAxis == axis) {
					double[] output = new double[3];
					System.arraycopy(_velocityGain, 0, output, 0, output.length);
					return output;
				} else {
					Log.w(logTag, "No response for: " + GET_GAINS_FN);
					return NAN_GAINS;
				}
			}
		} else {
			Log.w(logTag, "Not connected, can't perform: " + GET_GAINS_FN);
			return NAN_GAINS;
		}
	}

	/**
	 * @see AirboatControl#setVelocityGain(double, double, double, double)
	 */
	public boolean setVelocityGain(double axis, double kp, double ki, double kd) {
		// Call Amarino here
		Amarino.sendDataToArduino(
				_context,
				_arduinoAddr,
				SET_GAINS_FN,
				new float[] { (float) axis, (float) kp, (float) ki, (float) kd });
		logger.info("SETGAINS: " + axis + " " + kp + ", " + ki + ", " + kd);
		return true;
	}

	/**
	 * @see AirboatCommand#isConnected()
	 */
	public boolean isConnected() {
		return _isConnected;
	}

	/**
	 * Internal function used to set the connection status of this object
	 * (indicating whether currently in contact with vehicle controller).
	 */
	protected void setConnected(boolean isConnected) {
		_isConnected = isConnected;
	}

	/**
	 * Handles complete Arduino commands, once they are reassembled.
	 * 
	 * @param cmd
	 *            the list of arguments composing a command
	 */
	protected void onCommand(List<String> cmd) {

		// Just like Amarino, we use a flag to differentiate channels
		switch (cmd.get(0).charAt(0)) {
		case GET_GAINS_FN:
			// Check size of function
			if (cmd.size() != 6) {
				Log.w(logTag, "Received corrupt gain function: " + cmd);
				return;
			}

			// Return a set of PID values
			try {
				synchronized (_velocityGainLock) {
					// Read in the axis that was filled in
					_velocityGainAxis = Double.parseDouble(cmd.get(1));

					// Cast the parameters to double-valued gains
					for (int i = 0; i < 3; i++)
						_velocityGain[i] = Double.parseDouble(cmd.get(i + 2));

					// Notify the calling function (this is a synchronous call)
					_velocityGainLock.notifyAll();
				}
			} catch (NumberFormatException e) {
				Log.w(logTag, "Received corrupt gain function: " + cmd);
			}
			break;
		case GET_GYRO_FN:
			// Check size of function
			if (cmd.size() != 5) {
				Log.w(logTag, "Received corrupt gyro function: " + cmd);
				return;
			}

			// Update the gyro reading
			try {
				for (int i = 0; i < 3; i++)
					_gyroReadings[i] = Double.parseDouble(cmd.get(i + 1));
				filter.gyroUpdate(_gyroReadings[2], System.currentTimeMillis());
				logger.info("GYRO: " + cmd);
			} catch (NumberFormatException e) {
				for (int i = 0; i < 3; i++)
					_gyroReadings[i] = Double.NaN;
				Log.w(logTag, "Received corrupt gyro reading: " + cmd);
			}

			break;
		case GET_RUDDER_FN:
			logger.info("RUDDER: " + cmd);
			break;
		case GET_THRUST_FN:
			logger.info("THRUST: " + cmd);
			break;
		case GET_TE_FN:
			logger.info("TE: " + cmd);
			break;
		default:
			Log.w(logTag, "Received unknown function type: " + cmd);
			break;
		}
	}

	/**
	 * Waits for incoming Amarino data from our device, assembles it into a list
	 * of strings, then takes that and calls onCommand with it.
	 */
	public BroadcastReceiver dataCallback = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// The device address from which the data was sent
			final String address = intent
					.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);

			// Ignore data from other devices
			if (!address.equalsIgnoreCase(_arduinoAddr))
				return;

			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(
					AmarinoIntent.EXTRA_DATA_TYPE, -1);

			// Read in data as string and add to queue for processing
			if (dataType == AmarinoIntent.STRING_EXTRA) {
				String newCmd = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				_partialCommand.add(newCmd);

				// If a command is completed, attempt to execute it
				if (newCmd.indexOf('\r') >= 0 || newCmd.indexOf('\n') >= 0) {
					try {
						onCommand(_partialCommand);
					} catch (Throwable t) {
						Log.e(logTag, "Command failed:", t);
					}
					_partialCommand.clear();
				}
			}
		}
	};

	/**
	 * Listens for connection and disconnection of the vehicle controller.
	 */
	public BroadcastReceiver connectionCallback = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// Check for events indicating connection or disconnection
			if (intent.getAction().equals(AmarinoIntent.ACTION_CONNECTED)) {
				final String address = intent
						.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
				if (!address.equalsIgnoreCase(_arduinoAddr))
					return;

				Log.i(logTag, "Connected to " + _arduinoAddr);
				setConnected(true);
			} else if (intent.getAction().equals(
					AmarinoIntent.ACTION_DISCONNECTED)) {
				final String address = intent
						.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
				if (!address.equalsIgnoreCase(_arduinoAddr))
					return;

				Log.i(logTag, "Disconnected from " + _arduinoAddr);
				setConnected(false);
			} else if (intent.getAction().equals(
					AmarinoIntent.ACTION_CONNECTED_DEVICES)) {
				final String[] devices = intent
						.getStringArrayExtra(AmarinoIntent.EXTRA_CONNECTED_DEVICE_ADDRESSES);
				if (devices != null)
					for (String device : devices)
						if (device.equalsIgnoreCase(_arduinoAddr)) {
							Log.i(logTag, "Connected to " + _arduinoAddr);
							setConnected(true);
							return;
						}
				Log.i(logTag, "Disconnected from " + _arduinoAddr);
				setConnected(false);
			}
		}
	};

	public synchronized CompressedImage captureImage(int width, int height) {

		byte[] bytes = AirboatCameraActivity.takePhoto(_context, width, height);
		Log.i(logTag, "Sending image [" + bytes.length + "]");
		return toCompressedImage(width, height, bytes);
	}

	public synchronized boolean saveImage() {
		
		AirboatCameraActivity.savePhoto(_context);
		Log.i(logTag, "Saving image.");
		return true;
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
	public void setPID(int axis, double[] gains) {
		System.arraycopy(gains, 0, _gains[axis], 0, _gains[axis].length);
	}

	@Override
	public void setSensorType(int channel, SensorType type) {
		_sensorTypes[channel] = type;
	}



	@Override
	public void startCamera(final long numFrames, final double interval,
			final int width, final int height, ImagingObserver obs) {
		_isCapturing = true;

		new Thread(new Runnable() {

			@Override
			public void run() {
				int iFrame = 0;

				while (_isCapturing && (iFrame < numFrames)) {

					// Every so often, send out a random picture
					sendImage(captureImage(width, height));
					iFrame++;

					// Wait for a while
					try {
						Thread.sleep(UPDATE_INTERVAL_MS);
					} catch (InterruptedException ex) {
						return;
					}
				}
			}
		}).start();
	}

	@Override
	public void stopCamera() {
		// Stop the thread that sends out images
		_isCapturing = false;
	}

	
	double distToGoal(Pose x, Pose y) {
		double x1 = x.position.x, x2 = x.position.y, x3 = x.position.z;
		double y1 = y.position.x, y2 = y.position.y, y3 = y.position.z;

		return Math.sqrt(Math.pow(x1 - y1, 2) + Math.pow(x2 - y2, 2)
				+ Math.pow(x3 - y3, 2));
	}

	@Override
	public int getNumSensors() {
		// TODO Fix this
		return 0;
	}

	@Override
	public UtmPoseWithCovarianceStamped getState() {
		// TODO Auto-generated method stub
		return filter.pose(System.currentTimeMillis());

	}

	/**
	 * Takes a 6D vehicle pose, does appropriate internal computation to change
	 * the current estimate of vehicle state to match the specified pose. Used
	 * for user- or multirobot- pose corrections.
	 * 
	 * @param pose the corrected 6D pose of the vehicle: [x,y,z,roll,pitch,yaw]
	 */

	public void setState(UtmPose pose) {
		
		// Change the offset of this vehicle by modifying filter
		filter.reset(pose, System.currentTimeMillis());
		logger.info("POSE: " + pose);
	}

	public WaypointState getWaypointStatus() {
		// if (distToGoal(state, waypoint) > 0.5) {
		//TODO Write code for this
		return WaypointState.GOING;
	}
	
	@Override
	public void startWaypoint(UtmPose waypoint, WaypointObserver obs ) {
		
		_waypoint = waypoint.clone();
		_isNavigating = true;
	}
	
	@Override
	public void stopWaypoint() {

		_isNavigating = false;
		_waypoint = null;
	}

	/**
	 * Returns the current estimated 6D velocity of the vehicle.
	 */
	public TwistWithCovarianceStamped getVelocity() {
		TwistWithCovarianceStamped twistMsg = new TwistWithCovarianceStamped();
		twistMsg.twist.twist = _velocities.clone();
		return twistMsg;
	}

	/**
	 * Sets a desired 6D velocity for the vehicle.
	 */
	public void setVelocity(Twist vel) {
		_velocities = vel.clone();
	}

	@Override
	public CameraState getCameraStatus() {
		// TODO: fill this in with something reasonable
		return CameraState.UNKNOWN;
	}

	@Override
	public boolean isAutonomous() {
		return _isAutonomous;
	}

	@Override
	public void setAutonomous(boolean isAutonomous) {
		_isAutonomous = isAutonomous;
	}

}
