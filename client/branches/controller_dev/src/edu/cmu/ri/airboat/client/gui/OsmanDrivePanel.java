/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.VelocityListener;
import edu.cmu.ri.crw.data.Twist;

/**
 *
 * @author okhwaja
 */
public class OsmanDrivePanel extends DrivePanel {
    // Ranges for thrust and rudder servo commands
    public static final double THRUST_SERVO_MIN = 1000.0;
    public static final double THRUST_SERVO_MAX = 2200.0;
    public static final double RUDDER_SERVO_MIN = 30;
    public static final double RUDDER_SERVO_MAX = 150;
    
        // Sets velocities from sliders to control proxy
    @Override
    protected void sendVelocity() {
        if (_vehicle != null) {
            Twist twist = new Twist();
            // map velocities to servo commands to update velocity
            double thrust_slider_value = fromProgressToRange(jThrust.getValue(), THRUST_MIN, THRUST_MAX);
            double thrust_output = map(thrust_slider_value, THRUST_MIN, THRUST_MAX, THRUST_SERVO_MIN, THRUST_SERVO_MAX);
            twist.dx(thrust_output);
            
            double rudder_slider_value = fromProgressToRange(jRudder.getValue(), RUDDER_MIN, RUDDER_MAX);
            double rudder_output = map(rudder_slider_value, RUDDER_MIN, RUDDER_MAX, RUDDER_SERVO_MIN, RUDDER_SERVO_MAX);
            twist.drz(rudder_output);
            _vehicle.setVelocity(twist, null);
        }
    }
    
    @Override
        public void setVehicle(AsyncVehicleServer vehicle) {
        super.setVehicle(vehicle);
        vehicle.addVelocityListener(new VelocityListener() {

            public void receivedVelocity(Twist twist) {
                double thrust_input = twist.dx();
                int thrust_slider_value = (int) map(thrust_input, THRUST_SERVO_MIN, THRUST_SERVO_MAX, THRUST_MIN, THRUST_MAX);
                jThrustBar.setValue(thrust_slider_value);
                
                double rudder_input = twist.drz();
                int rudder_slider_value = (int) map(rudder_input, RUDDER_SERVO_MIN, RUDDER_SERVO_MAX, RUDDER_MIN, RUDDER_MAX);
                jRudderBar.setValue(rudder_slider_value);
                OsmanDrivePanel.this.repaint();
            }
        }, null);
    }
    
    
    private static double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
    
    
}
