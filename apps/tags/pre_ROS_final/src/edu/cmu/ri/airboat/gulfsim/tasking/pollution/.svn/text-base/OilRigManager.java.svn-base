/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.gulfsim.ProxyManager;
import edu.cmu.ri.airboat.gulfsim.tasking.PlanOperator;
import edu.cmu.ri.airboat.gulfsim.tasking.Role;
import edu.cmu.ri.airboat.gulfsim.tasking.TOP;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class OilRigManager {

    ProxyManager proxyManager = new ProxyManager();
    
    PollutionModel pollutionModel = new PollutionModel();
    
    ArrayList<OilRig> rigs = new ArrayList<OilRig>();
        
    // Config params
    public static int noRigs = 10;
    public static double minLat = 22.0;
    public static double extentLat = 5.0;
    public static double minLon = -95.0;
    public static double extentLon = 9.0;
    
    Random rand = new Random();

    public OilRigManager() {
        for (int i = 0; i < noRigs; i++) {
            
            double lat = minLat + (extentLat * rand.nextDouble());
            double lon = minLon + (extentLon * rand.nextDouble());

            OilRig rig = new OilRig("Rig" + i, lat, lon);
            
            Role role = new RepeatVisitRole("Check rig", new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0.0));
            PlanOperator po = new PlanOperator("Checker", role);
            TOP top = new TOP("Oil Rig Monitor" + i, po);
            proxyManager.injectPlan(top);
            
            rigs.add(rig);
        }
    }
}
