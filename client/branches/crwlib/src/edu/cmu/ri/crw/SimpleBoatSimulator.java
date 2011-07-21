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

/**
 * A simple simulation of an unmanned boat.
 * 
 * The vehicle is fixed on the ground (Z = 0.0), and can only turn along the 
 * Z-axis and move along the X-axis (a unicycle motion model).  Imagery and
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
	public final UtmPose _state = new UtmPose();
	public UtmPose _waypoint = null;
	
	private volatile boolean _isCapturing = false;
	private volatile boolean _isNavigating = false;
	
	public SimpleBoatSimulator() { }

	@Override
	public CompressedImage captureImage(int width, int height) {
		
		// Create an image and fill it with a random color
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = (Graphics2D)image.getGraphics();
		graphics.setPaint(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
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
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (_isNavigating) {

					// Every so often, update boat position 
					_state.pose.position.x += 1.0;
					_state.pose.position.y += 1.0;
					_state.pose.position.z += 1.0;
					
					if (obs != null) {
						obs.waypointUpdate(SimpleBoatSimulator.this);
					}
					
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
	public void stopWaypoint() {
		// Stop the thread that is doing the "navigation"
		_isNavigating = false;
		_waypoint = null;
	}
	
	@Override
	public void startCamera(final int numFrames, final double interval, final int width, final int height, final ImagingObserver obs) {
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
						obs.imagingUpdate(SimpleBoatSimulator.this);
					}
					
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

	public WaypointState getWaypointStatus()
	{
		//if (distToGoal(state, waypoint) > 0.5) {
		return WaypointState.GOING;
	}
	
	double distToGoal(Pose x, Pose y)
	{
		double x1=x.position.x, x2=x.position.y, x3=x.position.z;
		double y1=y.position.x, y2=y.position.y, y3=y.position.z;
		
		return Math.sqrt(Math.pow(x1-y1, 2)+Math.pow(x2-y2,2)+Math.pow(x3-y3, 2));
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
		// TODO: do something useful here
	}

	@Override
	public TwistWithCovarianceStamped getVelocity() {
		return new TwistWithCovarianceStamped();
	}
}
