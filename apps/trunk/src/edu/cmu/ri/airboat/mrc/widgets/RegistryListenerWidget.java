/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ConnectionPanel.java
 *
 * Created on Mar 2, 2011, 6:08:50 PM
 */
package edu.cmu.ri.airboat.mrc.widgets;

import edu.cmu.ri.airboat.general.SkeletonBoatProxy;
import edu.cmu.ri.airboat.general.widgets.Core;
import edu.cmu.ri.airboat.general.widgets.WidgetInterface;
import edu.cmu.ri.airboat.generalAlmost.FastSimpleBoatSimulator;
import edu.cmu.ri.crw.AsyncVehicleServer;
import edu.cmu.ri.crw.CrwNetworkUtils;
import edu.cmu.ri.crw.FunctionObserver;
import edu.cmu.ri.crw.FunctionObserver.FunctionError;
import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.Utm;
import edu.cmu.ri.crw.data.UtmPose;
import edu.cmu.ri.crw.udp.UdpVehicleServer;
import edu.cmu.ri.crw.udp.UdpVehicleService;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import robotutils.Pose3D;

/**
 *
 * @author pkv
 */
public class RegistryListenerWidget extends javax.swing.JPanel implements WidgetInterface {

    public static final String LAST_URI_KEY = "edu.cmu.ri.airboat.client.gui.LastConnection";
    public static int UPDATE_PERIOD_MS = 2000;
    private final Timer _timer = new Timer();
    private final UdpVehicleServer _vehicle = new UdpVehicleServer();
    private final HashSet<String> _cachedVehicles = new HashSet<String>();

    /**
     * Creates new form ConnectionPanel
     */
    public RegistryListenerWidget() {
        initComponents();
        initUpdates();
    }

    public JPanel getPanel() {
        return this;
    }

    public JPanel getControl() {
        return new ControlP();
    }

    class ControlP extends JPanel {

        int portBase = 9000;
        int count = 0;

        public ControlP() {
            setLayout(new FlowLayout());
            JButton addB = new JButton("Sim");
            addB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    count++;

                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Creating sim boat " + count, this);

                    // Create a local server
                    VehicleServer server = new FastSimpleBoatSimulator();
                    UdpVehicleService udpServer = new UdpVehicleService(portBase + count, server);

                    // Set location
                    UTMCoord utm = UTMCoord.fromLatLon(Angle.fromDegrees(40.0), Angle.fromDegrees(-22.0));
                    UtmPose p1 = new UtmPose(new Pose3D(utm.getEasting(), utm.getNorthing(), 0.0, 0.0, 0.0, 0.0), new Utm(utm.getZone(), utm.getHemisphere().contains("North")));
                    server.setPose(p1);

                    // Create the proxy
                    SkeletonBoatProxy proxy = new SkeletonBoatProxy("Sim boat @ " + count, count, new InetSocketAddress("localhost", portBase + count));
                    
                    (new Core()).addBoatProxy(proxy);
                }
            });
            add(addB);
        }
    }

    /**
     * Starts up a timer task that periodically checks to see if the proxy
     * command object is actually returning values. Changes the color of the
     * button to reflect this status.
     */
    private void initUpdates() {
        // Use proxies to periodically update status, check for connection
        _timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                _vehicle.getVehicleServices(new FunctionObserver<Map<SocketAddress, String>>() {
                    public void completed(Map<SocketAddress, String> v) {
                        registryCombo.setBackground(Color.GREEN);

                        // Compile a list of all the recent vehicles
                        HashSet<String> recentVehicles = new HashSet<String>(v.size());
                        for (Map.Entry<SocketAddress, String> e : v.entrySet()) {
                            recentVehicles.add(
                                    ((InetSocketAddress) e.getKey()).getAddress().getHostAddress()
                                    + ":" + ((InetSocketAddress) e.getKey()).getPort()
                                    + " - " + e.getValue());
                        }

                        synchronized (_cachedVehicles) {

                            // Validate old vehicle entries 
                            for (Iterator<String> it = _cachedVehicles.iterator(); it.hasNext();) {
                                String vehicle = it.next();
                                if (!recentVehicles.contains(vehicle)) {
                                    // @todo Remove old vehicles
                                }
                            }

                            // Add new vehicle entries
                            for (String vehicle : recentVehicles) {
                                if (!_cachedVehicles.contains(vehicle)) {
                                    _cachedVehicles.add(vehicle);

                                    // @todo Add new vehicles

                                }
                            }
                        }
                    }

                    public void failed(FunctionError fe) {
                        registryCombo.setBackground(Color.PINK);
                    }
                });
            }
        }, 0, UPDATE_PERIOD_MS);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        registryCombo = new javax.swing.JComboBox();
        registryLabel = new javax.swing.JLabel();

        registryCombo.setEditable(true);
        registryCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Registry", "athiri.cimds.ri.cmu.edu:6077" }));
        registryCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registryComboActionPerformed(evt);
            }
        });

        registryLabel.setText("  Registry:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(registryLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(registryCombo, 0, 157, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(registryLabel)
                    .add(registryCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void registryComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registryComboActionPerformed
        synchronized (this) {
            String registryAddr = ((String) registryCombo.getSelectedItem()).trim();
            InetSocketAddress addr = CrwNetworkUtils.toInetSocketAddress(registryAddr);
            _vehicle.setRegistryService(addr);
            System.out.println("SET REGISTRY TO " + _vehicle.getRegistryService());
        }
    }//GEN-LAST:event_registryComboActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox registryCombo;
    private javax.swing.JLabel registryLabel;
    // End of variables declaration//GEN-END:variables

    public static interface ConnectionListener {

        public void connectionChanged(AsyncVehicleServer vehicle);
    }
    private List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();

    public void addConnectionListener(ConnectionListener l) {
        listeners.add(l);
    }

    public void removeConnectionListener(ConnectionListener l) {
        listeners.remove(l);
    }

    protected void fireConnectionListener(AsyncVehicleServer vehicle) {
        for (int i = 0; i < listeners.size(); i++) {
            (listeners.get(i)).connectionChanged(vehicle);
        }
    }
}
