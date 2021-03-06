/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SingleImagePanel.java
 *
 * Created on Aug 25, 2011, 4:14:28 PM
 */
package edu.cmu.ri.airboat.floodtest;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

/**
 *
 * @author pscerri
 */
public class SingleImagePanel extends javax.swing.JPanel {

    BufferedImage img = null;
    /** Creates new form SingleImagePanel */
    public SingleImagePanel() {
        initComponents();

        (new Thread() {

            public void run() {
                getImage();
            }
        }).start();

    }

    private void getImage() {
        img = ImagePanel.getImage();
        if (img != null) {
            Image scaledImage = img.getScaledInstance(imgL.getWidth(), imgL.getHeight(), Image.SCALE_DEFAULT);
            imgL.setIcon(new ImageIcon(scaledImage));
        } else {
            getImage();
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

        doneB = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        enlargeB = new javax.swing.JButton();
        imgL = new javax.swing.JLabel();

        doneB.setText("Done");
        doneB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneBActionPerformed(evt);
            }
        });

        jButton2.setText("Store");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        enlargeB.setText("Enlarge");
        enlargeB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enlargeBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(doneB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(enlargeB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(imgL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(imgL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(doneB)
                    .add(enlargeB)
                    .add(jButton2)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void doneBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneBActionPerformed
        getImage();
    }//GEN-LAST:event_doneBActionPerformed

    private void enlargeBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enlargeBActionPerformed
        if (img != null) {
            ImageViewer imgV = new ImageViewer(img);
            imgV.setVisible(true);
        }
    }//GEN-LAST:event_enlargeBActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton doneB;
    private javax.swing.JButton enlargeB;
    private javax.swing.JLabel imgL;
    private javax.swing.JButton jButton2;
    // End of variables declaration//GEN-END:variables
}
