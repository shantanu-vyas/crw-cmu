/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BoatPanel.java
 *
 * Created on Feb 15, 2011, 9:02:30 PM
 */
package edu.cmu.ri.airboat.gulfsim;

import edu.cmu.ri.airboat.gulfsim.tasking.Role;
import gov.nasa.worldwind.geom.Position;
import javax.swing.DefaultListModel;

/**
 *
 * @author pscerri
 */
public class BoatPanel extends javax.swing.JPanel {

    BoatSimpleProxy proxy = null;
    TeleopFrame teleOpFrame = null;
    DefaultListModel lm = new DefaultListModel();

    /** Creates new form BoatPanel */
    public BoatPanel() {
        initComponents();
        taskL.setModel(lm);
        
        (new Thread() {
            public void run() {
                while(true) {
                    if (proxy != null) {
                        update();
                    }
                    try {
                        sleep(3000);
                    } catch (Exception e) {}
                }
            }
        }).start();
    }

    public BoatSimpleProxy getProxy() {
        return proxy;
    }

    private void update() {
        fuelPB.setValue((int) (100.0 * proxy.getFuelLevel()));
        
        lm.clear();
        for (Role role : proxy.getRoles()) {
            lm.addElement(role);
        }
    }

    public void setProxy(BoatSimpleProxy proxy) {
        this.proxy = proxy;

        if (proxy != null) {
            addressF.setText(proxy.toString());
            teleopB.setEnabled(true);

            update();
        } else {
            addressF.setText("None");
            teleopB.setEnabled(false);
        }

    }

    public void setWaypoint(Position targetPos) {
        if (proxy != null) {
            proxy.setWaypoint(targetPos);

            System.out.println("Boat panel processing");
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addressF = new javax.swing.JTextField();
        teleopB = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        taskL = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        reassignB = new javax.swing.JButton();
        fuelPB = new javax.swing.JProgressBar();

        addressF.setText("None");
        addressF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressFActionPerformed(evt);
            }
        });

        teleopB.setText("Teleop");
        teleopB.setEnabled(false);
        teleopB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teleopBActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(taskL);

        jLabel1.setText("Tasks");

        reassignB.setText("Reassign");
        reassignB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reassignBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(addressF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(47, 47, 47)
                                .add(jLabel1))
                            .add(reassignB))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(fuelPB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 243, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(240, 240, 240)
                .add(teleopB))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, fuelPB, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(addressF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(teleopB)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 115, Short.MAX_VALUE)
                        .add(reassignB))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addressFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressFActionPerformed
    }//GEN-LAST:event_addressFActionPerformed

    private void teleopBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teleopBActionPerformed

        if (teleOpFrame != null && teleOpFrame.isVisible()) {
        } else {
            teleOpFrame = new TeleopFrame(proxy.getController());
            teleOpFrame.setVisible(true);
            System.out.println("Created teleop frame");
        }

    }//GEN-LAST:event_teleopBActionPerformed

    private void reassignBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reassignBActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_reassignBActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressF;
    private javax.swing.JProgressBar fuelPB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton reassignB;
    private javax.swing.JList taskL;
    private javax.swing.JButton teleopB;
    // End of variables declaration//GEN-END:variables
}
