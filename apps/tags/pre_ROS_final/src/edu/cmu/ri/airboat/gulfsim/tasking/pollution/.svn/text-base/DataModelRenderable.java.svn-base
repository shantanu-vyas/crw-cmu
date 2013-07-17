/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.gulfsim.tasking.pollution;

import edu.cmu.ri.airboat.client.UtmUtils.UTM;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 *
 * @author pscerri
 */
public class DataModelRenderable implements DataModel.DataModelListener {
    
    // For testing filter
    public static void main(String argv[]) {
        DataServer.avgTesterDataTime = 100;
        new DataModelRenderable(new LayeredPFModel());
    }
    
    
    private final LayeredPFModel model;

    double minLatD = 41.06;
    double maxLatD = 41.13;
    double minLonD = 16.74;
    double maxLonD = 16.90;
    
    Angle minLat = Angle.fromDegrees(minLatD);
    Angle maxLat = Angle.fromDegrees(maxLatD);
    Angle minLon = Angle.fromDegrees(minLonD);
    Angle maxLon = Angle.fromDegrees(maxLonD);
    
    Sector sector = new Sector(minLat, maxLat, minLon, maxLon);
    
    private RenderableLayer rl = null;
    
    public DataModelRenderable(LayeredPFModel model) {
        this.model = model;
        model.setSector(sector);
        model.addListener(this);
        
        // Create a data server (don't know where this should be called
        new DataServer(model);
    }
    
    public void setRenderableLayer(RenderableLayer rl) {
        this.rl = rl;
        render();
    }

    public void dataModelUpdate() {
        render();
    }
    
    private void render() {
        if (rl != null) {
            rl.removeAllRenderables();
            SurfaceImage si = new SurfaceImage(getAsImage(), sector);
            rl.addRenderable(si);     
            System.out.println("Changed renderable");
        }
    }

    public BufferedImage getAsImage() {
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();

        int w = 100;
        int h = 100;
        
        double dLat = (maxLatD - minLatD)/((double)h);
        double dLon = (maxLonD - minLonD)/((double)w);
        
        int[] data = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                
                UTMCoord utm = UTMCoord.fromLatLon(Angle.fromDegrees(minLatD + (j * dLat)), Angle.fromDegrees(minLonD + (i * dLon)));
                double r = model.getValue(utm, 0);
                
                // @todo Hacked mapping of double to renderable int
                int ri = Math.min(255, (int)(r * 123.0));
                int gi = 0;
                
                if (r < 0) {
                    gi = -ri;
                    ri = 0;
                }
                
                int a = Math.min(220, 5 * Math.max(gi, ri)); 
                
                // System.out.println(r + " to " + ri + " " + gi);
                
                // @todo Make opacity variable
                data[i * h + j] = a << 24 | ri << 16 | gi << 8;                
            }
        }

        /*
        DataBufferFloat db = new DataBufferFloat(data, w*h);
        SampleModel sm = 
        
        WritableRaster wr = WritableRaster.createWritableRaster(sm, db, null);
         */

        BufferedImage img = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
        //BufferedImage img = gc.createCompatibleImage(w, h, Transparency.OPAQUE);
        img.setRGB(0, 0, w, h, data, 0, w);

        return img;
    }
}
