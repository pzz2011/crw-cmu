package edu.cmu.ri.crw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import org.ros.message.crwlib_msgs.Utm;
import org.ros.message.geometry_msgs.Pose;

import edu.cmu.ri.crw.AbstractVehicleServer;

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
	public final double[][] _gains = new double[6][];
	public final double[] _state = new double[7];
	public Pose _waypoint = null;
	public Pose _origin = new Pose(); // I just picked some random zone!
	
	private volatile boolean _isCapturing = false;
	private volatile boolean _isNavigating = false;
	
	public SimpleBoatSimulator() { }

	@Override
	public Image captureImage(int width, int height) {
		
		// Create an image and fill it with a random color
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D graphics = (Graphics2D)image.getGraphics();
		graphics.setPaint(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		
		return image;
	}

	@Override
	public double[] getPID(int axis) {
		// Make a copy of the current state (for immutability) and return it
		double[] gains = new double[_gains[axis].length];
		System.arraycopy(_gains, 0, gains[axis], 0, _gains[axis].length);
		return gains;
	}

	@Override
	public SensorType getSensorType(int channel) {
		return _sensorTypes[channel];
	}

	@Override
	public Pose getWaypoint() {
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
	public void setState(Pose state) {
		/*for (int i = 0; i < _state.length; ++i) {
			_state[i] = state[i];
		}*/
	}

	@Override
	public void startWaypoint(Utm waypoint) {
		_isNavigating = true;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (_isNavigating) {

					// Every so often, update boat position 
					_state[0] += 1.0;
					_state[1] += 1.0;
					_state[2] += 1.0;
					
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
	public void startCamera(final int numFrames, final double interval, final int width, final int height) {
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
	public Pose getOrigin() {
		return _origin;
	}

	@Override
	public Pose getState() {
		// Make a copy of the current state (for immutability) and return it
		double[] state = new double[7];
		System.arraycopy(_state, 0, state, 0, _state.length);
		return null;
	}

	@Override
	public void setOrigin(Pose Pose) {
		_origin = Pose;
	}

	@Override
	public int getNumSensors() {
		//TODO Fix this
		return 0;
	}

	@Override
	public boolean setVelocity(double[] velocity) {
		// TODO Auto-generated method stub
		return false;
	}

}
