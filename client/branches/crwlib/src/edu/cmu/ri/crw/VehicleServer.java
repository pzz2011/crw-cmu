package edu.cmu.ri.crw;

import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.geometry_msgs.Twist;
import org.ros.message.geometry_msgs.TwistWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

public interface VehicleServer {
	
	public enum SensorType { ANALOG, DIGITAL, TE };
	public enum WaypointState { GOING, DONE };
	public enum CameraState { CAPTURING, OFF };

	public void addStateListener(VehicleStateListener l);
	public void removeStateListener(VehicleStateListener l);
	public void setState(UtmPose state);
	public UtmPoseWithCovarianceStamped getState(); // TODO: add service call
	
	public void addImageListener(VehicleImageListener l);
	public void removeImageListener(VehicleImageListener l);
	public void startCamera(long numFrames, double interval, int width, int height, ImagingObserver obs);
	public void stopCamera();
	public CompressedImage captureImage(int width, int height); // TODO: add service call
	public CameraState getCameraStatus(); // TODO: add service call
	
	public void addSensorListener(int channel, VehicleSensorListener l);
	public void removeSensorListener(int channel, VehicleSensorListener l);
	public void setSensorType(int channel, SensorType type); // TODO: add service call
	public SensorType getSensorType(int channel); // TODO: add service call
	public int getNumSensors(); // TODO: add service call
	
	public void addVelocityListener(VehicleVelocityListener l);
	public void removeVelocityListener(VehicleVelocityListener l);
	public void setVelocity(Twist velocity);
	public TwistWithCovarianceStamped getVelocity(); // TODO: add service call
	
	public boolean isAutonomous(); // TODO: add service call
	public void setAutonomous(boolean auto); // TODO: add service call
	
	public void startWaypoint(UtmPose waypoint, WaypointObserver obs);
	public void stopWaypoint();
	public UtmPose getWaypoint(); // TODO: add service call
	public WaypointState getWaypointStatus(); // TODO: add service call
	
	public void setPID(int axis, double[] gains); // TODO: add service call
	public double[] getPID(int axis); // TODO: add service call
}
