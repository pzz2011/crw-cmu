package edu.cmu.ri.crw;

import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public abstract class AbstractVehicleServer implements VehicleServer {

    protected double[][] _gains = new double[6][3];
    protected final Map<Integer, List<VehicleSensorListener>> _sensorListeners = new TreeMap<Integer, List<VehicleSensorListener>>();
    protected final List<VehicleImageListener> _imageListeners = new ArrayList<VehicleImageListener>();
    protected final List<VehicleVelocityListener> _velocityListeners = new ArrayList<VehicleVelocityListener>();
    protected final List<VehicleStateListener> _stateListeners = new ArrayList<VehicleStateListener>();

    public double[] getGains(int axis) {
        if (axis < 0 || axis >= _gains.length) {
            return new double[0];
        }

        // Make a copy of the current state (for immutability) and return it
        double[] gains = new double[_gains[axis].length];
        System.arraycopy(_gains[axis], 0, gains, 0, _gains[axis].length);
        return gains;
    }

    public void setGains(int axis, double[] gains) {
        if (axis < 0 || axis >= _gains.length) {
            return;
        }

        // Make a copy of the provided state (for immutability)
        System.arraycopy(gains, 0, _gains[axis], 0, Math.min(gains.length, _gains[axis].length));
    }

    public void addStateListener(VehicleStateListener l) {
        synchronized (_stateListeners) {
            _stateListeners.add(l);
        }
    }

    public void removeStateListener(VehicleStateListener l) {
        synchronized (_stateListeners) {
            _stateListeners.remove(l);
        }
    }

    protected void sendState(UtmPose pose) {
        // Process the listeners last to first, notifying
        // those that are interested in this event
        synchronized (_stateListeners) {
            for (VehicleStateListener l : _stateListeners) {
                l.receivedState(pose);
            }
        }
    }

    public void addImageListener(VehicleImageListener l) {
        synchronized (_imageListeners) {
            _imageListeners.add(l);
        }
    }

    public void removeImageListener(VehicleImageListener l) {
        synchronized (_imageListeners) {
            _imageListeners.remove(l);
        }
    }

    protected static byte[] toCompressedImage(RenderedImage image) {
        // This might be inefficient, but it is far more inefficient to
        // uncompress hardware-compressed JPEG images on Android.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpeg", buffer);
            return buffer.toByteArray();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    protected void sendImage(byte[] image) {
        synchronized (_imageListeners) {
            for (VehicleImageListener l : _imageListeners) {
                l.receivedImage(image);
            }
        }
    }

    public void addSensorListener(int channel, VehicleSensorListener l) {
        synchronized (_sensorListeners) {
            // If there were no previous listeners for the channel, create a list
            if (!_sensorListeners.containsKey(channel)) {
                _sensorListeners.put(channel, new ArrayList<VehicleSensorListener>());
            }

            // Add the listener to the appropriate list
            _sensorListeners.get(channel).add(l);
        }
    }

    public void removeSensorListener(int channel, VehicleSensorListener l) {
        synchronized (_sensorListeners) {
            // If there is no list of listeners, there is nothing to remove
            if (!_sensorListeners.containsKey(channel)) {
                return;
            }

            // Remove the listener from the appropriate list
            _sensorListeners.get(channel).remove(l);

            // If there are no more listeners for the channel, delete the list
            if (_sensorListeners.get(channel).isEmpty()) {
                _sensorListeners.remove(channel);
            }
        }
    }

    protected void sendSensor(int channel, SensorData reading) {
        synchronized (_sensorListeners) {
            // If there is no list of listeners, there is nothing to notify
            if (!_sensorListeners.containsKey(channel)) {
                return;
            }

            // Notify each listener in the appropriate list
            for (VehicleSensorListener l : _sensorListeners.get(channel)) {
                l.receivedSensor(reading);
            }
        }
    }

    public void addVelocityListener(VehicleVelocityListener l) {
        synchronized (_velocityListeners) {
            _velocityListeners.add(l);
        }
    }

    public void removeVelocityListener(VehicleVelocityListener l) {
        synchronized (_velocityListeners) {
            _velocityListeners.remove(l);
        }
    }

    protected void sendVelocity(Twist velocity) {
        synchronized (_velocityListeners) {
            for (VehicleVelocityListener l : _velocityListeners) {
                l.receivedVelocity(velocity);
            }
        }
    }
}
