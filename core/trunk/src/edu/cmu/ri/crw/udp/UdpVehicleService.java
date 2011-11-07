package edu.cmu.ri.crw.udp;

// TODO: finish this class!

import edu.cmu.ri.crw.VehicleServer;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service that registers a vehicle server over UDP to allow control over the 
 * networn via a proxy server 
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class UdpVehicleService {
    private static final Logger logger = Logger.getLogger(UdpVehicleService.class.getName());
    
    public static final int REGISTRATION_RATE_MS = 5000;
    
    protected VehicleServer _server;
    protected final Object _serverLock = new Object();
    
    protected final DatagramSocket _socket;
    protected final Object _socketLock = new Object();
    
    protected final List<InetSocketAddress> _registries = new ArrayList<InetSocketAddress>();
    
    public UdpVehicleService() {
        _server = null;
        
        DatagramSocket s = null;
        try {
            s = new DatagramSocket();
        } catch(SocketException ex) {
            logger.log(Level.SEVERE, "Unable to open UDP socket.", ex);
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
    
    /**
     * Terminates service processes and de-registers the service from a 
     * registry, if one was being used.
     */
    public void shutdown() {
        if (_socket != null)
            _socket.close();
    }
}
