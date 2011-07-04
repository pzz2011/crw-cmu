package edu.cmu.ri.crw;

// TODO: add data structures to this class once they are generated 

public interface VehicleServer {

	public void addStateListener(VehicleStateListener l);
	public void removeStateListener(VehicleStateListener l);
	public void setState(Object p);
	
	public void addImageListener(VehicleImageListener l);
	public void removeImageListener(VehicleImageListener l);
	public void startCamera();
	public void stopCamera();
	public Object captureImage();
	
	public void addSensorListener(int channel, VehicleSensorListener s);
	public void removeSensorListener(int channel, VehicleSensorListener s);
	public void setSensorType(int channel, Object type);
	public Object getSensorType(int channel);
	
	public void setPID(double axis, double[] gains);
	public Object getPID(double axis);
	
	public void startWaypoint(Object waypoint);
	public void stopWaypoint();
	public Object getWaypoint();
}
