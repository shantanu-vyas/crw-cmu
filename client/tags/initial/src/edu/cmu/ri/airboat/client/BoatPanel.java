/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BoatPanel.java
 *
 * Created on Feb 15, 2011, 9:02:30 PM
 */
package edu.cmu.ri.airboat.client;

import gov.nasa.worldwind.geom.Position;

/**
 *
 * @author pscerri
 */
public class BoatPanel extends javax.swing.JPanel {

    BoatSimpleProxy proxy = null;
    TeleopFrame teleOpFrame = null;

    /** Creates new form BoatPanel */
    public BoatPanel() {
        initComponents();
    }

    public BoatSimpleProxy getProxy() {
        return proxy;
    }

    public void setProxy(BoatSimpleProxy proxy) {
        this.proxy = proxy;

        if (proxy != null) {
            addressF.setText(proxy.toString());
            teleopB.setEnabled(true);
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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(addressF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 245, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 580, Short.MAX_VALUE)
                .add(teleopB))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addressF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(teleopB))
                .addContainerGap(158, Short.MAX_VALUE))
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField addressF;
    private javax.swing.JButton teleopB;
    // End of variables declaration//GEN-END:variables
}
