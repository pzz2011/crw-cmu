package edu.cmu.ri.airboat.server;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.geography.coordinates.LatLong;
import org.jscience.geography.coordinates.UTM;
import org.jscience.geography.coordinates.crs.ReferenceEllipsoid;

import robotutils.Pose3D;
import robotutils.Quaternion;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import at.abraxas.amarino.AmarinoIntent;

import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.google.code.microlog4android.format.PatternFormatter;

import edu.cmu.ri.crw.CrwNetworkUtils;
import edu.cmu.ri.crw.CrwSecurityManager;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpVehicleService;

/**
 * Android Service to register sensor and Amarino handlers for Android.
 * Contains a RosVehicleServer and a VehicleServer object.
 * 
 * @author pkv
 * @author kshaurya
 *
 */
public class AirboatService extends Service {
	private static final int SERVICE_ID = 11312;
	private static final String TAG = AirboatService.class.getName();
	private static final com.google.code.microlog4android.Logger logger = LoggerFactory.getLogger();
	
	// Default values for parameters
	private static final String DEFAULT_LOG_PREFIX = "airboat_";
	final int GPS_UPDATE_RATE = 200; //in milliseconds
	
	// Intent fields definitions
	public static final String BD_ADDR = "BD_ADDR";
	public static final String UDP_REGISTRY_ADDR = "UDP_REGISTRY_ADDR";
	public static final String UPDATE_RATE = "UPDATE_RATE";
	
	// Binder object that receives interactions from clients.
    private final IBinder _binder = new AirboatBinder();
    
	// Flag indicating run status (Android has no way to query if a service is running)
	public static boolean isRunning = false;

	// Member parameters 
	private String _arduinoAddr;
	private InetSocketAddress _udpRegistryAddr;
	
	// Objects implementing actual functionality
	private AirboatImpl _airboatImpl;
	private UdpVehicleService _udpServer;
	
	// Logger that pipes log information for airboat classes to file
	private FileAppender _fileAppender; 
	
	// Raw accelerometer values (pre-integration with magnetometer)
    private float[] accelerometerValues;

	/**
	 * Handles GPS updates by calling the appropriate update.
	 */
	private LocationListener locationListener = new LocationListener() {
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
        
        public void onLocationChanged(Location location) {
        	
        	// Convert from lat/long to UTM coordinates
        	UTM utmLoc = UTM.latLongToUtm(
        				LatLong.valueOf(location.getLatitude(), location.getLongitude(), NonSI.DEGREE_ANGLE), 
        				ReferenceEllipsoid.WGS84
        			);

        	// Convert to UTM data structure
        	Pose3D pose = new Pose3D(
        			utmLoc.eastingValue(SI.METER),
        			utmLoc.northingValue(SI.METER),
        			(location.hasAltitude() ? location.getAltitude() : 0.0),
        			(location.hasBearing() ? Quaternion.fromEulerAngles(0.0, 0.0, (90.0 - location.getBearing()) * Math.PI / 180.0) : Quaternion.fromEulerAngles(0, 0, 0)) 
        			);
        	Utm origin = new Utm(utmLoc.longitudeZone(), utmLoc.latitudeZone() > 'o');
        	UtmPose utm = new UtmPose(pose, origin);
        	        	
        	// Apply update using filter object
        	if (_airboatImpl != null) {
        		_airboatImpl.filter.gpsUpdate(utm, location.getTime());
        		logger.info("GPS: " + utmLoc + ", " 
        				+ utmLoc.longitudeZone() + utmLoc.latitudeZone() + ", " 
        				+ location.getAltitude() + ", " + location.getBearing());
        	}
        }
      };
    
    /**
  	 * Handles accelerometer updates by calling the appropriate update.
  	 */
    private final SensorEventListener accelerometerListener = new SensorEventListener() {
		public void onAccuracyChanged(Sensor arg0, int arg1) {}
		
		public void onSensorChanged(SensorEvent event) {
			
			// Log these values, but do not use them directly in the filter
			accelerometerValues = event.values;
			logger.info("ACCELEROMETER: " + Float.toString(event.values[0]) + "," + Float.toString(event.values[1]) + "," + Float.toString(event.values[2]));
		}
    };
    
    /**
  	 * Handles magnetometer updates by calling the appropriate update.
  	 */
    private final SensorEventListener magneticListener = new SensorEventListener() {
    	
    	float[] R = new float[9];
		float[] I = new float[9];
		float[] values = new float[3];
    	
    	public void onAccuracyChanged(Sensor arg0, int arg1) {}
		
		public void onSensorChanged(SensorEvent event) {
			if (accelerometerValues == null)
				return;
			
			// Combine magnetometer and accelerometer to get orientation
			SensorManager.getRotationMatrix(R, I, accelerometerValues, event.values);
			SensorManager.getOrientation(R, values);
			logger.info("ORIENTATION: " + Arrays.toString(values));

			// Extract heading from orientation (in radians) and use in filter
			if (_airboatImpl != null) {
				double heading = values[0];
				_airboatImpl.filter.compassUpdate(Math.PI - heading, System.currentTimeMillis());
				logger.info("COMPASS: " +  heading);
			}
		}
    };

	
	
	
	/**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't deal with IPC.
     */
    public class AirboatBinder extends Binder {
        AirboatService getService() {
            return AirboatService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return _binder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Disable all DNS lookups (safer for private/ad-hoc networks)
		CrwSecurityManager.load();
		isRunning = true;
		
		// TODO: optimize this to allocate resources up here and handle multiple start commands
	}

	/**
	 * Access method to get underlying implementation of server functionality.
	 * @return An interface allowing high-level control of the boat.
	 */
	public AirboatImpl getServer() {
		return _airboatImpl;
	}

	
	/** 
     * Constructs a default filename from the current date and time.
     * @return the default filename for the current time.
     */
    private static String defaultLogFilename() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
        return DEFAULT_LOG_PREFIX + sdf.format(d) + ".txt";
    }
    
    /**
     * Main service initialization: called whenever a request is
     * made to start the Airboat service.  
     * 
     * This is where the vehicle implementation is started, sensors are 
     * registered, and the update loop and RPC server are started.   
     */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		// Ignore startup requests that don't include an intent
		if (intent == null) {
			Log.e(TAG, "Started with null intent.");
			return Service.START_STICKY;
		}
			
		// Ignore startup requests that don't include a device name
		if (!intent.hasExtra(BD_ADDR)) {
			Log.e(TAG, "Started with no bluetooth address.");
			return Service.START_STICKY;
		}
			
		// Ensure that we do not reinitialize if not necessary
		if (_airboatImpl != null || _udpServer != null) {
			Log.w(TAG, "Attempted to start while running.");
			return Service.START_STICKY;
		}

		// start tracing to "/sdcard/trace_crw.trace"
		//Debug.startMethodTracing("trace_crw");
		
		// Set up logging format to include time, tag, and value
        PropertyConfigurator.getConfigurator(this).configure();		    
		PatternFormatter formatter = new PatternFormatter(); 
		formatter.setPattern("%r %d %m %T");
		
		// Set up and register data logger to a date-stamped file
		String logFilename = defaultLogFilename();
		_fileAppender = new FileAppender();
		_fileAppender.setFileName(logFilename);
		_fileAppender.setAppend(true);
		_fileAppender.setFormatter(formatter);
		try {
			_fileAppender.open();
		} catch (IOException e) {
			Log.w(TAG, "Failed to open data log file: " + logFilename, e);
			sendNotification("Failed to open log: " + e.getMessage());
		}
	    logger.addAppender(_fileAppender);

		// Hook up to necessary Android sensors
        SensorManager sm; 
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor compass  = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener(magneticListener, compass, SensorManager.SENSOR_DELAY_NORMAL);
        Sensor accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        
        // Hook up to the GPS system
        LocationManager gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setPowerRequirement(Criteria.NO_REQUIREMENT);
        String provider = gps.getBestProvider(c, false);
        gps.requestLocationUpdates(provider, GPS_UPDATE_RATE, 0, locationListener);
		
        // Get necessary connection parameters
		_arduinoAddr = intent.getStringExtra(BD_ADDR);
		
		// Check if the provided ROS master URI parameter can be parsed
		String rosMasterStr = (intent.hasExtra(UDP_REGISTRY_ADDR) ? intent.getStringExtra(UDP_REGISTRY_ADDR) : getString(R.string.master_default_addr));
		_udpRegistryAddr = CrwNetworkUtils.toInetSocketAddress(rosMasterStr);
		if (_udpRegistryAddr == null) {
			logger.warn("Unable to parse " + rosMasterStr + " into UDP address.");
			sendNotification("Registry address invalid: " + rosMasterStr);
			stopSelf();
		}

        // Create a filter that listens to Amarino connection events
        IntentFilter amarinoFilter = new IntentFilter();
        amarinoFilter.addAction(AmarinoIntent.ACTION_CONNECTED_DEVICES);
        amarinoFilter.addAction(AmarinoIntent.ACTION_CONNECTED);
        amarinoFilter.addAction(AmarinoIntent.ACTION_DISCONNECTED);
        sendBroadcast(new Intent(AmarinoIntent.ACTION_GET_CONNECTED_DEVICES));
		
		// Create the data object
		_airboatImpl = new AirboatImpl(this, _arduinoAddr);
		registerReceiver(_airboatImpl.dataCallback, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		registerReceiver(_airboatImpl.connectionCallback, amarinoFilter);
		
		// Start up ROS processes in the background
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Create a RosVehicleServer to expose the data object
				try {
					_udpServer = new UdpVehicleService(_airboatImpl);
					_udpServer.addRegistry(_udpRegistryAddr);
				} catch (Exception e) {
					Log.e(TAG, "RosVehicleServer failed to launch", e);
					sendNotification("RosVehicleServer failed: " + e.getMessage());
					stopSelf();
					return;
				}
			}
		}).start();
		
		// Log the velocity gains before starting the service
		new Thread(new Runnable() {
			@Override
			public void run() {
				do {
					try { Thread.sleep(5000); } catch (InterruptedException ex) { }
					
					double[] velGains;
					if (_airboatImpl != null) {
						velGains = _airboatImpl.getGains(0);
						logger.info("PIDGAINS: " + "0 " + velGains[0] + "," + velGains[1] + "," + velGains[2]);
					}
					
					if (_airboatImpl != null) {
						velGains = _airboatImpl.getGains(5);
						logger.info("PIDGAINS: " + "5 " + velGains[0] + "," + velGains[1] + "," + velGains[2]);
					}
					
				} while (_airboatImpl == null);
			}
		}).start();

		// This is now a foreground service
		{
			// Set up the icon and ticker text
			int icon = R.drawable.icon; // TODO: change this to notification icon
			CharSequence tickerText = "Running normally.";
			long when = System.currentTimeMillis();
			
			// Set up the actual title and text
			Context context = getApplicationContext();
			CharSequence contentTitle = "Airboat Server";
			CharSequence contentText = tickerText;
			Intent notificationIntent = new Intent(this, AirboatActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		
			// Add a notification to the menu
			Notification notification = new Notification(icon, tickerText, when);
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		    startForeground(SERVICE_ID, notification);
		}
		
		// Indicate that the service should not be stopped arbitrarily
		Log.i(TAG,"AirboatService started.");
		return Service.START_STICKY;
	}
	
	/**
	 * Called when there are no longer any consumers of the service and 
	 * stopService has been called.
	 * 
	 * This is where the RPC server and update loops are killed, the sensors 
	 * are unregistered, and the current vehicle implementation is unhooked 
	 * from all of its callbacks and discarded (allowing safe spawning of a new
	 * implementation when the service is restarted).
	 */
	@Override
	public void onDestroy() {
		// stop tracing to "/sdcard/trace_crw.trace"
		Debug.stopMethodTracing();
		
		// Shutdown the ROS services
		if (_udpServer != null) {
			try {
				_udpServer.shutdown();
			} catch (Exception e) {
				Log.e(TAG, "RosVehicleServer shutdown error", e);
			}
			_udpServer = null;
		}
		
		// Disconnect from the Android sensors
        SensorManager sm; 
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.unregisterListener(magneticListener);
        sm.unregisterListener(accelerometerListener);
		
        // Disconnect from GPS updates
        LocationManager gps;
        gps = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gps.removeUpdates(locationListener);
        
		// Disconnect the data object from this service
		if (_airboatImpl != null) {
			unregisterReceiver(_airboatImpl.dataCallback);
			unregisterReceiver(_airboatImpl.connectionCallback);
			_airboatImpl.setConnected(false);
			_airboatImpl.shutdown();
			_airboatImpl = null;
		}

		// Remove the data log (a new one will be created on restart)
		if (_fileAppender != null) {
			logger.removeAppender(_fileAppender);
			try {
				_fileAppender.close();
			} catch (IOException e) {
				Log.e(TAG, "Data log shutdown error", e);
			}
		}

		// Disable this as a foreground service
		stopForeground(true);
		
		Log.i(TAG, "AirboatService stopped.");
		isRunning = false;
		super.onDestroy();
	}
	
	public void sendNotification(CharSequence text) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager notificationManager = (NotificationManager)getSystemService(ns);

		// Set up the icon and ticker text
		int icon = R.drawable.icon; // TODO: change this to notification icon
		CharSequence tickerText = text;
		long when = System.currentTimeMillis();
	
		// Set up the actual title and text
		Context context = getApplicationContext();
		CharSequence contentTitle = "Airboat Server";
		CharSequence contentText = text;
		Intent notificationIntent = new Intent(this, AirboatService.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
	
		Notification notification = new Notification(icon, tickerText, when);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		notificationManager.notify(SERVICE_ID, notification);
	}
}
