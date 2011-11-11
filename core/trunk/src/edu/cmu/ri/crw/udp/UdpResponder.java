/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.udp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Just some sketches of helper functions, don't use these for anything.
 *
 * Assumptions:
 * - Server function calls are FAST w.r.t. RPC timeouts
 * - Spurious packet loss is common
 * - Packets may be delayed for a relatively long time (~1s) due to lossy WLAN
 *
 * Overview of three-way RPC from server
 * 1. Receive function call: <12131, "GET_STATE", args>
 * 2. Execute function call on implementation
 * 3. Send response: <12131, response>
 * 4. No response after timeout
 * 5. Send response: <12131, response>
 * 6. Get function ack: <12131, "OK">
 * 
 * Overview of three-way RPC from client
 * 1. Send function call: <12131, "GET_STATE", args>
 * 2. No response after timeout
 * 3. Send function call: <12131, "GET_STATE", args>
 * 4. Get response: <12131, response>
 * 5. Send function ack: <12131, "OK">
 *
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public class UdpResponder {

    DelayQueue<Response> responses = new DelayQueue<Response>();

    public class ResponseMaker {
        private final ByteArrayOutputStream _buffer = new ByteArrayOutputStream(UdpConstants.INITIAL_PACKET_SIZE);
        public final ObjectOutputStream stream = null;

        public Response toResponse() {
            return null;
        }
    }

    public class Response implements Delayed {
        public final SocketAddress destination = null; // TODO: fix me
        public final byte[] bytes = null; // TODO: fix me
        public final int ticket = 0; // TODO: fix me
        public int ttl = UdpConstants.RETRY_COUNT;
        public long timeout = 0;

        public void resetDelay() {
            timeout = System.nanoTime() + UdpConstants.TIMEOUT_NS;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(timeout - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (o instanceof Response) {
                return Long.signum(timeout - ((Response)o).timeout);
            } else {
                return Long.signum(getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS));
            }
        }

        public DatagramPacket toPacket(SocketAddress addr) throws SocketException {
            return new DatagramPacket(bytes, bytes.length, addr);
        }

        public DatagramPacket toPacket() throws SocketException {
            return new DatagramPacket(bytes, bytes.length, destination);
        }
    }

    public class Responder implements Runnable {
        private final DatagramSocket _socket = null;  // TODO: fix me

        @Override
        public void run() {
            Response response = null;

            while(_socket.isBound() && !_socket.isClosed()) {
                // Wait for next response that has timed out or requires transmission
                try {
                    response = responses.take();
                } catch (InterruptedException e) {
                    // TODO: is this right?
                    // Assume that interruption means we are supposed to be stopping
                    // TODO: some logger thing here
                    return;
                }

                // Send the response to the requestor
                try {
                    _socket.send(response.toPacket());
                } catch (IOException e) {
                    // TODO: figure out which errors we need to return on or ignore here
                    // TODO: some logger thing here
                    return;
                }

                // Decrement TTL and reset timeout for retransmission
                if (response.ttl > 0) {
                    response.ttl--;
                    response.resetDelay();
                    responses.offer(response);
                }
            }
        }
    }

    /**
     * Respond to an existing function request.  Prepares the response for
     * automatic retransmission and acknowledgement.
     * 
     * @param response
     */
    public void respond(Response response) {
        throw new UnsupportedOperationException()
    }

    /**
     * Removes all responses that have the corresponding ticket an already
     * acknowledged response.
     *
     * @param ticket
     */
    public void acknowledge(long ticket) {
        Iterator<Response> itr = responses.iterator();

        while(itr.hasNext()) {
            if (itr.next().ticket == ticket) {
                itr.remove(); // TODO: should this stop after the first one?
            }
        }
    }

    // Assumptions:
    // * Server function calls are FAST w.r.t. RPC timeouts
    // * Spurious packet loss is common
    // * Packets may be delayed for a relatively long time (~1s) due to lossy WLAN
    //
    // Overview of three-way RPC from client
    // 1. Send function call: <12131, "GET_STATE", args>
    // 2. No response after timeout
    // 3. Send function call: <12131, "GET_STATE", args>
    // 4. Get response: <12131, response>
    // 5. Send function ack: <12131, "OK">
    //
    // Overview of three-way RPC from server
    // 1. Receive function call: <12131, "GET_STATE", args>
    // 2. Execute function call on implementation
    // 3. Send response: <12131, response>
    // 4. No response after timeout
    // 5. Send response: <12131, response>
    // 6. Get function ack: <12131, "OK">

    public void oneWayCall(String type, Object[] args) {
        
    }

    public void twoWayCall(int ticket, String type, Object[] args) {

    }

    public void response(int ticket, Object[] args) {
        
    }
    
}
