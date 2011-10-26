package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;

// TODO: finish this class!
/**
 * A vehicle server proxy that uses UDP to connect to the vehicle and forwards
 * functions calls and events.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UdpVehicleServer extends AbstractVehicleServer {

    @Override
    public void setState(UtmPose state) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UtmPose getState() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] captureImage(int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startCamera(long numFrames, double interval, int width, int height) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopCamera() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CameraState getCameraStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSensorType(int channel, SensorType type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SensorType getSensorType(int channel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getNumSensors() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setVelocity(Twist velocity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Twist getVelocity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startWaypoints(UtmPose[] waypoint, String controller) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopWaypoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UtmPose[] getWaypoints() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WaypointState getWaypointStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAutonomous() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAutonomous(boolean auto) {
        throw new UnsupportedOperationException("Not supported yet.");
    }   
}
