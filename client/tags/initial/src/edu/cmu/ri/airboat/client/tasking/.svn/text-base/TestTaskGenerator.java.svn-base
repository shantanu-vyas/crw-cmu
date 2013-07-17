/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.client.tasking;

import edu.cmu.ri.airboat.client.ProxyManager;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.util.Random;

/**
 * Coords for middle of Gulf of Mexico
 * 
 * 27.888232400787924° -95.41387253353845°
 * 22.17680553262256° -95.1410044879738°
 * 22.502117356995505° -85.67717280257853°
 * 28.26157399256305° -86.05905668657564°
 * @author pscerri
 */
public class TestTaskGenerator extends Thread {

    ProxyManager proxyManager = new ProxyManager();

    public TestTaskGenerator() {
        start();
    }
    
    double minLat = 22.0;
    double extentLat = 5.0;
    double minLon = -95.0;
    double extentLon = 9.0;
    Random rand = new Random();

    public void run() {

        for (int i = 0; i < 1; i++) {

            try {
                sleep(5000);
            } catch (Exception e) {
            }

            double lat = minLat + (extentLat * rand.nextDouble());
            double lon = minLon + (extentLon * rand.nextDouble());

            Role role = new Role("TestRole", new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0.0));
            PlanOperator po = new PlanOperator("Single", role);
            TOP top = new TOP(po);

            System.out.println("Created plan");

            proxyManager.injectPlan(top);
        }
    }
}
