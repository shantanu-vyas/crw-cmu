/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

/**
 * Create a check box that cannot be edited by a user.
 * @author pkv
 */
public class ReadOnlyCheckBox extends javax.swing.JCheckBox {

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_CLICKED) {
            this.fireActionPerformed(new ActionEvent(this, 0, "toggle"));
        }
    }
}
