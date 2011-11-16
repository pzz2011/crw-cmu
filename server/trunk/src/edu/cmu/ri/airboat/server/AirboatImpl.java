package edu.cmu.ri.airboat.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

import com.google.code.microlog4android.LoggerFactory;

import edu.cmu.ri.crw.AbstractVehicleServer;
import edu.cmu.ri.crw.VehicleFilter;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;

/**
 * Contains the actual implementation of vehicle functionality, accessible as a
 * singleton that is updated and maintained by a background service.
 * 
 * @author pkv
 * @author kss
 * 
 */
public class AirboatImpl extends AbstractVehicleServer {

	private static final com.google.code.microlog4android.Logger logger = 
		LoggerFactory.getLogger();

	private static final String logTag = AirboatImpl.class.getName();
	public static final int UPDATE_INTERVAL_MS = 100;
	public static final int NUM_SENSORS = 3;
	public static final AirboatController DEFAULT_CONTROLLER = AirboatController.POINT_AND_SHOOT;
	
	protected final SensorType[] _sensorTypes = new SensorType[NUM_SENSORS];
	protected UtmPose[] _waypoints = new UtmPose[0];

	protected final Object _captureLock = new Object();
	protected TimerTask _captureTask = null;
	
	protected final Object _navigationLock = new Object();
	protected TimerTask _navigationTask = null;
	
	private final Timer _timer = new Timer();

	/**
	 * Defines the PID gains that will be returned if there is an error.
	 */
	public static final double[] NAN_GAINS = 
		new double[] { Double.NaN, Double.NaN, Double.NaN };

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
	final AtomicBoolean _isConnected = new AtomicBoolean(true);
	final AtomicBoolean _isAutonomous = new AtomicBoolean(false);

	// Internal data structures for Amarino callbacks
	final Context _context;
	final String _arduinoAddr;
	final List<String> _partialCommand = new ArrayList<String>(10);
	final Object _velocityGainLock = new Object();
	final double[] _velocityGain = new double[3];
	double _velocityGainAxis = -1;

	/**
	 * Inertial state vector, currently containing a 6D pose estimate:
	 * [x,y,z,roll,pitch,yaw]
	 */
	UtmPose _utmPose = new UtmPose();

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
	final double[] _gyroReadings = new double[3];

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
		_utmPose = filter.pose(System.currentTimeMillis());
		logger.info("POSE: " + _utmPose);
		sendState(_utmPose.clone());
		
		// Call Amarino with new velocities here
		Amarino.sendDataToArduino(_context, _arduinoAddr, SET_VELOCITY_FN,
				new float[] { 
					(float) _velocities.dx(), 
					(float) _velocities.dy(),
					(float) _velocities.dz(), 
					(float) _velocities.drx(),
					(float) _velocities.dry(), 
					(float) _velocities.drz()
				});
		// Yes, I know this looks silly, but Amarino doesn't handle doubles
		
		// Log velocities
		logger.info("VEL: " + _velocities);
		
		// Send velocities 
		Twist vel = _velocities.clone();
		sendVelocity(vel);		
	}
	
	/**
	 * @see VehicleServer#getGains(int)
	 */
	@Override
	public double[] getGains(int axis) {

		// Call Amarino here
		Amarino.sendDataToArduino(_context, _arduinoAddr, GET_GAINS_FN, axis);

		// Wait for response here (only if connected)
		if (_isConnected.get()) {
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
	 * @see VehicleServer#setGains(int, double[])
	 */
	@Override
	public void setGains(int axis, double[] k) {
		
		// Call Amarino here
		Amarino.sendDataToArduino(
				_context,
				_arduinoAddr,
				SET_GAINS_FN,
				new float[] { (float) axis, (float) k[0], (float) k[1], (float) k[2] });
		logger.info("SETGAINS: " + axis + " " + Arrays.toString(k));
	}

	/**
	 * @see AirboatCommand#isConnected()
	 */
	public boolean isConnected() {
		return _isConnected.get();
	}

	/**
	 * Internal function used to set the connection status of this object
	 * (indicating whether currently in contact with vehicle controller).
	 */
	protected void setConnected(boolean isConnected) {
		_isConnected.set(isConnected);
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
			if (cmd.size() != 5) {
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
			if (cmd.size() != 4) {
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
			// Check size of function
			if (cmd.size() != 4) {
				Log.w(logTag, "Received corrupt sensor function: " + cmd);
				return;
			}
			
			// Broadcast the sensor reading
			try {
				SensorData reading = new SensorData();
				reading.data = new double[3];
				reading.type = SensorType.TE;
				for (int i = 0; i < 3; i++)
					reading.data[i] = Double.parseDouble(cmd.get(i + 1));
				sendSensor(0, reading);
				logger.info("TE: " + cmd);
			} catch (NumberFormatException e) {
				Log.w(logTag, "Received corrupt sensor reading: " + cmd);
			}
			
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
				
				// If a command is completed, attempt to execute it
				if (newCmd.indexOf('\r') >= 0 || newCmd.indexOf('\n') >= 0) {
					try {
						onCommand(_partialCommand);
					} catch (Throwable t) {
						Log.e(logTag, "Command failed:", t);
					}
					_partialCommand.clear();
				} else {
					// Otherwise, just add this command to the list
					_partialCommand.add(newCmd);
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

	public synchronized byte[] captureImage(int width, int height) {

		byte[] bytes = AirboatCameraActivity.takePhoto(_context, width, height);
		Log.i(logTag, "Sending image [" + bytes.length + "]");
		return bytes;
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
	public UtmPose[] getWaypoints() {
		UtmPose[] wpts = new UtmPose[_waypoints.length];
		synchronized(_navigationLock) {		
			System.arraycopy(_waypoints, 0, wpts, 0, wpts.length);
		}
		return wpts;
	}

	@Override
	public void setSensorType(int channel, SensorType type) {
		_sensorTypes[channel] = type;
	}

	@Override
	public void startCamera(final int numFrames, final double interval, final int width, final int height) {
		
		// Precompute the timing interval in ms
		final long int_ms = (long)(interval * 1000.0);
		
		// Create a new capturing task
		TimerTask newCaptureTask = new TimerTask() {
			int iFrame = 0;
			
			@Override
			public void run() {
 				// Stop if we took enough pictures
				if (numFrames > 0 && iFrame >= numFrames) {
					sendCameraUpdate(CameraState.DONE);
 					this.cancel();
				}
				
				// Send out a new picture
				sendImage(captureImage(width, height));
				iFrame++;
					
				// Report status
				sendCameraUpdate(CameraState.CAPTURING);
			}
		}; 
		
		// Kill old capture process and start up this new one
		synchronized(_captureLock) {
			if (_captureTask != null)
				_captureTask.cancel();
			_captureTask = newCaptureTask;
			_timer.schedule(_captureTask, 0, int_ms);
		}
		
		// Report the new imaging job in the log file
		logger.info("IMG: " + numFrames + " @ " + interval + "s, " +
				width + " x " + height);
	}

	@Override
	public void stopCamera() {
		// Stop the thread that sends out images by terminating its
		// capturing task process and then removing the reference.
		synchronized(_captureLock) {
			_captureTask.cancel();
			_captureTask = null;
		}
		
		sendCameraUpdate(CameraState.CANCELLED);
	}

	@Override
	public int getNumSensors() {
		return NUM_SENSORS;
	}

	@Override
	public UtmPose getPose() {
		return _utmPose;
	}

	/**
	 * Takes a 6D vehicle pose, does appropriate internal computation to change
	 * the current estimate of vehicle state to match the specified pose. Used
	 * for user- or multirobot- pose corrections.
	 * 
	 * @param pose the corrected 6D pose of the vehicle: [x,y,z,roll,pitch,yaw]
	 */
	@Override
	public void setPose(UtmPose pose) {
		
		// Change the offset of this vehicle by modifying filter
		filter.reset(pose, System.currentTimeMillis());
		
		// Copy this pose over the existing value
		_utmPose = pose.clone();
		
		// Report the new pose in the log file
		logger.info("POSE: " + _utmPose);
	}

	@Override
	public WaypointState getWaypointStatus() {
		synchronized(_navigationLock) {
			return (_navigationTask != null) ? WaypointState.GOING : WaypointState.OFF;
		}
	}
	
	@Override
	public void startWaypoints(final UtmPose[] waypoints, final String controller) {
		
		// Precompute timing interval in ms
		// TODO: use actual time to compute timesteps on the fly for navigation
		final double dt = (double)UPDATE_INTERVAL_MS / 1000.0;
		
		// Determine which controller was specified
		AirboatController vc;
		try {
			vc = AirboatController.valueOf(controller);
		} catch(IllegalArgumentException e) {
			vc = DEFAULT_CONTROLLER;
		}
		final AirboatController vehicleController = vc;
		
		// Start a navigation thread
		TimerTask newNavigationTask = new TimerTask() {

			@Override
			public void run() {
				
				// If we are not set in autonomous mode, don't try to drive!
				if (!_isAutonomous.get()) {
					sendWaypointUpdate(WaypointState.PAUSED);
					return;
				}
				
				// TODO: handle different UTM zones!

				// Figure out how to drive to waypoint
				vehicleController.controller.update(AirboatImpl.this, dt);
				
				// Report our status as moving toward target
				sendWaypointUpdate(WaypointState.GOING);
					
				// If we reach the target, stop the vehicle and report it
				if (getWaypoints().length <= 0) {
					_velocities = new Twist();
					sendWaypointUpdate(WaypointState.DONE);
				}
			}
		};
		
		// Set this to be the current navigation flag
		synchronized(_navigationLock) {
			if (_navigationTask != null)
				_navigationTask.cancel();
			_waypoints = new UtmPose[waypoints.length];
			System.arraycopy(waypoints, 0, _waypoints, 0, _waypoints.length);
			_navigationTask = newNavigationTask;
			_timer.schedule(_navigationTask, 0, UPDATE_INTERVAL_MS);	
		}
		
		// Report the new waypoint in the log file
		logger.info("NAV: " + vehicleController + " " + _utmPose);
	}
	
	@Override
	public void stopWaypoints() {
		// Stop the thread that is doing the "navigation" by terminating its
		// navigation task process and then removing the reference.
		synchronized(_navigationLock) {
			_navigationTask.cancel();
			_navigationTask = null;
			_waypoints = new UtmPose[0];
		}
	}

	/**
	 * Returns the current estimated 6D velocity of the vehicle.
	 */
	public Twist getVelocity() {
		return _velocities.clone();
	}

	/**
	 * Sets a desired 6D velocity for the vehicle.
	 */
	public void setVelocity(Twist vel) {
		_velocities = vel.clone();
	}

	@Override
	public CameraState getCameraStatus() {
		synchronized(_captureLock) {
			return (_captureTask != null) ? CameraState.CAPTURING : CameraState.OFF;
		}
	}

	@Override
	public boolean isAutonomous() {
		return _isAutonomous.get();
	}

	@Override
	public void setAutonomous(boolean isAutonomous) {
		_isAutonomous.set(isAutonomous);
		
		// Set velocities to zero to allow for safer transitions
		_velocities = new Twist();
	}

	/**
	 * Performs cleanup functions in preparation for stopping the server.
	 */
	public void shutdown() {
		stopWaypoints();
		stopCamera();
		
		_isAutonomous.set(false);
		_isConnected.set(false);
		
		_timer.cancel();
		_timer.purge();
	}
}
