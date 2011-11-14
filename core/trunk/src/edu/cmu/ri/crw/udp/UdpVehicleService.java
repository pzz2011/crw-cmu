package edu.cmu.ri.crw.udp;

// TODO: finish this class!

import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpServer.Request;
import edu.cmu.ri.crw.udp.UdpServer.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
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

    protected final List<InetSocketAddress> _registries = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _stateListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _imageListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _cameraListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _sensorListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _velocityListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _waypointListeners = new ArrayList<InetSocketAddress>();

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
                    // TODO: registration
                    // TODO: response
                    break;
                case CMD_SET_STATE:
                    _vehicleServer.setState(UdpConstants.readPose(req.stream));
                    _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_STATE:
                    UdpConstants.writePose(resp.stream, _vehicleServer.getState());
                    _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_IMAGE_LISTENER:
                    // TODO: registration
                    // TODO: response
                    break;
                case CMD_CAPTURE_IMAGE:
                    byte[] image = _vehicleServer.captureImage(req.stream.readInt(), req.stream.readInt());
                    resp.stream.writeInt(image.length);
                    resp.stream.write(image);
                    _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_CAMERA_LISTENER:
                    // TODO: registration
                    // TODO: response
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
                    // TODO: registration
                    // TODO: response
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
                    // TODO: registration
                    // TODO: response
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
                    // TODO: registration
                    // TODO: response
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
}
