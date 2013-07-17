/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.cmu.ri.airboat.buoytest;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author pscerri
 */
public class ProcessImage {

    public ProcessImage(BufferedImage img) {

        Raster raster = img.getData(new Rectangle(50, 50, 100, 100));
        
        int locX = 75;
        int locY = 75;
        
        BufferedImage sub = img.getSubimage(locX - 25, locY - 25, locX + 25, locY + 25);
    
    }

    public static void main(String[] argv) {
        BufferedImage img = null;

        try {
            img = ImageIO.read(new File("/Users/pscerri/Documents/Code/boat_img_paul/IMG_20111110_045836.jpg"));
            new ProcessImage(img);
        } catch (IOException e) {
            System.out.println("Failed: " + e);
        }

    }
}
