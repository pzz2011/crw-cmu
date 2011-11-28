package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.udp.UdpServer.Request;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standalone service for supporting vehicle registration for UDP communication.
 * 
 * Should be run standalone on a publicly visible server.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class VehicleRegistryService implements UdpServer.RequestHandler {
    private static final Logger logger = Logger.getLogger(VehicleRegistryService.class.getName());
    
    public static final int DEFAULT_UDP_PORT = 6077;
    public static final int DEFAULT_WEB_PORT = 8080;
    
    protected final UdpServer _udpServer;
    protected final Timer _registrationTimer = new Timer();
    protected final Map<SocketAddress, Client> _clients = new LinkedHashMap<SocketAddress, Client>();
    
    static class Client {
        int ttl;
        String name;
        SocketAddress addr;
    }
    
    public VehicleRegistryService(int udpPort, int webPort) {
        _udpServer = new UdpServer(udpPort);
        _udpServer.setHandler(this);
        _udpServer.start();
        
        _registrationTimer.scheduleAtFixedRate(_registrationTask, 0, UdpConstants.REGISTRATION_RATE_MS);
    }
    
    public void shutdown() {
        _udpServer.stop();
    }

    @Override
    public void received(Request req) {
        try {
            final String command = req.stream.readUTF();
            if (command.equals(UdpConstants.CMD_REGISTER)) {
                synchronized(_clients) {
                    // Look for client in table
                    Client c = _clients.get(req.source);
                    
                    // If not found, create a new entry
                    if (c == null) {
                        c = new Client();
                        c.addr = req.source;
                        c.name = req.stream.readUTF();
                        _clients.put(req.source, c);
                    }
                    
                    // Update the registration count for this client
                    c.ttl = UdpConstants.REGISTRATION_TIMEOUT_COUNT;
                }
            } else {
                logger.log(Level.WARNING, "Ignoring unknown command: {0}", command);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to parse request: {0}", req.ticket);
        }
    }

    @Override
    public void timeout(long ticket, SocketAddress destination) {
        throw new UnsupportedOperationException("Registry should not receive timeouts.");
    }
    
    // Removes outdated registrations from client list
    protected TimerTask _registrationTask = new TimerTask() {
        @Override
        public void run() {
            synchronized(_clients) {
                for (Map.Entry<SocketAddress, Client> client : _clients.entrySet()) {
                    if (client.getValue().ttl == 0) {
                        _clients.remove(client.getKey());
                    } else {
                        client.getValue().ttl--;
                    }
                }
            }
        }
    };
}
