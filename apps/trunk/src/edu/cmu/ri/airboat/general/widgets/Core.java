/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.general.widgets;

import edu.cmu.ri.airboat.general.SkeletonBoatProxy;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class Core {

    static _Core core = new _Core();

    public void addBoatProxy(SkeletonBoatProxy bp) {
        core.proxies.add(bp);
        for (ProxyListener proxyListener : core.listeners) {
            proxyListener.proxyAdded(bp);
        }
    }
    
    public void proxyUpdated(SkeletonBoatProxy bp) {
        for (ProxyListener proxyListener : core.listeners) {
            proxyListener.proxyUpdated(bp);
        }
    }
    
    public void addListener(ProxyListener l) {
        core.listeners.add(l);
    }

    public void removeListener(ProxyListener l) {
        core.listeners.remove(l);
    }

    private static class _Core {

        private ArrayList<ProxyListener> listeners = new ArrayList<ProxyListener>();
        
        private ArrayList<SkeletonBoatProxy> proxies = new ArrayList<SkeletonBoatProxy>();
        
    }
}
