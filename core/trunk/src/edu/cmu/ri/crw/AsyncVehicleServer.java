package edu.cmu.ri.crw;

import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.util.concurrent.Future;

/**
 * A variant of VehicleServer in which methods are asynchronous, and return
 * futures that represent their outcomes.
 *
 * @see VehicleServer
 * @see Future
 * 
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public interface AsyncVehicleServer {
	
	public Future<Void> addStateListener(PoseListener l);
	public Future<Void> removeStateListener(PoseListener l);
	public Future<Void> setState(UtmPose state);
	public Future<UtmPose> getState();
	
	public Future<Void> addImageListener(ImageListener l);
	public Future<Void> removeImageListener(ImageListener l);
        public Future<byte[]> captureImage(int width, int height);
        
        public Future<Void> addCameraListener(CameraListener l);
	public Future<Void> removeCameraListener(CameraListener l);
	public Future<Void> startCamera(long numFrames, double interval, int width, int height);
	public Future<Void> stopCamera();
	public Future<CameraState> getCameraStatus();
	
	public Future<Void> addSensorListener(int channel, SensorListener l);
	public Future<Void> removeSensorListener(int channel, SensorListener l);
	public Future<Void> setSensorType(int channel, SensorType type);
	public Future<SensorType> getSensorType(int channel);
	public Future<Integer> getNumSensors();
	
	public Future<Void> addVelocityListener(VelocityListener l);
	public Future<Void> removeVelocityListener(VelocityListener l);
	public Future<Void> setVelocity(Twist velocity);
	public Future<Twist> getVelocity();
	
        public Future<Void> addWaypointListener(WaypointListener l);
	public Future<Void> removeWaypointListener(WaypointListener l);
	public Future<Void> startWaypoints(UtmPose[] waypoint, String controller);
	public Future<Void> stopWaypoints();
	public Future<UtmPose[]> getWaypoints();
	public Future<WaypointState> getWaypointStatus();
	
        public Future<Boolean> isConnected();
        public Future<Boolean> isAutonomous();
	public Future<Void> setAutonomous(boolean auto);
        
	public Future<Void> setGains(int axis, double[] gains);
	public Future<double[]> getGains(int axis);
}
