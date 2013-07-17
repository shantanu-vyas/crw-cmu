package edu.cmu.ri.crw;

import org.ros.message.geometry_msgs.Quaternion;

/**
 * Transformation methods for ROS quaternions representing 3D rotation.
 * @author Prasanna Velagapudi <pkv@cs.cmu.edu>
 */
public class QuaternionUtils {
    
    /**
     * Determines if a de-serialized object is compatible with this class.
     *
     * Maintainers must change this value if and only if the new version
     * of this class is not compatible with old versions. See Sun docs
     * for <a href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     */
    public static final long serialVersionUID = 1L;
    
    /**
     * This defines the north pole singularity cutoff when converting 
     * from quaternions to Euler angles.
     */
    public static final double SINGULARITY_NORTH_POLE = 0.49999;
    
    /**
     * This defines the south pole singularity cutoff when converting 
     * from quaternions to Euler angles.
     */
    public static final double SINGULARITY_SOUTH_POLE = -0.49999;

    /**
     * Converts quaternion to (3x3) rotation matrix.
     *
     * @return a 2D 3x3 rotation matrix representing the quaternion.
     */
    public static double[][] toRotation(Quaternion q) {
        double[][] m = new double[3][3];
        
        // Compute necessary components
        double xx = q.x*q.x;
        double xy = q.x*q.y;
        double xz = q.x*q.z;
        double xw = q.x*q.w;
        double yy = q.y*q.y;
        double yz = q.y*q.z;
        double yw = q.y*q.w;
        double zz = q.z*q.z;
        double zw = q.z*q.w;
        
        // Compute rotation tranformation
        // Compute rotation tranformation
        m[0][0] = 1 - 2 * ( yy + zz );
        m[0][1] =     2 * ( xy - zw );
        m[0][2] =     2 * ( xz + yw );
        m[1][0] =     2 * ( xy + zw );
        m[1][1] = 1 - 2 * ( xx + zz );
        m[1][2] =     2 * ( yz - xw );
        m[2][0] =     2 * ( xz - yw );
        m[2][1] =     2 * ( yz + xw );
        m[2][2] = 1 - 2 * ( xx + yy );
        
        return m;
    }
    
    public static Quaternion fromTransform(double[][] m) {
        double[] q = new double[4];
        
        // Recover the magnitudes
        q[0] = Math.sqrt( Math.max( 0, 1 + m[0][0] + m[1][1] + m[2][2] ) ) / 2; 
        q[1] = Math.sqrt( Math.max( 0, 1 + m[0][0] - m[1][1] - m[2][2] ) ) / 2; 
        q[2] = Math.sqrt( Math.max( 0, 1 - m[0][0] + m[1][1] - m[2][2] ) ) / 2; 
        q[3] = Math.sqrt( Math.max( 0, 1 - m[0][0] - m[1][1] + m[2][2] ) ) / 2; 
        
        // Recover sign information
        q[1] *= Math.signum( m[2][1] - m[1][2] ); 
        q[2] *= Math.signum( m[0][2] - m[2][0] );
        q[3] *= Math.signum( m[1][0] - m[0][1] ); 
        
        // Put into quaternion data structure
        Quaternion quat = new Quaternion();
        quat.w = q[0];
        quat.x = q[1];
        quat.y = q[2];
        quat.z = q[3];
        return quat;
    }
    
    public static double[][] toTransform(Quaternion q) {
        double[][] m = new double[4][4];
        
        // Compute necessary components
        double xx = q.x*q.x;
        double xy = q.x*q.y;
        double xz = q.x*q.z;
        double xw = q.x*q.w;
        double yy = q.y*q.y;
        double yz = q.y*q.z;
        double yw = q.y*q.w;
        double zz = q.z*q.z;
        double zw = q.z*q.w;
        
        // Compute rotation tranformation
        m[0][0] = 1 - 2 * ( yy + zz );
        m[0][1] =     2 * ( xy - zw );
        m[0][2] =     2 * ( xz + yw );
        m[1][0] =     2 * ( xy + zw );
        m[1][1] = 1 - 2 * ( xx + zz );
        m[1][2] =     2 * ( yz - xw );
        m[2][0] =     2 * ( xz - yw );
        m[2][1] =     2 * ( yz + xw );
        m[2][2] = 1 - 2 * ( xx + yy );
        m[0][3] = m[1][3] = m[2][3] = m[3][0] = m[3][1] = m[3][2] = 0;
        m[3][3] = 1;
        
        return m;
    }
    
    public static Quaternion fromEulerAngles(double roll, double pitch, double yaw) {
        double q[] = new double[4];
        
        // Apply Euler angle transformations
        // Derivation from www.euclideanspace.com
        double c1 = Math.cos(yaw/2.0);
        double s1 = Math.sin(yaw/2.0);
        double c2 = Math.cos(pitch/2.0);
        double s2 = Math.sin(pitch/2.0);
        double c3 = Math.cos(roll/2.0);
        double s3 = Math.sin(roll/2.0);
        double c1c2 = c1*c2;
        double s1s2 = s1*s2;
        
        // Compute quaternion from components
        q[0] = c1c2*c3 - s1s2*s3;
        q[1] = c1c2*s3 + s1s2*c3;
        q[2] = s1*c2*c3 + c1*s2*s3;
        q[3] = c1*s2*c3 - s1*c2*s3;
        
        // Put into quaternion data structure
        Quaternion quat = new Quaternion();
        quat.w = q[0];
        quat.x = q[1];
        quat.y = q[2];
        quat.z = q[3];
        return quat;
    }
    
    /**
     * Returns the roll component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the roll (x-axis rotation) of the robot.
     */
    public static double toRoll(Quaternion q) {
        // This is a test for singularities
        double test = q.x*q.y + q.z*q.w;
        
        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return 0;
        
        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return 0;
            
        return Math.atan2( 
                    2*q.x*q.w - 2*q.y*q.z,
                    1 - 2*q.x*q.x - 2*q.z*q.z
                ); 
    }
    
    /**
     * Returns the pitch component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the pitch (y-axis rotation) of the robot.
     */
    public static double toPitch(Quaternion q) {
        // This is a test for singularities
        double test = q.x*q.y + q.z*q.w;
        
        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return Math.PI/2;
        
        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return -Math.PI/2;
        
        return Math.asin(2*test); 
    }
    
    /**
     * Returns the yaw component of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return the yaw (z-axis rotation) of the robot.
     */
    public static double toYaw(Quaternion q) {
        // This is a test for singularities
        double test = q.x*q.y + q.z*q.w;
        
        // Special case for north pole
        if (test > SINGULARITY_NORTH_POLE)
            return 2 * Math.atan2(q.x, q.w);
        
        // Special case for south pole
        if (test < SINGULARITY_SOUTH_POLE)
            return -2 * Math.atan2(q.x, q.w);
        
        return Math.atan2(
                    2*q.y*q.w - 2*q.x*q.z,
                    1 - 2*q.y*q.y - 2*q.z*q.z
                ); 

    }
    
    /**
     * Returns the components of the quaternion if it is represented
     * as standard roll-pitch-yaw Euler angles.
     * @return an array of the form {roll, pitch, yaw}.
     */
    public static double[] toEulerAngles(Quaternion q) {
        return new double[] { toRoll(q), toPitch(q), toYaw(q) };
    }
    
}
