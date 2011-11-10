/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.data.UtmPose;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * Helper class that enables the quick assembly of UDP datagram messages
 * containing primitive types.  No type information is preserved: the
 * receiving process must already know how to decode the message.
 *
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public class UdpResponse {

    public static final int DEFAULT_BUFFER_INITIAL_SIZE = 1024;

    private ByteArrayOutputStream _buffer = new ByteArrayOutputStream(1024);
    private DataOutputStream _out = new DataOutputStream(_buffer);

    /**
     * Clears the previously stacked items from the response buffer, but does
     * not reallocate the buffer itself.  This lets the same response object
     * be reused in certain cases for efficiency.
     */
    public synchronized void reset() {
        try {
            _out.flush();
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
        
        _buffer.reset();
    }

    public synchronized void writeString(String s) {
        try {
            _out.writeUTF(s);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeInt(int i) {
        try {
            _out.writeInt(i);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeLong(long l) {
        try {
            _out.writeLong(l);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeDouble(double d) {
        try {
            _out.writeDouble(d);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeBoolean(boolean b) {
        try {
            _out.writeBoolean(b);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeByte(byte b) {
        try {
            _out.writeByte(b);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void writeByteArray(byte[] b) {
        try {
            _out.writeInt(b.length);
            _out.write(b);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void writePose(UtmPose utmPose) {
        try {
            _out.writeDouble(utmPose.pose.getX());
            _out.writeDouble(utmPose.pose.getY());
            _out.writeDouble(utmPose.pose.getZ());

            _out.writeDouble(utmPose.pose.getRotation().getW());
            _out.writeDouble(utmPose.pose.getRotation().getX());
            _out.writeDouble(utmPose.pose.getRotation().getY());
            _out.writeDouble(utmPose.pose.getRotation().getZ());
            
            _out.writeInt(utmPose.origin.zone);
            _out.writeBoolean(utmPose.origin.isNorth);
        } catch (IOException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

    public synchronized void send(DatagramSocket socket, SocketAddress destination) throws IOException {
        byte[] bytes = _buffer.toByteArray();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, destination);
        socket.send(packet);
    }
}
