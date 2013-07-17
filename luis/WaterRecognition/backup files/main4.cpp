/* 
 * File:   main.cpp
 * Author: pototo
 *
 * Created on June 3, 2011, 10:28 AM
 * 
 * This program recognizes the water in front of the boat by usin ground
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

//access the elements of a picture
template<class T> class Image
{
  private:
      IplImage* imgp;
  public:
      Image(IplImage* img=0) {imgp=img;}
      ~Image(){imgp=0;}
      void operator=(IplImage* img) {imgp=img;}
      inline T* operator[](const int rowIndx) {
      return ((T *)(imgp->imageData + rowIndx*imgp->widthStep));}
};
typedef struct{
  unsigned char h,s,v;
} HsvPixel;
typedef struct{
  float h,s,v;
} HsvPixelFloat;
typedef Image<HsvPixel>       HsvImage;
typedef Image<HsvPixelFloat>  HsvImageFloat;
typedef Image<unsigned char>  BwImage;
typedef Image<float>          BwImageFloat;

int main(int argc, char** argv) 
{
    /***********************************************************************/
    IplImage* boatFront = cvLoadImage( "bigObstacle.jpg" );
    boatFront->origin = IPL_ORIGIN_TL;   //sets image origin to top left corner
    
    //Crop the image to the ROI
    cvSetImageROI(boatFront, cvRect(0,0,boatFront->height/0.5,boatFront->width/1.83));
    int X = boatFront->height;
    int Y = boatFront->width;
    //split the HSV image into individual channels
    IplImage* imageHue = cvCreateImage(cvSize(X/3,Y/3),IPL_DEPTH_8U,1);
    IplImage* imageSat = cvCreateImage(cvSize(X/3,Y/3),IPL_DEPTH_8U,1);
    IplImage* imageVal = cvCreateImage(cvSize(X/3,Y/3),IPL_DEPTH_8U,1);
    
    /***********************************************************************/
    int itemsNumber = 20;  //number of items for an n x n matrix and # of neighbors
    const int arraySize = itemsNumber;
    const int threeArraySize = 3*itemsNumber;
    //if n gets changed, then the algorithm might have to be
    //recalibrated. Try to keep it constant
    //these variables are used for the k nearest neighbors
    int accuracy;
    float trainingSampleCount = arraySize;
    //these variables are use to travers the picture by blocks of n x n pixels at
    //a time. 
    //Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
    //right way, of course)
    //x and y are the dimensions of the local patch of pixels
    int x = boatFront->height/2 + itemsNumber + 99; 
    int y = itemsNumber-1; 
    int ix = 0; 
    int iy = 0; 
    int skyX = 0; 
    int skyY = 0;
    //M controls the x axis (up and down); N controls the y axis (left and
    //right)
    int Mw = -49; 
    int Nw = 599; 
    int Mg = -299; 
    int Ng = -299;
    //ground sample
    CvMat* groundTrainingHue = cvCreateMat(threeArraySize,arraySize,CV_8UC1);
    CvMat* groundTrainingSat = cvCreateMat(threeArraySize,arraySize,CV_8UC1);
    CvMat* groundTrainingVal = cvCreateMat(threeArraySize,arraySize,CV_8UC1);
    //water sample
    CvMat* waterTrainingHue = cvCreateMat(threeArraySize,arraySize,CV_8UC1);
    CvMat* waterTrainingSat = cvCreateMat(threeArraySize,arraySize,CV_8UC1);
    CvMat* waterTrainingVal = cvCreateMat(threeArraySize,arraySize,CV_8UC1);
    //n x n sample patch taken from the picture
    CvMat* sampleHue = cvCreateMat(arraySize,arraySize,CV_8UC1);
    CvMat* sampleSat = cvCreateMat(arraySize,arraySize,CV_8UC1);
    CvMat* sampleVal = cvCreateMat(arraySize,arraySize,CV_8UC1);
    CvMat* resampleHue = cvCreateMat(arraySize,arraySize,CV_8UC1);
    CvMat* resampleSat = cvCreateMat(arraySize,arraySize,CV_8UC1);
    CvMat* resampleVal = cvCreateMat(arraySize,arraySize,CV_8UC1);
    //sky training sample
    CvMat* skyTrainingHue = cvCreateMat(arraySize,arraySize,CV_8UC1);
    CvMat* skyTrainingSat = cvCreateMat(arraySize,arraySize,CV_8UC1);
    CvMat* skyTrainingVal = cvCreateMat(arraySize,arraySize,CV_8UC1);
    //initialize each matrix element to zero for ease of use
    for(int i = 0; i < arraySize; i++)
    {
        for (int j=0; j<threeArraySize;j++)
        {
            groundTrainingHue[i][j] = 0;
            groundTrainingSat[i][j] = 0;
            groundTrainingVal[i][j] = 0;
    
            waterTrainingHue[i][j] = 0;
            waterTrainingSat[i][j] = 0;
            waterTrainingVal[i][j] = 0;
    /*
            sampleHue[i][j] = 0;
            sampleSat[i][j] = 0;
            sampleVal[i][j] = 0;
            resampleHue[i][j] = 0;
            resampleSat[i][j] = 0;
            resampleVal[i][j] = 0;
    
            skyTrainingHue[i][j] = 0;
            skyTrainingSat[i][j] = 0;
            skyTrainingVal[i][j] = 0;
     */
    }
    //Stores the votes for each channel (whether it belongs to water or not
    //1 is part of water, 0 not part of water
    //if sum of votes is bigger than 1/2 the number of elements, then it belongs to water
    int comparator [3];        //used when only three votes are needed
    int comparator2 [3][3];    //used when six votes are needed
    int votesSum = 0;
    //initial sum of votes is zero
    for(int i = 0; i < itemsNumber; i++)
    {   
        comparator[i] = 0;
        for(int j = 0; j < itemsNumber; i++)
        {
            comparator2[i][j] = 0;
        }
    }
    
    /***********************************************************************/
    //Convert from RGB to HSV to control the brightness of the objects.
    //work with reflexion
    //cvtColor(boatFront, boatFront, CV_RGB2HSV);
    /*Sky recognition. Might be useful for detecting reflexion on the water. If
      the sky is detected, and the reflection has the same characteristics of
      something below the horizon, that "something" might be water. Assume sky
      wont go below the horizon
    */
    //convert from RGB to HSV
    HsvImage I(boatFront);
    //split image into individual channels
    cvSplit(I, imageHue, imageSat, imageVal,0);
    for (int i=0; i<boatFront->height/2;i++)
    {
        for (int j=0; j<boatFront->width/3;j++)
        {
        //if something is bright enough, consider it sky and store the
        //value
            if (((imageVal[i][j] >= 0.9) && (imageSat[i][j] <= 0.8)) 
                && ((imageHue[i][j] >=170/255) || (imageHue[i][j] <= 215/255)))
            {
                //The HSV values vary between 0 and 1
                skyTrainingHue[skyX][skyY] = imageHue[i][j];
                skyTrainingSat[skyX][skyY] = imageSat[i][j];
                skyTrainingVal[skyX][skyY] = imageVal[i][j];
                imageHue[i][j] = 69/255;     //H (color)
                imageSat[i][j] = 0.3;          //S (color intensity)
                imageVal[i][j] = 0.6;          //V (brightness)
                if (skyY == itemsNumber)
                {
                   if (skyX == itemsNumber)
                     skyX = 1;
                   else
                     skyX = skyX + 1;
                   skyY = 1;
                }
                else
                  skyY = skyY + 1;
           }   
        }
    }
    
    /***********************************************************************/
    //Grab a random patch of water below the horizon and compare every other
    //pixel against it
    //The results of the water detection depend on where in the picture the
    //training samples are located. Maybe adding more training samples will
    //help improve this?

    //water patch sample (n X n matrix)
    for (int i=0;i<threeArraySize;i++)
    {
        for (int j=0;j<arraySize;j++)
        {
            waterTrainingHue[i][j] = imageHue[ceil(X/1.2866)+ceil(X/5.237)+i+Mw][ceil(Y/7.0755)+ceil(Y/21.01622)+j+Nw];
            waterTrainingSat[i][j] = imageSat[ceil(X/1.2866)+ceil(X/5.237)+i+Mw][ceil(Y/7.0755)+ceil(Y/21.01622)+j+Nw];
            waterTrainingVal[i][j] = imageVal[ceil(X/1.2866)+ceil(X/5.237)+i+Mw][ceil(Y/7.0755)+ceil(Y/21.01622)+i+Nw];        
        //patch is green (this is for me to know where the water patch sample is)
            imageHue[ceil(X/1.286624)+ceil(X/5.237)+i+Mw][ceil(Y/7.07552)+ceil(Y/21.01622)+j+Nw] = 85/255;
            imageSat[ceil(X/1.286624)+ceil(X/5.237)+i+Mw][ceil(Y/7.07552)+ceil(Y/21.01622)+j+Nw] = 255/255;
            imageVal[ceil(X/1.286624)+ceil(X/5.237)+i+Mw][ceil(Y/7.07552)+ceil(Y/21.01622)+j+Nw] = 128/255;

        //ground patch sample (n X n matrix)
        //Detecting the horizon in the picture might be an excellent visual aid to
        //choose where (above the horizon) you can take a ground training(1:3*n,1:n)g sample
        //from. The ground pixel sample can be at a constant distance from the
        //horizon
            groundTrainingHue[i][j] = imageHue[ceil(X/4.7291)+ceil(X/8.3176)+i+Mg][ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng];
            groundTrainingSat[i][j] = imageSat[ceil(X/4.7291)+ceil(X/8.3176)+i+Mg][ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng];
            groundTrainingVal[i][j] = imageVal[ceil(X/4.7291)+ceil(X/8.3176)+i+Mg][ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng];   
        //patch is red (this is for me to know where the ground patch sample is)
            imageHue[ceil(X/4.7291)+ceil(X/8.3176)+i+Mg][ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng] = 0;
            imageSat[ceil(X/4.7291)+ceil(X/8.3176)+i+Mg][ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng] = 1;
            imageVal[ceil(X/4.7291)+ceil(X/8.3176)+i+Mg][ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng] = 1;
        }
    }
    // Main loop. It traverses through the picture
    /**/
    skyX = 0; 
    skyY = 0;
    while (x < X-1)
    {
        //get a random sample
        //random sample taken from the picture. Must be determined whether
        //is is water or ground
        //Only using the Hue value from the picture
        //random sample taken from the picture. Must be determined whether
        //is water or ground
        for (int j1=0; j<itemsNumber;j++)
        {
            for (int j2=y-itemsNumber+iy-1; j2 < y; j2++)
            {
                sampleHue[ix][j1] = imageHue[x-itemsNumber+ix][j2];
                sampleSat[ix][j1] = imageSat[x-itemsNumber+ix][j2];
                sampleVal[ix][j1] = imageVal[x-itemsNumber+ix][j2];
            }
        }
        //Find the shortest distance between a pixel and the neighbors from each of
        //the training samples (sort of inefficient, but might do the job...sometimes)
        if (ix == itemsNumber)
        {
            //HSV for water sample
            [~, waterDistanceHue] = kNearestNeighbors(waterTrainingHue,sampleHue,n);
            [~, waterDistanceSat] = kNearestNeighbors(waterTrainingSat,sampleSat,n);
            [~, waterDistanceVal] = kNearestNeighbors(waterTrainingVal,sampleVal,n);
            //HSV for ground sample
            [~, groundDistanceHue] = kNearestNeighbors(groundTrainingHue,sampleHue,n);
            [~, groundDistanceSat] = kNearestNeighbors(groundTrainingSat,sampleSat,n);
            [~, groundDistanceVal] = kNearestNeighbors(groundTrainingVal,sampleVal,n);
            //HSV for sky sample
            if (skyTrainingHue(1,1)~=0 && skyTrainingSat(1,1)~=0 && skyTrainingVal(1,1)~=0)
            {
                [~, skyHue] = kNearestNeighbors(skyTrainingHue,sampleHue,n);
                [~, skySat] = kNearestNeighbors(skyTrainingSat,sampleSat,n);
                [~, skyVal] = kNearestNeighbors(skyTrainingVal,sampleVal,n);
            }
            
            //scan nearest neighbors to each pixel
            if ((skyTrainingHue[0][0]~=0) && (skyTrainingSat[1][1]~=0) && (skyTrainingVal[1][1]~=0))
            {
                for (int i =0; i<itemsNumber;i++)
                {
                    for (int j=0;j<itemsNumber;j++)
                    {
                        if (groundDistanceHue[i][j] > waterDistanceHue[i][j])
                        // mark water samples as green
                            comparator2[0][0] = 1;
                        else
                            comparator2[0][0] = 0;
                        if (((groundDistanceSat[i][j] > waterDistanceSat[i][j])) && (sampleSat[i][j] <= 0.8))
                            //mark water samples as green
                            comparator2[0][1] = 1;
                        else
                            comparator2[0][1] = 0;
                        if ((groundDistanceVal[i][j] < waterDistanceVal[i][j]) && (sampleVal[i][j] >= 0.9))
                        //mark water samples as green
                            comparator2[0][2] = 1;
                        else
                            comparator2[0][2] = 0;
                        // similar sky pixels on the water
                        if ((groundDistanceHue[i][j] > skyHue[i][j]) && (waterDistanceHue[i][j] > skyHue[i][j])) 
                        //mark water samples as green
                            comparator2[1][0] = 1;
                        else
                            comparator2(2,1) = 0;
                        if ((groundDistanceSat[i][j] > skyHue[i][j]) && (waterDistanceSat[i][j] < skySat[i][j]))
                        //mark water samples as green
                            comparator2[1][1] = 1;
                        else
                            comparator2[1][1] = 0;
                        if ((groundDistanceVal[i][j] > skyVal[i][j]) && (waterDistanceVal[i][j] < skyVal[i][j]))
                        //mark water samples as green
                            comparator2[1][2] = 1;
                        else
                            comparator2[1][2] = 0;
                        //count votes
                        for (int i=0;i<3;i++)
                        {
                            for (int j=0;j<3;j++)
                                votesSum = votesSum + comparator[i][j];
                        }
                        if ((votesSum > 3) && ((sampleSat[i][j] - sampleVal[i][j]) <= 0.1)) 
                        {
                        // classify pixel as water
                            resampleHue[skyX][skyY] = sampleHue[i][j];
                            resampleSat[skyX][skyY] = sampleSat[i][j];
                            resampleVal[skyX][skyY] = sampleVal[i][j];
                            if (skyY == itemsNumber-1)
                            {
                                if (skyX == itemsNumber-1)
                                    skyX = 1;
                                else
                                    skyX = skyX + 1;
                                skyY = 1;
                            }
                            else
                                skyY = skyY + 1; 
                            imageHue[x-itemsNumber+i-1][y-itemsNumber+j-1] = 85/255;
                            imageSat[x-itemsNumber+i-1][y-itemsNumber+j-1] = 1;
                            imageVal[x-itemsNumber+i-1][y-itemsNumber+j-1] = 128/255;
                        }
                        votesSum = 0;
                    }    
                }
            }
            else
            {
                for (int i=0;i<itemsNumber;i++)
                {
                    for (int j=0;j<itemsNumber;j++)
                    {
                        if (groundDistanceHue[i][j] > waterDistanceHue[i][j]) 
                        //mark water samples as green
                            comparator[0] = 1;
                        else
                            comparator[0] = 0;
                        if ((groundDistanceSat[i][j] > waterDistanceSat[i][j]) && (sampleSat[i][j] <= 0.8))
                        //mark water samples as green
                            comparator[1] = 1;
                        else
                            comparator[1] = 0;
                        if ((groundDistanceVal[i][j] < waterDistanceVal[i][j]) && (sampleVal[i][j] >= 0.9))
                        //mark water samples as green
                            comparator[2] = 1;
                        else
                            comparator[2] = 0;
                        //count votes
                        for (int i=0;i<3;i++)
                            votesSum = votesSum + comparator[i];
                        //sky detection 
                        if (votesSum > 1) //&& ((sampleSat[i][j] - sampleVal[i][j]) <= 0.1)
                        {
                        // classify pixel as water
                           //if resampleHue(n,n) == 0
                            resampleHue[skyX][skyY] = sampleHue[i][j];
                        //if resampleSat(n,n) == 0
                            resampleSat[skyX][skyY] = sampleSat[i][j];
                        //if resampleVal(n,n) == 0
                            resampleVal[skyX][skyY] = sampleVal[i][j];
                            if (skyY == itemsNumber-1)
                            {
                                if (skyX == itemsNumber-1)
                                    skyX = 1;
                                else
                                    skyX = skyX + 1;
                                skyY = 1;
                            }
                            else
                                skyY = skyY + 1; 
                            imageHue[x-itemsNumber+i][y-itemsNumber+j] = 85/255;
                            imageSat[x-itemsNumber+i][y-itemsNumber+j] = 1;
                            imageVal[x-itemsNumber+i][y-itemsNumber+j] = 128/255;
                        }
                        votesSum = 0;
                    }
                }
            }
        }
        // iteration goes from left-right, top-bottom
        if (ix < itemsNumber)
            ix = ix + 1;
        else
        {
            if (y < Y/3)
                y = y + itemsNumber-1;
            if (y > Y/3)
                y = Y/3;
            else if (y == Y/3)
            {
                x = x + itemsNumber-1;
                y = itemsNumber-1;
            }
            ix = 0;
        }
    }
    //%}
    /**/
    //use a set of data  that has been confirmed as water to resample everything
    //waterTrainingHue = resampleHue;
    //waterTrainingSat = resampleSat;
    //waterTrainingVal = resampleVal;
    x = X/2+itemsNumber+99; y = itemsNumber-1; ix = 0; iy = 0;
    while (x < X-1) 
    {
    //get a random sample
    //random sample taken from the picture. Must be determined whether
    // is is water or ground
    // Only using the Hue value from the picture
    // random sample taken from the picture. Must be determined whether
    // is water or ground
        for (int j1=0; j1<itemsNumber;j1++)
        {
            for (int j2=y-itemsNumber+iy-1; j2 < y; j2++)
            {
                sampleHue[ix][j1] = imageHue[x-itemsNumber+ix][j2];
                sampleSat[ix][j1] = imageSat[x-itemsNumber+ix][j2];
                sampleVal[ix][j1] = imageVal[x-itemsNumber+ix][j2];
            }
        }
    // Find the shortest distance between a pixel and the neighbors from each of
    //the training samples (sort of inefficient, but might do the job...sometimes)
        if (ix == itemsNumber-1)
        { 
        //HSV for water sample
            [~, waterDistanceHue] = kNearestNeighbors(waterTrainingHue,sampleHue,n);
            [~, waterDistanceSat] = kNearestNeighbors(waterTrainingSat,sampleSat,n);
            [~, waterDistanceVal] = kNearestNeighbors(waterTrainingVal,sampleVal,n);
        //HSV for ground sample
            [~, groundDistanceHue] = kNearestNeighbors(groundTrainingHue,sampleHue,n);
            [~, groundDistanceSat] = kNearestNeighbors(groundTrainingSat,sampleSat,n);
            [~, groundDistanceVal] = kNearestNeighbors(groundTrainingVal,sampleVal,n);
        //waterDistance
            for (int i=0;i<itemsNumber;i++)
            {
                for (int j=0;j<itemsNumber;j++)
                {
                // if sample has not been classified as water...
                    if ((sampleHue[i][j]~=85/255) && (sampleSat[i][j]~=1) && (sampleVal[i][j]~=128/255))
                    {
                        if (groundDistanceHue[i][j] > waterDistanceHue[i][j]) 
                        // mark water samples as green
                            comparator[0] = 1;
                        else
                            comparator[0] = 0;
                        if ((groundDistanceSat[i][j] > waterDistanceSat[i][j]) && (sampleSat[i][j] <= 0.8))
                        // mark water samples as green
                            comparator[1] = 1;
                        else
                            comparator[1] = 0;
                        if ((groundDistanceVal[i][j] < waterDistanceVal[i][j]) && (sampleVal[i][j] >= 0.9))
                        // mark water samples as green
                            comparator[2] = 1;
                        else
                            comparator[2] = 0;
                    //count votes
                        for (int i=0;i<3;i++)
                            votesSum = votesSum + comparator[i];
                        if ((votesSum > 1) %&& ((sampleSat[i][j] - sampleVal[i][j]) <= 0.1))
                        {
                            imageHue[x-itemsNumber+i][y-itemsNumber+j] = 85/255;
                            imageSat[x-itemsNumber+i][y-itemsNumber+j] = 1;
                            imageVal[x-itemsNumber+i][y-itemsNumber+j] = 128/255;
                        }
                    }
                }
            }
        }
    // iteration goes from left-right, top-bottom
        if (ix < itemsNumber-1)
            ix = ix + 1;
        else
        {
            if (y < Y/3-1)
            {
                y = y + itemsNumber-1;
                if (y > Y/3-1)
                y = Y/3-1;
            }
            else if (y == Y/3-1)
            {
                x = x + itemsNumber-1;
                y = itemsNumber-1;
            }
            ix = 0;
        }
    }
//%}
//cropped = hsv2rgb(cropped);
        
        
        
        
        
        
        
        
        
        
        
        
        
    }
    
    //cvSetImageROI(boatFront, cvRect(x,y,width,height));
    //cvAddS(boatFront, cvScalar(add),boatFront);
    cvNamedWindow( "Boat Front", 0); I(ceil(X/4.7291)+round(X/8.3176)+(1:3*n) + Mg,round(Y/7.78378)+round(Y/16.54468)+(1:n) + Ng,3);       //0 to maintains sizes regardless of image size
    cvResizeWindow("Boat Front",700,550); // new width/heigh in pixels
    //cvMoveWindow("Boat Front", 350, 150);
    //saturate_sv(boatFront);
    cvShowImage( "Boat Front", boatFront );
    //cvSaveImage("textFile.txt",boatFront);
    //cvRectangle(boatFront, cvPoint(5,10), cvPoint(20,30),cvScalar(255,255,255));
    //cout << "image height: " << boatFront->height << endl;
    //cout << "image width: " << boatFront->width << endl;
    //cout << "image depth: " << boatFront->depth << endl;
    //cout << "image channels: " << boatFront->nChannels << endl;
    //cout << "image size: " << boatFront->nSize << endl;
    //cout << "image buffer size: " << boatFront->width*boatFront->height*boatFront->nChannels << endl;
    //cvLine(boatFront, cvPoint(100,100), cvPoint(200,200), cvScalar(0,255,0), 1);
    
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

