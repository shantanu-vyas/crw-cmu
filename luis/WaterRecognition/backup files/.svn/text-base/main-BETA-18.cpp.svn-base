/* 
 * File:   main.cpp
 * Author: pototo
 *
 * Created on June 3, 2011, 10:28 AM
 * 
 * This program recognizes the water in front of the boat by using ground
 * plane detection techniques already available. This will look for the pixels 
 * from the boat, and go up the image until it finds the vector plane the repre-
 * sents the water
 */

#include <cstdlib>
#include <math.h>
#include <string.h>
#include <iostream>

#include "highgui.h"
#include "ml.h"
#include "cv.h"

using namespace std;

/*
 * 
 */

//access the elements of a picture
template<class T> class Image
{
  private:
      IplImage* imgp;
  public:
      Image(IplImage* img=0) {imgp=img;}
      ~Image(){imgp=0;}
      void operator=(IplImage* img) {imgp=img;}
      inline T* operator[](const int rowIndx) 
      {
          return ((T *)(imgp->imageData + rowIndx*imgp->widthStep));
      }
};
typedef struct
{
  unsigned char h,s,v;
} HsvPixel;
typedef struct
{
  float h,s,v;
} HsvPixelFloat;
typedef Image<HsvPixel>       HsvImage;
typedef Image<HsvPixelFloat>  HsvImageFloat;
typedef Image<unsigned char>  BwImage;
typedef Image<float>          BwImageFloat;

int main(int argc, char** argv) 
{
    /***********************************************************************/
    IplImage* boatFront = cvLoadImage("river2.jpg");
    //IplImage* hsvImage;
    boatFront->origin = IPL_ORIGIN_TL;   //sets image origin to top left corner
    
    //Crop the image to the ROI
    cvSetImageROI(boatFront, cvRect(0,0,boatFront->height/0.5,boatFront->width/1.83));
    int X = boatFront->height;
    int Y = boatFront->width;
    //split the HSV image into individual channels
    //IplImage* imageHue = cvCreateImage(cvSize(X/3,Y/3),IPL_DEPTH_8U,1);
    //IplImage* imageSat = cvCreateImage(cvSize(X/3,Y/3),IPL_DEPTH_8U,1);
    //IplImage* imageVal = cvCreateImage(cvSize(X/3,Y/3),IPL_DEPTH_8U,1);
    
    /***********************************************************************/
    int pixelsNumber = 30;  //number of pixels for an n x n matrix and # of neighbors
    const int arraySize = pixelsNumber;
    const int threeArraySize = pixelsNumber;
    //if n gets changed, then the algorithm might have to be
    //recalibrated. Try to keep it constant
    //these variables are used for the k nearest neighbors
    //int accuracy;
    //reponses for each of the classifications
    float responseWaterH, responseWaterS, responseWaterV; 
    float responseGroundH, responseGroundS, responseGroundV;
    float responseSkyH, responseSkyS, responseSkyV;
    //float trainingSampleCount = arraySize;
    int train_sample_count = 100;
    CvRNG rng_state = cvRNG(-1);
    float _sample[arraySize];
    CvMat* trainClasses = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    //CvMat sample = cvMat( 1, 2, CV_32FC1, _sample );
    //used with the classifier 
    CvMat* nearestWaterH = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestWaterS = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestWaterV = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestGroundH = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestGroundS = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestGroundV = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestSkyH = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestSkyS = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* nearestSkyV = cvCreateMat(1, pixelsNumber, CV_32FC1);
    //these variables are use to travers the picture by blocks of n x n pixels at
    //a time. 
    //Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
    //right way, of course)
    //x and y are the dimensions of the local patch of pixels
    int x = (boatFront->height)/2 + pixelsNumber + 99; 
    int y = pixelsNumber-1; 
    int ix = 0; 
    int iy = 0; 
    int skyX = 0; 
    int skyY = 0;
    //M controls the x axis (up and down); N controls the y axis (left and
    //right)
    int Mw = -600; 
    int Nw = 700; 
    int Mg = -199; 
    int Ng = 700;
    int row1 = 0;
    int column1 = 0;
    int row2 = 0;
    int column2 = 0;
    //ground sample
    CvMat* groundTrainingHue = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* groundTrainingSat = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* groundTrainingVal = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    //water sample
    CvMat* waterTrainingHue = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* waterTrainingSat = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* waterTrainingVal = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    //n x n sample patch taken from the picture
    CvMat* sampleHue = cvCreateMat(1,arraySize,CV_32FC1);
    CvMat* sampleSat = cvCreateMat(1,arraySize,CV_32FC1);
    CvMat* sampleVal = cvCreateMat(1,arraySize,CV_32FC1);
    CvMat* resampleHue = cvCreateMat(arraySize,arraySize,CV_32FC1);
    CvMat* resampleSat = cvCreateMat(arraySize,arraySize,CV_32FC1);
    CvMat* resampleVal = cvCreateMat(arraySize,arraySize,CV_32FC1);
    //sky training sample
    CvMat* skyTrainingHue = cvCreateMat(arraySize,arraySize,CV_32FC1);
    CvMat* skyTrainingSat = cvCreateMat(arraySize,arraySize,CV_32FC1);
    CvMat* skyTrainingVal = cvCreateMat(arraySize,arraySize,CV_32FC1);
    //initialize each matrix element to zero for ease of use
    cvZero(groundTrainingHue);
    cvZero(groundTrainingSat);
    cvZero(groundTrainingVal);
    cvZero(waterTrainingHue);
    cvZero(waterTrainingSat);
    cvZero(waterTrainingVal);
    cvZero(sampleHue);
    cvZero(sampleSat);
    cvZero(sampleVal);
    cvZero(resampleHue);
    cvZero(resampleSat);
    cvZero(resampleVal);
    cvZero(skyTrainingHue);
    cvZero(skyTrainingSat);
    cvZero(skyTrainingVal);
    /*CvKNearest knnWaterHue(waterTrainingHue, sampleHue, 0, false, pixelsNumber);
    CvKNearest knnWaterSat(waterTrainingSat, sampleSat, 0, false, pixelsNumber);
    CvKNearest knnWaterVal(waterTrainingVal, sampleVal, 0, false, pixelsNumber);
            //HSV for ground sample
    CvKNearest knnGroundHue(groundTrainingHue, sampleHue, 0, false, pixelsNumber);
    CvKNearest knnGroundSat(groundTrainingSat, sampleSat, 0, false, pixelsNumber);
    CvKNearest knnGroundVal(groundTrainingVal, sampleVal, 0, false, pixelsNumber);
            //HSV for sky sample
    CvKNearest knnSkyHue(skyTrainingHue, sampleHue, 0, false, pixelsNumber);
    CvKNearest knnSkySat(skyTrainingSat, sampleSat, 0, false, pixelsNumber);
    CvKNearest knnSkyVal(skyTrainingVal, sampleVal, 0, false, pixelsNumber); 
     */    
    //Stores the votes for each channel (whether it belongs to water or not
    //1 is part of water, 0 not part of water
    //if sum of votes is bigger than 1/2 the number of elements, then it belongs to water
    int votesSum = 0;
    int comparator[3];        //used when only three votes are needed
    int comparatorTwo [3][3];    //used when six votes are needed
    //initial sum of votes is zero
    //Error if initialize both matrices inside a single for loop. Dont know why
    for(int i = 0; i < 3; i++)
    {   
        //comparator[i] = 0;
        for(int j = 0; j < 3; j++)
        {
            comparatorTwo[i][j] = 0;
        }
    }
    for(int i = 0; i < 3; i++)
    {   
        comparator[i] = 0;
    }
 
    /***********************************************************************/
    //Convert from RGB to HSV to control the brightness of the objects.
    //work with reflexion
    /*Sky recognition. Might be useful for detecting reflexion on the water. If
      the sky is detected, and the reflection has the same characteristics of
      something below the horizon, that "something" might be water. Assume sky
      wont go below the horizon
    */
    //convert from RGB to HSV
    cvCvtColor(boatFront, boatFront, CV_RGB2HSV);
    HsvImage I(boatFront);
    //split image into individual channels
    //cvSplit(I, imageHue, imageSat, imageVal,0);
    for (int i=0; i<boatFront->height/3;i++)
    {
        for (int j=0; j<boatFront->width;j++)
        {
        //if something is bright enough, consider it sky and store the
        //value. HSV values go from 0 to 180 ... RGB goes from 0 to 255
            if (((I[i][j].v >= 252))) //&& (I[i][j].s <= 50)) 
                //&& ((I[i][j].h >=126) && (I[i][j].h <= 144)))
            {
                //The HSV values vary between 0 and 1
                cvmSet(skyTrainingHue,skyX,skyY,I[i][j].h);
                cvmSet(skyTrainingSat,skyX,skyY,I[i][j].s);
                cvmSet(skyTrainingVal,skyX,skyY,I[i][j].v);
                I[i][j].h = 0.3*180;       //H (color)
                I[i][j].s = 0.3*180;          //S (color intensity)
                I[i][j].v = 0.6*180;          //V (brightness)
                if (skyY == pixelsNumber-1)
                {
                   if (skyX == pixelsNumber-1)
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
    for (int i=0; i < threeArraySize; i++)
    {
        for (int j=0; j < arraySize; j++)
        {
            row1 = ceil(X/1.2866)+ceil(X/5.237)+i+Mw;
            column1 = ceil(Y/7.0755)+ceil(Y/21.01622)+j+Nw;
            row2 = ceil(X/4.7291)+ceil(X/8.3176)+i+Mg;
            column2 = ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng;
            
            cvmSet(waterTrainingHue,i,j,I[row1][column1].h);
            cvmSet(waterTrainingSat,i,j,I[row1][column1].s);
            cvmSet(waterTrainingVal,i,j,I[row1][column1].v);        
        //patch is green (this is for me to know where the water patch sample is)
            I[row1][column1].h = 120;
            I[row1][column1].s = 255;
            I[row1][column1].v = 255;

        //ground patch sample (n X n matrix)
        //Detecting the horizon in the picture might be an excellent visual aid to
        //choose where (above the horizon) you can take a ground training(1:3*n,1:n)g sample
        //from. The ground pixel sample can be at a constant distance from the
        //horizon
            //row2 = ceil(X/4.7291)+ceil(X/8.3176)+i+Mg;
            //column2 = ceil(Y/7.78378)+ceil(Y/16.54468)+j+Ng;
            cvmSet(groundTrainingHue,i,j,I[row2][column2].h);
            cvmSet(groundTrainingSat,i,j,I[row2][column2].s);
            cvmSet(groundTrainingVal,i,j,I[row2][column2].v);   
        //patch is red (this is for me to know where the ground patch sample is)
            I[row2][column2].h = 180;
            I[row2][column2].s = 255;
            I[row2][column2].v = 255;
        }
    }
    
    // Main loop. It traverses through the picture
    
    skyX = 0; 
    skyY = 0;
    /*
    while (x < X)
    {
        //get a random sample
        //random sample taken from the picture. Must be determined whether
        //is is water or ground
        //Only using the Hue value from the picture
        //for (int j1=0; j1<pixelsNumber;j1++)
        //{
            for (int j2=y-pixelsNumber+iy-1; j2 < y; j2++)
            {
                sampleHue->data.fl[ix] = (float)I[x-pixelsNumber+ix][j2].h;
                sampleSat->data.fl[ix] = (float)I[x-pixelsNumber+ix][j2].s;
                sampleVal->data.fl[ix] = (float)I[x-pixelsNumber+ix][j2].v;
                //cvmSet(sampleHue,ix,0,(float)I[x-pixelsNumber+ix][j2].h);
                //cvmSet(sampleSat,ix,0,(float)I[x-pixelsNumber+ix][j2].s);
                //cvmSet(sampleVal,ix,0,(float)I[x-pixelsNumber+ix][j2].v);
            }
        //}
        //Find the shortest distance between a pixel and the neighbors from each of
        //the training samples (sort of inefficient, but might do the job...sometimes)
        if (ix == pixelsNumber-1)
        {
            //HSV for water sample
            // learn classifier
            //CvKNearest knn(trainData, trainClasses, 0, false, itemsNumber);
            CvKNearest knnWaterHue(waterTrainingHue, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnWaterSat(waterTrainingSat, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnWaterVal(waterTrainingVal, trainClasses, 0, false, pixelsNumber);
            //HSV for ground sample
            CvKNearest knnGroundHue(groundTrainingHue, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnGroundSat(groundTrainingSat, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnGroundVal(groundTrainingVal, trainClasses, 0, false, pixelsNumber);
            //HSV for sky sample
            if (cvmGet(skyTrainingHue,0,0)!=0.0 && cvmGet(skyTrainingSat,0,0)!=0.0 && cvmGet(skyTrainingVal,0,0)!=0.0)
            {
                CvKNearest knnSkyHue(skyTrainingHue, trainClasses, 0, false, pixelsNumber);
                CvKNearest knnSkySat(skyTrainingSat, trainClasses, 0, false, pixelsNumber);
                CvKNearest knnSkyVal(skyTrainingVal, trainClasses, 0, false, pixelsNumber);
            }
            
            //scan nearest neighbors to each pixel
            responseWaterH = knnWaterHue.find_nearest(sampleHue,pixelsNumber,0,0,nearestWaterH,0);
            responseWaterS = knnWaterSat.find_nearest(sampleSat,pixelsNumber,0,0,nearestWaterS,0);
            responseWaterV = knnWaterVal.find_nearest(sampleVal,pixelsNumber,0,0,nearestWaterV,0);
            responseGroundH = knnGroundHue.find_nearest(sampleHue,pixelsNumber,0,0,nearestGroundH,0);
            responseGroundS = knnGroundSat.find_nearest(sampleSat,pixelsNumber,0,0,nearestGroundS,0);
            responseGroundV = knnGroundVal.find_nearest(sampleVal,pixelsNumber,0,0,nearestGroundV,0);
            if ((cvmGet(skyTrainingHue,0,0)!=0) && (cvmGet(skyTrainingSat,0,0)!=0) && (cvmGet(skyTrainingVal,0,0)!=0))
            {
                responseSkyH = knnGroundHue.find_nearest(sampleHue,pixelsNumber,0,0,nearestGroundH,0);
                responseSkyS = knnGroundSat.find_nearest(sampleSat,pixelsNumber,0,0,nearestGroundS,0);
                responseSkyV = knnGroundVal.find_nearest(sampleVal,pixelsNumber,0,0,nearestGroundV,0);
                for (int i=0; i < pixelsNumber;i++)
                {
                    for (int j=0;j<pixelsNumber;j++)
                    {
                        if (abs(nearestGroundH->data.fl[j]-responseGroundH) > abs(nearestWaterH->data.fl[j]-responseWaterH))
                        // mark water samples as green
                            comparatorTwo[0][0] = 1;
                        else
                            comparatorTwo[0][0] = 0;
                        if ((abs(nearestGroundS->data.fl[j]-responseGroundS) > abs(nearestWaterS->data.fl[j]-responseWaterS)) && (cvmGet(sampleSat,0,1) <= 0.8))
                            //mark water samples as green
                            comparatorTwo[0][1] = 1;
                        else
                            comparatorTwo[0][1] = 0;
                        if ((abs(nearestGroundV->data.fl[j]-responseGroundV) < abs(nearestWaterV->data.fl[j]-responseWaterV)) && (cvmGet(sampleVal,0,i) >= 0.9))
                        //mark water samples as green
                            comparatorTwo[0][2] = 1;
                        else
                            comparatorTwo[0][2] = 0;
                        // similar sky pixels on the water
                        if ((abs(nearestGroundH->data.fl[j]-responseGroundH) > abs(nearestSkyH->data.fl[j]-responseSkyH)) && (abs(nearestWaterH->data.fl[j]-responseWaterH) > abs(nearestSkyH->data.fl[j]-responseSkyH))) 
                        //mark water samples as green
                            comparatorTwo[1][0] = 1;
                        else
                            comparatorTwo[1][0] = 0;
                        if ((abs(nearestGroundS->data.fl[j]-responseGroundS) > abs(nearestSkyS->data.fl[j]-responseSkyS)) && (abs(nearestWaterS->data.fl[j]-responseWaterS) < abs(nearestSkyS->data.fl[j]-responseSkyS)))
                        //mark water samples as green
                            comparatorTwo[1][1] = 1;
                        else
                            comparatorTwo[1][1] = 0;
                        if ((abs(nearestGroundV->data.fl[j]-responseGroundV) > abs(nearestSkyV->data.fl[j]-responseSkyV)) && (abs(nearestWaterS->data.fl[j]-responseWaterS) < abs(nearestSkyS->data.fl[j]-responseSkyS)))
                        //mark water samples as green
                            comparatorTwo[1][2] = 1;
                        else
                            comparatorTwo[1][2] = 0;
                        //count votes
                        for (int i2=0;i2<3;i2++)
                        {
                            for (int j2=0;j2<3;j2++)
                                votesSum = votesSum + comparatorTwo[i2][j2];
                        }
                
                        if ((votesSum > 3) && ((cvmGet(sampleSat,0,i) - cvmGet(sampleVal,0,i)) <= 0.1)) 
                        {
                            // classify pixel as water
                            cvmSet(resampleHue,skyX,skyY,cvmGet(sampleHue,0,i));
                            cvmSet(resampleSat,skyX,skyY,cvmGet(sampleSat,0,i));
                            cvmSet(resampleVal,skyX,skyY,cvmGet(sampleVal,0,i));
                            if (skyY == pixelsNumber-1)
                            {
                                if (skyX == pixelsNumber-1)
                                    skyX = 1;
                                else
                                    skyX = skyX + 1;
                                skyY = 1;
                            }
                            else
                                skyY = skyY + 1; 
                            I[x-pixelsNumber+i-1][y-pixelsNumber+j-1].h = 85/255;
                            I[x-pixelsNumber+i-1][y-pixelsNumber+j-1].s = 1;
                            I[x-pixelsNumber+i-1][y-pixelsNumber+j-1].v = 128/255;
                        }
                        votesSum = 0;
                    }    
                }
            }
            else
            {
                for (int i=0;i<pixelsNumber;i++)
                {
                    for (int j=0;j<pixelsNumber;j++)
                    {
                        if (abs(nearestGroundH->data.fl[j]-responseGroundH) > abs(nearestWaterH->data.fl[j]-responseWaterH)) 
                        //mark water samples as green
                            comparator[0] = 1;
                        else
                            comparator[0] = 0;
                        if ((abs(nearestGroundS->data.fl[j]-responseGroundS) > abs(nearestWaterS->data.fl[j]-responseWaterS)) && (cvmGet(sampleSat,0,i) <= 0.8))
                        //mark water samples as green
                            comparator[1] = 1;
                        else
                            comparator[1] = 0;
                        if ((abs(nearestGroundV->data.fl[j]-responseGroundV) < abs(nearestWaterV->data.fl[j]-responseWaterV)) && (cvmGet(sampleVal,0,i) >= 0.9))
                        //mark water samples as green
                            comparator[2] = 1;
                        else
                            comparator[2] = 0;
                        //count votes
                        for (int i3=0;i3<3;i3++)
                            votesSum = votesSum + comparator[i3];
                        //sky detection 
                        if (votesSum > 1) //&& ((sampleSat[i][j] - sampleVal[i][j]) <= 0.1)
                        {
                        // classify pixel as water
                           //if resampleHue(n,n) == 0
                            cvmSet(resampleHue,skyX,skyY,cvmGet(sampleHue,0,i));
                        //if resampleSat(n,n) == 0
                            cvmSet(resampleSat,skyX,skyY,cvmGet(sampleSat,0,i));
                        //if resampleVal(n,n) == 0
                            cvmSet(resampleVal,skyX,skyY,cvmGet(sampleVal,0,i));
                            if (skyY == pixelsNumber-1)
                            {
                                if (skyX == pixelsNumber-1)
                                    skyX = 1;
                                else
                                    skyX = skyX + 1;
                                skyY = 1;
                            }
                            else
                                skyY = skyY + 1; 
                            I[x-pixelsNumber+i][y-pixelsNumber+j].h = 85/255;
                            I[x-pixelsNumber+i][y-pixelsNumber+j].s = 1;
                            I[x-pixelsNumber+i][y-pixelsNumber+j].v = 128/255;
                        }
                        votesSum = 0;
                    }
                }
            }
        }
        // iteration goes from left-right, top-bottom
        if (ix < pixelsNumber)
            ix = ix + 1;
        else
        {
            if (y < (Y/3)-1)
                y = y + pixelsNumber-1;
            if (y > (Y/3)-1)
                y = (Y/3)-1;
            else if (y == (Y/3)-1)
            {
                x = x + pixelsNumber-1;
                y = pixelsNumber-1;
            }
            ix = 0;
        }
    }
    
    //use a set of data  that has been confirmed as water to resample everything
    //I am confuse about how to set the training sample to the new sample. Maybe
    // by using a for loop?
    int elementAverageHue = 0;    //gets the  average of the elements of resample
    int elementAverageSat = 0;    //gets the  average of the elements of resample
    int elementAverageVal = 0;    //gets the  average of the elements of resample
    // Not sure if taking the average for each water channel is a good idea
    // Is there anything better to do about this?
    for (int i=0; i<arraySize; i++)
    {
        for (int j=0; j<arraySize; j++)
        {
            elementAverageHue = elementAverageHue + cvmGet(resampleHue,i,j);
            elementAverageSat = elementAverageSat + cvmGet(resampleSat,i,j);
            elementAverageVal = elementAverageVal + cvmGet(resampleVal,i,j);
        }
    }
    elementAverageHue = ceil(elementAverageHue / arraySize);
    elementAverageSat = ceil(elementAverageSat / arraySize);
    elementAverageVal = ceil(elementAverageVal / arraySize);
    for (int i=0; i<threeArraySize; i++)
    {
        for (int j=0; j<arraySize; j++)
        {
            cvmSet(waterTrainingHue,i,j,elementAverageHue);
            cvmSet(waterTrainingSat,i,j,elementAverageSat);
            cvmSet(waterTrainingVal,i,j,elementAverageVal);
        }
    }
    //waterTrainingHue = resampleHue;
    //waterTrainingSat = resampleSat;
    //waterTrainingVal = resampleVal;
    x = X/2+pixelsNumber+99; y = pixelsNumber-1; ix = 0; iy = 0;
    //resample using the known water values
    */
    /*
    while (x < X) 
    {
    //get a random sample
    //random sample taken from the picture. Must be determined whether
    // it is water or not
        //for (int j1=0; j1<pixelsNumber;j1++)
        //{
            for (int j2=y-pixelsNumber+iy-1; j2 < y; j2++)
            {
                sampleHue->data.fl[ix] = (float)I[x-pixelsNumber+ix][j2].h;
                sampleSat->data.fl[ix] = (float)I[x-pixelsNumber+ix][j2].s;
                sampleVal->data.fl[ix] = (float)I[x-pixelsNumber+ix][j2].v;
                
                //cvmSet(sampleHue,ix,0,I[x-pixelsNumber+ix][j2].h);
                //cvmSet(sampleSat,ix,0,I[x-pixelsNumber+ix][j2].s);
                //cvmSet(sampleVal,ix,0,I[x-pixelsNumber+ix][j2].v);
            }
        //}
    // Find the shortest distance between a pixel and the neighbors from each of
    //the training samples (sort of inefficient, but might do the job...sometimes)
        if (ix == pixelsNumber-1)
        { 
            //HSV for water sample
            // learn classifier
            //CvKNearest knn(trainData, trainClasses, 0, false, itemsNumber);
            CvKNearest knnWaterHue(waterTrainingHue, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnWaterSat(waterTrainingSat, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnWaterVal(waterTrainingVal, trainClasses, 0, false, pixelsNumber);
            //HSV for ground sample
            CvKNearest knnGroundHue(groundTrainingHue, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnGroundSat(groundTrainingSat, trainClasses, 0, false, pixelsNumber);
            CvKNearest knnGroundVal(groundTrainingVal, trainClasses, 0, false, pixelsNumber);
            //scan nearest neighbors to each pixel
            responseWaterH = knnWaterHue.find_nearest(sampleHue,pixelsNumber,0,0,nearestWaterH,0);
            responseWaterS = knnWaterSat.find_nearest(sampleSat,pixelsNumber,0,0,nearestWaterS,0);
            responseWaterV = knnWaterVal.find_nearest(sampleVal,pixelsNumber,0,0,nearestWaterV,0);
            responseGroundH = knnGroundHue.find_nearest(sampleHue,pixelsNumber,0,0,nearestGroundH,0);
            responseGroundS = knnGroundSat.find_nearest(sampleSat,pixelsNumber,0,0,nearestGroundS,0);
            responseGroundV = knnGroundVal.find_nearest(sampleVal,pixelsNumber,0,0,nearestGroundV,0);
            for (int i=0;i<pixelsNumber;i++)
            {
                for (int j=0;j<pixelsNumber;j++)
                {
                // if sample has not been classified as water...
                    if ((cvmGet(sampleHue,i,0)!=85/255) && (cvmGet(sampleSat,i,0)!=1) && (cvmGet(sampleVal,i,0)!=128/255))
                    {
                        if (abs(nearestGroundH->data.fl[j]-responseGroundH) > abs(nearestWaterH->data.fl[j]-responseWaterH)) 
                        //mark water samples as green
                            comparator[0] = 1;
                        else
                            comparator[0] = 0;
                        if ((abs(nearestGroundS->data.fl[j]-responseGroundS) > abs(nearestWaterS->data.fl[j]-responseWaterS)) && (cvmGet(sampleSat,i,0) <= 0.8))
                        //mark water samples as green
                            comparator[1] = 1;
                        else
                            comparator[1] = 0;
                        if ((abs(nearestGroundV->data.fl[j]-responseGroundV) < abs(nearestWaterV->data.fl[j]-responseWaterV)) && (cvmGet(sampleVal,i,0) >= 0.9))
                        //mark water samples as green
                            comparator[2] = 1;
                        else
                            comparator[2] = 0;
                    //count votes
                        for (int i4=0;i4<3;i4++)
                            votesSum = votesSum + comparator[i4];
                        if ((votesSum > 1)) //&& ((sampleSat[i][j] - sampleVal[i][j]) <= 0.1))
                        {
                            I[x-pixelsNumber+i][y-pixelsNumber+j].h = 85/255;
                            I[x-pixelsNumber+i][y-pixelsNumber+j].s = 1;
                            I[x-pixelsNumber+i][y-pixelsNumber+j].v = 128/255;
                        }
                    }
                }
            }
        }
    // iteration goes from left-right, top-bottom
        if (ix < pixelsNumber-1)
            ix = ix + 1;
        else
        {
            if (y < (Y/3)-1)
            {
                y = y + pixelsNumber-1;
                if (y > (Y/3)-1)
                y = (Y/3)-1;
            }
            else if (y == (Y/3)-1)
            {
                x = x + pixelsNumber-1;
                y = pixelsNumber-1;
            }
            ix = 0;
        }
    }
     */
//cropped = hsv2rgb(cropped);
    //convert from HSV to RGB
    cvCvtColor(boatFront, boatFront, CV_HSV2RGB);    
    cvNamedWindow( "Boat Front", 0);      //0 to maintains sizes regardless of image size
    cvResizeWindow("Boat Front",900,750); // new width/heigh in pixels
    //cvMoveWindow("Boat Front", 350, 150);
    cvShowImage( "Boat Front", boatFront );
    
    cvWaitKey(0);
    cvResetImageROI(boatFront);
    cvReleaseImage(&boatFront);
    cvDestroyWindow("Boat Front");
    
    return 0; 
}