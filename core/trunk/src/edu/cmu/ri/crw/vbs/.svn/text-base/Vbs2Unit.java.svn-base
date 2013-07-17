/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.vbs;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Simple command interface for instantiating and controlling a VBS2 unit.
 *
 * @author pkv
 */
public class Vbs2Unit extends Vbs2Link {

    public static final int DEFAULT_UPDATE_PERIOD_MS = 2000;
    private static double[] UNKNOWN_VALS = new double[] {Double.NaN, Double.NaN, Double.NaN};

    protected final String _name;
    protected final Vbs2Constants.Object _type;
    protected final List<double[]> _waypoints;

    protected final double[] _startPosition;
    protected final double[] _startRotation;

    public Vbs2Unit(Vbs2Constants.Object type, double[] position) {
        this(randomName(), type, position, new double[] {0.0,0.0,0.0});
    }

    public Vbs2Unit(Vbs2Constants.Object type, double[] position, double[] rotation) {
        this(randomName(), type, position, rotation);
    }

    public Vbs2Unit(String unitName, Vbs2Constants.Object type, double[] position) {
        this(unitName, type, position, new double[] {0.0,0.0,0.0});
    }

    public Vbs2Unit(String unitName, Vbs2Constants.Object type, double[] position, double[] rotation) {
        _type = type;
        _name = unitName;
        _startPosition = position;
        _startRotation = rotation;
        _waypoints = new Vbs2Utils.WaypointList(this, group());
    }

    protected static String randomName() {
        return "U" + UUID.randomUUID().toString().substring(0,8);
    }

    @Override
    public void connect(String hostname, int port) {
        
        // Connect to server
        super.connect(hostname, port);

        // Make VBS2 unit on successful connection
        if (isConnected())
            create();
    }

    @Override
    public void disconnect() {

        // Clean up VBS2 unit
        destroy();

        // Disconnect from server
        super.disconnect();
    }

    protected void create() {
        StringBuilder createCommand = new StringBuilder();

        // If an object already exists, destroy it
        // >> boolean = isnil variable
        // >> deleteVehicle object
        // >> deleteGroup group
        createCommand.append(
                "if(not isnil \"").append(name()).append("\")then{"
                + "{deleteVehicle _x}foreach(units ").append(group()).append(");"
                + "deleteVehicle (vehicle ").append(name()).append(");"
                + "deleteGroup ").append(group()).append(
                "};");

        // Make sure our side is initialized
        // >> sidehq = createCenter side
        createCommand.append("_westHq=createCenter west;");

        // Do unit-type specific initialization
        String spawnCommand;
        if (type().type == Vbs2Constants.Type.UNIT) {
            // Create a group for this new unit
            // >> group = createGroup side
            spawnCommand = group() + "=createGroup west;";

            // Create a member of the appropriate unit
            // >> type createUnit [ position, group, init, skill, rank]
            spawnCommand += "\"" + type().ident + "\" createUnit ["
                    + Arrays.toString(_startPosition) + "," // position
                    + group() + "," // group
                    + "\"" + name() + "=this\"," // init
                    + "0.5," // skill
                    + "\"private\"" // rank
                    + "];";
        } else {
            // Create a member of the appropriate vehicle
            // >> object = createVehicle [class, position, markers, placement, special]
            spawnCommand = name() + " = createVehicle ["
                    + "\"" + type().ident + "\","  //class
                    + Arrays.toString(_startPosition) + "," // position
                    + "[]," // markers
                    + "0," // placement
                    + (type().type == Vbs2Constants.Type.AERIAL ? "\"FLY\"" : "\"NONE\"") // special
                    + "];";

            // Create the group for this vehicle
            // >> group = createGroup side
            spawnCommand += group() + "=createGroup west;";

            // Create a leader and place in vehicle
            spawnCommand += "_unit=" + group() + " createUnit [\"" + Vbs2Constants.Object.CREW.ident + "\",[0,0,0],[],0,\"NONE\"];";
            spawnCommand += "_unit moveInDriver " + name() + ";";
        }
        createCommand.append(spawnCommand);

        // Take the entire initialization string and execute it
        this.send(createCommand.toString());
    }

    protected void destroy() {
        StringBuilder destroyCommand = new StringBuilder();
    
        // If our object still exists, destroy it
        // >> boolean = isnil variable
        // >> deleteVehicle object
        // >> deleteGroup group
        destroyCommand.append(
                "if (not isnil \"").append(name()).append("\") then {"
                + "{deleteVehicle _x}foreach(units ").append(group()).append(");"
                + "deleteVehicle (vehicle ").append(name()).append(");"
                + "deleteGroup ").append(group()).append(
                "};");
        
        this.send(destroyCommand.toString());
    }

    public final String name() {
        return _name;
    }

    public final String group() {
        return "G" + name();
    }

    public Vbs2Constants.Object type() {
        return _type;
    }

    public List<double[]> waypoints() {
        return _waypoints;
    }

    public void gotoWaypoint(int i) {
        send(group() + " setCurrentWaypoint [" + group() + ",0];"
                + group() + " setCurrentWaypoint [" + group() + "," + (i+1) + "]");
    }

    public double[] position() {
        if (!isConnected())
            return UNKNOWN_VALS;
        return Vbs2Utils.parseArray(evaluate("position " + name()));
    }
    
    public double[] rotation() {
        if (!isConnected())
            return UNKNOWN_VALS;

        // TODO: finish implementation for other dimensions
        double[][] rMat = rotationMatrix();
        return new double[] { Double.NaN, Double.NaN, -Math.atan2(rMat[1][0], rMat[0][0]) };
    }

    public double[][] rotationMatrix() {
        if (!isConnected())
            return new double[][]{UNKNOWN_VALS, UNKNOWN_VALS, UNKNOWN_VALS};
        double[][] rotationMatrix = new double[3][];

        // Get the forward and up vectors for unit
        String output = evaluate("format[\"%1;%2\",vectorDir " + name() + ",vectorUp " + name() + "]");
        String[] outputArgs = output.substring(1, output.length()-1).split(";");

        // Assume x-axis is forward
        rotationMatrix[0] = Vbs2Utils.parseArray(outputArgs[0]);

        // Assume z-axis is up
        rotationMatrix[2] = Vbs2Utils.parseArray(outputArgs[1]);
        
        // Assume y-axis is left (compute using cross product)
        rotationMatrix[1] = new double[] {
            rotationMatrix[2][1] * rotationMatrix[0][2] - rotationMatrix[2][2] * rotationMatrix[0][1],
            rotationMatrix[2][2] * rotationMatrix[0][0] - rotationMatrix[2][0] * rotationMatrix[0][2],
            rotationMatrix[2][0] * rotationMatrix[0][1] + rotationMatrix[2][1] * rotationMatrix[0][0]
        };

        // TODO: should this be inverted?
        return rotationMatrix;
    }

    public Origin origin() {
        String origin = evaluate("getOrigin");
        origin = origin.replaceAll("\\[", "");
        origin = origin.replaceAll("\\]", "");
        String[] args = origin.split(",");

        return new Origin(
                Double.parseDouble(args[0]),
                Double.parseDouble(args[1]),
                Integer.parseInt(args[2]),
                args[3].charAt(1)
            );
    }

    /**
     * Container class representing a UTM map origin with appropriate zone info.
     */
    public static class Origin {
        public final double northing;
        public final double easting;
        public final int zone;
        public final char hemisphere;

        public Origin(double easting, double northing, int zone, char hemisphere) {
            this.northing = northing;
            this.easting = easting;
            this.zone = zone;
            this.hemisphere = hemisphere;
        }
    }
}
