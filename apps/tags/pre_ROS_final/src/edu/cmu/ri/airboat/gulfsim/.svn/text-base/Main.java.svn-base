/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim;

import edu.cmu.ri.airboat.client.gui.PidPanel;
import edu.cmu.ri.airboat.client.gui.DrivePanel;
import com.flat502.rox.client.XmlRpcClient;
import edu.cmu.ri.airboat.interfaces.AirboatControl;
import edu.cmu.ri.airboat.server.AirboatDummy;
import edu.cmu.ri.airboat.server.AirboatSecurityManager;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Disable DNS lookups
        AirboatSecurityManager.load();

        // Start a local loopback server 
        // (useful for testing, no big deal if this server fails to start)
        AirboatDummy.defaultRpcInstance();
        System.out.println("Local dummy server started");
        
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
        String ipAddrStr = "http://localhost:5000";
        while (true) {
            
            // Query user for URL of boat, exit if cancel is pressed
            ipAddrStr = (String)JOptionPane.showInputDialog(null, "Enter URL of Server", "Connect to Airboat", JOptionPane.QUESTION_MESSAGE, null, null, ipAddrStr);
            if (ipAddrStr == null) System.exit(0);

            // Try to open this URL as XML-RPC server
            try {
                final XmlRpcClient client = new XmlRpcClient(new URL(ipAddrStr));
                final AirboatControl controller = (AirboatControl)client.proxyObject("control.", AirboatControl.class);

                // Connect the new controller to the GUI panels
                thrustPanel.setControl(controller);
                rudderPanel.setControl(controller);
                drivePanel.setControl(controller);

                // Create a task to test connectivity to boat
                isConnected.set(true);
                TimerTask connectionTask = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            controller.isConnected();
                        } catch (Exception ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
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
            } catch (MalformedURLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (timer != null) timer.cancel();
            }
        }
    }

}

