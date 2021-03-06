#include <jni.h>
#include <string.h>
#include <android/log.h>

#define LOGTAG "Sobel_Native"

JNIEXPORT void JNICALL Java_com_imrannazar_sobel_OverlayView_doNativeProcessing(
	JNIEnv *env, jobject this,
	jbyteArray frame, jint width, jint height, jobject diff)
{
	jboolean framecopy;
	jint *dbuf = (jint*)((*env)->GetDirectBufferAddress(env, diff));
	jbyte *fbuf = (*env)->GetByteArrayElements(env, frame, &framecopy);
	int x, y, maxx=width-1, maxy=height-1, p=width+1, px, py, ps;

	for(y=1; y<maxy; y++, p+=2)
	{
		for(x=1; x<maxx; x++, p++)
		{
			px = fbuf[p+width+1]-fbuf[p+width-1]+fbuf[p+1]+fbuf[p+1]-fbuf[p-1]-fbuf[p-1]+fbuf[p-width+1]-fbuf[p-width-1];
			py = fbuf[p-width-1]+fbuf[p-width]+fbuf[p-width]+fbuf[p-width+1]-fbuf[p+width-1]-fbuf[p+width]-fbuf[p+width]-fbuf[p+width+1];
			if(px<0) px=-px; if(py<0) py=-py;
			ps=px+py; if(ps>95) ps=255; if(ps<=95) ps=0;
			dbuf[p] = (ps<<24)|(ps<<16)|(ps<<8)|ps;
//			dbuf[p] = (ps<<24)|0x00FFFFFF;
		}
	}
}
