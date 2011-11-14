package edu.cmu.ri.crw.udp;

// TODO: finish this class!

import edu.cmu.ri.crw.CameraListener;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.PoseListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VelocityListener;
import edu.cmu.ri.crw.WaypointListener;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpServer.Request;
import edu.cmu.ri.crw.udp.UdpServer.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service that registers a vehicle server over UDP to allow control over the 
 * network via a proxy server 
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UdpVehicleService implements UdpServer.RequestHandler {
    private static final Logger logger = Logger.getLogger(UdpVehicleService.class.getName());
    
    protected VehicleServer _vehicleServer;
    protected final Object _serverLock = new Object();
    
    protected final UdpServer _udpServer = new UdpServer();

    protected final List<SocketAddress> _registries = new ArrayList<SocketAddress>();
    protected final Map<SocketAddress, Integer> _stateListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Map<SocketAddress, Integer> _imageListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Map<SocketAddress, Integer> _cameraListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final ArrayList<Map<SocketAddress,Integer>> _sensorListeners = new ArrayList<Map<SocketAddress, Integer>>();
    protected final Map<SocketAddress, Integer> _velocityListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Map<SocketAddress, Integer> _waypointListeners = new LinkedHashMap<SocketAddress, Integer>();

    public UdpVehicleService() {
        _udpServer.setHandler(this);
        _udpServer.start();
    }
    
    public UdpVehicleService(VehicleServer server) {
        this();
        _vehicleServer = server;
    }
    
    public SocketAddress getSocketAddress() {
        return _udpServer.getSocketAddress();
    }
    
    public void setServer(VehicleServer server) {
        synchronized(_serverLock) {
            _vehicleServer = server;
        }
    }
    
    public VehicleServer getServer() {
        synchronized(_serverLock) {
            return _vehicleServer;
        }
    }
    
    public void addRegistry(InetSocketAddress addr) {
        synchronized(_registries) {
            _registries.add(addr);
        }
    }
    
    public void removeRegistry(InetSocketAddress addr) {
        synchronized(_registries) {
            _registries.remove(addr);
        }
    }
    
    public InetSocketAddress[] listRegistries() {
        synchronized(_registries) {
            return _registries.toArray(new InetSocketAddress[0]);
        }
    }

    @Override
    public void received(Request req) {

        try {
            final String command = req.stream.readUTF();
            Response resp = new Response(req);
            resp.stream.writeUTF(command);
            
            // TODO: remove me
            logger.log(Level.INFO, "Received command {0}:{1}", new Object[]{req.ticket, command});

            switch (UdpConstants.COMMAND.fromStr(command)) {
                case CMD_REGISTER_STATE_LISTENER:
                    synchronized(_stateListeners) {
                        _stateListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_SET_STATE:
                    _vehicleServer.setPose(UdpConstants.readPose(req.stream));
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_STATE:
                    UdpConstants.writePose(resp.stream, _vehicleServer.getPose());
                    _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_IMAGE_LISTENER:
                    synchronized(_imageListeners) {
                        _imageListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_CAPTURE_IMAGE:
                    byte[] image = _vehicleServer.captureImage(req.stream.readInt(), req.stream.readInt());
                    resp.stream.writeInt(image.length);
                    resp.stream.write(image);
                    _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_CAMERA_LISTENER:
                    synchronized(_cameraListeners) {
                        _cameraListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_START_CAMERA:
                    _vehicleServer.startCamera(
                            req.stream.readInt(), req.stream.readDouble(),
                            req.stream.readInt(), req.stream.readInt());
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_STOP_CAMERA:
                    _vehicleServer.stopCamera();
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_CAMERA_STATUS:
                    resp.stream.writeByte(_vehicleServer.getCameraStatus().ordinal());
                    _udpServer.respond(resp); 
                    break;
                case CMD_REGISTER_SENSOR_LISTENER:
                    synchronized(_sensorListeners) {
                        int channel = req.stream.readInt();
                        
                        // Make sure the sensorListener array is large enough
                        _sensorListeners.ensureCapacity(channel+1);
                        while (_sensorListeners.size() < channel+1)
                            _sensorListeners.add(null);
                        
                        // Retrive the sensor sublisteners
                        Map<SocketAddress, Integer> _listeners = _sensorListeners.get(channel);
                        if (_listeners == null) {
                            _listeners = new LinkedHashMap<SocketAddress, Integer>();
                            _sensorListeners.set(channel, _listeners);
                        }
                        
                        // Add the address to the appropriate sublistener list
                        _listeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_SET_SENSOR_TYPE:
                    _vehicleServer.setSensorType(req.stream.readInt(), 
                            VehicleServer.SensorType.values()[req.stream.readByte()]);
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_SENSOR_TYPE:
                    resp.stream.writeByte(_vehicleServer.getSensorType(req.stream.readInt()).ordinal());
                    _udpServer.respond(resp); 
                    break;
                case CMD_GET_NUM_SENSORS:
                    resp.stream.writeInt(_vehicleServer.getNumSensors());
                    _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_VELOCITY_LISTENER:
                    synchronized(_velocityListeners) {
                        _velocityListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_SET_VELOCITY:
                    _vehicleServer.setVelocity(UdpConstants.readTwist(req.stream));
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_VELOCITY:
                    UdpConstants.writeTwist(resp.stream, _vehicleServer.getVelocity());
                    _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_WAYPOINT_LISTENER:
                    synchronized(_waypointListeners) {
                        _waypointListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_START_WAYPOINTS:
                    UtmPose[] swPoses = new UtmPose[req.stream.readInt()];
                    for (int i = 0; i < swPoses.length; ++i) {
                        swPoses[i] = UdpConstants.readPose(req.stream);
                    }
                    _vehicleServer.startWaypoints(swPoses, req.stream.readUTF());
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_STOP_WAYPOINTS:
                    _vehicleServer.stopWaypoints();
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_WAYPOINTS:
                    UtmPose[] gwPoses = _vehicleServer.getWaypoints();
                    resp.stream.writeInt(gwPoses.length);
                    for (int i = 0; i < gwPoses.length; ++i) {
                        UdpConstants.writePose(resp.stream, gwPoses[i]);
                    }
                    _udpServer.respond(resp);
                    break;
                case CMD_GET_WAYPOINT_STATUS:
                    resp.stream.writeByte(_vehicleServer.getWaypointStatus().ordinal());
                    _udpServer.respond(resp);
                    break;
                case CMD_IS_CONNECTED:
                    resp.stream.writeBoolean((_vehicleServer != null) && _vehicleServer.isConnected());
                    _udpServer.respond(resp);
                    break;
                case CMD_IS_AUTONOMOUS:
                    resp.stream.writeBoolean(_vehicleServer.isAutonomous());
                    _udpServer.respond(resp);
                    break;
                case CMD_SET_AUTONOMOUS:
                    _vehicleServer.setAutonomous(req.stream.readBoolean());
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_SET_GAINS:
                    int sgAxis = req.stream.readInt();
                    double[] sgGains = new double[req.stream.readInt()];
                    for (int i = 0; i < sgGains.length; ++i) {
                        sgGains[i] = req.stream.readDouble();
                    }
                    _vehicleServer.setGains(sgAxis, sgGains);
                    _udpServer.respond(resp);
                    break;
                case CMD_GET_GAINS:
                    double[] ggGains = _vehicleServer.getGains(req.stream.readInt());
                    resp.stream.writeInt(ggGains.length);
                    for (int i = 0; i < ggGains.length; ++i) {
                        resp.stream.writeDouble(ggGains[i]);
                    }
                    _udpServer.respond(resp);
                default:
                    logger.log(Level.WARNING, "Ignoring unknown command: {0}", command);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to parse request: {0}", req.ticket);
        }

    }

    @Override
    public void timeout(long ticket, SocketAddress destination) {
        logger.log(Level.WARNING, "No response for: {0} @ {1}", new Object[]{ticket, destination});
    }

    /**
     * Terminates service processes and de-registers the service from a 
     * registry, if one was being used.
     */
    public void shutdown() {
        _udpServer.stop();
    }
    
    private final StreamHandler _handler = new StreamHandler();
    
    private class StreamHandler implements PoseListener, ImageListener, CameraListener, SensorListener, VelocityListener, WaypointListener {

        @Override
        public void receivedPose(UtmPose pose) {
            try {
                // Construct pose message
                Response resp = new Response(UdpConstants.NO_TICKET, null);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_STATE.str);
                UdpConstants.writePose(resp.stream, pose);
            
                // Send to all listeners
                _udpServer.bcast(resp, _stateListeners.keySet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize pose");
            }
        }

        @Override
        public void receivedImage(byte[] image) {
            try {
                // Construct pose message
                Response resp = new Response(UdpConstants.NO_TICKET, null);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_IMAGE.str);
                resp.stream.writeInt(image.length);
                resp.stream.write(image);
            
                // Send to all listeners
                _udpServer.bcast(resp, _imageListeners.keySet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize image");
            }
        }

        @Override
        public void imagingUpdate(CameraState status) {
            try {
                // Construct pose message
                Response resp = new Response(UdpConstants.NO_TICKET, null);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_CAMERA.str);
                resp.stream.writeByte(status.ordinal());
            
                // Send to all listeners
                _udpServer.bcast(resp, _cameraListeners.keySet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize camera");
            }
        }

        @Override
        public void receivedSensor(SensorData sensor) {
            try {
                // Construct pose message
                Response resp = new Response(UdpConstants.NO_TICKET, null);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_SENSOR.str);
                resp.stream.writeInt(sensor.channel);
                resp.stream.writeByte(sensor.type.ordinal());
                resp.stream.writeInt(sensor.data.length);
                for (int i = 0; i < sensor.data.length; ++i)
                    resp.stream.writeDouble(sensor.data[i]);
            
                // Send to all listeners
                Map<SocketAddress, Integer> _listeners = _sensorListeners.get(sensor.channel);
                if (_listeners != null)
                    _udpServer.bcast(resp, _listeners.keySet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize sensor " + sensor.channel);
            }
        }

        @Override
        public void receivedVelocity(Twist velocity) {
            try {
                // Construct pose message
                Response resp = new Response(UdpConstants.NO_TICKET, null);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_VELOCITY.str);
                UdpConstants.writeTwist(resp.stream, velocity);
            
                // Send to all listeners
                _udpServer.bcast(resp, _velocityListeners.keySet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize camera");
            }
        }

        @Override
        public void waypointUpdate(WaypointState status) {
            try {
                // Construct pose message
                Response resp = new Response(UdpConstants.NO_TICKET, null);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_WAYPOINT.str);
                resp.stream.writeByte(status.ordinal());
            
                // Send to all listeners
                _udpServer.bcast(resp, _waypointListeners.keySet());
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize camera");
            }
        }
        
    }
}
