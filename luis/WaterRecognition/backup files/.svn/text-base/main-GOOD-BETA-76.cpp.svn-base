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
 * 
 * Resolution is a big factor in the way the algorithm works. If the resolution
 * is changed, the algorithm might get messed up and will probably have to be
 * recalibrated to work with the new image resolution.
 * The current image is 640x480
 */

#include <iostream>
#include <iomanip>

#include <cstdlib>
#include <math.h>
#include <string.h>
#include <iostream>

#include "highgui.h"
#include "ml.h"
#include "cv.h"

// Main blob library include
//#include "cvblobs/BlobResult.h"
#include <opencv/cvblob.h>

#include "KNN.h"

using namespace std;
using namespace cv;
//cvBlob
using namespace cvb;

/*
 * 
 */

#define PI 3.14159265

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

int bwareaopen_(IplImage *image, int size)
{
    /* OpenCV equivalent of Matlab's bwareaopen.
    image must be 8 bits, 1 channel, black and white
    (objects) with values 0 and 255 respectively */

    CvMemStorage *storage;
    CvSeq *contour = NULL;
    CvScalar white, black;
    IplImage *input = NULL; // cvFindContours changes the input
    double area;
    int foundCountours = 0;

    black = CV_RGB( 0, 0, 0 );
    white = CV_RGB( 255, 255, 255 );

    if(image == NULL || size == 0)
        return(foundCountours);

    input = cvCloneImage(image);

    storage = cvCreateMemStorage(0); // pl.Ensure you will have enough
    //room here.

    cvFindContours(input, storage, &contour, sizeof (CvContour),
    CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));

    while(contour)
    {
        area = cvContourArea(contour, CV_WHOLE_SEQ );
        if( -size <= area && area <= 0)
        { // removes white dots
            cvDrawContours( image, contour, black, black, -1, CV_FILLED, 8 );
        }
        else
        {
            if( 0 < area && area <= size) // fills in black holes
                cvDrawContours( image, contour, white, white, -1, CV_FILLED, 8 );       
        }
        contour = contour->h_next;
    }
    
    cvNamedWindow( "contour", 0);      //0 to maintains sizes regardless of image size
    cvResizeWindow("contour",900,750); // new width/heigh in pixels
    cvShowImage( "contour", image );
    cvWaitKey(0);
    //cvResetImageROI(boatFront);
    cvDestroyWindow("contour");
    cvReleaseMemStorage( &storage ); // desallocate CvSeq as well.
    cvReleaseImage(&input);
    return(foundCountours);
}

int main(int argc, char** argv) 
{
    /***********************************************************************/
    //live image coming streamed straight from the boat's camera
    IplImage* boatFront = cvLoadImage("20110806_082209.jpg");
    //The boat takes flipped images, so you need to flip them back to normal
    //cvFlip(boatFront, boatFront, 0);
    IplImage* backUpImage = cvCloneImage(boatFront);
    boatFront->origin = IPL_ORIGIN_TL;   //sets image origin to top left corner
    int X = boatFront->height;
    int Y = boatFront->width;
    cout << "height " << X << endl;
    cout << "width " << Y << endl;    
    
    /*********************Image Filtering variables****************************/
    //these images are used for segmenting objects from the overall background 
    //create a one channel image to convert from RGB to GRAY
    IplImage* grayImage = cvCreateImage(cvGetSize(boatFront),IPL_DEPTH_8U,1);
    //convert grayImage to binary (final step after converting from GRAY)
    IplImage* bwImage = cvCreateImage(cvGetSize(grayImage),IPL_DEPTH_8U,1);
    //variables used for the flood fill segmentation
    CvPoint seed_point = cvPoint(boatFront->height/1.45,0);       //not sure how this variable works
    CvScalar color = CV_RGB(250,0,0);
    CvMemStorage* grayStorage = NULL;     //memory storage for contour sequence
    CvSeq* contours = 0;
    // get blobs and filter them using its area
    IplConvKernel* morphKernel = cvCreateStructuringElementEx(5, 5, 1, 1, CV_SHAPE_RECT, NULL);
    CvBlobs blobs;
    CvBlobs *currentBlob;
    IplImage *original, *originalThr;
    IplImage *segmentated = cvCreateImage(cvGetSize(boatFront), 8, 1);
    unsigned int blobNumber = 0;
    IplImage *labelImg = cvCreateImage(cvGetSize(boatFront), IPL_DEPTH_LABEL, 1);

    /***********************************************************************/
    //boat's edge distance from the camera. This is used for visual calibration
    //to know the distance from the boat to the nearest obstacles.
    //With respect to the mounted camera, distance is 21 inches (0.5334 m) side to side
    //and 15 inches (0.381 m).
    //float boatFrontDistance = 0.381;    //distance in meters
    //float boatSideDistance = 0.5334;    //distance in meters
    
    // These variables tell the distance from the center bottom of the image
    // (the camera) to the square surrounding a the obstacle
    float xObstacleDistance = 0.0;
    float yObstacleDistance = 0.0;
    float obstacleDistance = 0.0;
    float obstacleHeading = 0.0;
    
    int pixelsNumber = 50;  //number of pixels for an n x n matrix and # of neighbors
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
    float averageHue = 0.0;
    float averageSat = 0.0;
    float averageVal = 0.0;
    CvMat* trainClassesH = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    CvMat* trainClassesS = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    CvMat* trainClassesV = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    CvMat* trainClasses2 = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
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
    //Distance
    CvMat* distanceWaterH = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceWaterS = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceWaterV = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceGroundH = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceGroundS = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceGroundV = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceSkyH = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceSkyS = cvCreateMat(1, pixelsNumber, CV_32FC1);
    CvMat* distanceSkyV = cvCreateMat(1, pixelsNumber, CV_32FC1);
    //these variables are use to traverse the picture by blocks of n x n pixels at
    //a time. 
    //Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
    //right way, of course)
    //x and y are the dimensions of the local patch of pixels
    int x = (boatFront->height)/1.45;//(boatFront->height)/2.5 + 105; 
    int y = 0;  
    int skyX = 0; 
    int skyY = 0;
    int row1 = 0;
    int column1 = 0;
    //these two variables are used in order to divide the grid in the
    //resample segmentation part
    int xDivisor = 200;
    int yDivisor = 200;
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
    CvMat* resampleHue = cvCreateMat(boatFront->height/xDivisor,boatFront->width/yDivisor,CV_32FC1);
    CvMat* resampleSat = cvCreateMat(boatFront->height/xDivisor,boatFront->width/yDivisor,CV_32FC1);
    CvMat* resampleVal = cvCreateMat(boatFront->height/xDivisor,boatFront->width/yDivisor,CV_32FC1);
    int xDiv = 20;
    int yDiv = 20;
    CvMat* resampleHue2 = cvCreateMat(boatFront->height/xDiv,boatFront->width/yDiv,CV_32FC1);
    CvMat* resampleSat2 = cvCreateMat(boatFront->height/xDiv,boatFront->width/yDiv,CV_32FC1);
    CvMat* resampleVal2 = cvCreateMat(boatFront->height/xDiv,boatFront->width/yDiv,CV_32FC1);
    //sky training sample
    CvMat* skyTrainingHue = cvCreateMat(arraySize,arraySize,CV_32FC1);
    CvMat* skyTrainingSat = cvCreateMat(arraySize,arraySize,CV_32FC1);
    CvMat* skyTrainingVal = cvCreateMat(arraySize,arraySize,CV_32FC1);
    //initialize each matrix element to zero for ease of use
    //cvZero(groundTrainingHue);
    //cvZero(groundTrainingSat);
    //cvZero(groundTrainingVal);
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
    //Stores the votes for each channel (whether it belongs to water or not
    //1 is part of water, 0 not part of water
    //if sum of votes is bigger than 1/2 the number of elements, then it belongs to water
    int votesSum = 0;
    int comparator[3];        //used when only three votes are needed
    //int comparatorTwo [3][3];    //used when six votes are needed
    //initial sum of votes is zero
    //Error if initialize both matrices inside a single for loop. Dont know why
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
    cvCvtColor(boatFront, boatFront, CV_BGR2HSV);
    cvCvtColor(backUpImage, backUpImage, CV_BGR2HSV);
    HsvImage I(boatFront);
    HsvImage IBackUp(backUpImage);
    //Sky detection
   for (int i=0; i<boatFront->height;i++)
    {
        for (int j=0; j<boatFront->width;j++)
        {
        //if something is bright enough, consider it sky and store the
        //value. HSV values go from 0 to 180 ... RGB goes from 0 to 255
            if (((I[i][j].v >= 180) && (I[i][j].s <= 16)))
                // && ((I[i][j].h >=10)))) //&& (I[i][j].h <= 144))))
            {
                //The HSV values vary between 0 and 1
                cvmSet(skyTrainingHue,skyX,skyY,I[i][j].h);
                cvmSet(skyTrainingSat,skyX,skyY,I[i][j].s);
                cvmSet(skyTrainingVal,skyX,skyY,I[i][j].v);
                //I[i][j].h = 0.3*180;       //H (color)
                //I[i][j].s = 0.3*180;          //S (color intensity)
                //I[i][j].v = 0.6*180;          //V (brightness)
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
    //offline input pictures. Samples of water properties are taken from these 
    //pictures to get a range of values for H, S, V that will be stored into a 
    //pre-defined classifier
    IplImage* imageSample1 = cvLoadImage("20110805_032255.jpg");
    cvCvtColor(imageSample1, imageSample1, CV_BGR2HSV);
    HsvImage I1(imageSample1);
    IplImage* imageSample2 = cvLoadImage("20110805_032257.jpg");
    cvCvtColor(imageSample2, imageSample2, CV_BGR2HSV);
    HsvImage I2(imageSample2);
    IplImage* imageSample3 = cvLoadImage("20110805_032259.jpg");
    cvCvtColor(imageSample3, imageSample3, CV_BGR2HSV);
    HsvImage I3(imageSample3);
    IplImage* imageSample4 = cvLoadImage("20110805_032301.jpg");
    cvCvtColor(imageSample4, imageSample4, CV_BGR2HSV);
    HsvImage I4(imageSample4);
    IplImage* imageSample5 = cvLoadImage("20110805_032303.jpg");
    cvCvtColor(imageSample5, imageSample5, CV_BGR2HSV);
    HsvImage I5(imageSample5);
    IplImage* imageSample6 = cvLoadImage("20110805_032953.jpg");
    cvCvtColor(imageSample6, imageSample6, CV_BGR2HSV);
    HsvImage I6(imageSample6);
    IplImage* imageSample7 = cvLoadImage("20110805_032955.jpg");
    cvCvtColor(imageSample7, imageSample7, CV_BGR2HSV);
    HsvImage I7(imageSample7);
    IplImage* imageSample8 = cvLoadImage("20110805_032957.jpg");
    cvCvtColor(imageSample8, imageSample8, CV_BGR2HSV);
    HsvImage I8(imageSample8);
    IplImage* imageSample9 = cvLoadImage("20110805_032959.jpg");
    cvCvtColor(imageSample9, imageSample9, CV_BGR2HSV);
    HsvImage I9(imageSample9);
    IplImage* imageSample10 = cvLoadImage("20110805_033001.jpg");
    cvCvtColor(imageSample10, imageSample10, CV_BGR2HSV);
    HsvImage I10(imageSample10);
    IplImage* imageSample11 = cvLoadImage("20110805_033009.jpg");
    cvCvtColor(imageSample11, imageSample11, CV_BGR2HSV);
    HsvImage I11(imageSample11);
    IplImage* imageSample12 = cvLoadImage("20110805_063443.jpg");
    cvCvtColor(imageSample12, imageSample12, CV_BGR2HSV);
    HsvImage I12(imageSample12);
    
    for (int i=0; i < threeArraySize; i++)
    {
        for (int j=0; j < arraySize; j++)
        {
            row1 = ceil(X/1.2866)+ceil(X/5.237)+i+ceil(-X/3.534545455) + ceil(X/4.8);
            column1 = ceil(Y/7.0755)+ceil(Y/21.01622)+j+ceil(X/1.495384615);
            averageHue = (I1[row1][column1].h + I2[row1][column1].h + I3[row1][column1].h + I4[row1][column1].h + I5[row1][column1].h + 	
            I6[row1][column1].h + I7[row1][column1].h + I8[row1][column1].h + I9[row1][column1].h + I10[row1][column1].h + I11[row1][column1].h + I12[row1][column1].h) / 12;
            averageSat = (I1[row1][column1].s + I2[row1][column1].s + I3[row1][column1].s + I4[row1][column1].s + I5[row1][column1].s + 
            I6[row1][column1].s + I7[row1][column1].s + I8[row1][column1].s + I9[row1][column1].s + I10[row1][column1].s + I11[row1][column1].s + I12[row1][column1].s) / 12;
            averageVal = (I1[row1][column1].v + I2[row1][column1].v + I3[row1][column1].v + I4[row1][column1].v + I5[row1][column1].v + 
            I6[row1][column1].v + I7[row1][column1].v + I8[row1][column1].v + I9[row1][column1].v + I10[row1][column1].v + I11[row1][column1].v + I12[row1][column1].v) / 12;
            //water patch sample (n X n matrix)
            cvmSet(waterTrainingHue,i,j,averageHue);
            cvmSet(waterTrainingSat,i,j,averageSat);
            cvmSet(waterTrainingVal,i,j,averageVal);  
             //patch is red (this is for me to know where the ground patch sample is)
            //I[row1][column1].h = 0;
            //I[row1][column1].s = 255;
            //I[row1][column1].v = 255;
        }
    }
    //creating a training sample from the an image taken on the fly
    row1 = 0;
    column1 = 0;
    for (int i=0; i<pixelsNumber; i++)
    {
        for (int j=0; j<pixelsNumber; j++)
        {
           row1 = ceil(X/1.2866)+ceil(X/5.237)+i+ceil(-X/3.534545455) + ceil(X/4.8);
           column1 = ceil(Y/7.0755)+ceil(Y/21.01622)+j+ceil(X/1.495384615);
           cvmSet(trainClassesH,i,0,I[row1][column1].h);
           cvmSet(trainClassesS,i,0,I[row1][column1].s);
           cvmSet(trainClassesV,i,0,I[row1][column1].v);
           
        }
    }
    //order the water samples in ascending order on order to know a range
    cvSort(waterTrainingHue, waterTrainingHue, CV_SORT_ASCENDING);
    cvSort(waterTrainingSat, waterTrainingSat, CV_SORT_ASCENDING);
    cvSort(waterTrainingVal, waterTrainingVal, CV_SORT_ASCENDING);
    // find the maximum and minimum values in the array to create a range
    int maxH = cvmGet(waterTrainingHue,0,0);
    int maxS = cvmGet(waterTrainingSat,0,0);
    int maxV = cvmGet(waterTrainingVal,0,0);
    int minH = cvmGet(waterTrainingHue,0,0);
    int minS = cvmGet(waterTrainingSat,0,0);
    int minV = cvmGet(waterTrainingVal,0,0);
    for (int i=0; i < threeArraySize; i++)
    {
        for (int j=0; j < arraySize; j++)
        {
            if (cvmGet(waterTrainingHue,i,j) > maxH)
                maxH = cvmGet(waterTrainingHue,i,j);
            if (cvmGet(waterTrainingSat,i,j) > maxS)
                maxS = cvmGet(waterTrainingHue,i,j);
            if (cvmGet(waterTrainingVal,i,j) > maxV)
                maxV = cvmGet(waterTrainingVal,i,j);
            if (cvmGet(waterTrainingHue,i,j) < minH)
                minH = cvmGet(waterTrainingHue,i,j);
            if (cvmGet(waterTrainingSat,i,j) < minS)
                minS = cvmGet(waterTrainingSat,i,j);
            if (cvmGet(waterTrainingVal,i,j) < minV)
                minV = cvmGet(waterTrainingVal,i,j);
        }
    }
    //cout << "Min Value in the range: " << endl;
    //cout << minH << endl;
    //cout << minS << endl;
    //cout << minV << endl;
    //cout << "Max Value in the range: " << endl;
    //cout << maxH << endl;
    //cout << maxS << endl;
    //cout << maxV << endl << endl;
    
    /*********** Main loop. It traverses through the picture**********/
    
    /**********************************************************************/
    //Ignore unused parts of the image and convert them to black
    for (int i=0; i<boatFront->height/1.45 - 1;i++)
    {
        for (int j=0; j<Y-1;j++)
        {
            I[i][j].h = 0;
            I[i][j].s = 0;
            I[i][j].v = 0;
        }
    }
    
    /*********************************************************************
    // Use nearest neighbors to increase accuracy
    skyX = 0; 
    skyY = 0;
    while (x < X-1)
    {
        //get a random sample taken from the picture. Must be determined whether
        //it is water or ground
        for (int i = 0; i<6;i++)
        {
            column1 = y+i;
            if (column1 > Y-1)
                column1 = Y-1;
            cvmSet(sampleHue,0,i,I[x][column1].h);
            cvmSet(sampleSat,0,i,I[x][column1].s);
            cvmSet(sampleVal,0,i,I[x][column1].v);
        }
        //Find the shortest distance between a pixel and the neighbors from each of
        //the training samples (sort of inefficient, but might do the job...sometimes)
        //HSV for water sample
        // learn classifier
        //CvKNearest knn(trainData, trainClasses, 0, false, itemsNumber);
        CvKNearest knnWaterHue(waterTrainingHue, trainClassesH, 0, false, pixelsNumber);
        CvKNearest knnWaterSat(waterTrainingSat, trainClassesS, 0, false, pixelsNumber);
        CvKNearest knnWaterVal(waterTrainingVal, trainClassesV, 0, false, pixelsNumber);
        //HSV for ground sample
        //CvKNearest knnGroundHue(groundTrainingHue, trainClasses2, 0, false, pixelsNumber);
        //CvKNearest knnGroundSat(groundTrainingSat, trainClasses2, 0, false, pixelsNumber);
        //CvKNearest knnGroundVal(groundTrainingVal, trainClasses2, 0, false, pixelsNumber);
        //HSV for sky sample
        //if (cvmGet(skyTrainingHue,0,0)!=0.0 && cvmGet(skyTrainingSat,0,0)!=0.0 && cvmGet(skyTrainingVal,0,0)!=0.0)
        //{
        //    CvKNearest knnSkyHue(skyTrainingHue, trainClasses, 0, false, pixelsNumber);
        //    CvKNearest knnSkySat(skyTrainingSat, trainClasses, 0, false, pixelsNumber);
        //    CvKNearest knnSkyVal(skyTrainingVal, trainClasses, 0, false, pixelsNumber);
        //}

        //scan nearest neighbors to each pixel
        responseWaterH = knnWaterHue.find_nearest(sampleHue,pixelsNumber,0,0,nearestWaterH,0);
        responseWaterS = knnWaterSat.find_nearest(sampleSat,pixelsNumber,0,0,nearestWaterS,0);
        responseWaterV = knnWaterVal.find_nearest(sampleVal,pixelsNumber,0,0,nearestWaterV,0);
        //responseGroundH = knnGroundHue.find_nearest(sampleHue,pixelsNumber,0,0,nearestGroundH,0);
        //responseGroundS = knnGroundSat.find_nearest(sampleSat,pixelsNumber,0,0,nearestGroundS,0);
        //responseGroundV = knnGroundVal.find_nearest(sampleVal,pixelsNumber,0,0,nearestGroundV,0);
        //for (int i=0;i<pixelsNumber;i++)
        //{
            for (int j=0;j<pixelsNumber;j++)
            {
                if ((nearestWaterH->data.fl[j] == responseWaterH) )//&& (nearestWaterH->data.fl[j] == responseWaterH + 5))
                        // mark water samples as green
                    comparator[0] = 1;
                else
                    comparator[0] = 0;
                if ((nearestWaterS->data.fl[j] == responseWaterS) )//&& (nearestWaterS->data.fl[j] < responseWaterS + 5))
                    //mark water samples as green
                    comparator[1] = 1;
                else
                    comparator[1] = 0;
                if ((nearestWaterV->data.fl[j] == responseWaterV) )//&& (nearestWaterV->data.fl[j] < responseWaterV + 5))
                //mark water samples as green
                    comparator[2] = 1;
                else
                    comparator[2] = 0;
                // similar sky pixels on the water
                //count votes
                for (int i3=0; i3 < 3; i3++)
                    votesSum = votesSum + comparator[i3]; 
                if (votesSum > 1)
                {
                    I[x][y-6+j].h = 0;
                    I[x][y-6+j].s = 255;
                    I[x][y-6+j].v = 255;
                }
                votesSum = 0;
            }
        }
        if (y < Y-1)
            //5 use to be equal to pixelsNumber-1.
            y = y + 5;
        if (y > Y-1)
            y = Y-1;
        else if (y == Y-1)
        {
            //5 use to be equal to pixelsNumber-1
            x = x + 1;
            y = 0;
        }
       // ix = 0;
    }
    
    /*********************************************************************/
    for(int i = 0; i < 3; i++)
    {   
        comparator[i] = 0;
    }
    //int counter = 0;
    column1 = 0;
    row1 = 0;
    x = boatFront->height/1.45;
    y = 0;
    while (x < X-1)
    {
        //get a random sample taken from the picture. Must be determined whether
        //it is water or ground
        for (int i = 0; i<6;i++)
        {
            column1 = y+i;
            if (column1 > Y-1)
                column1 = Y-1;
            cvmSet(sampleHue,0,i,I[x][column1].h);
            cvmSet(sampleSat,0,i,I[x][column1].s);
            cvmSet(sampleVal,0,i,I[x][column1].v);
        }
        for (int i=0;i<6;i++)
        {
            for (int j=0;j<6;j++)
            {
                if ((minH < cvmGet(sampleHue,0,j)) && (maxH > cvmGet(sampleHue,0,j)))
                    //mark water samples as green
                    comparator[0] = 1;
                else
                    comparator[0] = 0;
                if ((minS < cvmGet(sampleSat,0,j)) && (maxS > cvmGet(sampleSat,0,j)))
                //mark water samples as green
                    comparator[1] = 1;
                else
                    comparator[1] = 0;
                if ((minV < cvmGet(sampleVal,0,j)) && (maxV > cvmGet(sampleVal,0,j)))
                //mark water samples as red
                    comparator[2] = 1;
                else
                    comparator[2] = 0;
                //count votes
                for (int i3=0; i3 < 3; i3++)
                    votesSum = votesSum + comparator[i3];
                if (votesSum > 1)
                {
                    //use the known water samples as new training data
                    if((i<boatFront->height/xDivisor) && (j<boatFront->width/yDivisor))
                    {
                        cvmSet(resampleHue,i,j,cvmGet(sampleHue,0,j));
                        cvmSet(resampleSat,i,j,cvmGet(sampleSat,0,j));
                        cvmSet(resampleVal,i,j,cvmGet(sampleVal,0,j));
                    }
                    //6 use to be equal to pixelsNumber. 
                    I[x][y-6+j].h = 0;
                    I[x][y-6+j].s = 255;
                    I[x][y-6+j].v = 255;   
                }
                votesSum = 0;
            }
        }
        if (y < Y-1)
            //5 use to be equal to pixelsNumber-1.
            y = y + 5;
        if (y > Y-1)
            y = Y-1;
        else if (y == Y-1)
        {
            //5 use to be equal to pixelsNumber-1
            x = x + 1;
            y = 0;
        }
        //ix = 0;
    }
    
    /***************Deal with reflection*****************/
    for(int i = 0; i < 3; i++)
    {   
        comparator[i] = 0;
    }
    //int counter = 0;
    votesSum = 0;
    column1 = 0;
    row1 = 0;
    x = boatFront->height/1.45;
    y = 0;
    while (x < X-1)
    {
        //get a random sample taken from the picture. Must be determined whether
        //it is water or ground
        for (int i = 0; i<6;i++)
        {
            column1 = y+i;
            if (column1 > Y-1)
                column1 = Y-1;
            cvmSet(sampleHue,0,i,I[x][column1].h);
            cvmSet(sampleSat,0,i,I[x][column1].s);
            cvmSet(sampleVal,0,i,I[x][column1].v);
        }
        for (int i=0;i<6;i++)
        {
            for (int j=0;j<6;j++)
            {
                if ((minH < cvmGet(sampleHue,0,j)) && (maxH > cvmGet(sampleHue,0,j)))
                    //mark water samples as green
                    comparator[0] = 1;
                else
                    comparator[0] = 0;
                if ((0.8*255 > cvmGet(sampleSat,0,j)))// && (maxS < cvmGet(sampleSat,0,j)))
                //mark water samples as green
                    comparator[1] = 1;
                else
                    comparator[1] = 0;
                if ((0.6*255 < cvmGet(sampleVal,0,j)))// || (maxV < cvmGet(sampleVal,0,j)))
                //mark water samples as green
                    comparator[2] = 1;
                else
                    comparator[2] = 0;
                //count votes
                for (int i3=0; i3 < 3; i3++)
                    votesSum = votesSum + comparator[i3]; 
                if (votesSum > 1)
                {
                    //use the known water samples as new training data
                    if((i<boatFront->height/xDivisor) && (j<boatFront->width/yDivisor))
                    {
                        cvmSet(resampleHue,i,j,cvmGet(sampleHue,0,j));
                        cvmSet(resampleSat,i,j,cvmGet(sampleSat,0,j));
                        cvmSet(resampleVal,i,j,cvmGet(sampleVal,0,j));
                    }
                    //6 use to be equal to pixelsNumber. 
                    I[x][y-6+j].h = 0;
                    I[x][y-6+j].s = 255;
                    I[x][y-6+j].v = 255;   
                }
                votesSum = 0;
            }
        }
        if (y < Y-1)
            //5 use to be equal to pixelsNumber-1.
            y = y + 5;
        if (y > Y-1)
            y = Y-1;
        else if (y == Y-1)
        {
            //5 use to be equal to pixelsNumber-1
            x = x + 1;
            y = 0;
        }
        //ix = 0;
    }
    
    /**********Resample the entire patch**********/
    /*********find a new min and max for a new sample range*************/
    for(int i = 0; i < 3; i++)
    {   
        comparator[i] = 0;
    }
    //int counter = 0;
    votesSum = 0;
    column1 = 0;
    row1 = 0;
    x = boatFront->height/1.45;
    y = 0;
    maxH = cvmGet(resampleHue,0,0);
    maxS = cvmGet(resampleSat,0,0);
    maxV = cvmGet(resampleVal,0,0);
    minH = cvmGet(resampleHue,0,0);
    minS = cvmGet(resampleSat,0,0);
    minV = cvmGet(resampleVal,0,0);
    for (int i=0; i < boatFront->height/xDivisor; i++)
    {
        for (int j=0; j < boatFront->width/yDivisor; j++)
        {
            if (cvmGet(resampleHue,i,j) > maxH)
                maxH = cvmGet(resampleHue,i,j);
            if (cvmGet(resampleSat,i,j) > maxS)
                maxS = cvmGet(resampleSat,i,j);
            if (cvmGet(resampleVal,i,j) > maxV)
                maxV = cvmGet(resampleVal,i,j);
            if (cvmGet(resampleHue,i,j) < minH)
                minH = cvmGet(resampleHue,i,j);
            if (cvmGet(resampleSat,i,j) < minS)
                minS = cvmGet(resampleSat,i,j);
            if (cvmGet(resampleVal,i,j) < minV)
                minV = cvmGet(resampleVal,i,j);
        }
    }
    while (x < X-1)
    {
        for (int i=0;i<6;i++)
        {
            for (int j=0;j<6;j++)
            {
                if ((minH < I[x][y-6+j].h) && (maxH > I[x][y-6+j].h))
                    //mark water samples as red
                    I[x][y-6+j].h = 0;
                else
                    comparator[0] = 0;
                if ((minS < I[x][y-6+j].s) && (maxS > I[x][y-6+j].s))
                //mark water samples as red
                    I[x][y-6+j].s = 255;
                else
                    comparator[1] = 0;
                if ((minV < I[x][y-6+j].v) && (maxV > I[x][y-6+j].v))
                //mark water samples as red
                    I[x][y-6+j].v = 255;
            }
        }
        if (y < Y-1)
            //5 use to be equal to pixelsNumber-1.
            y = y + 5;
        if (y > Y-1)
            y = Y-1;
        else if (y == Y-1)
        {
            //5 use to be equal to pixelsNumber-1
            x = x + 1;
            y = 0;
        }
    }
    //cout << "Sample data from current images" << endl;
    //for (int i = 0; i<20;i++)
    //{
      //  cout << "HUE: " << cvmGet(sampleHue,0,i) << endl;
      //  cout << "Saturation: " << cvmGet(sampleSat,0,i) << endl;
      //  cout << "Value: " << cvmGet(sampleVal,0,i) << endl;
    //}
    //traverse through the image one more time, divide the image in grids of
    // 500x500 pixels, and see how many pixels of water are in each grid. If
    // most of the pixels are labeled water, then mark all the other pixels
    // as water as well
    //int counter = 0;
    votesSum = 0;
    column1 = 0;
    row1 = 0;
    x = boatFront->height/1.45;
    y = 0;
    
    /***************Divide the picture in cells for filtering**********/
    while (x < X-1)
    {
        //get a random sample taken from the picture. Must be determined whether
        //it is water or ground
        for (int i = 0; i < boatFront->height/xDivisor; i++)
        {
            for(int j = 0; j < boatFront->width/yDivisor; j++)
            {
                cvmSet(resampleHue2,i,j,I[x+i][y+j].h);
                cvmSet(resampleSat2,i,j,I[x+i][y+j].s);
                cvmSet(resampleVal2,i,j,I[x+i][y+j].v);
                if(cvmGet(resampleHue2,i,j)==0 && cvmGet(resampleSat2,i,j)==255 && cvmGet(resampleVal2,i,j)==255)
                {
                    votesSum++;
                }
            }
        }
        if (votesSum > (((boatFront->height/xDivisor)*(boatFront->width/yDivisor))*(4/5)))
        {   
        // if bigger than 4/5 the total number of pixels in a square, then consider the entire thing as water  
        // We might need to use other smaller quantities (like 5/6 maybe?)
            for (int i = 0; i < boatFront->height/xDivisor;i++)
            {
                for (int j = 0; j < boatFront->width/yDivisor; j++)
                {
                    row1 = x + i;
                    if (row1 > X-1)
                        row1 = X-1;
                    column1 = y+j;
                    if (column1 > Y-1)
                        column1 = Y-1;
                    I[row1][column1].h = 0;
                    I[row1][column1].s = 255;
                    I[row1][column1].v = 255;
                }
            }
        }
        else
        {   
        // If not water, eliminate all red pixels and turn those pixels
        // back to the original color
            for (int i = 0; i < boatFront->height/xDivisor;i++)
            {
                for (int j = 0; j < boatFront->width/yDivisor; j++)
                {
                    row1 = x + i;
                    if (row1 > X-1)
                        row1 = X-1;
                    column1 = y+j;
                    if (column1 > Y-1)
                        column1 = Y-1;
                    I[row1][column1].h = 255;//IBackUp[row1][column1].h;
                    I[row1][column1].s = 255;//IBackUp[row1][column1].s;
                    I[row1][column1].v = 255;//IBackUp[row1][column1].v;
                }
            }
        }
        y = y + boatFront->width/xDivisor;
        if (y > Y-1)
        {
            x = x + boatFront->height/yDivisor;
            y = 0;
        }
        votesSum = 0;
    }
    
    /********************Isolate obstacles************************/
    votesSum = 0;
    int paint = 0;
    column1 = 0;
    row1 = 0;
    x = boatFront->height/1.45;
    y = 0;
    xDiv = 20;
    yDiv = 20;
    /***************Divide the picture in cells for filtering**********/
    // Small pixel areas (noise) are going to be eliminated from the picture
    // living only the big obstacles
    while (x < X-1)
    {
        //get a random sample taken from the picture. Must be determined whether
        //it is water or ground
        for (int i = 0; i < boatFront->height/xDiv; i++)
        {
            for(int j = 0; j < boatFront->width/yDiv; j++)
            {
                row1 = x + i;
                if (row1 > X-1)
                    row1 = X-1;
                column1 = y+j;
                if (column1 > Y-1)
                    column1 = Y-1;
                cvmSet(resampleHue2,i,j,I[row1][column1].h);
                cvmSet(resampleSat2,i,j,I[row1][column1].s);
                cvmSet(resampleVal2,i,j,I[row1][column1].v);
                if(cvmGet(resampleHue2,i,j)==0 && cvmGet(resampleSat2,i,j)==255 && cvmGet(resampleVal2,i,j)==255)
                {
                    votesSum++;
                }
            }
        }
        if (votesSum > (((boatFront->height/xDiv)*(boatFront->width/yDiv))*(4.5/5)))
        {   
        // if bigger than 4/5 the total number of pixels in a square, then consider the entire thing as water  
        // We might need to use other smaller quantities (like 5/6 maybe?)
            for (int i = 0; i < boatFront->height/xDiv;i++)
            {
                for (int j = 0; j < boatFront->width/yDiv; j++)
                {
                    row1 = x + i;
                    if (row1 > X-1)
                        row1 = X-1;
                    column1 = y+j;
                    if (column1 > Y-1)
                        column1 = Y-1;
                    I[row1][column1].h = 0;
                    I[row1][column1].s = 255;
                    I[row1][column1].v = 255;
                }
            }
        }
        else
        {   
        // If not water, eliminate all red pixels and turn those pixels
        // back to the original color
            for (int i = 0; i < boatFront->height/xDiv;i++)
            {
                for (int j = 0; j < boatFront->width/yDiv; j++)
                {
                    row1 = x + i;
                    if (row1 > X-1)
                        row1 = X-1;
                    column1 = y+j;
                    if (column1 > Y-1)
                        column1 = Y-1;
                    I[row1][column1].h = 255;
                    I[row1][column1].s = 255;
                    I[row1][column1].v = 255;
                }
            }
            //The distance formula calculated by plotting points is given by:
            //These formulas only work for 640X480 images
    /***********  distance = 0.1622208546*pow(1.0186851612,pixels)  *****************/
    /***********  pixel = 87.0413255*pow(distance,0.4062956891) *****************/
            //Get the obstacles coordinate
            // x,y coordinates of the obstacle from the bottom center of the image
            //Ignore everything less than 0.3 meters apart (anything too close to the boat)
            if ((X - (row1 -(boatFront->height/xDiv)/2)) > (87.0413255*pow(0.3,0.4062956891)))
            {
                xObstacleDistance = 0.1622208546*pow(1.0186851612,X - (row1 -(boatFront->height/xDiv)/2));
                if (xObstacleDistance == 0.0)                  //try to ignore obstacle that are too close
                        xObstacleDistance = 0.01;                  //robot shall tell operator if there is
                                                           //a problem with a close by obstacle
                yObstacleDistance = 0.1622208546*pow(1.0186851612,Y/2 - (column1 - (boatFront->width/yDiv)/2));
                //obstacle distance
                obstacleDistance = sqrt(pow(xObstacleDistance,2) + pow(yObstacleDistance,2));
                //obstacle heading
                obstacleHeading = tan((yObstacleDistance/xObstacleDistance)*PI/180);
                cout << "Obstacle polar coordinates: " << endl;
                cout << "x: " << xObstacleDistance << " Y: " << yObstacleDistance << endl;
                cout << "Distance (meters) " << obstacleDistance << endl;
                cout << "Direction (degrees): " << obstacleHeading << endl << endl;
            }
        }
        y = y + boatFront->width/yDiv;
        if (y > Y-1)
        {
            x = x + boatFront->height/xDiv;
            if (x > X-1)
                x = X-1;
            y = 0;
        }
        votesSum = 0;
    }
    
    /****************Find Obstacles boundaries*********************************/
    if( grayStorage == NULL )
    {
        grayStorage = cvCreateMemStorage(0);
    } 
    else 
    {
        cvClearMemStorage(grayStorage);
    }
    backUpImage = cvCloneImage(boatFront);
    //convert from HSV to RGB
    cvCvtColor(boatFront, boatFront, CV_HSV2BGR);
    cvCvtColor(backUpImage, backUpImage, CV_HSV2BGR);
    //do flood fill for obstacles
    cvFloodFill( backUpImage, seed_point, color, cvScalarAll(255), cvScalarAll(2), NULL, 8, NULL);
    //convert to to gray to do more obstacle segmentation
    cvCvtColor(backUpImage, grayImage, CV_BGR2GRAY);
    //convert to binary
    cvThreshold(grayImage, bwImage, 100, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);
    //eliminate small unnecessary pixel areas
    //int findCountours = bwareaopen_(bwImage, 200);
    //find contours of obstacles in image
    cvFindContours(bwImage, grayStorage, &contours);
    cvZero( bwImage );
    if( contours )
    {
        cvDrawContours(bwImage, contours, cvScalarAll(255), cvScalarAll(255), 8);
    }
    // find non-white blobs in thresholded image
    //cvMorphologyEx(segmentated, segmentated, NULL, morphKernel, CV_MOP_OPEN, 1);
    //unsigned int result = CvLabel(segmentated, labelImg, blobs);
    //cvFilterByArea(blobs, 50, 100);
    //cvRenderBlobs(labelImg, blobs, bwImage, bwImage, CV_BLOB_RENDER_BOUNDING_BOX);
    
    /***********************************************************************/
    cvNamedWindow( "Boat Front", 0);      //0 to maintains sizes regardless of image size
    cvResizeWindow("Boat Front",900,750); // new width/heigh in pixels
    cvShowImage( "Boat Front", bwImage );
    cvWaitKey(0);
    //cvResetImageROI(boatFront);
    cvReleaseImage(&boatFront);
    cvReleaseImage(&backUpImage);
    cvReleaseImage(&grayImage);
    cvReleaseImage(&bwImage);
    cvDestroyWindow("Boat Front");
    //cvReleaseMat(&trainClassesH);
    //cvReleaseMat(&trainClassesH);
    return 0; 
}
              
/*             
// main.cpp : Defines the entry point for the console application.
//

//#include "stdafx.h" //AO
#include "opencv/cv.h"
#include "opencv/highgui.h"
#include <stdio.h>
//#include <conio.h> //AO

// Main blob library include
#include "opencv/BlobResult.h"

char wndname[] = "Blob Extraction";
char tbarname1[] = "Threshold";
char tbarname2[] = "Blob Size";

// The output and temporary images
IplImage* originalThr = 0;
IplImage* original = 0;
IplImage* displayedImage = 0;

int param1,param2;



// threshold trackbar callback
void on_trackbar( int dummy )
{
	if(!originalThr)
	{
		originalThr = cvCreateImage(cvGetSize(original), IPL_DEPTH_8U,1);
	}

	if(!displayedImage)
	{
		displayedImage = cvCreateImage(cvGetSize(original), IPL_DEPTH_8U,3);
	}
	
	// threshold input image
	cvThreshold( original, originalThr, param1, 255, CV_THRESH_BINARY );

	// get blobs and filter them using its area
	CBlobResult blobs;
	int i;
	CBlob *currentBlob;

	// find blobs in image
	blobs = CBlobResult( originalThr, NULL, 255 );
	blobs.Filter( blobs, B_EXCLUDE, CBlobGetArea(), B_LESS, param2 );

	// display filtered blobs
	cvMerge( originalThr, originalThr, originalThr, NULL, displayedImage );

	for (i = 0; i < blobs.GetNumBlobs(); i++ )
	{
		currentBlob = blobs.GetBlob(i);
		currentBlob->FillBlob( displayedImage, CV_RGB(255,0,0));
	}
	 
    cvShowImage( wndname, displayedImage );
	
}



int main( int argc, char** argv )
{

	param1 = 100;
	param2 = 2000;
	
	// open input image
	original = cvLoadImage("pic6.png",0);

	cvNamedWindow("input");
	cvShowImage("input", original );
	
	cvNamedWindow(wndname, 0);
    cvCreateTrackbar( tbarname1, wndname, &param1, 255, on_trackbar );
	cvCreateTrackbar( tbarname2, wndname, &param2, 30000, on_trackbar );
	
	// Call to update the view
	for(;;)
    {
        int c;
        
        // Call to update the view
        on_trackbar(0);

        c = cvWaitKey(0);

	   if( c == 27 )
            break;
	}
    
    cvReleaseImage( &original );
	cvReleaseImage( &originalThr );
	cvReleaseImage( &displayedImage );
    
    cvDestroyWindow( wndname );
    
    return 0;
}
             * 
             * **/