package edu.cmu.ri.crw;

import java.awt.Image;

public interface VehicleServer {
	
	public enum SensorType { ANALOG, DIGITAL, TE };
	public enum WaypointState { GOING, DONE };

	public void addStateListener(VehicleStateListener l);
	public void removeStateListener(VehicleStateListener l);
	public void setState(double[] state);
	public void setOrigin(UTM utm);
	public double[] getState();
	public UTM getOrigin();
	
	public void addImageListener(VehicleImageListener l);
	public void removeImageListener(VehicleImageListener l);
	public void startCamera(double interval, int width, int height);
	public void stopCamera();
	public Image captureImage(int width, int height);
	
	public void addSensorListener(int channel, VehicleSensorListener s);
	public void removeSensorListener(int channel, VehicleSensorListener s);
	public void setSensorType(int channel, SensorType type);
	public SensorType getSensorType(int channel);
	public int getNumSensors();
	
	public void setPID(int axis, double[] gains);
	public double[] getPID(int axis);
	
	public void startWaypoint(UTM waypoint);
	public void stopWaypoint();
	public UTM getWaypoint();
	public WaypointState getWaypointStatus(); 
}
