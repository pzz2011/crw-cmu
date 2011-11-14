package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CameraListener;
import edu.cmu.ri.crw.FunctionObserver;
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
import edu.cmu.ri.crw.udp.UdpServer.Request;
import edu.cmu.ri.crw.udp.UdpServer.Response;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

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
public class UdpVehicleServer implements AsyncVehicleServer, UdpServer.RequestHandler {
    private static final Logger logger = Logger.getLogger(UdpVehicleService.class.getName());

    protected final UdpServer _server;
    protected SocketAddress _vehicleServer;

    final Timer _registrationTimer = new Timer(true);
    final ConcurrentHashMap<Long, FunctionObserver> _tickets = new ConcurrentHashMap<Long, FunctionObserver>();
    final AtomicLong _ticketCounter = new AtomicLong(new Random().nextLong() << 32); // Start ticket with random offset to prevent collisions across multiple clients

    protected final Map<Integer, List<SensorListener>> _sensorListeners = new TreeMap<Integer, List<SensorListener>>();
    protected final List<ImageListener> _imageListeners = new ArrayList<ImageListener>();
    protected final List<VelocityListener> _velocityListeners = new ArrayList<VelocityListener>();
    protected final List<PoseListener> _stateListeners = new ArrayList<PoseListener>();
    protected final List<CameraListener> _cameraListeners = new ArrayList<CameraListener>();
    protected final List<WaypointListener> _waypointListeners = new ArrayList<WaypointListener>();

    public UdpVehicleServer() {
        // Create a UDP server that will handle RPC
        _server = new UdpServer();
        _server.setHandler(this);
        _server.start();

        // Start a task to periodically register for stream updates
        _registrationTimer.scheduleAtFixedRate(new RegistrationTask(), 0, UdpConstants.REGISTRATION_RATE_MS);
    }

    private void registerListener(List listenerList, UdpConstants.COMMAND registerCommand) {
        synchronized(listenerList) {
            if (!listenerList.isEmpty()) {
                try {
                    Response response = new Response(UdpConstants.NO_TICKET, _vehicleServer);
                    response.stream.writeUTF(registerCommand.str);
                    _server.send(response);
                } catch (IOException e) {
                    // TODO: not sure what to do here?
                    System.err.println("HELP: IMPLEMENTATION NOT COMPLETE: WHAT DO I DO?");
                }
            }
        }
    }

    private class RegistrationTask extends TimerTask {
        @Override
        public void run() {
            registerListener(_imageListeners, UdpConstants.COMMAND.CMD_REGISTER_IMAGE_LISTENER);
            registerListener(_velocityListeners, UdpConstants.COMMAND.CMD_REGISTER_VELOCITY_LISTENER);
            registerListener(_stateListeners, UdpConstants.COMMAND.CMD_REGISTER_STATE_LISTENER);
            registerListener(_cameraListeners, UdpConstants.COMMAND.CMD_REGISTER_CAMERA_LISTENER);
            registerListener(_waypointListeners, UdpConstants.COMMAND.CMD_REGISTER_WAYPOINT_LISTENER);

            synchronized (_sensorListeners) {
                for (Integer i : _sensorListeners.keySet()) {
                    try {
                        Response response = new Response(UdpConstants.NO_TICKET, _vehicleServer);
                        response.stream.writeUTF(UdpConstants.COMMAND.CMD_REGISTER_SENSOR_LISTENER.str);
                        response.stream.writeInt(i);
                        _server.send(response);
                    } catch (IOException e) {
                        // TODO: not sure what to do here?
                        System.err.println("HELP: IMPLEMENTATION NOT COMPLETE: WHAT DO I DO?");
                    }
                }
            }
        }
    }
    
    @Override
    public void received(Request req) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void timeout(long ticket, SocketAddress destination) {
        FunctionObserver obs = _tickets.get(ticket);
        if (obs != null) {
            obs.failed(FunctionObserver.FunctionError.TIMEOUT);
        }
    }
    
    @Override
    public void addStateListener(PoseListener l, FunctionObserver<Void> obs) {
        synchronized (_stateListeners) {
            _stateListeners.add(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void removeStateListener(PoseListener l, FunctionObserver<Void> obs) {
        synchronized (_stateListeners) {
            _stateListeners.remove(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void setState(UtmPose state, FunctionObserver<Void> obs) {
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_SET_STATE.str);
            UdpConstants.writePose(response.stream, state);
            _server.send(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getState(FunctionObserver<UtmPose> obs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addImageListener(ImageListener l, FunctionObserver<Void> obs) {
        synchronized (_imageListeners) {
            _imageListeners.add(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void removeImageListener(ImageListener l, FunctionObserver<Void> obs) {
        synchronized (_imageListeners) {
            _imageListeners.remove(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void captureImage(int width, int height, FunctionObserver<byte[]> obs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addCameraListener(CameraListener l, FunctionObserver<Void> obs) {
        synchronized (_cameraListeners) {
            _cameraListeners.add(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void removeCameraListener(CameraListener l, FunctionObserver<Void> obs) {
        synchronized (_cameraListeners) {
            _cameraListeners.remove(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
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
        synchronized (_sensorListeners) {
            // If there were no previous listeners for the channel, create a list
            if (!_sensorListeners.containsKey(channel)) {
                _sensorListeners.put(channel, new ArrayList<SensorListener>());
            }

            // Add the listener to the appropriate list
            _sensorListeners.get(channel).add(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void removeSensorListener(int channel, SensorListener l, FunctionObserver<Void> obs) {
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
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void setSensorType(int channel, SensorType type, FunctionObserver<Void> obs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getSensorType(int channel, FunctionObserver<Void> obs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void getNumSensors(FunctionObserver<Integer> obs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addVelocityListener(VelocityListener l, FunctionObserver<Void> obs) {
        synchronized (_velocityListeners) {
            _velocityListeners.add(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void removeVelocityListener(VelocityListener l, FunctionObserver<Void> obs) {
        synchronized (_velocityListeners) {
            _velocityListeners.remove(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
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
        synchronized (_waypointListeners) {
            _waypointListeners.add(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
    }

    @Override
    public void removeWaypointListener(WaypointListener l, FunctionObserver<Void> obs) {
        synchronized (_waypointListeners) {
            _waypointListeners.remove(l);
        }
        if (obs != null) {
            obs.completed(null);
        }
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
}
