/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general.widgets;

import edu.cmu.ri.airboat.general.BoatProxy;
import edu.cmu.ri.crw.data.SensorData;
import edu.cmu.ri.crw.data.UtmPose;

/**
 *
 * @author pscerri
 */
public interface DataListener {

    public void addData(BoatProxy proxy, SensorData sd, UtmPose _pose);
    
}
