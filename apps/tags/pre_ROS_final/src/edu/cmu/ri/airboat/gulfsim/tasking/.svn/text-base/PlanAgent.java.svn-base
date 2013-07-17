/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.cmu.ri.airboat.gulfsim.tasking;

import edu.cmu.ri.airboat.gulfsim.ProxyManager;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class PlanAgent {
    private final TOP top;

    ProxyManager proxyManager = new ProxyManager();
    
    private ArrayList<RoleAgent> currRoleAgents = new ArrayList<RoleAgent>();
    
    public PlanAgent(TOP top) {
        this.top = top;
    
        System.out.println("New plan agent for " + top);
        startRoles();
    }
    
    /**
     * Very simple version
     */
    private void startRoles() {
        PlanOperator root = top.getRootOp();
        Role r = root.getRole();
        if (r != null) {
            System.out.println("RoleAgent created for " + r);
            RoleAgent ra = new RoleAgent(r, this);
            currRoleAgents.add(ra);
            
            BasicMarkerAttributes attr = new BasicMarkerAttributes(Material.WHITE, BasicMarkerShape.SPHERE, 1.0);            
            proxyManager.getMarkers().add(new TaskMarker(top, r.getLocation(), attr));
            proxyManager.redraw();
        }
    }
}
