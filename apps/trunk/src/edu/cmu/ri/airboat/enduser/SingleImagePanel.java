/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SingleImagePanel.java
 *
 * Created on Aug 25, 2011, 4:14:28 PM
 */
package edu.cmu.ri.airboat.enduser;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
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

    AtomicBoolean in = new AtomicBoolean(false);
    
    private void getImage() {
        
        if (in.get()) return;
        
        in.set(true);
        if (ImagePanel.willBlock()) {
            imgL.setIcon(null);
        }
        img = ImagePanel.getImage();
        if (img != null) {
            Image scaledImage = img.getScaledInstance(imgL.getWidth(), imgL.getHeight(), Image.SCALE_DEFAULT);
            imgL.setIcon(new ImageIcon(scaledImage));
        } else {
            getImage();
        }
        in.set(false);
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
        enlargeB = new javax.swing.JButton();
        imgL = new javax.swing.JLabel();

        doneB.setText("Done");
        doneB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneBActionPerformed(evt);
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
                .add(0, 53, Short.MAX_VALUE))
            .add(imgL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(imgL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(doneB)
                    .add(enlargeB)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // @todo make this a proper semaphore
    boolean lock = false;    
    private void doneBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneBActionPerformed
        
        if (!lock) {
            lock = true;
            (new Thread() {
                public void run() {
                    getImage();
                    lock = false;
                }
            }).start();
        }
        
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
    // End of variables declaration//GEN-END:variables
}
