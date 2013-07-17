/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import gov.nasa.worldwind.geom.coords.UTMCoord;

/**
 *
 * @author pscerri
 */
public interface DataModel {

    public double getValue(UTMCoord utm, long t);
    
    public void addListener(DataModelListener l);
    
    public interface DataModelListener {
        public void dataModelUpdate();
    }
    
}
