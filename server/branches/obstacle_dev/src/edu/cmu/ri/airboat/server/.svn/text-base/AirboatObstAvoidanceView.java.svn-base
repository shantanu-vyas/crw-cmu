package edu.cmu.ri.airboat.server;

import edu.cmu.ri.crw.VehicleServer;
import edu.cmu.ri.crw.data.Twist;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AirboatObstAvoidanceView extends AirboatObstAvoidanceViewBase {

	private int mFrameSize;
	private Bitmap mBitmap;
	private int[] mRGBA;
	int[] obstacleLocations;
	static int framenum=0;
	private VehicleServer vehicleServer = null;
	
    public AirboatObstAvoidanceView(Context context) {
        super(context);
    }

	@Override
	protected void onPreviewStared(int previewWidtd, int previewHeight) {
		mFrameSize = previewWidtd * previewHeight;
		mRGBA = new int[mFrameSize];
		mBitmap = Bitmap.createBitmap(previewWidtd, previewHeight, Bitmap.Config.ARGB_8888);
	}

	@Override
	protected void onPreviewStopped() {
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		mRGBA = null;
	}
	
	// decode Y, U, and V values on the YUV 420 buffer described as YCbCr_422_SP by Android 
	// David Manpearl 081201 
	public void decodeYUV(int[] out, byte[] fg, int width, int height)
	        throws NullPointerException, IllegalArgumentException {
	    int sz = width * height;
	    if (out == null)
	        throw new NullPointerException("buffer out is null");
	    if (out.length < sz)
	        throw new IllegalArgumentException("buffer out size " + out.length
	                + " < minimum " + sz);
	    if (fg == null)
	        throw new NullPointerException("buffer 'fg' is null");
	    if (fg.length < sz)
	        throw new IllegalArgumentException("buffer fg size " + fg.length
	                + " < minimum " + sz * 3 / 2);
	    int i, j;
	    int Y, Cr = 0, Cb = 0;
	    for (j = 0; j < height; j++) {
	        int pixPtr = j * width;
	        final int jDiv2 = j >> 1;
	        for (i = 0; i < width; i++) {
	            Y = fg[pixPtr];
	            if (Y < 0)
	                Y += 255;
	            if ((i & 0x1) != 1) {
	                final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
	                Cb = fg[cOff];
	                if (Cb < 0)
	                    Cb += 127;
	                else
	                    Cb -= 128;
	                Cr = fg[cOff + 1];
	                if (Cr < 0)
	                    Cr += 127;
	                else
	                    Cr -= 128;
	            }
	            int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
	            if (R < 0)
	                R = 0;
	            else if (R > 255)
	                R = 255;
	            int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1)
	                    + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
	            if (G < 0)
	                G = 0;
	            else if (G > 255)
	                G = 255;
	            int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
	            if (B < 0)
	                B = 0;
	            else if (B > 255)
	                B = 255;
	            out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
	        }
	    }

	}
	

    @Override
    protected Bitmap processFrame(byte[] data){//(byte[][] data) {
        //int[] rgba = mRGBA;
        int[] rgb = new int[mFrameSize*4];//*3];//*4];
        /////////////////////////////
     
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inDither = true;	
//        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(data, 0, getFrameWidth(), opt);//, opts);//
       // decodeYUV(rgb, data, 720, 480);

        obstacleLocations = FindFeatures(getFrameWidth(), getFrameHeight(), data, rgb, framenum);
      
//        Intent obstacleInfo  = new Intent(AirboatImpl.OBSTACLE);
//        obstacleInfo.putExtra("shit", true);
//        this.getContext().sendBroadcast();
        
        framenum++;
        
        //Bitmap bmp = Bitmap.createBitmap(720, 480, Bitmap.Config.ARGB_8888);
        //bmp.setPixels(rgb, 0/* offset */, 720 /* stride */, 0, 0, 720, 480);
        
        Bitmap bmp = mBitmap; 
        bmp.setPixels(rgb, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
        
        Twist twist = new Twist(); 
		// Compute the distance and angle to the waypoint
		double distanceSq = 7;//7;//10;//planarDistanceSq(pose, waypoint);
		int leftStr8OrRight= obstacleLocations[0];
		double thrust=1250;
		 
		if (leftStr8OrRight==1) { //complete right turn
			twist.dx(0); 
			twist.drz(90);//60);//between 30 and 90
		    vehicleServer = ((AirboatObstAvoidanceActivity)this.getContext()).airboatService.getServer();
			vehicleServer.setVelocity(twist);
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			twist.dx(thrust); 
			twist.drz(150);//60);//120);//between 90 and 150
		}
		else if (leftStr8OrRight==3) //hard left 
		{ 
			twist.dx(0); 
			twist.drz(90);//60);//between 30 and 90
		    vehicleServer = ((AirboatObstAvoidanceActivity)this.getContext()).airboatService.getServer();
			vehicleServer.setVelocity(twist);
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			twist.dx(thrust); 
			twist.drz(30);//60);//between 30 and 90
		} 
		else if (leftStr8OrRight==2) //half turn right
		{
			twist.dx(thrust);
			twist.drz(120);    
		} 
		else if (leftStr8OrRight==5) //half left turn
		{    
			twist.dx(thrust); 
			twist.drz(60);              
		}   	 
		else if (leftStr8OrRight==4)//fwd 
		{ 
			twist.dx(thrust);
			twist.drz(90);   
		}  
		else if (leftStr8OrRight==0)
		{
			//twist.dx(0); /* Min speed of 1 m/s and Max speed of 3 m/s and max speed of whatever its capped at*/
			//twist.drz(0); //Ensures min turn rate (apart from 0) is increased by a factor of 2 capped off at 0.78	
			twist.dx(0);
			twist.drz(90);      
		}
//		twist.dx(1200);  
//		twist.drz(30);
           
		//UNCOMMENT WHEN TESTING WITH THE BOATS
	    vehicleServer = ((AirboatObstAvoidanceActivity)this.getContext()).airboatService.getServer();
		vehicleServer.setVelocity(twist);
	
        return bmp;
    }

    public native int[] FindFeatures(int width, int height, byte yuv[]/*byte yuv[][]*/, int[] rgba, int framenum);

    static {
        System.loadLibrary("native_sample");
    }
}
