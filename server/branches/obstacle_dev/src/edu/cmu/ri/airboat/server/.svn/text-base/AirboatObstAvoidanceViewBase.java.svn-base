package edu.cmu.ri.airboat.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.OrientationEventListener;

public abstract class AirboatObstAvoidanceViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	private static final String TAG = AirboatObstAvoidanceViewBase.class.getName();

    private Camera              mCamera;
    private SurfaceHolder       mHolder;
    private int                 mFrameWidth;
    private int                 mFrameHeight;
    private byte[]              mFrame;
    private boolean             mThreadRun;
    private byte[]              mBuffer;
	private OrientationEventListener MyOrientationEventListener;
	private int 				framecounter=0;
	private byte[][] framestack;// = new byte[5][];
	int frame_sz = 1536000;//518400 for george phone, nexus s. 1536000 for balajee's samsung note
	private byte[] frameArr = new byte[frame_sz];
	int N=5;

    
    public AirboatObstAvoidanceViewBase(Context context) {
        super(context);
        framestack = new byte[N][frame_sz];
        //frameArr = new byte[mFrame.length];
        mHolder = getHolder();
        mHolder.addCallback(this);
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public int getFrameWidth() {
        return mFrameWidth;
    }

    public int getFrameHeight() {
        return mFrameHeight;
    }

    public void setPreview() throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            mCamera.setPreviewTexture( new SurfaceTexture(10) );
        else
        	mCamera.setPreviewDisplay(null);
	}
    

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceCreated");
        mCamera.stopPreview();
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            mFrameWidth = width;
            mFrameHeight = height;

            // selecting optimal camera preview size
            {
                int  minDiff = Integer.MAX_VALUE;
                for (Camera.Size size : sizes) {
                    if (Math.abs(size.height - height) < minDiff) {
                        mFrameWidth = size.width;
                        mFrameHeight = size.height;
                        minDiff = Math.abs(size.height - height);
                    }
                }
            }

            params.setPreviewSize(getFrameWidth(), getFrameHeight());

           // mCamera.setDisplayOrientation(180);
            
            List<String> FocusModes = params.getSupportedFocusModes();
            if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
            {
            	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            
            //params.setRotation(90);
            //mCamera.setDisplayOrientation(180);
            mCamera.setParameters(params);

            /* Now allocate the buffer */
            params = mCamera.getParameters();
            int size = params.getPreviewSize().width * params.getPreviewSize().height;
            size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
            mBuffer = new byte[size];
            /* The buffer where the current frame will be coppied */
            mFrame = new byte [size];
            mCamera.addCallbackBuffer(mBuffer);

			try {
				setPreview();
			} catch (IOException e) {
				Log.e(TAG, "mCamera.setPreviewDisplay/setPreviewTexture fails: " + e);
			}

            /* Notify that the preview is about to be started and deliver preview size */
            onPreviewStared(params.getPreviewSize().width, params.getPreviewSize().height);

            /* Now we can start a preview */
            mCamera.startPreview();
        }
    }

//    public static void setCameraDisplayOrientation(Activity activity,
//            int cameraId, android.hardware.Camera camera) {
//        android.hardware.Camera.CameraInfo info =
//                new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(cameraId, info);
//        int rotation = 2;//activity.getWindowManager().getDefaultDisplay()
//                //.getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0: degrees = 0; break;
//            case Surface.ROTATION_90: degrees = 90; break;
//            case Surface.ROTATION_180: degrees = 180; break;
//            case Surface.ROTATION_270: degrees = 270; break;
//        }
//
//        int result;
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360;  // compensate the mirror
//        } else {  // back-facing
//            result = (info.orientation - degrees + 360) % 360;
//        }
//        camera.setDisplayOrientation(result);
//    }
   
    
    
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mCamera = Camera.open();
        //Camera.Parameters params = mCamera.getParameters();
        //params.setRotation(90);
        //mCamera.setParameters(params);
        //mCamera.setDisplayOrientation(180);
         
//        MyOrientationEventListener = new OrientationEventListener(this.getContext(),
//				SensorManager.SENSOR_DELAY_NORMAL) {
//			@Override
//			public void onOrientationChanged(int orientation) {
//				//mCamera.setDisplayOrientation(orientation);
//				if (orientation == ORIENTATION_UNKNOWN) return;
//			     android.hardware.Camera.CameraInfo info =
//			            new android.hardware.Camera.CameraInfo();
//			     android.hardware.Camera.getCameraInfo(0, info);
//			     orientation = (orientation + 45) / 90 * 90;
//			     int rotation = 0;
//			     rotation = (90 + orientation) % 360;
//			     Camera.Parameters params = mCamera.getParameters();
//			     params.setRotation(rotation);
//			     mCamera.setParameters(params);
//			     //mCamera.setDisplayOrientation(degrees)
//			     System.out.println(orientation+" "+info.orientation);
//			}
//		};
//
//		if (MyOrientationEventListener.canDetectOrientation()) {
//			MyOrientationEventListener.enable();
//		}
        
        //setCameraDisplayOrientation((Activity)this.getContext(), 0, mCamera);
        
        mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                synchronized (AirboatObstAvoidanceViewBase.this) {
//                	Camera.Parameters params = mCamera.getParameters();
//                    params.setRotation(180);
//                    mCamera.setParameters(params);
                	//mCamera.setDisplayOrientation(270);
                    System.arraycopy(data, 0, mFrame, 0, data.length);
                    AirboatObstAvoidanceViewBase.this.notify(); 
                }
                camera.addCallbackBuffer(mBuffer);
            }
        });
                    
        (new Thread(this)).start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        mThreadRun = false;
        if (mCamera != null) {
            synchronized (this) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
        onPreviewStopped();
    }

    /* The bitmap returned by this method shall be owned by the child and released in onPreviewStopped() */
    protected abstract Bitmap processFrame(byte[] data);//(byte[][] data);

    /**
     * This method is called when the preview process is beeing started. It is called before the first frame delivered and processFrame is called
     * It is called with the width and height parameters of the preview process. It can be used to prepare the data needed during the frame processing.
     * @param previewWidth - the width of the preview frames that will be delivered via processFrame
     * @param previewHeight - the height of the preview frames that will be delivered via processFrame
     */
    protected abstract void onPreviewStared(int previewWidtd, int previewHeight);

    /**
     * This method is called when preview is stopped. When this method is called the preview stopped and all the processing of frames already completed.
     * If the Bitmap object returned via processFrame is cached - it is a good time to recycle it.
     * Any other resourcses used during the preview can be released.
     */
    protected abstract void onPreviewStopped();

    public void run() {
    	
    	//uncomment on boat 	
//    	try{
//    		Thread.sleep(10000);
//    	}
//    	catch (Exception e) {
//            e.printStackTrace();
//        }
    
    	  
        mThreadRun = true;
        Log.i(TAG, "Starting processing thread");
        while (mThreadRun) {
            Bitmap bmp = null; 

            synchronized (this) {
                try {
                    this.wait();
                    
                    //for v1 approach
                	//if(mFrame!=null || !mFrame.equals(null))
                	bmp = processFrame(mFrame);
                    
                	
                	//for paul v2 approach
                    
//                    if(framecounter%N==0 && framecounter!=0)
//                    	bmp = processFrame(framestack);
//                    else
//                    {
//                    	//framestack[framecounter%5] = frameArr;
//                    	//byte[] oneframe = mFrame;
//                    	for(int k=0; k<mFrame.length; k++)
//                    	{
//                    		//=mFrame[k];
//                    		framestack[framecounter%N][k] = mFrame[k];
//                    	}
//                    	//framestack[framecounter%5] = mFrame.clone();
//                    }    
                    framecounter++;
                } catch (Exception e) {
                    e.printStackTrace();
                }      
            }                   
        
            if (bmp != null) {
                Canvas canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(bmp, (canvas.getWidth() - getFrameWidth()) / 2, (canvas.getHeight() - getFrameHeight()) / 2, null);
                    mHolder.unlockCanvasAndPost(canvas);
                      
                    if(framecounter%5==0)
                    {  
	                   	Date date = new Date();
	        			date.getDate();
	        			SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd_hhmmss");
	                    String filename = sdf.format(date) + ".jpg";
	                    
	                    // TODO: improve external file storage path handling (don't use Environment.getExternalStorageDirectory())
	                    // Make sure the photo path is ready to go (clear existing files)
	                    String filepath = Environment.getExternalStorageDirectory() + "/boatpix/" + filename;
	        			File photo = new File(filepath);
	        	        if (photo.exists()) photo.delete();
	        			
	        	        // Try to write the photo to the specified filename
	        			try {
	        				FileOutputStream fos = new FileOutputStream(photo.getPath(), true);
	        				//fos.(bmp);//jpeg[0]);
	        				bmp.compress(Bitmap.CompressFormat.JPEG, 50, fos);
	        				fos.close();
	        			} catch (java.io.IOException e) {
	        				Log.e(TAG, "Exception in photoCallback", e);
	        			}
                    }
        			
                    
                }
            }
        }
        //recorder.stop();
    }
    
    
}


//package edu.cmu.ri.airboat.server;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.pm.ActivityInfo;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.ImageFormat;
//import android.graphics.SurfaceTexture;
//import android.hardware.Camera;
//import android.hardware.SensorManager;
//import android.hardware.Camera.PreviewCallback;
//import android.hardware.Camera.CameraInfo;
//import android.media.CamcorderProfile;
//import android.media.MediaRecorder;
//import android.os.Build;
//import android.os.Environment;
//import android.util.Log;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.OrientationEventListener;
//
//public abstract class AirboatObstAvoidanceViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
//	private static final String TAG = AirboatObstAvoidanceViewBase.class.getName();
//
//    private Camera              mCamera;
//    private SurfaceHolder       mHolder;
//    private int                 mFrameWidth;
//    private int                 mFrameHeight;
//    private byte[]              mFrame;
//    private boolean             mThreadRun;
//    private byte[]              mBuffer;
//	private OrientationEventListener MyOrientationEventListener;
//	private int 				framecounter=0;
//	private byte[][] framestack;// = new byte[5][];
//	int frame_sz = 1536000;//518400 for george phone, nexus s. 1536000 for balajee's samsung note
//	private byte[] frameArr = new byte[frame_sz];
//	int N=5;
//
//    
//    public AirboatObstAvoidanceViewBase(Context context) {
//        super(context);
//        //framestack = new byte[N][frame_sz];
//        //frameArr = new byte[mFrame.length];
//        mHolder = getHolder();
//        mHolder.addCallback(this);
//        Log.i(TAG, "Instantiated new " + this.getClass());
//    }
//
//    public int getFrameWidth() {
//        return mFrameWidth;
//    }
//
//    public int getFrameHeight() {
//        return mFrameHeight;
//    }
//
//    public void setPreview() throws IOException {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//            mCamera.setPreviewTexture( new SurfaceTexture(10) );
//        else
//        	mCamera.setPreviewDisplay(null);
//	}
//    
//
//    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
//        Log.i(TAG, "surfaceCreated");
//        mCamera.stopPreview();
//        if (mCamera != null) {
//            Camera.Parameters params = mCamera.getParameters();
//            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
//            mFrameWidth = width;
//            mFrameHeight = height;
//
//            // selecting optimal camera preview size
//            {
//                int  minDiff = Integer.MAX_VALUE;
//                for (Camera.Size size : sizes) {
//                    if (Math.abs(size.height - height) < minDiff) {
//                        mFrameWidth = size.width;
//                        mFrameHeight = size.height;
//                        minDiff = Math.abs(size.height - height);
//                    }
//                }
//            }
//
//            params.setPreviewSize(getFrameWidth(), getFrameHeight());
//
//           // mCamera.setDisplayOrientation(180);
//            
//            List<String> FocusModes = params.getSupportedFocusModes();
//            if (FocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
//            {
//            	params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//            }
//            
//            //params.setRotation(90);
//            //mCamera.setDisplayOrientation(180);
//            mCamera.setParameters(params);
//
//            /* Now allocate the buffer */
//            params = mCamera.getParameters();
//            int size = params.getPreviewSize().width * params.getPreviewSize().height;
//            size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
//            mBuffer = new byte[size];
//            /* The buffer where the current frame will be coppied */
//            mFrame = new byte [size];
//            mCamera.addCallbackBuffer(mBuffer);
//
//			try {
//				setPreview();
//			} catch (IOException e) {
//				Log.e(TAG, "mCamera.setPreviewDisplay/setPreviewTexture fails: " + e);
//			}
//
//            /* Notify that the preview is about to be started and deliver preview size */
//            onPreviewStared(params.getPreviewSize().width, params.getPreviewSize().height);
//
//            /* Now we can start a preview */
//            mCamera.startPreview();
//        }
//    }
//
//       
//    public void surfaceCreated(SurfaceHolder holder) {
//        Log.i(TAG, "surfaceCreated");
//        mCamera = Camera.open();
//        //Camera.Parameters params = mCamera.getParameters();
//        //params.setRotation(90);
//        //mCamera.setParameters(params);
//        //mCamera.setDisplayOrientation(180);
//         
////        MyOrientationEventListener = new OrientationEventListener(this.getContext(),
////				SensorManager.SENSOR_DELAY_NORMAL) {
////			@Override
////			public void onOrientationChanged(int orientation) {
////				//mCamera.setDisplayOrientation(orientation);
////				if (orientation == ORIENTATION_UNKNOWN) return;
////			     android.hardware.Camera.CameraInfo info =
////			            new android.hardware.Camera.CameraInfo();
////			     android.hardware.Camera.getCameraInfo(0, info);
////			     orientation = (orientation + 45) / 90 * 90;
////			     int rotation = 0;
////			     rotation = (90 + orientation) % 360;
////			     Camera.Parameters params = mCamera.getParameters();
////			     params.setRotation(rotation);
////			     mCamera.setParameters(params);
////			     //mCamera.setDisplayOrientation(degrees)
////			     System.out.println(orientation+" "+info.orientation);
////			}
////		};
////
////		if (MyOrientationEventListener.canDetectOrientation()) {
////			MyOrientationEventListener.enable();
////		}
//        
//        //setCameraDisplayOrientation((Activity)this.getContext(), 0, mCamera);
//        
//        mCamera.setPreviewCallbackWithBuffer(new PreviewCallback() {
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                synchronized (AirboatObstAvoidanceViewBase.this) {
////                	Camera.Parameters params = mCamera.getParameters();
////                    params.setRotation(180);
////                    mCamera.setParameters(params);
//                	mCamera.setDisplayOrientation(270);
//                    System.arraycopy(data, 0, mFrame, 0, data.length);
//                    AirboatObstAvoidanceViewBase.this.notify(); 
//                }
//                camera.addCallbackBuffer(mBuffer);
//            }
//        });
//                    
//        (new Thread(this)).start();
//    }
//
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.i(TAG, "surfaceDestroyed");
//        mThreadRun = false;
//        if (mCamera != null) {
//            synchronized (this) {
//                mCamera.stopPreview();
//                mCamera.setPreviewCallback(null);
//                mCamera.release();
//                mCamera = null;
//            }
//        }
//        onPreviewStopped();
//    }
//
//    /* The bitmap returned by this method shall be owned by the child and released in onPreviewStopped() */
//    protected abstract Bitmap processFrame(byte[] data);//(byte[][] data);
//
//    /**
//     * This method is called when the preview process is beeing started. It is called before the first frame delivered and processFrame is called
//     * It is called with the width and height parameters of the preview process. It can be used to prepare the data needed during the frame processing.
//     * @param previewWidth - the width of the preview frames that will be delivered via processFrame
//     * @param previewHeight - the height of the preview frames that will be delivered via processFrame
//     */
//    protected abstract void onPreviewStared(int previewWidtd, int previewHeight);
//
//    /**
//     * This method is called when preview is stopped. When this method is called the preview stopped and all the processing of frames already completed.
//     * If the Bitmap object returned via processFrame is cached - it is a good time to recycle it.
//     * Any other resourcses used during the preview can be released.
//     */
//    protected abstract void onPreviewStopped();
//
//    public void run() {
//    	
//    	//uncomment on boat 	
//    	try{
//    		Thread.sleep(10000);
//    	}
//    	catch (Exception e) {
//            e.printStackTrace();
//        }
//    	
//    	
//    	
//    	//start video capture
////    	MediaRecorder recorder = new MediaRecorder();;
////    	//recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
////        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
////
////        CamcorderProfile cpHigh = CamcorderProfile
////                .get(CamcorderProfile.QUALITY_HIGH);
////        recorder.setProfile(cpHigh);
////        recorder.setOutputFile("/sdcard/videocapture_example.mp4");
////        recorder.setMaxDuration(50000); // 50 seconds
////        recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
////    	
////        recorder.start();
//        
////        try {
////			recorder.prepare();
////		} catch (IllegalStateException e1) {
////			// TODO Auto-generated catch block
////			e1.printStackTrace();
////		} catch (IOException e1) {
////			// TODO Auto-generated catch block
////			e1.printStackTrace();
////		}
//    	
//    	  
//        mThreadRun = true;
//        Log.i(TAG, "Starting processing thread");
//        while (mThreadRun) {
//            Bitmap bmp = null; 
//
//            synchronized (this) {
//                try {
//                    this.wait();
//                    
//                    //for v1 approach
//                	//if(mFrame!=null || !mFrame.equals(null))
//                	bmp = processFrame(mFrame);
//                    
//                    
//                    //for paul v2 approach
//                    
////                    if(framecounter%N==0 && framecounter!=0)
////                    	bmp = processFrame(framestack);
////                    else
////                    {
////                    	//framestack[framecounter%5] = frameArr;
////                    	//byte[] oneframe = mFrame;
////                    	for(int k=0; k<mFrame.length; k++)
////                    	{
////                    		//=mFrame[k];
////                    		framestack[framecounter%N][k] = mFrame[k];
////                    	}
////                    	//framestack[framecounter%5] = mFrame.clone();
////                    }
//                    framecounter++;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (bmp != null) {
//                Canvas canvas = mHolder.lockCanvas();
//                if (canvas != null) {
//                    canvas.drawBitmap(bmp, (canvas.getWidth() - getFrameWidth()) / 2, (canvas.getHeight() - getFrameHeight()) / 2, null);
//                    mHolder.unlockCanvasAndPost(canvas);
//                    
//                    if(framecounter%100==0)
//                    {
//	                   	Date date = new Date();
//	        			date.getDate();
//	        			SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd_hhmmss");
//	                    String filename = sdf.format(date) + ".jpg";
//	                    
//	                    // TODO: improve external file storage path handling (don't use Environment.getExternalStorageDirectory())
//	                    // Make sure the photo path is ready to go (clear existing files)
//	                    String filepath = Environment.getExternalStorageDirectory() + "/boatpix/" + filename;
//	        			File photo = new File(filepath);
//	        	        if (photo.exists()) photo.delete();
//	        			
//	        	        // Try to write the photo to the specified filename
//	        			try {
//	        				FileOutputStream fos = new FileOutputStream(photo.getPath(), true);
//	        				//fos.(bmp);//jpeg[0]);
//	        				bmp.compress(Bitmap.CompressFormat.JPEG, 50, fos);
//	        				fos.close();
//	        			} catch (java.io.IOException e) {
//	        				Log.e(TAG, "Exception in photoCallback", e);
//	        			}
//                    }
//        			
//                    
//                }
//            }
//        }
//        //recorder.stop();
//    }
//    
//    
//}
