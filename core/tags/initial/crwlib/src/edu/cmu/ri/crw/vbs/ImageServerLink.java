/*
 * ImageServerLink.java
 *
 * Created on June 9, 2007, 5:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package edu.cmu.ri.crw.vbs;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.EventListener;
import java.util.EventObject;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.event.EventListenerList;

/**
 * Connects to the ImageServer and parses incoming images, then sends them
 * to the appropriate listeners.
 * @author pkv
 */
public class ImageServerLink implements Runnable {
    private static final Logger logger = Logger.getLogger(ImageServerLink.class.getName());
    
    public static final int CAM_WIDTH = 320;
    public static final int CAM_HEIGHT = 240;
    
    public static final float CAM_FOV_X = 0.8727f;
    public static final float CAM_FOV_Y = 0.8727f * ((float)CAM_HEIGHT/(float)CAM_WIDTH);
    public static final float CAM_ERROR = 0.05f;
    
    // Connection to video server
    private String host;
    private int port;
    private Socket vidSock;
    private BufferedWriter out;
    private DataInputStream in;
    
    // Listener notification
    private Thread handler;
    protected EventListenerList listenerList;
     
    // Buffer for imaging
    private byte[] imgBuffer;
    
    // Multiview config information
    private boolean isMultiview = false;
    private int mvTileX = 0;
    private int mvTileY = 0;
    
    /** Creates a new instance of ImageServerLink */
    public ImageServerLink( String videoHost, int videoPort ) {
        host = videoHost;
        port = videoPort;
        vidSock = new Socket();
        imgBuffer = new byte[0];
        handler = new Thread();
        listenerList = new EventListenerList();
    }
    
    /**
     * Attempt a connection to the ImageServer, or do nothing if there is
     * already a connection open.
     * @return the success of the connection attempt.
     */
    public boolean connect() {
        // Don't connect if a connection is already open
        if (vidSock.isConnected() && !vidSock.isClosed()) return true;
        
        // Open a new connection to the server
        try {
            vidSock = new Socket();
            vidSock.connect(new InetSocketAddress(host, port), 2000);
            vidSock.setSoTimeout(500);
            out = new BufferedWriter(new OutputStreamWriter(vidSock.getOutputStream()));
            in = new DataInputStream(vidSock.getInputStream());
            
            handler = new Thread(this);
            handler.start();
            return true;
        } catch (Exception e) {
        	logger.warning("ImageServerLink: Connection error - " + e);
            return false;
        }
    }
    
    /**
     * Disconnect the current link, or do nothing if there is no connection.
     */
    public void disconnect() {
        try{
            // Close connection, if it exists
            vidSock.close();
            try { handler.join(2000); } catch (InterruptedException e) {}
        } catch (IOException e) {
            logger.warning("ImageServerLink: Disconnection error - " + e);
        }
    }
    
    /**
     * Indicates the connection status of the link.
     * @return true if there is a connection, false if not.
     */
    public boolean isConnected() {
        return (vidSock.isConnected() && !vidSock.isClosed());
    }
    
    /** 
     * Adds a new listener that will receive images from the server.
     * @param listener the listener object that will be added.
     */
    public void addImageEventListener(ImageEventListener listener) {
        listenerList.add(ImageEventListener.class, listener);
    }
    
    /**
     * Removes an existing listener from the notification list for receiving 
     * images from the server.
     * @param listener the listener object that will be removed.
     */
    public void removeUpdateEventListener(ImageEventListener listener) {
        listenerList.remove(ImageEventListener.class, listener);
    }
    
    /**
     * Handler that dispatches images to all of the listeners.
     * @param image the image received from the server.
     */
    protected void fireUpdateEventMessage(ImageEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i]==ImageEventListener.class) {
                ((ImageEventListener)listeners[i+1]).receivedImage(evt);
            }
        }

    }
    
    /** Creates a new instance of ImageServerLink */
    public ImageServerLink( String videoHost, int videoPort,
            int multiviewTileX, int multiviewTileY) {
        this(videoHost, videoPort);
        if (multiviewTileX >= 0 && multiviewTileY >=0 )
            this.isMultiview = true;
        this.mvTileX = multiviewTileX;
        this.mvTileY = multiviewTileY;
    }
    
    /** 
     * Reads in a valid image and stores it.
     * @return the success of the read operation.
     * @throws java.io.IOException
     */
    private synchronized boolean receiveImage() throws IOException {
        byte type = in.readByte();
        int size = in.readInt();

        // We expect types above 1, they are JPEG compressed
        if (type <= 1) 
            return false;

        // If we get a ridiculously sized image, just drain
        if ((size > 250000)||(size <= 0)) {
            drainImage();
            logger.warning("Failure : Invalid ImgServer image size : " + size);
            return false;
        }
        
        // Reallocate buffer to match image stream
        if (imgBuffer.length != size);
            imgBuffer = new byte[size];

        // Read in the whole image
        int pos = 0;
        while (pos < size) {
            pos += in.read(imgBuffer,pos,size-pos);
            Thread.yield();
        }
        
        // If we are using Multiview, isolate the appropriate section of the image
        if (isMultiview) {
            int offsetX = mvTileX*CAM_WIDTH;
            int offsetY = mvTileY*CAM_HEIGHT;
            
            BufferedImage fullImg = ImageIO.read(new java.io.ByteArrayInputStream(imgBuffer));
            if (offsetX + CAM_WIDTH > fullImg.getWidth())
                offsetX = fullImg.getWidth() - CAM_WIDTH;
            if (offsetY + CAM_HEIGHT > fullImg.getHeight())
                offsetY = fullImg.getHeight() - CAM_HEIGHT;
            
            BufferedImage croppedImg = fullImg.getSubimage(offsetX, offsetY, CAM_WIDTH, CAM_HEIGHT);
            java.io.ByteArrayOutputStream outputImg = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(croppedImg, "jpg", outputImg);
            imgBuffer = outputImg.toByteArray();
        }
        
        return true;
    }
   
    /**
     * Reads a valid image and ignores it.
     * @throws java.io.IOException
     */
    @SuppressWarnings("unused")
	private synchronized void skipImage() throws IOException {
        byte type = in.readByte();
        int size = in.readInt();

        // We expect types above 1, they are JPEG compressed
        if (type <= 1) 
            return;

        // If we get a ridiculously sized image, just drain
        if ((size > 250000)||(size <= 0)) {
            drainImage();
        	logger.warning("Failure : Invalid ImgServer image size : " + size);
            return;
        }
        
        // Skip the whole image
        int pos = 0;
        while (pos < size) {
            pos += in.skipBytes(size-pos);
            Thread.yield();
        }
    }
    
    /** 
     * Drains remaining bytes on input buffer.
     * @throws java.io.IOException
     */
    private synchronized void drainImage() throws IOException {
        // Just drain the read buffer, we don't want the data
        in.skipBytes(in.available());
    }
    
    /**
     * Request the next image from the image server.
     * @throws java.io.IOException
     */
    private synchronized void ackImage() throws IOException {
        if (out == null) return;
       
        // Send out the official 'OK'
        out.write("OK");
        out.flush();
    }
    
    /**
     * This gets a pointer to the <b>current image buffer</b>.
     * @return the current image.
     */
    public synchronized byte[] getDirectImage() {
        return imgBuffer;
    }
    
    /** 
     * This returns a copy of the current image.
     * @return a copy of the current image.
     */
    public synchronized byte[] getImage() {
        return imgBuffer.clone();
    }
    
    /** 
     * Non-blocking trigger to capture a single picture.
     */
    public void takePicture() {
        try {
            ackImage();
        } catch (IOException e) {
            logger.warning("Failed to request image from image server : " + e);
        }
    }
 
    @Override
    public void run() {
        while (vidSock.isConnected()) {
            try {
                // Wait for new data to come in
                while(in.available() == 0) { 
                    try {Thread.sleep(100);} 
                    catch (InterruptedException e) {} 
                }
              
                // Get the next image
                if (receiveImage()) {
                    // Alert the image listeners
                    fireUpdateEventMessage(new ImageEvent(this));
                    
                    // TODO: Remove this to disable streaming
                    try {Thread.sleep(100);}
                    catch (InterruptedException e) {}
                    takePicture();
                } else {
                    // Remove bits of this image, then re-request
                    drainImage();
                    takePicture();
                }
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                logger.warning("Error in image server connection : " + e);
                break;
            }
        }
    }
    
    public static class ImageEvent extends EventObject {
        private static final long serialVersionUID = 1L;

		public ImageEvent(ImageServerLink source) {
            super(source);
        }
    }
    
    public static interface ImageEventListener extends EventListener {
        public void receivedImage(ImageEvent evt);
    }

}

