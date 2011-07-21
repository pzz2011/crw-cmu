package edu.cmu.ri.crw;

import java.awt.Image;

import org.ros.message.crwlib_msgs.Utm;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.geometry_msgs.Pose;

public interface VehicleServer {
	
	public enum SensorType { ANALOG, DIGITAL, TE };
	public enum WaypointState { GOING, DONE };

	public void addStateListener(VehicleStateListener l);
	public void removeStateListener(VehicleStateListener l);
	public void setState(Pose state);
	public void setOrigin(Pose utm);
	public Pose getState();
	public Pose getOrigin();
	
	public void addImageListener(VehicleImageListener l);
	public void removeImageListener(VehicleImageListener l);
	public void startCamera(int numFrames, double interval, int width, int height);
	public void stopCamera();
	public Image captureImage(int width, int height);
	
	public void addSensorListener(int channel, VehicleSensorListener s);
	public void removeSensorListener(int channel, VehicleSensorListener s);
	public void setSensorType(int channel, SensorType type);
	public SensorType getSensorType(int channel);
	public int getNumSensors();
	
	public void setPID(int axis, double[] gains);
	public double[] getPID(int axis);
	
	public void startWaypoint(Utm waypoint);
	public void stopWaypoint();
	public Pose getWaypoint();
	public WaypointState getWaypointStatus(); 
	
	public boolean setVelocity(double[] velocity);
}
