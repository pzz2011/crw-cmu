package edu.cmu.ri.crw.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Core implementation of UDP messaging server.  Used within both client and
 * server RPC mechanisms.
 *
 * Assumptions:
 * - Server function calls are FAST w.r.t. RPC timeouts
 * - Spurious packet loss is common
 * - Packets may be delayed for a relatively long time (~1s) due to lossy WLAN
 *
 * Overview of three-way RPC from client
 * 1. Send function call: <12131, "GET_STATE", args>
 * 2. (No response after timeout)
 * 3. Send function call: <12131, "GET_STATE", args>
 * 4. Get response: <12131, response>
 * 5. Send function ack: <12131, "OK">
 *
 * Overview of three-way RPC from server
 * 1. Receive function call: <12131, "GET_STATE", args>
 * 2. Execute function call on implementation
 * 3. Send response: <12131, response>
 * 4. (No response after timeout)
 * 5. Send response: <12131, response>
 * 6. Get function ack: <12131, "OK">
 * 
 *
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public class UdpServer {

    final DatagramSocket _socket;
    final DelayQueue<QueuedResponse> _responses;
    RequestHandler _handler;
    
    public UdpServer() {
        
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            // TODO: log something here;
        }
        
        _socket = socket;
        _responses = new DelayQueue<QueuedResponse>();
        
    }
    
    public void start() {
        new Thread(new Responder()).start();
        new Thread(new Receiver()).start();
    }
    
    public void stop() {
        if (_socket != null) {
            _socket.close();
        }
    }
    
    public void setHandler(RequestHandler handler) {
        _handler = handler;
    }
    
    public interface RequestHandler {
        public void received(Request req);
    }

    public class Request {
        private final ByteArrayInputStream _buffer;
        public final DataInputStream stream;
        public final long ticket;
        public final SocketAddress source;

        public Request(DatagramPacket packet) {
            // Convert the packet into a data stream
            _buffer = new ByteArrayInputStream(packet.getData());
            stream = new DataInputStream(_buffer);

            // Extract the socket address from the packet
            source = packet.getSocketAddress();

            // Extract the ticket from the data payload
            long t = UdpConstants.NO_TICKET;
            try {
                t = stream.readLong();
            } catch (IOException e) {
                // TODO: report some sort of error here
            }
            ticket = t;
        }

        public void reset() {
            _buffer.reset();
            try {
                stream.readLong(); // Clear ticket from start of buffer
            } catch (IOException e) {
                // TODO: report some sort of error here
            }
        }
    }

    public class Response {
        private final ByteArrayOutputStream _buffer;
        public final DataOutputStream stream;
        public final SocketAddress destination;
        public final long ticket;

        public Response(Request r) {
            this(r.ticket, r.source);
        }

        public Response(long t, SocketAddress d) {
             _buffer = new ByteArrayOutputStream(UdpConstants.MAX_PACKET_SIZE);
             stream = new DataOutputStream(_buffer);

             ticket = t;
             destination = d;
        }

        public void reset() {
            try {
                stream.flush();
            } catch (IOException e) {
                // Shouldn't happen, but resetting stream anyway
            }
            _buffer.reset();
        }

        public QueuedResponse toQueuedResponse() {
            return null;
        }
    }

    public class QueuedResponse implements Delayed {
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
            if (o instanceof QueuedResponse) {
                return Long.signum(timeout - ((QueuedResponse)o).timeout);
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

    class Receiver implements Runnable {

        byte[] _buffer = new byte[UdpConstants.MAX_PACKET_SIZE];
        DatagramPacket _packet = new DatagramPacket(_buffer, _buffer.length);
        
        @Override
        public void run() {
            while(_socket.isBound() && !_socket.isClosed()) {
                
                // Get the next packet from the socket
                try {
                    _socket.receive(_packet);
                } catch (IOException e) {
                    // TODO: error handling, but for now return
                    return;
                }
                
                // Decode it into a request
                Request request = new Request(_packet);
                
                // Extract the command string (to check if this is an ACK)
                String cmd = null;
                try {
                    cmd = request.stream.readUTF();
                    request.reset();
                } catch (IOException e) {
                    // TODO: add error handling (failed to decode message)
                }
                
                // If it is an ACK, remove the corresponding outgoing messages,
                // otherwise, send out an ACK and handle the message
                if (cmd.equals(UdpConstants.COMMAND.CMD_ACKNOWLEDGE)) {
                    acknowledge(request.ticket);
                } else {
                    Response response = new Response(request);

                    // Construct an ack and send it out
                    try {
                        response.stream.writeUTF(UdpConstants.COMMAND.CMD_ACKNOWLEDGE.str);
                        send(response);
                    } catch (IOException e) {
                        // TODO: more elegant error message
                        throw new RuntimeException("This shouldn't happen.", e);
                    }
                    
                    // Pass this request along to the handler
                    if (_handler != null) {
                        _handler.received(request);
                    }
                }
            }
        }
        
    }
    
    class Responder implements Runnable {
        
        @Override
        public void run() {
            QueuedResponse response = null;

            while(_socket.isBound() && !_socket.isClosed()) {
                // Wait for next response that has timed out or requires transmission
                try {
                    response = _responses.take();
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
                    _responses.offer(response);
                }
            }
        }
    }

    /**
     * Respond to an existing function request.  Prepares the response for
     * automatic retransmission and acknowledgement.
     * 
     * @param response the response to be sent
     */
    public void respond(Response response) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Send out a function response that is broadcast to a list of addresses.
     * No retransmission will be done on these messages.  The SocketAddress
     * specified in the Response object is not used in this case.
     *
     * @param response the response to be sent
     * @param destinations the destination addresses to which it will be sent
     */
    public void bcast(Response response, List<SocketAddress> destinations) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    /**
     * Send out a function response that is unicast to the specified address.
     * No retransmission will be done on these messages.
     *
     * @param response the response to be sent
     */
    public void send(Response response) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Removes all responses that have the corresponding ticket to an already
     * acknowledged response.
     *
     * @param ticket
     */
    public void acknowledge(long ticket) {
        Iterator<QueuedResponse> itr = _responses.iterator();

        while(itr.hasNext()) {
            if (itr.next().ticket == ticket) {
                itr.remove(); // TODO: should this stop after the first one?
            }
        }
    }
}
