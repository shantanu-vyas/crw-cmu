package edu.cmu.ri.crw.udp;

import edu.cmu.ri.crw.VehicleServer.SensorType;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.Twist;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import robotutils.Pose3D;

/**
 * Static helper class that contains constants and definitions required by the
 * UDP communications system.
 *
 * @author Pras Velagapudi <psigen@gmail.com>
 */
public class UdpConstants {

    public static final int REGISTRATION_RATE_MS = 1000;
    public static final int REGISTRATION_TIMEOUT_COUNT = 5;

    public static final long INITIAL_RETRY_RATE_NS = TimeUnit.NANOSECONDS.convert(200, TimeUnit.MILLISECONDS);
    public static final long RETRANSMISSION_DELAY_NS = TimeUnit.NANOSECONDS.convert(20, TimeUnit.MILLISECONDS);;
    public static final int RETRY_COUNT = 4;
    public static final long TIMEOUT_NS = (RETRY_COUNT + 1) * INITIAL_RETRY_RATE_NS;
    public static final int NO_TICKET = -1;
    public static final int TICKET_CACHE_SIZE = 100;

    public static final int INITIAL_PACKET_SIZE = 512;
    public static final int MAX_PACKET_SIZE = 4096;
    public static final int MAX_PAYLOAD_SIZE = 512;
    public static final String CMD_ACKNOWLEDGE = "OK";

    /**
     * Enumeration of tunneled commands and the strings used in the UDP packet
     * to represent them.
     */
    public enum COMMAND {
        UNKNOWN(""),
        CMD_REGISTER("HI"),
        CMD_LIST("HL"),
        CMD_CONNECT("CC"),
        CMD_REGISTER_POSE_LISTENER("RPL"),
        CMD_SEND_POSE("_P"),
        CMD_SET_POSE("SP"),
        CMD_GET_POSE("GP"),
        CMD_REGISTER_IMAGE_LISTENER("RIL"),
        CMD_SEND_IMAGE("_I"),
        CMD_CAPTURE_IMAGE("CI"),
        CMD_REGISTER_CAMERA_LISTENER("CIL"),
        CMD_SEND_CAMERA("_C"),
        CMD_START_CAMERA("STC"),
        CMD_STOP_CAMERA("SPC"),
        CMD_GET_CAMERA_STATUS("CS"),
        CMD_REGISTER_SENSOR_LISTENER("RSL"),
        CMD_SEND_SENSOR("_S"),
        CMD_SET_SENSOR_TYPE("SS"),
        CMD_GET_SENSOR_TYPE("GS"),
        CMD_GET_NUM_SENSORS("GNS"),
        CMD_REGISTER_VELOCITY_LISTENER("RVL"),
        CMD_SEND_VELOCITY("_V"),
        CMD_SET_VELOCITY("SV"),
        CMD_GET_VELOCITY("GV"),
        CMD_REGISTER_WAYPOINT_LISTENER("RWL"),
        CMD_SEND_WAYPOINT("_W"),
        CMD_START_WAYPOINTS("STW"),
        CMD_STOP_WAYPOINTS("SPW"),
        CMD_GET_WAYPOINTS("GW"),
        CMD_GET_WAYPOINT_STATUS("GWS"),
        CMD_IS_CONNECTED("IC"),
        CMD_IS_AUTONOMOUS("IA"),
        CMD_SET_AUTONOMOUS("SA"),
        CMD_SET_GAINS("SG"),
        CMD_GET_GAINS("GG");

        COMMAND(String s) {
            str = s;
        }

        public final String str;
        
        static final TreeMap<String, COMMAND> _lookups = new TreeMap<String, COMMAND>();
        static {
            for (COMMAND cmd : COMMAND.values()) {
                _lookups.put(cmd.str, cmd);
            }
        }
        
        public static COMMAND fromStr(String str) {
            COMMAND result = _lookups.get(str);
            return (result == null) ? UNKNOWN : result;
        }
    }

    public static void writeTwist(DataOutputStream out, Twist twist) throws IOException {
        out.writeDouble(twist.dx());
        out.writeDouble(twist.dy());
        out.writeDouble(twist.dz());
        
        out.writeDouble(twist.drx());
        out.writeDouble(twist.dry());
        out.writeDouble(twist.drz());
    }
    
    public static Twist readTwist(DataInputStream in) throws IOException {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();

        double rx = in.readDouble();
        double ry = in.readDouble();
        double rz = in.readDouble();
        
        return new Twist(x,y,z,rx,ry,rz);
    }
    
    public static void writePose(DataOutputStream out, UtmPose utmPose) throws IOException {
        out.writeDouble(utmPose.pose.getX());
        out.writeDouble(utmPose.pose.getY());
        out.writeDouble(utmPose.pose.getZ());

        out.writeDouble(utmPose.pose.getRotation().getW());
        out.writeDouble(utmPose.pose.getRotation().getX());
        out.writeDouble(utmPose.pose.getRotation().getY());
        out.writeDouble(utmPose.pose.getRotation().getZ());

        out.writeByte(utmPose.origin.zone);
        out.writeBoolean(utmPose.origin.isNorth);
    }
    
    public static UtmPose readPose(DataInputStream in) throws IOException {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();

        double qw = in.readDouble();
        double qx = in.readDouble();
        double qy = in.readDouble();
        double qz = in.readDouble();
        
        int utmZone = in.readByte();
        boolean utmHemi = in.readBoolean();
        
        Pose3D pose = new Pose3D(x, y, z, qw, qx, qy, qz);
        Utm utm = new Utm(utmZone, utmHemi);
        
        return new UtmPose(pose, utm);
    }
 
    public static void writeSensorData(DataOutputStream out, SensorData sensor) throws IOException {
        out.writeInt(sensor.channel);
        out.writeByte(sensor.type.ordinal());
        out.writeInt(sensor.data.length);
        for (int i = 0; i < sensor.data.length; ++i)
            out.writeDouble(sensor.data[i]);
    }
    
    public static SensorData readSensorData(DataInputStream in) throws IOException {
        SensorData sensor = new SensorData();
        sensor.channel = in.readInt();
        sensor.type = SensorType.values()[in.readByte()];
        sensor.data = new double[in.readInt()];
        for (int i = 0; i < sensor.data.length; ++i)
            sensor.data[i] = in.readDouble();
        return sensor;
    }
}
