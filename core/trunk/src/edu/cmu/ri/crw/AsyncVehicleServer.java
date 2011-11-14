package edu.cmu.ri.crw;

import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.util.concurrent.CountDownLatch;

/**
 * A variant of VehicleServer in which methods are asynchronous, and allows the
 * registration of observers that represent their outcomes.
 *
 * @see VehicleServer
 * 
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public interface AsyncVehicleServer {
	
	public void addPoseListener(PoseListener l, FunctionObserver<Void> obs);
	public void removePoseListener(PoseListener l, FunctionObserver<Void> obs);
	public void setPose(UtmPose pose, FunctionObserver<Void> obs);
	public void getPose(FunctionObserver<UtmPose> obs);
	
	public void addImageListener(ImageListener l, FunctionObserver<Void> obs);
	public void removeImageListener(ImageListener l, FunctionObserver<Void> obs);
        public void captureImage(int width, int height, FunctionObserver<byte[]> obs);
        
        public void addCameraListener(CameraListener l, FunctionObserver<Void> obs);
	public void removeCameraListener(CameraListener l, FunctionObserver<Void> obs);
	public void startCamera(int numFrames, double interval, int width, int height, FunctionObserver<Void> obs);
	public void stopCamera(FunctionObserver<Void> obs);
	public void getCameraStatus(FunctionObserver<CameraState> obs);
	
	public void addSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs);
	public void removeSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs);
	public void setSensorType(int channel, SensorType type, FunctionObserver<Void> obs);
	public void getSensorType(int channel, FunctionObserver<SensorType> obs);
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
            public static AsyncVehicleServer toAsync(final VehicleServer server) {
                return new AsyncVehicleServer() {

                    @Override
                    public void addPoseListener(PoseListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void removePoseListener(PoseListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void setPose(UtmPose state, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getPose(FunctionObserver<UtmPose> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void addImageListener(ImageListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void removeImageListener(ImageListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void captureImage(int width, int height, FunctionObserver<byte[]> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void addCameraListener(CameraListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void removeCameraListener(CameraListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void startCamera(int numFrames, double interval, int width, int height, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void stopCamera(FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getCameraStatus(FunctionObserver<CameraState> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void addSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void removeSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void setSensorType(int channel, SensorType type, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getSensorType(int channel, FunctionObserver<SensorType> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getNumSensors(FunctionObserver<Integer> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void addVelocityListener(VelocityListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void removeVelocityListener(VelocityListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void setVelocity(Twist velocity, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getVelocity(FunctionObserver<Twist> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void addWaypointListener(WaypointListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void removeWaypointListener(WaypointListener l, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void startWaypoints(UtmPose[] waypoint, String controller, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void stopWaypoints(FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getWaypoints(FunctionObserver<UtmPose[]> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getWaypointStatus(FunctionObserver<WaypointState> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void isConnected(FunctionObserver<Boolean> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void isAutonomous(FunctionObserver<Boolean> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void setAutonomous(boolean auto, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void setGains(int axis, double[] gains, FunctionObserver<Void> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void getGains(int axis, FunctionObserver<double[]> obs) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }

            /**
             * Converts AsyncVehicleServer implementation into synchronous implementation.
             *
             * @param server the asynchronous vehicle server implementation that will be wrapped
             * @return a synchronous vehicle server using the specified implementation
             */
            public static VehicleServer toSync(final AsyncVehicleServer server) {
                return new VehicleServer() {

                    /**
                     * Simple delay class that blocks a synchronous function
                     * call until the backing asynchronous one completes.
                     */
                    class Delayer<V> implements FunctionObserver<V> {
                        final CountDownLatch _latch = new CountDownLatch(1);
                        private V _result = null;
                        
                        public V awaitResult() {
                            try { _latch.await(); } catch (InterruptedException e) {}
                            return _result;
                        }
                        
                        @Override
                        public void completed(V result) {
                            _latch.countDown();
                            _result = result;
                        }

                        @Override
                        public void failed(FunctionError cause) {
                            _latch.countDown();
                        }                        
                    }
                    
                    @Override
                    public void addPoseListener(PoseListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addPoseListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removePoseListener(PoseListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removePoseListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setPose(UtmPose state) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setPose(state, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public UtmPose getPose() {
                        final Delayer<UtmPose> delayer = new Delayer<UtmPose>();
                        server.getPose(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addImageListener(ImageListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addImageListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeImageListener(ImageListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeImageListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public byte[] captureImage(int width, int height) {
                        final Delayer<byte[]> delayer = new Delayer<byte[]>();
                        server.captureImage(width, height, delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addCameraListener(CameraListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addCameraListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeCameraListener(CameraListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeCameraListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void startCamera(int numFrames, double interval, int width, int height) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.startCamera(numFrames, interval, width, height, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void stopCamera() {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.stopCamera(delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public CameraState getCameraStatus() {
                        final Delayer<CameraState> delayer = new Delayer<CameraState>();
                        server.getCameraStatus(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addSensorListener(int channel, SensorListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addSensorListener(channel, l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeSensorListener(int channel, SensorListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeSensorListener(channel, l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setSensorType(int channel, SensorType type) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setSensorType(channel, type, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public SensorType getSensorType(int channel) {
                        final Delayer<SensorType> delayer = new Delayer<SensorType>();
                        server.getSensorType(channel, delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public int getNumSensors() {
                        final Delayer<Integer> delayer = new Delayer<Integer>();
                        server.getNumSensors(delayer);
                        Integer nSensors = delayer.awaitResult();
                        return (nSensors != null) ? nSensors : -1;
                    }

                    @Override
                    public void addVelocityListener(VelocityListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addVelocityListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeVelocityListener(VelocityListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeVelocityListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setVelocity(Twist velocity) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setVelocity(velocity, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public Twist getVelocity() {
                        final Delayer<Twist> delayer = new Delayer<Twist>();
                        server.getVelocity(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public void addWaypointListener(WaypointListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.addWaypointListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void removeWaypointListener(WaypointListener l) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.removeWaypointListener(l, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void startWaypoints(UtmPose[] waypoint, String controller) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.startWaypoints(waypoint, controller, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void stopWaypoints() {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.stopWaypoints(delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public UtmPose[] getWaypoints() {
                        final Delayer<UtmPose[]> delayer = new Delayer<UtmPose[]>();
                        server.getWaypoints(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public WaypointState getWaypointStatus() {
                        final Delayer<WaypointState> delayer = new Delayer<WaypointState>();
                        server.getWaypointStatus(delayer);
                        return delayer.awaitResult();
                    }

                    @Override
                    public boolean isConnected() {
                        final Delayer<Boolean> delayer = new Delayer<Boolean>();
                        server.isConnected(delayer);
                        Boolean b = delayer.awaitResult();
                        return (b != null) ? b : false;
                    }

                    @Override
                    public boolean isAutonomous() {
                        final Delayer<Boolean> delayer = new Delayer<Boolean>();
                        server.isAutonomous(delayer);
                        Boolean b = delayer.awaitResult();
                        return (b != null) ? b : false;
                    }

                    @Override
                    public void setAutonomous(boolean auto) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setAutonomous(auto, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public void setGains(int axis, double[] gains) {
                        final Delayer<Void> delayer = new Delayer<Void>();
                        server.setGains(axis, gains, delayer);
                        delayer.awaitResult();
                    }

                    @Override
                    public double[] getGains(int axis) {
                        final Delayer<double[]> delayer = new Delayer<double[]>();
                        server.getGains(axis, delayer);
                        return delayer.awaitResult();
                    }
                };
            }
        }
}
