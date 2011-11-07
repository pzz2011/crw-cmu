package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CameraListener;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.PoseListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VelocityListener;
import edu.cmu.ri.crw.WaypointListener;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// TODO: finish this class!
/**
 * A vehicle server proxy that uses UDP to connect to the vehicle and forwards
 * functions calls and events.
 *
 * Implementation details:
 *
 * For stream communication, the server transmits a heartbeat packet
 * indicating that it should be notified for a given event stream.  If this
 * packet is lost, notifications will cease to be sent.
 *
 * For two-way calls, the server sends a packet to the server and puts the
 * future in a blocking queue and a hash map.  If the server gets
 * a response, the future's result is filled in and completed.  If not, the
 * future is taken out of the blocking queue and removed.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UdpVehicleServer implements AsyncVehicleServer {

    public static final int RETRY_RATE_MS = 1000;
    public static final int RETRY_COUNT = 3;
    public static final int TIMEOUT_NS = RETRY_RATE_MS * RETRY_COUNT * 1000;

    final DelayQueue<UdpFuture> _timeouts = new DelayQueue<UdpFuture>();
    final ConcurrentHashMap<Integer, UdpFuture> _tickets = new ConcurrentHashMap<Integer, UdpFuture>();

    /**
     * Implements a future that waits for a UDP response, timing out if no
     * such response is returned within the specified amount of time.
     *
     * @param <V>
     */
    private class UdpFuture<V> implements Future<V>, Delayed {

        private V _result = null;
        private boolean isCancelled = false;
        private boolean isDone = false;
        
        private final long _timeoutTime;
        private final int _ticket;

        public UdpFuture(int ticket, long timeout_ns) {
            _timeoutTime = System.nanoTime() + timeout_ns;
            _ticket = ticket;
        }

        @Override
        public synchronized boolean cancel(boolean mayInterruptIfRunning) {
            if (isCancelled || isDone || !mayInterruptIfRunning)
                return false;

            _result = null;
            isCancelled = true;
            isDone = false;
            _tickets.remove(_ticket);
            return true;
        }

        @Override
        public synchronized boolean isCancelled() {
            return isCancelled;
        }

        @Override
        public synchronized boolean isDone() {
            return isDone;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            synchronized(this) {
                while(!isCancelled && !isDone)
                    this.wait();
                return _result;
            }
            // TODO: better error handling
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            synchronized(this) {
                while(!isCancelled && !isDone)
                    this.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
                return _result;
            }
            // TODO: better error handling
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(_timeoutTime - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (o instanceof UdpFuture) {
                return Long.signum(this._timeoutTime - ((UdpFuture)o)._timeoutTime);
            } else {
                return Long.signum(this.getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS));
            }
        }
    }

    private int call(String name, Object... params) {
        return -1;
    }

    @Override
    public Future<Void> addStateListener(PoseListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> removeStateListener(PoseListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> setState(UtmPose state) {
        return new UdpFuture<Void>(call("setState", state), TIMEOUT_NS);
    }

    @Override
    public Future<UtmPose> getState() {
        return new UdpFuture<UtmPose>(call("getState"), TIMEOUT_NS);
    }

    @Override
    public Future<Void> addImageListener(ImageListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> removeImageListener(ImageListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<byte[]> captureImage(int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> addCameraListener(CameraListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> removeCameraListener(CameraListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> startCamera(long numFrames, double interval, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> stopCamera() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<CameraState> getCameraStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> addSensorListener(int channel, SensorListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> removeSensorListener(int channel, SensorListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> setSensorType(int channel, SensorType type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<SensorType> getSensorType(int channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Integer> getNumSensors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> addVelocityListener(VelocityListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> removeVelocityListener(VelocityListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> setVelocity(Twist velocity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Twist> getVelocity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> addWaypointListener(WaypointListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> removeWaypointListener(WaypointListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> startWaypoints(UtmPose[] waypoint, String controller) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> stopWaypoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<UtmPose[]> getWaypoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<WaypointState> getWaypointStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Boolean> isConnected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Boolean> isAutonomous() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> setAutonomous(boolean auto) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<Void> setGains(int axis, double[] gains) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Future<double[]> getGains(int axis) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
