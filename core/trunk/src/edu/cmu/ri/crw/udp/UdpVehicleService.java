package edu.cmu.ri.crw.udp;

// TODO: finish this class!

import edu.cmu.ri.crw.CameraListener;
import edu.cmu.ri.crw.ImageListener;
import edu.cmu.ri.crw.PoseListener;
import edu.cmu.ri.crw.SensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VelocityListener;
import edu.cmu.ri.crw.WaypointListener;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpServer.Request;
import edu.cmu.ri.crw.udp.UdpServer.Response;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A service that registers a vehicle server over UDP to allow control over the 
 * network via a proxy server.
 * 
 * @author Prasanna Velagapudi <psigen@gmail.com>
 */
@SuppressWarnings("LoggerStringConcat")
public class UdpVehicleService implements UdpServer.RequestHandler {
    private static final Logger logger = Logger.getLogger(UdpVehicleService.class.getName());
    private static final SocketAddress DUMMY_ADDRESS = new InetSocketAddress(0);
    
    protected VehicleServer _vehicleServer;
    protected final Object _serverLock = new Object();
    protected final AtomicInteger _imageSeq = new AtomicInteger();
    
     // Start ticket with random offset to prevent collisions across multiple clients
    protected final AtomicLong _ticketCounter = new AtomicLong(new Random().nextLong() << 32);
    
    protected final UdpServer _udpServer;

    protected final List<SocketAddress> _registries = new ArrayList<SocketAddress>();
    protected final Map<SocketAddress, Integer> _poseListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Map<SocketAddress, Integer> _imageListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Map<SocketAddress, Integer> _cameraListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Map<Integer, Map<SocketAddress,Integer>> _sensorListeners = new TreeMap<Integer, Map<SocketAddress, Integer>>();
    protected final Map<SocketAddress, Integer> _velocityListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Map<SocketAddress, Integer> _waypointListeners = new LinkedHashMap<SocketAddress, Integer>();
    protected final Timer _registrationTimer = new Timer();

    public UdpVehicleService(int port) {
        _udpServer = (port > 0) ? new UdpServer(port) : new UdpServer();
        _udpServer.setHandler(this);
        _udpServer.start();
        
        _registrationTimer.scheduleAtFixedRate(_registrationTask, 0, UdpConstants.REGISTRATION_RATE_MS);
    }
    
    public UdpVehicleService() {
        this(-1);
    }
    
    public UdpVehicleService(VehicleServer server) {
        this();
        setServer(server);
    }
    
    public UdpVehicleService(int port, VehicleServer server) {
        this(port);
        setServer(server);
    }
    
    public SocketAddress getSocketAddress() {
        return _udpServer.getSocketAddress();
    }
    
    public final void setServer(VehicleServer server) {
        synchronized(_serverLock) {
            if (_vehicleServer != null) unregister();
            _vehicleServer = server;
            if (_vehicleServer != null) register();
        }
    }
    
    private void register() {
        _vehicleServer.addCameraListener(_handler);
        _vehicleServer.addImageListener(_handler);
        _vehicleServer.addPoseListener(_handler);
        _vehicleServer.addVelocityListener(_handler);
        _vehicleServer.addWaypointListener(_handler);
        
        for (int i = 0; i < _vehicleServer.getNumSensors(); ++i) {
            _vehicleServer.addSensorListener(i, _handler);
        }
    }
    
    private void unregister() {
        _vehicleServer.removeCameraListener(_handler);
        _vehicleServer.removeImageListener(_handler);
        _vehicleServer.removePoseListener(_handler);
        _vehicleServer.removeVelocityListener(_handler);
        _vehicleServer.removeWaypointListener(_handler);
        
        for (int i = 0; i < _vehicleServer.getNumSensors(); ++i) {
            _vehicleServer.removeSensorListener(i, _handler);
        }
    }
    
    public final VehicleServer getServer() {
        synchronized(_serverLock) {
            return _vehicleServer;
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

    @Override
    public void received(Request req) {

        try {
            final String command = req.stream.readUTF();
            Response resp = new Response(req);
            resp.stream.writeUTF(command);
            
            // TODO: remove me
            //logger.log(Level.INFO, "Received command " + req.ticket + ": " + command + ", " + UdpConstants.COMMAND.fromStr(command));

            switch (UdpConstants.COMMAND.fromStr(command)) {
                case CMD_REGISTER_POSE_LISTENER:
                    synchronized(_poseListeners) {
                        _poseListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_SET_POSE:
                    _vehicleServer.setPose(UdpConstants.readPose(req.stream));
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_POSE:
                    UdpConstants.writePose(resp.stream, _vehicleServer.getPose());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_IMAGE_LISTENER:
                    synchronized(_imageListeners) {
                        _imageListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_CAPTURE_IMAGE:
                    byte[] image = _vehicleServer.captureImage(req.stream.readInt(), req.stream.readInt());
                    resp.stream.writeInt(image.length);
                    resp.stream.write(image);
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_CAMERA_LISTENER:
                    synchronized(_cameraListeners) {
                        _cameraListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_START_CAMERA:
                    _vehicleServer.startCamera(
                            req.stream.readInt(), req.stream.readDouble(),
                            req.stream.readInt(), req.stream.readInt());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_STOP_CAMERA:
                    _vehicleServer.stopCamera();
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_CAMERA_STATUS:
                    resp.stream.writeByte(_vehicleServer.getCameraStatus().ordinal());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); 
                    break;
                case CMD_REGISTER_SENSOR_LISTENER:
                    synchronized(_sensorListeners) {
                        int channel = req.stream.readInt();
                        
                        // Retrive the sensor sublisteners
                        Map<SocketAddress, Integer> _listeners = _sensorListeners.get(channel);
                        if (_listeners == null) {
                            _listeners = new LinkedHashMap<SocketAddress, Integer>();
                            _sensorListeners.put(channel, _listeners);
                        }
                        
                        // Add the address to the appropriate sublistener list
                        _listeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_SET_SENSOR_TYPE:
                    _vehicleServer.setSensorType(req.stream.readInt(), 
                            VehicleServer.SensorType.values()[req.stream.readByte()]);
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_SENSOR_TYPE:
                    resp.stream.writeByte(_vehicleServer.getSensorType(req.stream.readInt()).ordinal());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); 
                    break;
                case CMD_GET_NUM_SENSORS:
                    resp.stream.writeInt(_vehicleServer.getNumSensors());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_VELOCITY_LISTENER:
                    synchronized(_velocityListeners) {
                        _velocityListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_SET_VELOCITY:
                    _vehicleServer.setVelocity(UdpConstants.readTwist(req.stream));
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_VELOCITY:
                    UdpConstants.writeTwist(resp.stream, _vehicleServer.getVelocity());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_REGISTER_WAYPOINT_LISTENER:
                    synchronized(_waypointListeners) {
                        _waypointListeners.put(req.source, UdpConstants.REGISTRATION_TIMEOUT_COUNT);
                    }
                    break;
                case CMD_START_WAYPOINTS:
                    UtmPose[] swPoses = new UtmPose[req.stream.readInt()];
                    for (int i = 0; i < swPoses.length; ++i) {
                        swPoses[i] = UdpConstants.readPose(req.stream);
                    }
                    _vehicleServer.startWaypoints(swPoses, req.stream.readUTF());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_STOP_WAYPOINTS:
                    _vehicleServer.stopWaypoints();
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_GET_WAYPOINTS:
                    UtmPose[] gwPoses = _vehicleServer.getWaypoints();
                    resp.stream.writeInt(gwPoses.length);
                    for (int i = 0; i < gwPoses.length; ++i) {
                        UdpConstants.writePose(resp.stream, gwPoses[i]);
                    }
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_GET_WAYPOINT_STATUS:
                    resp.stream.writeByte(_vehicleServer.getWaypointStatus().ordinal());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_IS_CONNECTED:
                    resp.stream.writeBoolean((_vehicleServer != null) && _vehicleServer.isConnected());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_IS_AUTONOMOUS:
                    resp.stream.writeBoolean(_vehicleServer.isAutonomous());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_SET_AUTONOMOUS:
                    _vehicleServer.setAutonomous(req.stream.readBoolean());
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp); // Send void response
                    break;
                case CMD_SET_GAINS:
                    int sgAxis = req.stream.readInt();
                    double[] sgGains = new double[req.stream.readInt()];
                    for (int i = 0; i < sgGains.length; ++i) {
                        sgGains[i] = req.stream.readDouble();
                    }
                    _vehicleServer.setGains(sgAxis, sgGains);
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_GET_GAINS:
                    double[] ggGains = _vehicleServer.getGains(req.stream.readInt());
                    resp.stream.writeInt(ggGains.length);
                    for (int i = 0; i < ggGains.length; ++i) {
                        resp.stream.writeDouble(ggGains[i]);
                    }
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(resp);
                    break;
                case CMD_CONNECT:
                    // Unpack the forwarded server
                    String hostname = req.stream.readUTF();
                    int port = req.stream.readInt();
                    SocketAddress addr = new InetSocketAddress(hostname, port);
                    
                    // Send off a one-time command to the forwarded server
                    Response r = new Response(req.ticket, addr);
                    r.stream.writeUTF(UdpConstants.COMMAND.CMD_CONNECT.str);
                    if (resp.ticket != UdpConstants.NO_TICKET)
                        _udpServer.respond(r);
                    break;
                default:
                    String warning = "Ignoring unknown command: " + command;
                    logger.log(Level.WARNING, warning);
            }
        } catch (IOException e) {
            String warning = "Failed to parse request: " + req.ticket;
            logger.log(Level.WARNING, warning);
        }

    }

    @Override
    public void timeout(long ticket, SocketAddress destination) {
        String warning = "No response for: " + ticket + " @ " + destination;
        logger.warning(warning);
    }

    /**
     * Terminates service processes and de-registers the service from a 
     * registry, if one was being used.
     */
    public void shutdown() {
        _udpServer.stop();
        _registrationTimer.cancel();
        _registrationTimer.purge();
    }
    
    // TODO: Clean up old streams!!
    private final StreamHandler _handler = new StreamHandler();
    
    private class StreamHandler implements PoseListener, ImageListener, CameraListener, SensorListener, VelocityListener, WaypointListener {

        @Override
        public void receivedPose(UtmPose pose) {
            // Quickly check if anyone is listening
            synchronized(_poseListeners) {
                if (_poseListeners.isEmpty()) return;
            }
            
            try {
                // Construct message
                Response resp = new Response(UdpConstants.NO_TICKET, DUMMY_ADDRESS);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_POSE.str);
                UdpConstants.writePose(resp.stream, pose);
            
                // Send to all listeners
                synchronized(_poseListeners) {
                    _udpServer.bcast(resp, _poseListeners.keySet());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize pose");
            }
        }

        @Override
        public void receivedImage(byte[] image) {
            // Quickly check if anyone is listening
            synchronized(_imageListeners) {
                if (_imageListeners.isEmpty()) return;
            }
            
            try {
                final int imageSeq = _imageSeq.incrementAndGet();
                
                // Figure out how many pieces into which to fragment the image
                final int totalIdx = (image.length - 1) / UdpConstants.MAX_PAYLOAD_SIZE + 1;
                
                // Transmit each piece in a separate packet
                for (int pieceIdx = 0; pieceIdx < totalIdx; ++pieceIdx) {
                    
                    // Compute the length of this piece
                    int pieceLen = (pieceIdx + 1 < totalIdx) ? UdpConstants.MAX_PAYLOAD_SIZE : image.length - pieceIdx*UdpConstants.MAX_PAYLOAD_SIZE;
                    
                    // Send to all listeners
                    synchronized(_imageListeners) {
                        for (SocketAddress il : _imageListeners.keySet()) {
                            // Construct message
                            // TODO: find a more efficient way to handle this serialization
                            Response resp = new Response(_ticketCounter.incrementAndGet(), il);
                            resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_IMAGE.str);
                            resp.stream.writeInt(imageSeq);
                            resp.stream.writeInt(totalIdx);
                            resp.stream.writeInt(pieceIdx);

                            resp.stream.writeInt(pieceLen);
                            resp.stream.write(image, pieceIdx*UdpConstants.MAX_PAYLOAD_SIZE, pieceLen);

                            _udpServer.respond(resp);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize image");
            }
        }

        @Override
        public void imagingUpdate(CameraState status) {
            // Quickly check if anyone is listening
            synchronized(_cameraListeners) {
                if (_cameraListeners.isEmpty()) return;
            }
            
            try {
                // Construct message
                Response resp = new Response(UdpConstants.NO_TICKET, DUMMY_ADDRESS);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_CAMERA.str);
                resp.stream.writeByte(status.ordinal());
            
                // Send to all listeners
                synchronized(_cameraListeners) {
                    _udpServer.bcast(resp, _cameraListeners.keySet());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize camera");
            }
        }

        @Override
        public void receivedSensor(SensorData sensor) {
            // Quickly check if anyone is listening
            synchronized(_sensorListeners) {
                if (!_sensorListeners.containsKey(sensor.channel)) return;
            }
            
            try {
                // Construct message
                Response resp = new Response(UdpConstants.NO_TICKET, DUMMY_ADDRESS);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_SENSOR.str);
                UdpConstants.writeSensorData(resp.stream, sensor);
            
                // Send to all listeners
                synchronized(_sensorListeners) {
                    Map<SocketAddress, Integer> _listeners = _sensorListeners.get(sensor.channel);
                    _udpServer.bcast(resp, _listeners.keySet());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize sensor " + sensor.channel);
            }
        }

        @Override
        public void receivedVelocity(Twist velocity) {
            // Quickly check if anyone is listening
            synchronized(_velocityListeners) {
                if (_velocityListeners.isEmpty()) return;
            }
            
            try {
                // Construct message
                Response resp = new Response(UdpConstants.NO_TICKET, DUMMY_ADDRESS);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_VELOCITY.str);
                UdpConstants.writeTwist(resp.stream, velocity);
            
                // Send to all listeners
                synchronized(_velocityListeners) {
                    _udpServer.bcast(resp, _velocityListeners.keySet());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize camera");
            }
        }

        @Override
        public void waypointUpdate(WaypointState status) {
            // Quickly check if anyone is listening
            synchronized(_waypointListeners) {
                if (_waypointListeners.isEmpty()) return;
            }
            
            try {
                // Construct message
                Response resp = new Response(UdpConstants.NO_TICKET, DUMMY_ADDRESS);
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_SEND_WAYPOINT.str);
                resp.stream.writeByte(status.ordinal());
            
                // Send to all listeners
                synchronized(_waypointListeners) {
                    _udpServer.bcast(resp, _waypointListeners.keySet());
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize camera");
            }
        }
    }
    
    /**
     * Takes a list of registered listeners and their timeout counts, and 
     * decrements the timeouts.  If a listener count hits zero, it is removed.
     * 
     * @param registrationList the list that should be updated
     */
    protected void updateRegistrations(Map<SocketAddress, Integer> registrationList) {
        synchronized(registrationList) {
            for (Iterator<Map.Entry<SocketAddress, Integer>> it = registrationList.entrySet().iterator(); it.hasNext();) {
                Map.Entry<SocketAddress, Integer> e = it.next();
                if (e.getValue() == 0) {
                    it.remove();
                } else {
                    e.setValue(e.getValue() - 1);
                }
            }
        }
    }
    
    protected TimerTask _registrationTask = new TimerTask() {
        final Response resp = new Response(UdpConstants.NO_TICKET, DUMMY_ADDRESS);
        {
            try {
                resp.stream.writeUTF(UdpConstants.COMMAND.CMD_REGISTER.str);
                resp.stream.writeUTF("Vehicle");
            } catch (IOException e) {
                throw new RuntimeException("Failed to construct registration message.", e);
            }
        }
        
        @Override
        public void run() {
            // Send registration commands to all the specified registries
            synchronized(_registries) {
                _udpServer.bcast(resp, _registries);
            }

            // Update each of the registration lists to remove outdated listeners
            updateRegistrations(_poseListeners);
            updateRegistrations(_imageListeners);
            updateRegistrations(_cameraListeners);
            updateRegistrations(_velocityListeners);
            updateRegistrations(_waypointListeners);
            synchronized(_sensorListeners) {
                for (Map<SocketAddress, Integer> sensorListener : _sensorListeners.values())
                    updateRegistrations(sensorListener);
            }
        }
    };
}
