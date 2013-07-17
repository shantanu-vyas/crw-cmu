/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.crw.AsyncVehicleServer;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;

/**
 * TeleopFrame provides a simple control panel for manually steering the 
 * boat.  It can be used by other applications to handle simple boat control.
 * 
 * @author pkv
 */
public class TeleopFrame extends JFrame {

    public TeleopFrame(final AsyncVehicleServer vehicle) {

        // Set up basic frame properties
        super("Airboat Teleop Frame");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create components for controlling the boat
        final DrivePanel drivePanel = new OsmanDrivePanel();
        final PidPanel thrustPanel = new PidPanel(0);
        final PidPanel rudderPanel = new PidPanel(5);
        
        // Make a pretty(-ish) GUI for driving the boat
        Container contentPane = getContentPane();
        contentPane.add(drivePanel, BorderLayout.CENTER);
        contentPane.add(thrustPanel, BorderLayout.WEST);
        contentPane.add(rudderPanel, BorderLayout.SOUTH);
        pack();
        
        // Connect the panels to the controller object
        drivePanel.setVehicle(vehicle);
        thrustPanel.setVehicle(vehicle);
        rudderPanel.setVehicle(vehicle);
    }
}
