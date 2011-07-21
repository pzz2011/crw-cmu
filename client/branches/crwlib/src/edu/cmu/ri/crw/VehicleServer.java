package edu.cmu.ri.crw;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

public interface VehicleServer {
	
	public enum SensorType { ANALOG, DIGITAL, TE };
	public enum WaypointState { GOING, DONE };

	public void addStateListener(VehicleStateListener l);
	public void removeStateListener(VehicleStateListener l);
	public void setState(UtmPose state);
	public UtmPoseWithCovarianceStamped getState();
	
	public void addImageListener(VehicleImageListener l);
	public void removeImageListener(VehicleImageListener l);
	public void startCamera(int numFrames, double interval, int width, int height);
	public void stopCamera();
	public CompressedImage captureImage(int width, int height);
	
	public void addSensorListener(int channel, VehicleSensorListener l);
	public void removeSensorListener(int channel, VehicleSensorListener l);
	public void setSensorType(int channel, SensorType type);
	public SensorType getSensorType(int channel);
	public int getNumSensors();
	
	public void addVelocityListener(VehicleVelocityListener l);
	public void removeVelocityListener(VehicleVelocityListener l);
	public void setVelocity(Twist velocity);
	public TwistWithCovarianceStamped getVelocity();
	
	public void startWaypoint(UtmPose waypoint);
	public void stopWaypoint();
	public UtmPose getWaypoint();
	public WaypointState getWaypointStatus();
	
	public void setPID(int axis, double[] gains);
	public double[] getPID(int axis);
}
