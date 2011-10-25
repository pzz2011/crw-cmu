package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;

public interface VehicleServer {
	
	public enum SensorType { ANALOG, DIGITAL, TE, WATERCANARY, BATTERY, UNKNOWN };
	public enum WaypointState { GOING, DONE, CANCELLED, OFF, UNKNOWN };
	public enum CameraState { CAPTURING, DONE, CANCELLED, OFF, UNKNOWN };

	public void addStateListener(PoseListener l);
	public void removeStateListener(PoseListener l);
	public void setState(UtmPose state);
	public UtmPose getState();
	
	public void addImageListener(ImageListener l);
	public void removeImageListener(ImageListener l);
        public byte[] captureImage(int width, int height);
        
        public void addCameraListener(CameraListener l);
	public void removeCameraListener(CameraListener l);
	public void startCamera(long numFrames, double interval, int width, int height);
	public void stopCamera();
	public CameraState getCameraStatus();
	
	public void addSensorListener(int channel, SensorListener l);
	public void removeSensorListener(int channel, SensorListener l);
	public void setSensorType(int channel, SensorType type);
	public SensorType getSensorType(int channel);
	public int getNumSensors();
	
	public void addVelocityListener(VelocityListener l);
	public void removeVelocityListener(VelocityListener l);
	public void setVelocity(Twist velocity);
	public Twist getVelocity();
	
        public void addWaypointListener(WaypointListener l);
	public void removeWaypointListener(WaypointListener l);
	public void startWaypoints(UtmPose[] waypoint, String controller);
	public void stopWaypoints();
	public UtmPose[] getWaypoints();
	public WaypointState getWaypointStatus();
	
        public boolean isConnected();
        public boolean isAutonomous();
	public void setAutonomous(boolean auto);
        
	public void setGains(int axis, double[] gains);
	public double[] getGains(int axis);
}
