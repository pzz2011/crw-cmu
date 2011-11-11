package edu.cmu.ri.crw;

import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;

/**
 * A variant of VehicleServer in which methods are asynchronous, and allows the
 * registration of observers that represent their outcomes.
 *
 * @see VehicleServer
 * 
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public interface AsyncVehicleServer {
	
	public void addStateListener(PoseListener l, FunctionObserver<Void> obs);
	public void removeStateListener(PoseListener l, FunctionObserver<Void> obs);
	public void setState(UtmPose state, FunctionObserver<Void> obs);
	public void getState(FunctionObserver<UtmPose> obs);
	
	public void addImageListener(ImageListener l, FunctionObserver<Void> obs);
	public void removeImageListener(ImageListener l, FunctionObserver<Void> obs);
        public void captureImage(int width, int height, FunctionObserver<byte[]> obs);
        
        public void addCameraListener(CameraListener l, FunctionObserver<Void> obs);
	public void removeCameraListener(CameraListener l, FunctionObserver<Void> obs);
	public void startCamera(long numFrames, double interval, int width, int height, FunctionObserver<Void> obs);
	public void stopCamera(FunctionObserver<Void> obs);
	public void getCameraStatus(FunctionObserver<CameraState> obs);
	
	public void addSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs);
	public void removeSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs);
	public void setSensorType(int channel, SensorType type, FunctionObserver<Void> obs);
	public void getSensorType(int channel, FunctionObserver<Void> obs);
	public void getNumSensors(FunctionObserver<Integer> obs);
	
	public void addVelocityListener(VelocityListener l, FunctionObserver<Void> obs);
	public void removeVelocityListener(VelocityListener l, FunctionObserver<Void> obs);
	public void setVelocity(Twist velocity, FunctionObserver<Void> obs);
	public void getVelocity(FunctionObserver<Twist> obs);
	
        public void addWaypointListener(WaypointListener l, FunctionObserver<Void> obs);
	public void removeWaypointListener(WaypointListener l, FunctionObserver<Void> obs);
	public void startWaypoints(UtmPose[] waypoint, String controller, FunctionObserver<Void> obs);
	public void stopWaypoints(FunctionObserver<Void> obs);
	public void getWaypoints(FunctionObserver<UtmPose[]> obs);
	public void getWaypointStatus(FunctionObserver<WaypointState> obs);
	
        public void isConnected(FunctionObserver<Boolean> obs);
        public void isAutonomous(FunctionObserver<Boolean> obs);
	public void setAutonomous(boolean auto, FunctionObserver<Void> obs);
        
	public void setGains(int axis, double[] gains, FunctionObserver<Void> obs);
	public void getGains(int axis, FunctionObserver<double[]> obs);

        /**
         * Utility class for handling AsyncVehicleServer objects.
         */
        public static class Util {
            /**
             * Converts VehicleServer implementation into asynchronous implementation.
             *
             * @param server the synchronous vehicle server implementation that will be wrapped
             * @return an asynchronous vehicle server using the specified implementation
             */
            public AsyncVehicleServer toAsyncVehicleServer(VehicleServer server) {
                throw new UnsupportedOperationException("Not implemented yet.");
            }

            /**
             * Converts AsyncVehicleServer implementation into synchronous implementation.
             *
             * @param server the asynchronous vehicle server implementation that will be wrapped
             * @return a synchronous vehicle server using the specified implementation
             */
            public VehicleServer toVehicleServer(AsyncVehicleServer server) {
                throw new UnsupportedOperationException("Not implemented yet.");
            }
        }
}
