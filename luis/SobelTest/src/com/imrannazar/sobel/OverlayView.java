package com.imrannazar.sobel;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class OverlayView extends SurfaceView implements SurfaceHolder.Callback {
	private Camera mCam;
	private SurfaceHolder mOverSH;
	private byte[] mFrame;
	private IntBuffer mFrameDiff;
	private Camera.Size mFrameSize;
	private boolean mRunning;
	private PostMortemReportExceptionHandler pmeh;

	private native void doNativeProcessing(byte[] frame, int width, int height,
			IntBuffer diff);

	static {
		System.loadLibrary("sobel-native");
	}

	public OverlayView(Context c, AttributeSet attr) {
		super(c, attr);
		mOverSH = getHolder();
		mOverSH.addCallback(this);
	}

	public void setPostMortemReportExceptionHandler(
			PostMortemReportExceptionHandler p) {
		pmeh = p;
	}

	public void setCamera(Camera c) {
		mCam = c;
		if (mCam == null)
			return;

		mCam.setPreviewCallback(new PreviewCallback() {
			public void onPreviewFrame(byte[] f, Camera c) {
				if (f.length != (mFrame.length * 1.5)) {
					pmeh.preErrStr += "Preview input doesn't match frame size; skipping\n";
					return;
				}

				Camera.Parameters p = mCam.getParameters();
				Camera.Size fs = p.getPreviewSize();
				if (fs.width != mFrameSize.width
						|| fs.height != mFrameSize.height) {
					setPreviewSize(fs);
				}

				Canvas cOver = null;
				Paint pt = new Paint();
				pt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
				pt.setColor(Color.WHITE);
				pt.setAlpha(0xFF);

				if (mRunning)
					cOver = mOverSH.lockCanvas(null);

				if (mRunning && cOver != null) {
					try {
						// Log.v(LOGTAG,
						// "Preview input: "+f.length+" bytes; frame copy: "+mFrame.length+" bytes");
						pmeh.preErrStr += "Preview input: " + f.length
								+ " bytes; frame copy: " + mFrame.length
								+ " bytes\n";
						System.arraycopy(f, 0, mFrame, 0, mFrame.length);
						doNativeProcessing(mFrame, mFrameSize.width,
								mFrameSize.height, mFrameDiff);
						mFrameDiff.position(0);

						Bitmap bmp = Bitmap
								.createBitmap(mFrameSize.width >> 0,
										mFrameSize.height >> 0,
										Bitmap.Config.ARGB_8888);
						bmp.copyPixelsFromBuffer(mFrameDiff);
						Rect src = new Rect(0, 0, (mFrameSize.width >> 0) - 1,
								(mFrameSize.height >> 0) - 1);
						RectF dst = new RectF(0, 0, cOver.getWidth() - 1, cOver
								.getHeight() - 1);

						cOver.drawBitmap(bmp, src, dst, pt);
					} finally {
						mOverSH.unlockCanvasAndPost(cOver);
					}
				}
			}
		});
	}

	public void setRunning(boolean r) {
		mRunning = r;
	}

	public void setPreviewSize(Camera.Size s) {
		// Log.v(LOGTAG, "Setting new preview size: "+s.width+"x"+s.height);
		pmeh.preErrStr += "Setting new preview size: " + s.width + "x"
				+ s.height + "\n";
		mFrameSize = s;
		mFrame = new byte[s.width * s.height];
		mFrameDiff = ByteBuffer.allocateDirect((s.width * s.height) << 2)
				.asIntBuffer();
	}

	public void surfaceCreated(SurfaceHolder sh) {
	}

	public void surfaceDestroyed(SurfaceHolder sh) {
	}

	public void surfaceChanged(SurfaceHolder sh, int format, int w, int h) {
	}
}
