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
import java.util.logging.Level;
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

    protected final UdpServer _udpServer;
    protected SocketAddress _vehicleServer;

    final Timer _timer = new Timer(true);
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
        _udpServer = new UdpServer();
        _udpServer.setHandler(this);
        _udpServer.start();

        // Start a task to periodically register for stream updates
        _timer.scheduleAtFixedRate(new RegistrationTask(), 0, UdpConstants.REGISTRATION_RATE_MS);
    }
    
    public UdpVehicleServer(SocketAddress addr) {
        this();
        _vehicleServer = addr;
    }
    
    public void shutdown() {
        _timer.cancel();
        _udpServer.stop();
    }
    
    public void setVehicleServer(SocketAddress addr) {
        _vehicleServer = addr;
    }
    
    public SocketAddress getVehicleServer() {
        return _vehicleServer;
    }

    private void registerListener(List listenerList, UdpConstants.COMMAND registerCommand) {
        synchronized(listenerList) {
            if (!listenerList.isEmpty()) {
                try {
                    Response response = new Response(UdpConstants.NO_TICKET, _vehicleServer);
                    response.stream.writeUTF(registerCommand.str);
                    _udpServer.send(response);
                } catch (IOException e) {
                    // TODO: should probably change state or something
                    logger.log(Level.WARNING, "Failed to transmit listener registration: {0}", registerCommand);
                }
            }
        }
    }

    private class RegistrationTask extends TimerTask {
        @Override
        public void run() {
            // Don't need to register if we don't know the server
            if (_vehicleServer == null)
                return;
            
            // Check the lists for listeners, register if there are any
            registerListener(_imageListeners, UdpConstants.COMMAND.CMD_REGISTER_IMAGE_LISTENER);
            registerListener(_velocityListeners, UdpConstants.COMMAND.CMD_REGISTER_VELOCITY_LISTENER);
            registerListener(_stateListeners, UdpConstants.COMMAND.CMD_REGISTER_STATE_LISTENER);
            registerListener(_cameraListeners, UdpConstants.COMMAND.CMD_REGISTER_CAMERA_LISTENER);
            registerListener(_waypointListeners, UdpConstants.COMMAND.CMD_REGISTER_WAYPOINT_LISTENER);

            // Special case to handle sensor listener channels
            synchronized (_sensorListeners) {
                for (Integer i : _sensorListeners.keySet()) {
                    try {
                        Response response = new Response(UdpConstants.NO_TICKET, _vehicleServer);
                        response.stream.writeUTF(UdpConstants.COMMAND.CMD_REGISTER_SENSOR_LISTENER.str);
                        response.stream.writeInt(i);
                        _udpServer.send(response);
                    } catch (IOException e) {
                        // TODO: should probably change state or something
                        logger.log(Level.WARNING, "Failed to transmit listener registration: {0}", 
                                UdpConstants.COMMAND.CMD_REGISTER_SENSOR_LISTENER.str);
                    }
                }
            }
        }
    }
    
    @Override
    public void received(Request req) {
        try {
            final String command = req.stream.readUTF();
            // TODO: remove me
            logger.log(Level.INFO, "Received command {0}:{1}", new Object[]{req.ticket, command});
            
            FunctionObserver obs = _tickets.get(req.ticket);
            if (obs == null) return;

            switch (UdpConstants.COMMAND.fromStr(command)) {
                case CMD_GET_STATE:
                    obs.completed(UdpConstants.readPose(req.stream));
                    break;
                case CMD_CAPTURE_IMAGE:
                    byte[] image = new byte[req.stream.readInt()];
                    req.stream.readFully(image);
                    obs.completed(req);
                    break;    
                case CMD_GET_CAMERA_STATUS:
                    obs.completed(CameraState.values()[req.stream.readByte()]);
                    break;
                case CMD_GET_SENSOR_TYPE:
                    obs.completed(SensorType.values()[req.stream.readByte()]);
                    break;
                case CMD_GET_NUM_SENSORS:
                    obs.completed(req.stream.readInt());
                    break;
                case CMD_GET_VELOCITY:
                    obs.completed(UdpConstants.readTwist(req.stream));
                    break;
                case CMD_GET_WAYPOINTS:
                    UtmPose[] poses = new UtmPose[req.stream.readInt()];
                    for (int i = 0; i < poses.length; ++i) {
                        poses[i] = UdpConstants.readPose(req.stream);
                    }
                    obs.completed(poses);
                    break;
                case CMD_GET_WAYPOINT_STATUS:
                    obs.completed(WaypointState.values()[req.stream.readByte()]);
                    break;
                case CMD_IS_CONNECTED:
                    obs.completed(req.stream.readBoolean());
                    break;
                case CMD_IS_AUTONOMOUS:
                    obs.completed(req.stream.readBoolean());
                    break;
                case CMD_GET_GAINS:
                    double[] gains = new double[req.stream.readInt()];
                    for (int i = 0; i < gains.length; ++i) {
                        gains[i] = req.stream.readDouble();
                    }
                    obs.completed(gains);
                case CMD_SET_STATE:
                case CMD_SET_SENSOR_TYPE:
                case CMD_SET_VELOCITY:
                case CMD_SET_AUTONOMOUS:
                case CMD_SET_GAINS:
                case CMD_START_CAMERA:
                case CMD_STOP_CAMERA:
                case CMD_START_WAYPOINTS:
                case CMD_STOP_WAYPOINTS:
                    obs.completed(null);
                    break;
                default:
                    logger.log(Level.WARNING, "Ignoring unknown command: {0}", command);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to parse request: {0}", req.ticket);
        }
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
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_SET_STATE.str);
            UdpConstants.writePose(response.stream, state);
            _udpServer.respond(response);

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
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_STATE.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
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
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_CAPTURE_IMAGE.str);
            response.stream.writeInt(width);
            response.stream.writeInt(height);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
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
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_START_CAMERA.str);
            response.stream.writeInt(numFrames);
            response.stream.writeDouble(interval);
            response.stream.writeInt(width);
            response.stream.writeInt(height);           
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void stopCamera(FunctionObserver<Void> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_STOP_CAMERA.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getCameraStatus(FunctionObserver<CameraState> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_CAMERA_STATUS.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
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
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_SET_SENSOR_TYPE.str);
            response.stream.writeInt(channel);
            response.stream.writeByte(type.ordinal());
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getSensorType(int channel, FunctionObserver<SensorType> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_SENSOR_TYPE.str);
            response.stream.writeInt(channel);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getNumSensors(FunctionObserver<Integer> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_NUM_SENSORS.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
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
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_SET_VELOCITY.str);
            UdpConstants.writeTwist(response.stream, velocity);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getVelocity(FunctionObserver<Twist> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_VELOCITY.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
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
    public void startWaypoints(UtmPose[] waypoints, String controller, FunctionObserver<Void> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_START_WAYPOINTS.str);
            response.stream.writeInt(waypoints.length);
            for (int i = 0; i < waypoints.length; ++i) {
                UdpConstants.writePose(response.stream, waypoints[i]);
            }
            response.stream.writeUTF(controller);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void stopWaypoints(FunctionObserver<Void> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_STOP_WAYPOINTS.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getWaypoints(FunctionObserver<UtmPose[]> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_WAYPOINTS.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getWaypointStatus(FunctionObserver<WaypointState> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_WAYPOINT_STATUS.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void isConnected(FunctionObserver<Boolean> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_IS_CONNECTED.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void isAutonomous(FunctionObserver<Boolean> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_IS_AUTONOMOUS.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void setAutonomous(boolean auto, FunctionObserver<Void> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_SET_AUTONOMOUS.str);
            response.stream.writeBoolean(auto);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void setGains(int axis, double[] gains, FunctionObserver<Void> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_SET_GAINS.str);
            response.stream.writeInt(axis);
            response.stream.writeInt(gains.length);
            for (int i = 0; i < gains.length; ++i) {
                response.stream.writeDouble(gains[i]);
            }
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }

    @Override
    public void getGains(int axis, FunctionObserver<double[]> obs) {
        if (_vehicleServer == null) {
            obs.failed(FunctionObserver.FunctionError.ERROR);
            return;
        }
        
        long ticket = (obs == null) ? UdpConstants.NO_TICKET : _ticketCounter.incrementAndGet();
        
        try {
            Response response = new Response(ticket, _vehicleServer);
            response.stream.writeUTF(UdpConstants.COMMAND.CMD_GET_GAINS.str);
            _udpServer.respond(response);

            if (obs != null)
                _tickets.put(ticket, obs);
        } catch (IOException e) {
            // TODO: Should I also flag something somewhere?
            if (obs != null)
                obs.failed(FunctionObserver.FunctionError.ERROR);
        }
    }
}
