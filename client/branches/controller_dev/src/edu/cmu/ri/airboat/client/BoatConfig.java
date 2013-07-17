/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client;

import edu.cmu.ri.airboat.client.gui.PidPanel;
import edu.cmu.ri.airboat.client.gui.DrivePanel;
import edu.cmu.ri.airboat.client.gui.OsmanDrivePanel;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CrwNetworkUtils;
import edu.cmu.ri.crw.CrwSecurityManager;
import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.SimpleBoatSimulator;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.udp.UdpVehicleServer;
import edu.cmu.ri.crw.udp.UdpVehicleService;
import java.awt.BorderLayout;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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

        // Create a simulated boat and run a ROS server around it
        VehicleServer server = new SimpleBoatSimulator();
        UdpVehicleService testServer = new UdpVehicleService(server);
        System.out.println("Local dummy server started: " + testServer.getSocketAddress());
        
        // Create components for controlling the boat
        final DrivePanel drivePanel = new OsmanDrivePanel();
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
        String ipAddrStr = "localhost:" + ((InetSocketAddress)testServer.getSocketAddress()).getPort();
        while (true) {
            
            // Query user for URL of boat, exit if cancel is pressed
            ipAddrStr = (String)JOptionPane.showInputDialog(null, "Enter URL of Server", "Connect to Airboat", JOptionPane.QUESTION_MESSAGE, null, null, ipAddrStr);
            if (ipAddrStr == null) System.exit(0);
            // TODO: add socket parsing here

            // Create a ROS proxy server that accesses the same object
            try {
                SocketAddress serverAddr = CrwNetworkUtils.toInetSocketAddress(ipAddrStr);
                final AsyncVehicleServer vehicle = new UdpVehicleServer(serverAddr);

                // Connect the new controller to the GUI panels
                thrustPanel.setVehicle(vehicle);
                rudderPanel.setVehicle(vehicle);
                drivePanel.setVehicle(vehicle);

                // Create a task to test connectivity to boat
                isConnected.set(true);
                TimerTask connectionTask = new TimerTask() {
                    @Override
                    public void run() {
                        final TimerTask parent = this;
                        
                        vehicle.isAutonomous(new FunctionObserver<Boolean>() {

                            public void completed(Boolean v) { }

                            public void failed(FunctionError fe) {
                                parent.cancel();
                                
                                synchronized(isConnected) {
                                    isConnected.set(false);
                                    isConnected.notifyAll();
                                }
                            }
                        });
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

