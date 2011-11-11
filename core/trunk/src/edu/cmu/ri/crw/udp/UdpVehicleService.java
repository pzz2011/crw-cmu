package edu.cmu.ri.crw.udp;

// TODO: finish this class!

import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import robotutils.Pose3D;

/**
 * A service that registers a vehicle server over UDP to allow control over the 
 * networn via a proxy server 
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UdpVehicleService {
    private static final Logger logger = Logger.getLogger(UdpVehicleService.class.getName());
    
    public static final int REGISTRATION_RATE_MS = 5000;
    public static final int PACKET_SIZE = 32767;
   
    protected VehicleServer _server;
    protected final Object _serverLock = new Object();
    
    protected final DatagramSocket _socket;

    protected final List<InetSocketAddress> _registries = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _stateListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _imageListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _cameraListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _sensorListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _velocityListeners = new ArrayList<InetSocketAddress>();
    protected final List<InetSocketAddress> _waypointListeners = new ArrayList<InetSocketAddress>();

    public UdpVehicleService() {
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
        } catch(SocketException ex) {
            logger.log(Level.SEVERE, "Unable to open UDP socket", ex);
        }
        _socket = s;
    }
    
    public UdpVehicleService(VehicleServer server) {
        this();
        
        _server = server;
    }
    
    public void setServer(VehicleServer server) {
        synchronized(_serverLock) {
            _server = server;
        }
    }
    
    public VehicleServer getServer() {
        synchronized(_serverLock) {
            return _server;
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

    void respondVoid() {
        
    }

    static UtmPose toUtmPose(DataInputStream in) throws IOException {
        Pose3D pose = new Pose3D(in.readDouble(), in.readDouble(), in.readDouble(),
                in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
        Utm origin = new Utm(in.readInt(), in.readBoolean());
        return new UtmPose(pose, origin);
    }

    static void fromUtmPose(DataOutputStream out, UtmPose utmPose) throws IOException {
        out.writeDouble(utmPose.pose.getX());
        out.writeDouble(utmPose.pose.getY());
        out.writeDouble(utmPose.pose.getZ());
        out.writeDouble(utmPose.pose.getRotation().getW());
        out.writeDouble(utmPose.pose.getRotation().getX());
        out.writeDouble(utmPose.pose.getRotation().getY());
        out.writeDouble(utmPose.pose.getRotation().getZ());
        out.writeInt(utmPose.origin.zone);
        out.writeBoolean(utmPose.origin.isNorth);
    }

    /**
     * Listener class that received UDP packets and decodes the function calls
     * inside them.
     */
    class UdpListener implements Runnable {
        private final DatagramPacket _packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

        @Override
        public void run() {
            while (_socket.isBound() && !_socket.isClosed()) {
                try {
                    _socket.receive(_packet);
                    // TODO: thread this
                    
                    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(_packet.getData()));
                    final DataOutputStream out = new DataOutputStream(new ByteArrayOutputStream());

                    final int ticket = in.readInt();
                    final String command = in.readUTF();
                    
                    try {
                        switch (UdpConstants.COMMAND.valueOf(command)) {
                            case CMD_REGISTER_STATE_LISTENER:
                                // TODO: registration
                                // TODO: response
                                break;
                            case CMD_SET_STATE:
                                _server.setState(toUtmPose(in));
                                // TODO: response
                                break;
                            case CMD_GET_STATE:
                                fromUtmPose(out, _server.getState());
                                // TODO: response
                                break;
                            case CMD_REGISTER_IMAGE_LISTENER:
                                // TODO: registration
                                // TODO: response
                                break;
                            case CMD_CAPTURE_IMAGE:
                                _server.captureImage(in.readInt(), in.readInt());
                                // TODO: response
                                break;
                            case CMD_REGISTER_CAMERA_LISTENER:
                                // TODO: registration
                                // TODO: response
                                break;
                            case CMD_START_CAMERA:
                                _server.startCamera(in.readLong(), in.readDouble(), in.readInt(), in.readInt());
                                // TODO: response
                                break;
                            case CMD_STOP_CAMERA:
                                _server.stopCamera();
                                // TODO: response
                                break;
                            case CMD_GET_CAMERA_STATUS:
                                _server.getCameraStatus();
                                // TODO: response
                                break;
                            case CMD_REGISTER_SENSOR_LISTENER:
                                // TODO: registration
                                // TODO: response
                                break;
                            case CMD_SET_SENSOR_TYPE:
                                _server.setSensorType(in.readInt(), VehicleServer.SensorType.values()[in.readByte()]);
                                // TODO: response
                                break;
                            case CMD_GET_SENSOR_TYPE:
                                _server.getSensorType(in.readInt());
                                // TODO: response
                                break;
                            case CMD_GET_NUM_SENSORS:
                                _server.getNumSensors();
                                // TODO: response
                                break;
                            case CMD_REGISTER_VELOCITY_LISTENER:
                                // TODO: registration
                                // TODO: response
                                break;
                            case CMD_SET_VELOCITY:
                                _server.setVelocity(new Twist(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt()));
                                // TODO: response
                                break;
                            case CMD_GET_VELOCITY:
                                _server.getVelocity();
                                // TODO: response
                                break;
                            case CMD_REGISTER_WAYPOINT_LISTENER:
                                // TODO: registration
                                // TODO: response
                                break;
                            case CMD_START_WAYPOINTS:
                                UtmPose[] swPoses = new UtmPose[in.readInt()];
                                for (int i = 0; i < swPoses.length; ++i)
                                    swPoses[i] = toUtmPose(in);
                                _server.startWaypoints(swPoses, in.readUTF());
                                // TODO: response
                                break;
                            case CMD_STOP_WAYPOINTS:
                                _server.stopWaypoints();
                                // TODO: response
                                break;
                            case CMD_GET_WAYPOINTS:
                                UtmPose[] gwPoses = _server.getWaypoints();
                                // TODO: response
                                break;
                            case CMD_GET_WAYPOINT_STATUS:
                                _server.getWaypointStatus();
                                // TODO: response
                                break;
                            case CMD_IS_CONNECTED:
                                _server.isConnected();
                                // TODO: response
                                break;
                            case CMD_IS_AUTONOMOUS:
                                _server.isAutonomous();
                                // TODO: response
                                break;
                            case CMD_SET_AUTONOMOUS:
                                _server.setAutonomous(in.readBoolean());
                                // TODO: response
                                break;
                            case CMD_SET_GAINS:
                                double[] sgGains = new double[in.readInt()];
                                for (int i = 0; i < sgGains.length; ++i)
                                    sgGains[i] = in.readDouble();
                                _server.setGains(in.readInt(), sgGains);
                                // TODO: response
                                break;
                            case CMD_GET_GAINS:
                                double[] gains = _server.getGains(in.readInt());
                                // TODO: response
                            default:
                                throw new IllegalStateException("Unknown command received.");
                        }
                    } catch (IllegalArgumentException e) {
                        // TODO: error handling
                    }

                } catch (IOException e) {
                    // TODO: error handling
                }
            }
        }
    }

    /**
     * Terminates service processes and de-registers the service from a 
     * registry, if one was being used.
     */
    public void shutdown() {
        if (_socket != null)
            _socket.close();
    }
}
