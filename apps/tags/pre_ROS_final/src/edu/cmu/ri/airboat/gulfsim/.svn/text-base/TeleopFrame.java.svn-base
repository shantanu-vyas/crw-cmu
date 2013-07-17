/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim;

import edu.cmu.ri.airboat.client.gui.PidPanel;
import edu.cmu.ri.airboat.client.gui.DrivePanel;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;

/**
 *
 * @author pkv
 */
public class TeleopFrame extends JFrame {

    public TeleopFrame(final AirboatControl controller) {

        // Set up basic frame properties
        super("Airboat Teleop Frame");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create components for controlling the boat
        final DrivePanel drivePanel = new DrivePanel();
        final PidPanel thrustPanel = new PidPanel(0);
        final PidPanel rudderPanel = new PidPanel(5);
        
        // Make a pretty(-ish) GUI for driving the boat
        Container contentPane = getContentPane();
        contentPane.add(drivePanel, BorderLayout.CENTER);
        contentPane.add(thrustPanel, BorderLayout.WEST);
        contentPane.add(rudderPanel, BorderLayout.SOUTH);
        pack();
        
        // Connect the panels to the controller object
        drivePanel.setControl(controller);
        thrustPanel.setControl(controller);
        rudderPanel.setControl(controller);
    }
}
