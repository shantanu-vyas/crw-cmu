/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.gulfsim.UtmUtils;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.io.SAXReader;

/**
 * Dummy
 * 
 * @author pscerri
 */
public class DataServer {

    public static long avgTesterDataTime = 10000;
    
    private final LearnedDataModel model;
    Random rand = new Random();

    public DataServer(LearnedDataModel m) {
        this.model = m;

        (new TempTestThread()).start();
    }

    class TempTestThread extends Thread {

        double minLatD = 41.06;
        double maxLatD = 41.13;
        double minLonD = 16.74;
        double maxLonD = 16.90;
        double dLat = maxLatD - minLatD;
        double dLon = maxLonD - minLonD;
        Angle minLat = Angle.fromDegrees(minLatD);
        Angle maxLat = Angle.fromDegrees(maxLatD);
        Angle minLon = Angle.fromDegrees(minLonD);
        Angle maxLon = Angle.fromDegrees(maxLonD);
        Sector sector = new Sector(minLat, maxLat, minLon, maxLon);
        UTMCoord center = UTMCoord.fromLatLon(Angle.fromDegrees(41.11), Angle.fromDegrees(16.85));

        public void run() {

            while (true) {

                try {
                    sleep(rand.nextInt(10000));
                } catch (Exception e) {
                }

                double realTemp = getYahooTemp();
                
                UTMCoord loc = UTMCoord.fromLatLon(Angle.fromDegrees(minLatD + (rand.nextDouble() * dLat)), Angle.fromDegrees(minLonD + (rand.nextDouble() * dLon)));
                double dist = Math.min(10.0, 1.0 / (UtmUtils.dist(loc, center) / 10000.0)); 
                
                // @todo This should be coming in from client
                // double obsTemp = realTemp + (dist * rand.nextDouble() - 0.5);
                double obsTemp = realTemp + dist;
                System.out.println("Generated " + obsTemp + " with " + dist + " and real dist " + (UtmUtils.dist(loc, center)));

                model.addObservation(obsTemp - realTemp, loc, 0);

            }

        }
    }
    double prevYahooTemp = -1.0;
    long prevYahooTempTime = 0;

    private double getYahooTemp() {

        long currTime = System.currentTimeMillis();
        if (currTime - prevYahooTempTime > 1000 * 60 * 10) {

            String url = "http://weather.yahooapis.com/forecastrss?w=710722&u=c";
            try {
                URLConnection conn = new URL(url).openConnection();


                Map<String, String> uris = new HashMap<String, String>();
                uris.put("y", "http://xml.weather.yahoo.com/ns/rss/1.0");

                DocumentFactory factory = new DocumentFactory();
                factory.setXPathNamespaceURIs(uris);

                SAXReader xmlReader = new SAXReader();
                xmlReader.setDocumentFactory(factory);

                Document doc = xmlReader.read(conn.getInputStream());

                String temp = doc.valueOf("/rss/channel/item/y:condition/@temp");

                System.out.println("Yahoo returned " + temp);

                prevYahooTemp = Double.parseDouble(temp);

            } catch (Exception e) {
                System.out.println("Getting temp failed: " + e);
            }
            prevYahooTempTime = currTime;
        } else {
            //System.out.println("Using previous temp");
        }

        return prevYahooTemp;
    }
}
