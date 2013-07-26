package edu.cmu.ri.crw.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
@SuppressWarnings("LoggerStringConcat")
public class UdpServer {
    private static final Logger logger = Logger.getLogger(UdpVehicleService.class.getName());
    private static final int IPTOS_LOWDELAY = 0x10;

    final DatagramSocket _socket;
    final DelayQueue<QueuedResponse> _responses = new DelayQueue<QueuedResponse>();
    final List<Long> _oldTickets = new ArrayList<Long>(UdpConstants.TICKET_CACHE_SIZE);
    
    final Object _retransmissionLock = new Object();
    long _retransmissionTimeout = UdpConstants.INITIAL_RETRY_RATE_NS;
    
    RequestHandler _handler;
    
    public UdpServer() {
        
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSendBufferSize(UdpConstants.MAX_PACKET_SIZE);
            socket.setTrafficClass(IPTOS_LOWDELAY);
        } catch (SocketException e) {
            logger.severe("Unable to open desired UDP socket.");
            throw new RuntimeException("Unable to open desired UDP socket.", e);
        }
        _socket = socket;
    }
    
    public UdpServer(int port) {
        
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            logger.severe("Unable to open desired UDP socket.");
            throw new RuntimeException("Unable to open desired UDP socket.", e);
        }
        _socket = socket;
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
    
    public SocketAddress getSocketAddress() {
        return _socket.getLocalSocketAddress();
    }
    
    public void setHandler(RequestHandler handler) {
        _handler = handler;
    }
    
    public interface RequestHandler {
        public void received(Request req);
        // TODO: should we save the response for some reason?
        public void timeout(long ticket, SocketAddress destination);
    }

    public static class Request {
        private final ByteArrayInputStream _buffer;
        public final DataInputStream stream;
        public final long ticket;
        public final SocketAddress source;

        public Request(DatagramPacket packet) {
            // Convert the packet into a data stream
            _buffer = new ByteArrayInputStream(packet.getData());
            stream = new DataInputStream(_buffer);

            // Extract the socket address data from the packet
//            InetAddress foo = packet.getAddress();
//            int bar = packet.getPort();
//            
//            // Put in some dummy hostname and reconstruct
//            CrwNetworkUtils.injectHostname(foo, "DO_NOT_RESOLVE");
//            source = new InetSocketAddress(foo, bar);
            // Extract the socket address data from the packet,
            // put in a blank hostname and reconstruct (to avoid DNS lookups)
            InetAddress addr = null;
            int port = 9999;
            try {
                addr = InetAddress.getByAddress(null, packet.getAddress().getAddress());
                port = packet.getPort();
            } catch (UnknownHostException e) {
                logger.log(Level.WARNING, "Failed to get valid source", e);
            }
            source = new InetSocketAddress(addr, port);
            
            
            
            
            
            
            // Extract the ticket from the data payload
            long t = UdpConstants.NO_TICKET;
            try {
                t = stream.readLong();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to get valid ticket", e);
            }
            ticket = t;
        }

        public void reset() {
            _buffer.reset();
            try {
                stream.readLong(); // Clear ticket from start of buffer
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to get valid ticket", e);
            }
        }
    }

    public static class Response {
        protected final ByteArrayOutputStream _buffer;
        public final DataOutputStream stream;
        public final SocketAddress destination;
        public final long ticket;

        public Response(Request r) {
            this(r.ticket, r.source);
        }

        public Response(long t, SocketAddress d) {
            _buffer = new ByteArrayOutputStream(UdpConstants.INITIAL_PACKET_SIZE);
            stream = new DataOutputStream(_buffer);

            ticket = t;
            destination = d;

            try {
                stream.writeLong(ticket);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write ticket to response buffer", e);
            }
        }
        
        private Response(long t, SocketAddress d, DataOutputStream s, ByteArrayOutputStream b) {
            _buffer = b;
            stream = s;
            destination = d;
            ticket = t;
        }

        public void reset() {
            try {
                stream.flush();
            } catch (IOException e) {
                // Shouldn't happen, but this is just resetting the stream anyway
            }
            _buffer.reset();
            
            // Reinsert ticket into stream
            try {
                stream.writeLong(ticket);
            } catch (IOException e) {
                throw new RuntimeException("Failed to write ticket to response buffer", e);
            }
        }
        
        public byte[] getBytes() {
            try {
                stream.flush();
            } catch (IOException e) {
                throw new RuntimeException("Failed to write to response buffer", e);
            }
            return _buffer.toByteArray();
        }
    }

    public static class QueuedResponse implements Delayed {
        public final SocketAddress destination;
        public final byte[] bytes;
        public final long ticket;
        public final long sentTime = System.nanoTime();
        private int ttl = UdpConstants.RETRY_COUNT;
        private long timeout;
        
        public QueuedResponse(Response resp, long delay) {
            destination = resp.destination;
            bytes = resp.getBytes();
            ticket = resp.ticket;
            
            resetDelay(delay);
        }

        public final void resetDelay(long delay) {
            timeout = System.nanoTime() + delay;
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
                    // The following line is a temporary fix for an Android ICS bug
                    // http://code.google.com/p/android/issues/detail?id=24748
                    _packet.setLength(_buffer.length);
                    
                    _socket.receive(_packet);
                } catch (SocketException e) {
                    if (!e.getMessage().equalsIgnoreCase("Socket closed"))
                        logger.log(Level.WARNING, "Failed to receive packet, exiting receiver", e);
                    return;
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to receive packet, exiting receiver", e);
                    return;
                }
                
                // Decode it into a request
                Request request = new Request(_packet);
                
                // Extract the command string (to check if this is an ACK)
                String cmd = null;
                try {
                    cmd = request.stream.readUTF().trim();
                    request.reset();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to decode message (perhaps it is ill-formed?)", e);
                    continue;
                }
                
                // If it is an ACK, remove the corresponding outgoing messages,
                // otherwise, send out an ACK and handle the message
                //System.out.println("GOT PACKET " + _socket.getLocalSocketAddress() + " CMD " + cmd);
                if (cmd.equals(UdpConstants.CMD_ACKNOWLEDGE)) {
                    acknowledge(request.ticket);
                } else {
                    // Construct an ack and send it out if there was a valid ticket
                    if (request.ticket != UdpConstants.NO_TICKET) {
                        try {
                            Response response = new Response(request);
                            response.stream.writeUTF(UdpConstants.CMD_ACKNOWLEDGE);
                            //System.out.println("ACKING " + cmd + " : " + response.ticket + " from " + request.source);
                            send(response);
                        } catch (IOException e) {
                            // TODO: more elegant error message
                            throw new RuntimeException("This shouldn't happen.", e);
                        }
                        
                        // If we have seen this ticket before, ignore it
                        if (_oldTickets.contains(request.ticket)) {
                            continue;
                        } else {            
                            if (_oldTickets.size() >= UdpConstants.TICKET_CACHE_SIZE) 
                                _oldTickets.remove(0);
                            _oldTickets.add(request.ticket);
                        }
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
                    //System.out.println("RESENDING [" + response.ttl + "]: " + response.ticket + " to " + response.destination);
                    _socket.send(response.toPacket());
                } catch(SocketException e) { 
                    if (e.getMessage().equalsIgnoreCase("Socket is closed")) {
                        logger.log(Level.WARNING, "Message dropped, server was shutdown.");
                    } else {
                        logger.log(Level.WARNING, "Failed to respond.", e);
                    }
                } catch (IOException e) {
                    // TODO: figure out which errors we need to return on or ignore here
                    logger.log(Level.WARNING, "Failed to resend data.", e);
                    return;
                }

                // Decrement TTL and reset timeout for retransmission
                if (response.ttl > 0) {
                    response.ttl--;
                    response.resetDelay(getRetransmissionTimeout());
                    _responses.offer(response);
                } else {
                    // If the TTL is at zero, report a transmission loss
                    if (_handler != null) {
                        _handler.timeout(response.ticket, response.destination);
                    }
                }
            }
        }
    }
    
    /**
     * Retrieves the current retransmission timeout, which is based on a 
     * round-trip time average plus a retransmission delay.
     * 
     * @return the current retransmission timeout
     */
    public long getRetransmissionTimeout() {
        synchronized(_retransmissionLock) {
            return _retransmissionTimeout;
        }
    }
    
    /**
     * Updates the retransmission timeout using a round-trip time measurement.
     * 
     * @param rtt a measurement of round-trip time (RTT)
     */
    protected void learnRetransmissionTimeout(long rtt) {
        synchronized(_retransmissionLock) {
            _retransmissionTimeout *= 9;
            _retransmissionTimeout /= 10;
            _retransmissionTimeout += (2*rtt + UdpConstants.RETRANSMISSION_DELAY_NS)/10;
        }
    }

    /**
     * Respond to an existing function request.  Prepares the response for
     * automatic retransmission and acknowledgement.
     * 
     * @param response the response to be sent
     */
    public void respond(Response response) {
        try {
            QueuedResponse qr = new QueuedResponse(response, getRetransmissionTimeout());
            _responses.add(qr);
            _socket.send(qr.toPacket());
            //System.out.println("RESPOND " + qr.ticket + " FROM " + _socket.getLocalSocketAddress() + " TO " + qr.destination);
        } catch (SocketException e) {
            if (e.getMessage().equalsIgnoreCase("Socket is closed")) {
                logger.log(Level.WARNING, "Message dropped, server was shutdown.");
            } else {
                logger.log(Level.WARNING, "Failed to respond.", e);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to respond.", e);
        }
    }

    /**
     * Send out a function response that is broadcast to a list of addresses.
     * No retransmission will be done on these messages.  The SocketAddress
     * specified in the Response object is not used in this case.
     *
     * @param response the response to be sent
     * @param destinations the destination addresses to which it will be sent
     */
    public void bcast(Response response, Collection<SocketAddress> destinations) {
        try {
            // TODO: this shouldn't generate a ticket!
            DatagramPacket packet = new QueuedResponse(response, getRetransmissionTimeout()).toPacket();
            
            for (SocketAddress dest : destinations) {
                packet.setSocketAddress(dest);
                _socket.send(packet);
            }
            
            //System.out.println("BCAST " + response.ticket + " FROM " + _socket.getLocalSocketAddress() + " TO " + packet.getSocketAddress());
        } catch (SocketException e) {
            if (e.getMessage().equalsIgnoreCase("Socket is closed")) {
                logger.log(Level.WARNING, "Message dropped, server was shutdown.");
            } else {
                logger.log(Level.WARNING, "Failed to respond.", e);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to respond.", e);
        }
    }
    
    /**
     * Send out a function response that is unicast to the specified address.
     * No retransmission will be done on these messages.
     *
     * @param response the response to be sent
     */
    public void send(Response response) {
        try {
            // TODO: this shouldn't generate a ticket!
            DatagramPacket packet = new QueuedResponse(response, getRetransmissionTimeout()).toPacket();
            _socket.send(packet);
            
            //System.out.println("SEND " + response.ticket + " FROM " + _socket.getLocalSocketAddress() + " TO " + packet.getSocketAddress());
        } catch (SocketException e) {
            if (e.getMessage().equalsIgnoreCase("Socket is closed")) {
                logger.log(Level.WARNING, "Message dropped, server was shutdown.");
            } else {
                logger.log(Level.WARNING, "Failed to respond.", e);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to respond.", e);
        }
    }

    /**
     * Removes all responses that have the corresponding ticket to an already
     * acknowledged response 
     *
     * @param ticket
     */
    public void acknowledge(long ticket) {
        Iterator<QueuedResponse> itr = _responses.iterator();

        while(itr.hasNext()) {
            QueuedResponse resp = itr.next();
            if (resp.ticket == ticket) {
                 // TODO: should this stop after the first one?
                itr.remove();
                
                // Learn the new retransmission rate
                learnRetransmissionTimeout(System.nanoTime() - resp.sentTime);
            }
        }
    }
}
