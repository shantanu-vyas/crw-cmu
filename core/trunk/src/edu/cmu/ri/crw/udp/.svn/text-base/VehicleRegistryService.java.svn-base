package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.udp.UdpServer.Request;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
public class VehicleRegistryService {
    private static final Logger logger = Logger.getLogger(VehicleRegistryService.class.getName());
    
    public static final int DEFAULT_UDP_PORT = 6077;
    
    protected final UdpServer _udpServer;
    protected final Timer _registrationTimer = new Timer();
    protected final Map<SocketAddress, Client> _clients = new LinkedHashMap<SocketAddress, Client>();
    
    protected static class Client {
        int ttl;
        String name;
        SocketAddress addr;
    }
    
    public VehicleRegistryService() {
        this(DEFAULT_UDP_PORT);
    }
    
    public VehicleRegistryService(int udpPort) {
        _udpServer = new UdpServer(udpPort);
        _udpServer.setHandler(_handler);
        _udpServer.start();
        
        _registrationTimer.scheduleAtFixedRate(_registrationTask, 0, UdpConstants.REGISTRATION_RATE_MS);
    }
    
    public void shutdown() {
        _udpServer.stop();
    }

    private final UdpServer.RequestHandler _handler = new UdpServer.RequestHandler() {
        
        @Override
        public void received(Request req) {
            try {
                final String command = req.stream.readUTF();
                
                switch (UdpConstants.COMMAND.fromStr(command)) {
                case CMD_REGISTER:
                    
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
                    break;
                case CMD_CONNECT:
                    
                    // Unpack address to which to connect
                    String hostname = req.stream.readUTF();
                    int port = req.stream.readInt();
                    InetSocketAddress addr = new InetSocketAddress(hostname, port);
                    
                    // Forward this connection request to the server in question
                    UdpServer.Response respCon = new UdpServer.Response(req.ticket, addr);
                    respCon.stream.writeUTF(command);
                    respCon.stream.writeUTF(((InetSocketAddress)req.source).getAddress().getHostAddress());
                    respCon.stream.writeInt(((InetSocketAddress)req.source).getPort());
                    _udpServer.respond(respCon);
                    break;
                case CMD_LIST:
                    
                    // Create a response to the same client
                    UdpServer.Response respList = new UdpServer.Response(req);
                    respList.stream.writeUTF(command);
                    
                    // List all of the clients
                    synchronized(_clients) {
                        respList.stream.writeInt(_clients.size());
                        for (Map.Entry<SocketAddress, Client> e : _clients.entrySet()) {
                            respList.stream.writeUTF(e.getValue().name);
                            respList.stream.writeUTF(((InetSocketAddress)e.getKey()).getAddress().getHostAddress());
                            respList.stream.writeInt(((InetSocketAddress)e.getKey()).getPort());
                        }
                    }
                    _udpServer.respond(respList);
                    break;
                default:
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
    };
    
    // Removes outdated registrations from client list
    protected TimerTask _registrationTask = new TimerTask() {
        @Override
        public void run() {
            synchronized(_clients) {
                for (Iterator<Map.Entry<SocketAddress, Client>> it = _clients.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<SocketAddress, Client> client = it.next();
                    if (client.getValue().ttl == 0) {
                        it.remove();
                    } else {
                        client.getValue().ttl--;
                    }
                }
            }            
        }
    };
    
    /**
     * Returns a map containing the current set of clients.
     * 
     * @return a map from the socket address of clients to their text names
     */
    public Map<SocketAddress, String> getClients() {
        HashMap<SocketAddress, String> map = new LinkedHashMap<SocketAddress, String>();
        
        synchronized(_clients) {
            for (Client client : _clients.values()) {
                map.put(client.addr, client.name);
            }
        }
        
        return map;
    }
    
    /**
     * Simple startup script that runs the VehicleRegistryService using the
     * default udp port and prints a list of connected clients.
     * 
     * @param args these arguments will be ignored
     */
    public static void main(String args[]) {
        final VehicleRegistryService service = new VehicleRegistryService();
        
        // Periodically print the registered clients
        Timer printer = new Timer();
        printer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Collection<Map.Entry<SocketAddress, String>> clients = service.getClients().entrySet();
                if (clients.size() > 0) {                
                    System.out.println("CLIENT LIST:");
                    for (Map.Entry<SocketAddress, String> e : clients) {
                        System.out.println("\t" + e.getValue() + " : " + e.getKey());
                    }
                } else {
                    System.out.println("NO CLIENTS.");
                }
            }
        }, 0, 1000);
    }
}
