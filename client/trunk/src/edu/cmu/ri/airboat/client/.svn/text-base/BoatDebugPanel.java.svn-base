/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client;

import edu.cmu.ri.airboat.client.gui.AirboatComponent;
import edu.cmu.ri.airboat.client.gui.CameraPanel;
import edu.cmu.ri.airboat.client.gui.PidPanel;
import edu.cmu.ri.airboat.client.gui.DrivePanel;
import edu.cmu.ri.airboat.client.gui.PosePanel;
import edu.cmu.ri.airboat.client.gui.SimpleWorldPanel;
import edu.cmu.ri.airboat.client.gui.WaypointPanel;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.SimpleBoatSimulator;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * Provides a comprehensive interface for directly connecting to and interacting
 * with the airboat interfaces exposed through XML-RPC.
 *
 * @author pkv
 */
public class BoatDebugPanel extends javax.swing.JPanel {

    private SimpleWorldPanel _worldPanel;
    private CommandPanel _cmdPanel;
    private ControlPanel _ctrlPanel;

    public BoatDebugPanel() {
        initComponents();
    }

    private void initComponents() {
        
        setLayout(new BorderLayout());

        _worldPanel = new SimpleWorldPanel();
        _ctrlPanel = new ControlPanel();
        _cmdPanel = new CommandPanel();

        // Lay out the command and control panes
        JScrollPane cmdScrollPane = new JScrollPane(_cmdPanel);
        cmdScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        cmdScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cmdScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane ctrlScrollPane = new JScrollPane(_ctrlPanel);
        ctrlScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        ctrlScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ctrlScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JTabbedPane configPane = new JTabbedPane();
        configPane.addTab("Command", cmdScrollPane);
        configPane.addTab("Control", ctrlScrollPane);

        // Put together the right side panel
        JPanel sidePane = new JPanel();
        sidePane.setLayout(new BoxLayout(sidePane, BoxLayout.PAGE_AXIS));
        sidePane.add(configPane);

        // Put everything together, using the content pane's BorderLayout.
        add(_worldPanel, BorderLayout.CENTER);
        add(sidePane, BorderLayout.EAST);
    }

    private class CommandPanel extends javax.swing.JPanel implements AirboatComponent {

        private PosePanel posePanel;
        private WaypointPanel waypointPanel;
        private CameraPanel cameraPanel;

        public CommandPanel() {
            initComponents();
        }

        private void initComponents() {
            posePanel = new PosePanel();
            waypointPanel = new WaypointPanel();
            cameraPanel = new CameraPanel();

            //Put everything together, using a vertical BoxLayout
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            posePanel.setBorder(BorderFactory.createTitledBorder("Pose"));
            posePanel.setWorldPanel(_worldPanel);
            add(posePanel);

            add(Box.createRigidArea(new Dimension(0,5)));

            waypointPanel.setBorder(BorderFactory.createTitledBorder("Waypoint"));
            waypointPanel.setWorldPanel(_worldPanel);
            add(waypointPanel);
            
            add(Box.createRigidArea(new Dimension(0,5)));

            cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
            add(cameraPanel);
        }

        public void setVehicle(AsyncVehicleServer vehicle) {
            posePanel.setVehicle(vehicle);
            waypointPanel.setVehicle(vehicle);
            cameraPanel.setVehicle(vehicle);
        }

        public void setUpdateRate(long period_ms) {
            posePanel.setUpdateRate(period_ms);
            waypointPanel.setUpdateRate(period_ms);
            cameraPanel.setUpdateRate(period_ms);
        }
    }

    private static class ControlPanel extends javax.swing.JPanel implements AirboatComponent {

        private PidPanel pidThrustPanel;
        private PidPanel pidRudderPanel;
        private DrivePanel drivePanel;

        public ControlPanel() {
            initComponents();
        }

        private void initComponents() {
            pidThrustPanel = new PidPanel(0);
            pidRudderPanel = new PidPanel(5);
            drivePanel = new DrivePanel();

            //Put everything together, using a vertical BoxLayout
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            drivePanel.setBorder(BorderFactory.createTitledBorder("Velocity"));
            add(drivePanel);

            add(Box.createRigidArea(new Dimension(0,5)));
            
            pidThrustPanel.setBorder(BorderFactory.createTitledBorder("Thrust PID"));
            add(pidThrustPanel);

            add(Box.createRigidArea(new Dimension(0,5)));

            pidRudderPanel.setBorder(BorderFactory.createTitledBorder("Rudder PID"));
            add(pidRudderPanel);
        }

        public void setVehicle(AsyncVehicleServer vehicle) {
            pidThrustPanel.setVehicle(vehicle);
            pidRudderPanel.setVehicle(vehicle);
            drivePanel.setVehicle(vehicle);
        }

        public void setUpdateRate(long period_ms) {
            pidThrustPanel.setUpdateRate(period_ms);
            pidRudderPanel.setUpdateRate(period_ms);
            drivePanel.setUpdateRate(period_ms);
        }
    }

    /**
     * Use this function to set the current VehicleServer that should be
     * displayed and used by this panel.
     *
     * @param vehicle
     */
    public void setServer(AsyncVehicleServer vehicle) {

        // When the connection is changed, update all subcomponents
        _cmdPanel.setVehicle(vehicle);
        _ctrlPanel.setVehicle(vehicle);
    }

    public static void main(String args[]) {

        // Create a simulated boat
        final AsyncVehicleServer server = AsyncVehicleServer.Util.toAsync(new SimpleBoatSimulator());
        
        // Start up the debugger GUI
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                BoatDebugPanel boatPanel = new BoatDebugPanel();
                boatPanel.setServer(server);

                JFrame mainFrame = new JFrame();
                mainFrame.setTitle("Boat Debugging Panel");
                mainFrame.getContentPane().add(boatPanel);
                mainFrame.setLocation(100, 100);
                mainFrame.pack();
                mainFrame.setVisible(true);
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }

}
