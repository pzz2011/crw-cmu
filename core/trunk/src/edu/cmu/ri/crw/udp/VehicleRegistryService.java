/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.crw.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standalone service for supporting vehicle registration for UDP communication.
 * 
 * Should be run standalone on a publicly visible server.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class VehicleRegistryService {
    private static final Logger logger = Logger.getLogger(UdpVehicleService.class.getName());
    private static final Charset US_ASCII = Charset.forName("US_ASCII");
    
    public static final int DEFAULT_PORT = 6077;
    public static final int MAX_MESSAGE_SIZE = 32767;
    
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_UNREGISTER = "UNREGISTER";
    public static final String CMD_CONNECT = "CONNECT";
    public static final String CMD_LIST = "LIST";
    
    protected DatagramSocket _socket;    
    protected final Map<String, SocketAddress> _clientName = new LinkedHashMap<String, SocketAddress>();
    
    public void start() {
        start(DEFAULT_PORT);
    }
    
    public synchronized void start(int port) {
        if (isRunning()) return;
        
        try {
            _socket = new DatagramSocket(port);
            new Thread(new DatagramListener(_socket)).start();
            logger.log(Level.INFO, "Started vehicle registry on {0}", _socket.getLocalSocketAddress());
        } catch (SocketException ex) {
            logger.log(Level.INFO, "Failed to start vehicle registry.", ex);
        }
    }
    
    public boolean isRunning() {
        return _socket.isBound() && !_socket.isClosed();
    }
    
    public synchronized void stop() {
        if (!isRunning()) return;
        
        _socket.close();
    }

    protected class DatagramListener implements Runnable {

        private final DatagramSocket _socket;
        private final byte[] _buffer = new byte[MAX_MESSAGE_SIZE];
        private final DatagramPacket _packet = new DatagramPacket(_buffer, _buffer.length);
        
        public DatagramListener(DatagramSocket socket) {
            _socket = socket;
        }
        
        @Override
        public void run() {
            try {
                while(_socket.isBound() && !_socket.isClosed()) {
                    _socket.receive(_packet);
                    handlePacket(_packet);
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Shutting down vehicle registry.", ex);
            }
        }        
    }
    
    protected final void handlePacket(DatagramPacket packet) throws IOException {
        
        // Decode the information in the packet
        SocketAddress srcAddr = packet.getSocketAddress();
        String payload = new String(packet.getData(), US_ASCII);
        String args[] = payload.split(" ");        
        
        PARSER:
        {
            if (args[0].equalsIgnoreCase(CMD_REGISTER)) {
                if (args.length != 2) break PARSER;
                String name = args[1];
                
                synchronized(_clientName) {
                    _clientName.put(name, srcAddr);
                }
                
                return;
            } else if (args[0].equalsIgnoreCase(CMD_UNREGISTER)) {
                if (args.length != 2) break PARSER;
                String name = args[1];
                
                synchronized(_clientName) {
                    _clientName.remove(name);
                }
                
                return;
            } else if (args[0].equalsIgnoreCase(CMD_CONNECT)) {
                if (args.length != 2) break PARSER;
                String destName = args[1];
                
                SocketAddress destAddr = null;
                synchronized(_clientName) {
                     destAddr = _clientName.get(destName);
                     if (destAddr == null) break PARSER;
                }
                
                // Send connect request to source
                String requestSrc = CMD_CONNECT + " " + destAddr;
                byte[] bufferSrc = requestSrc.getBytes(US_ASCII);
                packet.setData(bufferSrc);
                packet.setSocketAddress(srcAddr);
                _socket.send(packet);
                    
                // Send connect request to destination
                String requestDest = CMD_CONNECT + " " + srcAddr;
                byte[] bufferDest = requestDest.getBytes(US_ASCII);
                packet.setData(bufferDest);
                packet.setSocketAddress(srcAddr);
                _socket.send(packet);
                
                return;
            } else if (args[0].equalsIgnoreCase(CMD_LIST)) {
                if (args.length != 1) break PARSER;
                
                StringBuilder listing = new StringBuilder(CMD_LIST);
                synchronized(_clientName) {
                    for (String name : _clientName.keySet()) {
                        listing.append(" ");
                        listing.append(name);
                    }
                }
                
                byte[] buffer = listing.toString().getBytes(US_ASCII);
                packet.setData(buffer);
                packet.setSocketAddress(srcAddr);
                _socket.send(packet);
            }
        }
        
        logger.log(Level.WARNING, "Invalid command: {0}", payload);
        return;
    }
}
