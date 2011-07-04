package edu.cmu.ri.crw;

/**
 * A simple simulation of an unmanned boat.
 * 
 * The vehicle is fixed on the ground (Z = 0.0), and can only turn along the 
 * Z-axis and move along the X-axis (a unicycle motion model).  Imagery and
 * sensor are simulated using simple artificial generator functions that produce
 * recognizable basic patterns.  
 * 
 * @author pkv
 *
 */
public class SimpleBoatSimulator extends AbstractVehicleServer {

	@Override
	public Object captureImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPID(double axis) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getSensorType(int channel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getWaypoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPID(double axis, double[] gains) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSensorType(int channel, Object type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setState(Object p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startCamera() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startWaypoint(Object waypoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopCamera() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopWaypoint() {
		// TODO Auto-generated method stub
		
	}

}
