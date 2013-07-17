package com.example.android.photobyintent;

public class OpenCV {
	static{
		System.loadLibrary("opencv");
	}
	public native boolean setSourceImage(int[] pixels, int width, int height);
	public native byte[] getSourceImage();
	public native void extractWater();
}