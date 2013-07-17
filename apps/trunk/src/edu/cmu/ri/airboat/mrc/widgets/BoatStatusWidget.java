/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.mrc.widgets;

import edu.cmu.ri.airboat.general.SkeletonBoatProxy;
import edu.cmu.ri.airboat.general.widgets.Core;
import edu.cmu.ri.airboat.general.widgets.ProxyListener;
import edu.cmu.ri.airboat.general.widgets.WidgetInterface;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author pscerri
 */
public class BoatStatusWidget implements WidgetInterface, ProxyListener {

    JPanel panel = new BSWPanel();

    public BoatStatusWidget() {
        (new Core()).addListener(this);
    }
    
    public JPanel getPanel() {
        return panel;
    }

    public JPanel getControl() {
        return null;
    }

    public String getName() {
        return "Boat status";
    }

    public void proxyUpdated(SkeletonBoatProxy p) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Updated recieved", this);
    }

    public void proxyAdded(SkeletonBoatProxy p) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Told of new boat", this);
        panel.add(new JTextField("Boat!"));
        panel.revalidate();
    }
    
    private class BSWPanel extends JPanel {

        public BSWPanel() {
            setLayout(new FlowLayout());
            add(new JTextField("Hello"));
        }
        
    }
    
}
