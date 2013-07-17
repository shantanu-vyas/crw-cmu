package edu.cmu.ri.airboat.interfaces;

/**
 * This interface defines direct access to the sensors available on the airboat.
 * It can be used by external modules to log and process various environmental
 * data.
 * 
 * @author pkv
 * 
 */
public interface AirboatSensor {

    public static String ANALOG = "ANALOG";
    public static String FIVETE = "5TE";

    /**
     * Returns the current 3D gyro reading in radians.
     *
     * @return a rotational velocity vector in the form [rx, ry, rz]
     */
    public double[] getGyro();

    /**
     * Takes an image using the onboard camera and immediately returns it
     * as a JPEG compressed byte array.
     *
     * @return a byte array containing a recently-taken image
     */
    public byte[] getImage();

    /**
     * Takes an image using the onboard camera and saves it to onboard storage
     * on the device running the vehicle server.
     *
     * @return true if the image was taken successfully
     */
    public boolean saveImage();

    public String[] getTypes();

    public String getType(int port);

    public boolean setType(int port, String type);

    public double[] read(int port);
}
