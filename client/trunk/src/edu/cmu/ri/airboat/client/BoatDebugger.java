/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client;

import edu.cmu.ri.airboat.client.gui.AirboatComponent;
import edu.cmu.ri.airboat.client.gui.CameraPanel;
import edu.cmu.ri.airboat.client.gui.ConnectionPanel;
import edu.cmu.ri.airboat.client.gui.PidPanel;
import edu.cmu.ri.airboat.client.gui.DrivePanel;
import edu.cmu.ri.airboat.client.gui.PosePanel;
import edu.cmu.ri.airboat.client.gui.SimpleWorldPanel;
import edu.cmu.ri.airboat.client.gui.WaypointPanel;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.udp.UdpVehicleService;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 * Provides a comprehensive interface for directly connecting to and interacting
 * with the airboat interfaces exposed through XML-RPC.
 *
 * @author pkv
 */
public class BoatDebugger extends javax.swing.JFrame {

    private SimpleWorldPanel _worldPanel;
    private ConnectionPanel _connectPanel;
    private CommandPanel _cmdPanel;
    private ControlPanel _ctrlPanel;

    public BoatDebugger() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Airboat Debugger");

        _worldPanel = new SimpleWorldPanel();
        _connectPanel = new ConnectionPanel();
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

        // Set up the connection panel
        _connectPanel.setBorder(BorderFactory.createTitledBorder("Connection"));
        _connectPanel.addConnectionListener(_connectionListener);

        // Put together the right side panel
        JPanel sidePane = new JPanel();
        sidePane.setLayout(new BoxLayout(sidePane, BoxLayout.PAGE_AXIS));
        sidePane.add(_connectPanel);
        sidePane.add(configPane);

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(_worldPanel, BorderLayout.CENTER);
        contentPane.add(sidePane, BorderLayout.EAST);
        pack();
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

    private ConnectionPanel.ConnectionListener _connectionListener =
            new ConnectionPanel.ConnectionListener() {

        public void connectionChanged(AsyncVehicleServer vehicle) {

            // When the connection is changed, update all subcomponents
            _cmdPanel.setVehicle(vehicle);
            _ctrlPanel.setVehicle(vehicle);
        }
    };

    public static void main(String args[]) {

        // Disable DNS lookups
        //CrwSecurityManager.loadIfDNSIsSlow();

        // Create a simulated boat and run a ROS server around it
        VehicleServer server = new SimpleBoatSimulator();
        UdpVehicleService testServer = new UdpVehicleService(11411, server);
        System.out.println("Local dummy server started: " + testServer.getSocketAddress());

        // Start up the debugger GUI
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BoatDebugger().setVisible(true);
            }
        });
    }

}
