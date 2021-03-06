/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * IrrigationGUI.java
 *
 * Created on Jul 18, 2011, 3:50:11 PM
 */
package edu.cmu.ri.airboat.irrigationtest;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import javax.swing.JFrame;

/**
 *
 * @author pscerri
 */
public class IrrigationGUI extends javax.swing.JFrame {

    AutonomyController autonomy = null;
    GridDisplay gridDisplay = new GridDisplay();

    /** Creates new form IrrigationGUI */
    public IrrigationGUI() {
        initComponents();
        
        modelContainerP.setMinimumSize(gridDisplay.getMinimumSize());
        modelContainerP.setLayout(new BorderLayout());
        modelContainerP.add(gridDisplay, BorderLayout.CENTER);

        //HardCoded data to avoid entering it repeatedly on the GUI
        minLonF.setText("432220.0");
        minLatF.setText("4371545.0");
        maxLonF.setText("432240.0");
        maxLatF.setText("4371595.0");

        autonomy = new AutonomyController(getUL(), getLR(), ((Integer)lonCountS.getValue()).intValue(), ((Integer)latCountS.getValue()).intValue());
        gridDisplay.setAutonomy(autonomy);
        
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                // @todo This doesn't feel right, but appears to work, make it work properly.
                // System.out.println("WINDOW SHUTTING DOWN: " + e.getNewState() + " " + e.getOldState() + " " + WindowEvent.WINDOW_CLOSING + " " + WindowEvent.WINDOW_CLOSED);
                // if (e.getNewState() == WindowEvent.WINDOW_CLOSING|| e.getNewState() == WindowEvent.WINDOW_CLOSED ) {
                    System.out.println("Autonomy shutting down");
                    autonomy.shutdown();
                    // System.exit(0);
                // }
    }

        }
        );

        //setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    double[] getUL() {
        double [] ul = new double[2];
        
        ul[0] = Double.parseDouble(minLonF.getText());
        ul[1] = Double.parseDouble(maxLatF.getText());
        
        return ul;
    }

    double[] getLR() {
        double [] lr = new double[2];
        
        lr[0] = Double.parseDouble(maxLonF.getText());
        lr[1] = Double.parseDouble(minLatF.getText());
        
        return lr;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modelContainerP = new javax.swing.JPanel();
        maxLonF = new javax.swing.JTextField();
        minLonF = new javax.swing.JTextField();
        maxLatF = new javax.swing.JTextField();
        minLatF = new javax.swing.JTextField();
        algC = new javax.swing.JComboBox();
        lonCountS = new javax.swing.JSpinner();
        latCountS = new javax.swing.JSpinner();
        boatSpeedS = new javax.swing.JSlider();
        sleepS = new javax.swing.JSlider();
        meanCB = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        modelContainerP.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.jdesktop.layout.GroupLayout modelContainerPLayout = new org.jdesktop.layout.GroupLayout(modelContainerP);
        modelContainerP.setLayout(modelContainerPLayout);
        modelContainerPLayout.setHorizontalGroup(
            modelContainerPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1157, Short.MAX_VALUE)
        );
        modelContainerPLayout.setVerticalGroup(
            modelContainerPLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 707, Short.MAX_VALUE)
        );

        maxLonF.setText("100.0000");
        maxLonF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxLonFActionPerformed(evt);
            }
        });

        minLonF.setText("0.0000000000");
        minLonF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minLonFActionPerformed(evt);
            }
        });

        maxLatF.setText("100.00000");
        maxLatF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxLatFActionPerformed(evt);
            }
        });

        minLatF.setText("0.0000000");
        minLatF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minLatFActionPerformed(evt);
            }
        });

        algC.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Lawnmower", "Max Uncertainty", "Max Grad." }));
        algC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algCActionPerformed(evt);
            }
        });

        lonCountS.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10), Integer.valueOf(0), null, Integer.valueOf(1)));
        lonCountS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lonCountSStateChanged(evt);
            }
        });

        latCountS.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10), Integer.valueOf(1), null, Integer.valueOf(1)));
        latCountS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                latCountSStateChanged(evt);
            }
        });

        boatSpeedS.setMaximum(10);
        boatSpeedS.setToolTipText("Boat speed");
        boatSpeedS.setValue(1);
        boatSpeedS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                boatSpeedSStateChanged(evt);
            }
        });

        sleepS.setMaximum(10000);
        sleepS.setToolTipText("Sleep time boat updates");
        sleepS.setValue(1000);
        sleepS.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sleepSStateChanged(evt);
            }
        });

        meanCB.setSelected(true);
        meanCB.setText("Mean");
        meanCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meanCBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(layout.createSequentialGroup()
                                    .add(34, 34, 34)
                                    .add(minLatF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .add(layout.createSequentialGroup()
                                    .add(50, 50, 50)
                                    .add(latCountS)))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(maxLatF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(modelContainerP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(254, 254, 254)
                        .add(minLonF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(355, 355, 355)
                        .add(lonCountS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 444, Short.MAX_VALUE)
                        .add(maxLonF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(algC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(boatSpeedS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 335, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sleepS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 351, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(meanCB)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(maxLonF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(boatSpeedS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(algC, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(sleepS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(meanCB))
                        .add(66, 66, 66)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, minLonF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, lonCountS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(maxLatF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 358, Short.MAX_VALUE)
                        .add(latCountS, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(249, 249, 249)
                        .add(minLatF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .add(modelContainerP, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void boatSpeedSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_boatSpeedSStateChanged
        InterfaceTester.speed = (double)boatSpeedS.getValue();
    }//GEN-LAST:event_boatSpeedSStateChanged

    private void sleepSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sleepSStateChanged
        InterfaceTester.sleepTime = (long)sleepS.getValue();
    }//GEN-LAST:event_sleepSStateChanged

    private void algCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algCActionPerformed
        autonomy.setAlgorithm((String)algC.getSelectedItem());
    }//GEN-LAST:event_algCActionPerformed

    private void resetExtents() {
        autonomy.setExtent(getUL(), getLR());
    }
    
    private void minLonFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minLonFActionPerformed
        resetExtents();
    }//GEN-LAST:event_minLonFActionPerformed

    private void maxLonFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxLonFActionPerformed
        resetExtents();
    }//GEN-LAST:event_maxLonFActionPerformed

    private void maxLatFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxLatFActionPerformed
        resetExtents();
    }//GEN-LAST:event_maxLatFActionPerformed

    private void minLatFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minLatFActionPerformed
        resetExtents();
    }//GEN-LAST:event_minLatFActionPerformed

    private void lonCountSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lonCountSStateChanged
        autonomy.setxCount((Integer)lonCountS.getValue());
    }//GEN-LAST:event_lonCountSStateChanged

    private void latCountSStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_latCountSStateChanged
                autonomy.setyCount((Integer)latCountS.getValue());
    }//GEN-LAST:event_latCountSStateChanged

    private void meanCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meanCBActionPerformed
        gridDisplay.setShowMean(meanCB.isSelected());
    }//GEN-LAST:event_meanCBActionPerformed

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new IrrigationGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algC;
    private javax.swing.JSlider boatSpeedS;
    private javax.swing.JSpinner latCountS;
    private javax.swing.JSpinner lonCountS;
    private javax.swing.JTextField maxLatF;
    private javax.swing.JTextField maxLonF;
    private javax.swing.JCheckBox meanCB;
    private javax.swing.JTextField minLatF;
    private javax.swing.JTextField minLonF;
    private javax.swing.JPanel modelContainerP;
    private javax.swing.JSlider sleepS;
    // End of variables declaration//GEN-END:variables
}
