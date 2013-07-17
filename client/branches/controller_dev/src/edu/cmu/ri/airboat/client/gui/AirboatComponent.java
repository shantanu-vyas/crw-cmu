/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.client.gui;

import edu.cmu.ri.crw.AsyncVehicleServer;

/**
 * Generic interface used to describe GUI elements that directly interact with
 * airboat command and control interfaces.
 *
 * @author pkv
 */
public interface AirboatComponent {

    public void setVehicle(AsyncVehicleServer vehicle);
    public void setUpdateRate(long period_ms);
}
