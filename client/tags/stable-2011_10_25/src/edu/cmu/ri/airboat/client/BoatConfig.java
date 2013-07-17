/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client;

import edu.cmu.ri.airboat.client.gui.PidPanel;
import edu.cmu.ri.airboat.client.gui.DrivePanel;
import edu.cmu.ri.crw.CrwSecurityManager;
import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.ros.RosVehicleProxy;
import edu.cmu.ri.crw.ros.RosVehicleServer;
import java.awt.BorderLayout;
import java.net.URI;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.ros.RosCore;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeRunner;

/**
 *
 * @author pkv
 */
public class BoatConfig {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Disable DNS lookups
        CrwSecurityManager.loadIfDNSIsSlow();

        // Start a local loopback server 
        // (useful for testing, no big deal if this server fails to start)
        // Create a local loopback server for testing
        // (Not a big deal if this fails)
        // Start a local ros core
        RosCore core = RosCore.newPublic(11411);
        NodeRunner.newDefault().run(core, NodeConfiguration.newPrivate());
        core.awaitStart();

        // Create a simulated boat and run a ROS server around it
        VehicleServer server = new SimpleBoatSimulator();
        RosVehicleServer testServer = new RosVehicleServer(core.getUri(), "testVehicle", server);
        System.out.println("Local dummy server started: " + testServer);
        
        // Create components for controlling the boat
        final DrivePanel drivePanel = new DrivePanel();
        final PidPanel thrustPanel = new PidPanel(0);
        final PidPanel rudderPanel = new PidPanel(5);
        final AtomicBoolean isConnected = new AtomicBoolean();
        
        // Make a pretty(-ish) GUI for driving the boat
        final JFrame frame = new JFrame("Airboat Debug Client");
        frame.getContentPane().add(drivePanel, BorderLayout.CENTER);
        frame.getContentPane().add(thrustPanel, BorderLayout.WEST);
        frame.getContentPane().add(rudderPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make XML-RPC connection to boat
        Timer timer = new Timer();
        String ipAddrStr = "http://localhost:11411";
        while (true) {
            
            // Query user for URL of boat, exit if cancel is pressed
            ipAddrStr = (String)JOptionPane.showInputDialog(null, "Enter URL of Server", "Connect to Airboat", JOptionPane.QUESTION_MESSAGE, null, null, ipAddrStr);
            if (ipAddrStr == null) System.exit(0);

            // Create a ROS proxy server that accesses the same object
            try {
                URI masterUri = new URI(ipAddrStr);
                final VehicleServer vehicle = new RosVehicleProxy(masterUri, "vehicle_client" + new Random().nextInt(1000000));

                // Connect the new controller to the GUI panels
                thrustPanel.setVehicle(vehicle);
                rudderPanel.setVehicle(vehicle);
                drivePanel.setVehicle(vehicle);

                // Create a task to test connectivity to boat
                isConnected.set(true);
                TimerTask connectionTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            vehicle.isAutonomous();
                        } catch (Exception ex) {
                            Logger.getLogger(BoatConfig.class.getName()).log(Level.SEVERE, null, ex);
                            this.cancel();

                            synchronized(isConnected) {
                                isConnected.set(false);
                                isConnected.notifyAll();
                            }
                        }
                    }
                };

                // Do this update at fixed rate
                timer.scheduleAtFixedRate(connectionTask, 0, 1000);
                
                // Wait for connection failure
                synchronized(isConnected) {
                    while (isConnected.get()) {
                        isConnected.wait();
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(BoatConfig.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (timer != null) timer.cancel();
            }
        }
    }

}

