/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.irrigationtest;

import edu.cmu.ri.airboat.general.Observation;

/**
 *
 * @author pscerri
 */
public interface IrrigationTestInterface  {
    
    public void setWaypoints(int boatNo, double [][] poses);
    
    public void addListener(IrrigationTestInterfaceListener l);
    
    public void setExtent(double [] ul, double [] lr);
    
    public void shutdown();
    
    public interface IrrigationTestInterfaceListener {
        
        public void newObservation (Observation o);
        public void newBoatPosition (int boatNo, double [] pose);
        public void boatDone(int no);
    }
    
}
