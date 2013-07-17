/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.floodtest;

import edu.cmu.ri.crw.ImagingObserver;
import edu.cmu.ri.crw.QuaternionUtils;
import edu.cmu.ri.crw.VehicleImageListener;
import edu.cmu.ri.crw.VehicleSensorListener;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.VehicleServer.CameraState;
import edu.cmu.ri.crw.VehicleServer.WaypointState;
import edu.cmu.ri.crw.VehicleStateListener;
import edu.cmu.ri.crw.WaypointObserver;
import edu.cmu.ri.crw.ros.RosVehicleProxy;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import gov.nasa.worldwind.render.markers.Marker;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import org.ros.message.crwlib_msgs.SensorData;
import org.ros.message.crwlib_msgs.UtmPose;
import org.ros.message.crwlib_msgs.UtmPoseWithCovarianceStamped;
import org.ros.message.sensor_msgs.CompressedImage;

/**
 * @todo Need a flag for autonomous or under human control
 * 
 * @author pscerri
 */
public class BoatSimpleProxy extends Thread {

    // @todo Needs to be configurable
    private double atWaypointTolerance = 30.0;
    private final String name;
    private final ArrayList<Marker> markers;
    private final ArrayList<Marker> ownMarkers = new ArrayList<Marker>();
    private final Material material;
    final AtomicBoolean isConnected = new AtomicBoolean(false);
    // Tasking stuff
    // private ArrayList<RoleAgent> roles = new ArrayList<RoleAgent>();
    private ProxyManager proxyManager = new ProxyManager();
    private Position currWaypoint = null;
    private Position currLoc = null;
    private UTMCoord currUtm = null;
    // private RoleAgent currRole = null;
    private int lngZone = -1;
    // Simulated
    private double fuelLevel = 1.0;
    private double fuelUsageRate = 1.0e-2;
    // ROS update
    private boolean _waypointsWereUpdated;
    VehicleStateListener _stateListener;
    VehicleSensorListener _sensorListener;
    int _boatNo;
    UtmPose _pose;
    volatile boolean _isShutdown = false;
    URI masterURI = null;
    //
    // New Control variables
    //

    private enum StateEnum {

        IDLE, WAYPOINT, PATH, AREA
    };
    private StateEnum state = StateEnum.IDLE;
    final Queue<UtmPose> _waypoints = new LinkedList<UtmPose>();
    private UtmPose currentWaypoint = null;
    BoatSimpleProxy self = null;
    RosVehicleProxy _server;
    private Polygon currentArea = null;
    private Polyline currentPath = null;
    private Color color = null;

    public BoatSimpleProxy(final String name, final ArrayList<Marker> markers, Color color, final int boatNo, URI masterURI, String nodeName) throws URISyntaxException {

        self = this;

        this.masterURI = masterURI;
        this.name = name;
        this.markers = markers;
        this.material = new Material(color);
        this.color = color;

        //Initialize the boat by initalizing a proxy server for it
        // Connect to boat
        _boatNo = boatNo;
        _server = new RosVehicleProxy(masterURI, nodeName);


        _server.startCamera(0, 1.0, 640, 480, new ImagingObserver() {

            @Override
            public void imagingUpdate(CameraState status) {
                System.err.println("IMAGES: " + status);
            }
        });

        _server.addImageListener(new VehicleImageListener() {

            public void receivedImage(CompressedImage ci) {
                // Take a picture, and put the resulting image into the panel
                try {
                    BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(ci.data));
                    if (image != null) {
                        ImagePanel.addImage(image);
                    } else {
                        System.err.println("Failed to decode image.");
                    }
                } catch (IOException ex) {
                    System.err.println("Failed to decode image: " + ex);
                }

            }
        });

        _stateListener = new VehicleStateListener() {

            Marker marker = null;

            public void receivedState(UtmPoseWithCovarianceStamped upwcs) {
                _pose = new UtmPose();
                _pose.pose = upwcs.pose.pose.pose.clone();
                _pose.utm = upwcs.utm.clone();

                // System.out.println("Pose: [" + _pose.pose.position.x + ", " + _pose.pose.position.y + "], zone = " + _pose.utm.zone);

                try {

                    int longZone = _pose.utm.zone;

                    // Convert hemisphere to arbitrary worldwind codes
                    String wwHemi = (_pose.utm.isNorth) ? "gov.nasa.worldwind.avkey.North" : "gov.nasa.worldwind.avkey.South";

                    // Fill in UTM data structure
                    // System.out.println("Converting from " + longZone + " " + wwHemi + " " + _pose.pose.position.x + " " + _pose.pose.position.y);
                    UTMCoord boatPos = UTMCoord.fromUTM(longZone, wwHemi, _pose.pose.position.x, _pose.pose.position.y);

                    // UTMCoord boatPos = UTMCoord.fromLatLon(Angle.fromDegrees(14.22), Angle.fromDegrees(121.32));
                    // System.out.println("Boatpos: " + boatPos.getHemisphere() + " " + boatPos.getZone() + " " + boatPos.getLatitude() + " " + boatPos.getLongitude());

                    LatLon latlon = new LatLon(boatPos.getLatitude(), boatPos.getLongitude());

                    /*
                    // Convert yaw (0 at +x, CCW) to heading (0 at +y, CW)
                    
                    _worldPanel.boat.setHeading(Angle.fromRadians(Math.PI / 2.0
                    - QuaternionUtils.toYaw(location.pose.orientation)));
                     * 
                     */

                    Position p = new Position(latlon, 0.0);

                    if (marker == null) {
                        marker = new BoatMarker(self, p, new BasicMarkerAttributes(material, BasicMarkerShape.ORIENTED_SPHERE, 0.9));
                        markers.add(marker);
                    }
                    marker.setPosition(p);
                    marker.setHeading(Angle.fromRadians(Math.PI / 2.0 - QuaternionUtils.toYaw(_pose.pose.orientation)));

                } catch (Exception e) {
                    System.err.println("Invalid pose received: " + e);
                }



                // @todo Report locations commented out
                // reportLoc(_boatNo, UtmPoseToDouble(_pose.pose));
            }
        };

        _sensorListener = new VehicleSensorListener() {

            public void receivedSensor(SensorData sd) {
                //TODO Perform Sensor value assignment correctly
                //Since the sensor Update is called just after state update
                //There shouldn't be too much error with regards to the
                //position of the sampling point

                /* @todo Observation handling commented out
                try {
                Observation o = new Observation(
                "Sensor" + sd.type,
                sd.data[0],
                UtmPoseToDouble(_pose.pose),
                _pose.utm.zone,
                _pose.utm.isNorth);
                
                System.out.println("Data:" + sd.data[0]);
                reportObs(o);
                
                } catch (NullPointerException e) {
                System.out.println("Problem in receivedSensor, null pointer " + sd + " " + _pose);
                }
                 * 
                 */
            }
        };

        System.out.println("New boat created, boat # " + _boatNo);

        //add Listeners
        _server.addStateListener(_stateListener);
        _server.addSensorListener(0, _sensorListener);

    }

    public void stopBoat() {
        // @todo How to stop the boat

        _waypoints.clear();
        _server.stopWaypoint();

        clearRenderables();

        state = StateEnum.IDLE;
    }

    private void clearRenderables() {
        if (currentArea != null) {
            OperatorConsole.removeRenderable(currentArea);
        }
        if (currentPath != null) {
            OperatorConsole.removeRenderable(currentPath);
        }
    }

    public void run() {
    }

    public void setWaypoints(Polyline pLine) {
        clearRenderables();
        currentPath = pLine;
        pLine.setColor(color);
        setWaypoints(pLine.getPositions());
        state = StateEnum.PATH;
    }

    public void setWaypoints(Iterable<Position> ps) {
        _waypoints.clear();

        for (Position position : ps) {
            UTMCoord utm = UTMCoord.fromLatLon(position.latitude, position.longitude);
            UtmPose pose = new UtmPose();
            pose.pose.position.x = utm.getEasting();
            pose.pose.position.y = utm.getNorthing();
            pose.utm.isNorth = utm.getHemisphere().contains("North");
            pose.utm.zone = (byte) utm.getZone();
            _waypoints.add(pose);
        }

        setWaypoint(_waypoints.poll());
    }

    public void setWaypoint(Position p) {

        clearRenderables();

        if (p == null) {
            System.out.println("Null Position waypoint provided to BoatSimpleProxy");
            return;
        }

        UTMCoord utm = UTMCoord.fromLatLon(p.latitude, p.longitude);

        UtmPose wputm = new UtmPose();
        wputm.pose.position.x = utm.getEasting();
        wputm.pose.position.y = utm.getNorthing();
        wputm.utm.isNorth = utm.getHemisphere().contains("North");
        wputm.utm.zone = (byte) utm.getZone();

        System.out.println("Setting waypoint for " + this + " to " + wputm);

        setWaypoint(wputm);

        state = StateEnum.WAYPOINT;
    }

    public void setWaypoint(UtmPose wputm) {

        if (wputm == null) {
            System.out.println("Null utm waypoint provided to BoatSimpleProxy");
            return;
        }

        currentWaypoint = wputm;
        _server.startWaypoint(wputm, null, new WaypointObserver() {

            @Override
            public void waypointUpdate(WaypointState status) {
                if (status == WaypointState.DONE) {
                    setWaypoint(_waypoints.poll());
                } else {
                    // @todo Handle this status
                    System.out.println("Unhandled STATUS: " + status);
                }
            }
        });

    }

    public void setArea(Polygon poly) {

        clearRenderables();
       
        ShapeAttributes normalAttributes = new BasicShapeAttributes();
        normalAttributes.setInteriorMaterial(new Material(color));
        normalAttributes.setOutlineOpacity(0.5);
        normalAttributes.setInteriorOpacity(0.2);
        normalAttributes.setOutlineMaterial(new Material(color));
        normalAttributes.setOutlineWidth(2);
        normalAttributes.setDrawOutline(true);
        normalAttributes.setDrawInterior(true);
        normalAttributes.setEnableLighting(true);

        ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
        highlightAttributes.setOutlineMaterial(Material.WHITE);
        highlightAttributes.setOutlineOpacity(1);
        poly.setAttributes(normalAttributes);
        poly.setHighlightAttributes(highlightAttributes);

        currentArea = poly;

        ArrayList<Position> ps = new ArrayList<Position>();

        for (LatLon ll : poly.getOuterBoundary()) {
            Position pos = new Position(ll, 0.0);
            ps.add(pos);
        }

        setWaypoints(ps);

        state = StateEnum.AREA;
    }

    public Position getCurrWaypoint() {
        return currWaypoint;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public Color getColor() {
        return color;
    }

    private boolean at(Position p1, Position p2) {
        UTMCoord utm1 = UTMCoord.fromLatLon(p1.latitude, p1.longitude);
        UTMCoord utm2 = UTMCoord.fromLatLon(p2.latitude, p2.longitude);

        // @todo This only really works for short distances
        double dx = utm1.getEasting() - utm2.getEasting();
        double dy = utm1.getNorthing() - utm2.getNorthing();

        double dist = Math.sqrt(dx * dx + dy * dy);

        //System.out.println("Dist to waypoint now: " + dist);

        if (utm1.getHemisphere().equalsIgnoreCase(utm2.getHemisphere()) && utm1.getZone() == utm2.getZone()) {
            return dist < atWaypointTolerance;
        } else {
            return false;
        }

    }

    public VehicleServer getVehicleServer() {
        return _server;
    }

    public String toString() {
        return name + "@" + (masterURI == null ? "Unknown" : masterURI.toString());
    }
}
