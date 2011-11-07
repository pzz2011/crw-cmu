/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.crw.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Represents a vehicle registry object, allows clients to perform registry
 * functions.  Requires an existing UDP socket.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
public class VehicleRegistry {
   
    // TODO: proper error handling here.
    
    private static final Charset US_ASCII = Charset.forName("US_ASCII");
    
    
    protected final InetSocketAddress _address;
    protected final DatagramSocket _socket;
    protected final String _name;
    
    public VehicleRegistry(InetSocketAddress addr, DatagramSocket socket, String vehicleName) {
        _address = addr;
        _socket = socket;
        _name = vehicleName;
    }
    
    public void register(String name) {
        send(VehicleRegistryService.CMD_REGISTER + " " + _name);
    }
    
    public void unregister(String name) {
        send(VehicleRegistryService.CMD_UNREGISTER + " " + _name);
    }
    
    public void connect(String name) {
        send(VehicleRegistryService.CMD_CONNECT + " " + _name);
    }
    
    public void list() {
        send(VehicleRegistryService.CMD_LIST);
    }
    
    protected void send(String data) {
        if (!_socket.isBound() || _socket.isClosed())
            throw new IllegalStateException("Socket is not open.");
        
        byte[] buffer = data.getBytes(US_ASCII);
        
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, _address);
            _socket.send(packet);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
