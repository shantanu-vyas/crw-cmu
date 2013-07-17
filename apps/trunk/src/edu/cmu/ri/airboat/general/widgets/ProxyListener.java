/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general.widgets;

import edu.cmu.ri.airboat.general.SkeletonBoatProxy;

/**
 *
 * @author pscerri
 */
public interface ProxyListener {

    public void proxyUpdated(SkeletonBoatProxy p);
    
    public void proxyAdded(SkeletonBoatProxy p);
    
}
