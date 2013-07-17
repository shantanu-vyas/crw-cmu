/* 
 * File:   main.cpp
 * Author: pototo
 *
 * Created on June 3, 2011, 10:28 AM
 * 
 * This program recognizes the water in front of the boat by usign ground
 * plane detection techniques already available. This will look for the pixels 
 * from the boat, and go up the image until it finds the vector plane the repre-
 * sents the water
 */

#include <cstdlib>
#include <math.h>
#include <string.h>
#include <iostream>

#include "highgui.h"
#include "cv.h"

using namespace std;

/*
 * 
 */
//Maxing out (saturating) only the “S” and “V” parts of an HSV image
void saturate_sv( IplImage* img );

int main(int argc, char** argv) 
{
    IplImage* boatFront = cvLoadImage( "2011-06-02 19.32.27.jpg" );
    boatFront->origin = IPL_ORIGIN_TL;   //sets image origin to top left corner
    //cvSetImageROI(boatFront, cvRect(x,y,width,height));
    //cvAddS(boatFront, cvScalar(add),boatFront);
    cvNamedWindow( "Boat Front", 0);   //0 to maintains sizes regardles of image size
    cvResizeWindow("Boat Front",700,550); // new width/heigh in pixels
    //cvMoveWindow("Boat Front", 350, 150);
    //saturate_sv(boatFront);
    cvShowImage( "Boat Front", boatFront );
    //cvSaveImage("textFile.txt",boatFront);
    //cvRectangle(boatFront, cvPoint(5,10), cvPoint(20,30),cvScalar(255,255,255));
    cout << "image height: " << boatFront->height << endl;
    cout << "image width: " << boatFront->width << endl;
    cout << "image depth: " << boatFront->depth << endl;
    cout << "image channels: " << boatFront->nChannels << endl;
    cout << "image size: " << boatFront->nSize << endl;
    cout << "image buffer size: " << boatFront->width*boatFront->height*boatFront->nChannels << endl;
    cvLine(boatFront, cvPoint(100,100), cvPoint(200,200), cvScalar(0,255,0), 1);
    cvWaitKey(0);
    cvResetImageROI(boatFront);
    cvReleaseImage( &boatFront );
    cvDestroyWindow( "Boat Front" );
    
    return 0;
    
}

void saturate_sv( IplImage* img ) 
{
    for( int y=0; y < img->height; y++ ) 
    {
        uchar* ptr = (uchar*)(img->imageData + y*img->widthStep);
        for( int x=0; x < img->width; x++ ) 
        {
            if (ptr[3*x+1] > ptr[3*x+2])
                ptr[3*x+1] = 255;
            else
                ptr[3*x+2] = 255; 
        }
    }
}

