/* 
 * File:   my_subscriber.cpp
 * Author: pototo
 *
 * Created on July 28, 2011,  9:56 PM
 * 
 * This program recognizes the water in front of the boat by using ground
 * plane detection techniques already available. This will look for the pixels 
 * from the boat, and go up the image until it finds the vector plane the repre-
 * sents the water
 */

#include <ros/ros.h>
#include <image_transport/image_transport.h>
//#include <compressed_image_transport/compressed_image_transport.h>
#include "std_msgs/String.h"
#include "std_msgs/Float32.h"

#include <opencv/cv.h>
#include <opencv/highgui.h>
#include <opencv/ml.h>

#include <cv_bridge/CvBridge.h>

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
    CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE, cvPoint(0,0));
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
    
    //cvNamedWindow( "contour", 0);      //0 to maintains sizes regardless of image size
   // cvResizeWindow("contour",900,750); // new width/heigh in pixels
    //cvShowImage( "contour", image );
    //cvWaitKey(0);
    //cvResetImageROI(boatFront);
   // cvDestroyWindow("contour");
    cvReleaseMemStorage( &storage ); // desallocate CvSeq as well.
    cvReleaseImage(&input);
    return(foundCountours);
}

void imageCallback(const sensor_msgs::ImageConstPtr& msg)
{
	//bridge that will transform the message (image) from ROS code back to "image" code
  sensor_msgs::CvBridge bridge;
  fprintf(stderr, "\n call Back funtion \n");
  
  //publish data (obstacle waypoints) back to the boat
  ros::NodeHandle n;
  std_msgs::Float32 xWaypoint_msg;         // X coordinate obstacle message object
  std_msgs::Float32 zWaypoint_msg;         // Y coordinate obstacle message object
  //std::stringstream ss;
  //publish the waypoint data             
  ros::Publisher Xwaypoint_info_pub = n.advertise<std_msgs::Float32>("Xwaypoint_info", 0.01);
  ros::Publisher Zwaypoint_info_pub = n.advertise<std_msgs::Float32>("Zwaypoint_info", 0.01);
  
  /***********************************************************************/
    //live image coming streamed straight from the boat's camera
    IplImage* boatFront = bridge.imgMsgToCv(msg, "bgr8");
    //The boat takes flipped images, so you need to flip them back to normal
    cvFlip(boatFront, boatFront, 0);
    IplImage* backUpImage = cvCloneImage(boatFront);
    boatFront->origin = IPL_ORIGIN_TL;   //sets image origin to top left corner
    int X = boatFront->height;
    int Y = boatFront->width;
    //cout << "height " << X << endl;
    //cout << "width " << Y << endl;    
    
    /*********************Image Filtering variables****************************/
    //these images are used for segmenting objects from the overall background 
    //create a one channel image to convert from RGB to GRAY
    IplImage* grayImage = cvCreateImage(cvGetSize(boatFront),IPL_DEPTH_8U,1);
    //convert grayImage to binary (final step after converting from GRAY)
    IplImage* bwImage = cvCreateImage(cvGetSize(grayImage),IPL_DEPTH_8U,1);
    //variables used for the flood fill segmentation
    CvPoint seed_point = cvPoint(boatFront->height/2 + 70,0);       //not sure how this variable works
    CvScalar color = CV_RGB(250,0,0);
    CvMemStorage* grayStorage = NULL;     //memory storage for contour sequence
    CvSeq* contours = 0;
    // get blobs and filter them using their area
    //IplConvKernel* morphKernel = cvCreateStructuringElementEx(5, 5, 1, 1, CV_SHAPE_RECT, NULL);
    //IplImage* original, *originalThr;
    //IplImage* segmentated = cvCreateImage(cvGetSize(boatFront), 8, 1);
    //unsigned int blobNumber = 0;
    //IplImage* labelImg = cvCreateImage(cvGetSize(boatFront), IPL_DEPTH_LABEL, 1);
    CvMoments moment;
    
    /***********************************************************************/
    //boat's edge distance from the camera. This is used for visual calibration
    //to know the distance from the boat to the nearest obstacles.
    //With respect to the mounted camera, distance is 21 inches (0.5334 m) side to side
    //and 15 inches (0.381 m).
    //float boatFrontDistance = 0.381;    //distance in meters
    //float boatSideDistance = 0.5334;    //distance in meters
    // These variables tell the distance from the center bottom of the image
    // (the camera) to the square surrounding a the obstacle
    float obstacleDistance = 0.0;            //Euclidean distance to object
    float obstacleHeading = 0.0;
    //distance variables from the camera calibration matrix
    int xPixel = 0;       //pixels from left to right
    int yPixel = 0;       //pixels from bottom to top
    float zObstacleDistance = 0;    //object distance from the camera
    float xObstacleDistance = 0;
    float yObstacleDistance = 0.1143;  //distance in meters from water to camera.
                          //its gonna be constant assuming boat barely moves up and down in the water
    
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
    CvMat* trainClasses = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    CvMat* trainClasses2 = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    //CvMat sample = cvMat( 1, 2, CV_32FC1, _sample );
    //used with the classifier 
    CvMat* trainClassesH = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    CvMat* trainClassesS = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    CvMat* trainClassesV = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    //CvMat* trainClasses2 = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
    //CvMat sample = cvMat( 1, 2, CV_32FC1, _sample );
    //used with the classifier 
    /*CvMat* nearestWaterH = cvCreateMat(1, pixelsNumber, CV_32FC1);
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
    CvMat* distanceSkyV = cvCreateMat(1, pixelsNumber, CV_32FC1); */
    //these variables are use to traverse the picture by blocks of n x n pixels at
    //a time. 
    //Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
    //right way, of course)
    //x and y are the dimensions of the local patch of pixels
    int x = (boatFront->height)/2 + 70;//(boatFront->height)/2.5 + 105; 
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
    //CvMat* groundTrainingHue = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    //CvMat* groundTrainingSat = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    //CvMat* groundTrainingVal = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    //water sample
    CvMat* waterTrainingHue = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* waterTrainingSat = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* waterTrainingVal = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    //n x n sample patch taken from the picture
    CvMat* sampleHue = cvCreateMat(1,arraySize,CV_32FC1);
    CvMat* sampleSat = cvCreateMat(1,arraySize,CV_32FC1);
    CvMat* sampleVal = cvCreateMat(1,arraySize,CV_32FC1);
    CvMat* resampleHue0 = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* resampleSat0 = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
    CvMat* resampleVal0 = cvCreateMat(threeArraySize,arraySize,CV_32FC1);
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
    cvZero(resampleHue0);
    cvZero(resampleSat0);
    cvZero(resampleVal0);
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
    /*
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
    cvSetImageROI(imageSample1, cvRect(0,0,imageSample1->height/0.5,imageSample1->width/1.83));
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
    IplImage* imageSample12 = cvLoadImage("20110805_033011.jpg");
    cvCvtColor(imageSample12, imageSample12, CV_BGR2HSV);
    HsvImage I12(imageSample12);
    //IplImage* imageSample13 = cvLoadImage("20110812_110924.jpg");
    //cvCvtColor(imageSample13, imageSample13, CV_BGR2HSV);
    //HsvImage I13(imageSample13);
    
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
                maxS = cvmGet(waterTrainingSat,i,j);
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

    /*********** Main loop. It traverses through the picture**********/
    
    /******************** Live water samples *******************************/
    //learn how "current water" looks like on the fly
    row1 = 0;
    column1 = 0;
    for (int i=0; i<pixelsNumber; i++)
    {
        for (int j=0; j<pixelsNumber; j++)
        {
           //front of boat might appear in the image. Account for that
           row1 = ceil(X/1.2866)+ceil(X/5.237)+i+ceil(-X/3.534545455) + ceil(X/4.8) - 55;
           column1 = ceil(Y/7.0755)+ceil(Y/21.01622)+j+ceil(X/1.495384615);
           cvmSet(resampleHue0,i,j,I[row1][column1].h);
           cvmSet(resampleSat0,i,j,I[row1][column1].s);
           cvmSet(resampleVal0,i,j,I[row1][column1].v);
           //visualize "resample" patch
          // I[row1][column1].h = 0;
           //I[row1][column1].s = 0;
           //I[row1][column1].v = 0;
        }
    }
    //order the water samples in ascending order on order to know a range
    cvSort(resampleHue0, resampleHue0, CV_SORT_ASCENDING);
    cvSort(resampleSat0, resampleSat0, CV_SORT_ASCENDING);
    cvSort(resampleVal0, resampleVal0, CV_SORT_ASCENDING);
    // find the maximum and minimum values in the array to create a range
    int maxH0 = cvmGet(resampleHue0,0,0);
    int maxS0 = cvmGet(resampleSat0,0,0);
    int maxV0 = cvmGet(resampleVal0,0,0);
    int minH0 = cvmGet(resampleHue0,0,0);
    int minS0 = cvmGet(resampleSat0,0,0);
    int minV0 = cvmGet(resampleVal0,0,0);
    for (int i=0; i < threeArraySize; i++)
    {
        for (int j=0; j < arraySize; j++)
        {
            if (cvmGet(resampleHue0,i,j) > maxH0)
                maxH0 = cvmGet(resampleHue0,i,j);
            if (cvmGet(resampleSat0,i,j) > maxS0)
                maxS0 = cvmGet(resampleSat0,i,j);
            if (cvmGet(resampleVal0,i,j) > maxV0)
                maxV0 = cvmGet(resampleVal0,i,j);
            if (cvmGet(resampleHue0,i,j) < minH0)
                minH0 = cvmGet(resampleHue0,i,j);
            if (cvmGet(resampleSat0,i,j) < minS0)
                minS0 = cvmGet(resampleSat0,i,j);
            if (cvmGet(resampleVal0,i,j) < minV0)
                minV0 = cvmGet(resampleVal0,i,j);
        }
    }
    for(int i = 0; i < 3; i++)
    {   
        comparator[i] = 0;
    }
    //int counter = 0;
    column1 = 0;
    row1 = 0;
    x = boatFront->height/2 + 70;
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
                if ((minH0 < cvmGet(sampleHue,0,j)) && (maxH0 > cvmGet(sampleHue,0,j)))
                    //mark water samples as green
                    comparator[0] = 1;
                else
                    comparator[0] = 0;
                if ((minS0 < cvmGet(sampleSat,0,j)) && (maxS0 > cvmGet(sampleSat,0,j)))
                //mark water samples as green
                    comparator[1] = 1;
                else
                    comparator[1] = 0;
                if ((minV0 < cvmGet(sampleVal,0,j)) && (maxV0 > cvmGet(sampleVal,0,j)))
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
                    //if((i<boatFront->height/xDivisor) && (j<boatFront->width/yDivisor))
                    //{
                     //   cvmSet(resampleHue,i,j,cvmGet(sampleHue,0,j));
                      //  cvmSet(resampleSat,i,j,cvmGet(sampleSat,0,j));
                       // cvmSet(resampleVal,i,j,cvmGet(sampleVal,0,j));
                    //}
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
    x = boatFront->height/2 + 70;
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
    x = boatFront->height/2 + 70;
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
    x = boatFront->height/2 + 70;
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
    x = boatFront->height/2 + 70;
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
                    I[row1][column1].h = IBackUp[row1][column1].h;//255;//IBackUp[row1][column1].h;
                    I[row1][column1].s = IBackUp[row1][column1].s;//255;//IBackUp[row1][column1].s;
                    I[row1][column1].v = IBackUp[row1][column1].v;//255;//IBackUp[row1][column1].v;
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
    x = boatFront->height/2 + 40;
    y = 0;
    xDiv = 40;
    yDiv = 40;
    /***************Divide the picture in cells for filtering**********/
    // Small pixel areas (noise) are going to be eliminated from the picture
    // living only the big obstacles
    while (x < X-2)
    {
        //get a random sample taken from the picture. Must be determined whether
        //it is water or ground
        for (int i = 0; i < boatFront->height/xDiv; i++)
        {
            for(int j = 0; j < boatFront->width/yDiv; j++)
            {
                row1 = x + i;
                if (row1 > X-2)
                    row1 = X-2;
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
        if (votesSum > (((boatFront->height/xDiv)*(boatFront->width/yDiv))*(4/5)))
        {   
        // if bigger than 4/5 the total number of pixels in a square, then consider the entire thing as water  
        // We might need to use other smaller quantities (like 5/6 maybe?)
            for (int i = 0; i < boatFront->height/xDiv;i++)
            {
                for (int j = 0; j < boatFront->width/yDiv; j++)
                {
                    row1 = x + i;
                    if (row1 > X-2)
                        row1 = X-2;
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
            int count = 0;
        // If not water, eliminate all red pixels and turn those pixels
        // back to the original color
            for (int i = 0; i < boatFront->height/xDiv;i++)
            {
                for (int j = 0; j < boatFront->width/yDiv; j++)
                {
                    row1 = x + i;
                    if (row1 > X-2)
                        row1 = X-2;
                    column1 = y+j;
                    if (column1 > Y-1)
                        column1 = Y-1;
                    I[row1][column1].h = IBackUp[row1][column1].h;//255;
                    I[row1][column1].s = IBackUp[row1][column1].s;//255;
                    I[row1][column1].v = IBackUp[row1][column1].v;//255;
                   // count++;
                }
            }
        }
        y = y + boatFront->width/yDiv;
        if (y > Y-1)
        {
            x = x + boatFront->height/xDiv;
            if (x > X-2)
                x = X-2;
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
    //backUpImage = cvCloneImage(boatFront);
    //IBackUp(backUpImage);
    //Ignore unused parts of the image and convert them to black
    for (int i=0; i<backUpImage->height;i++)
    {
        for (int j=0; j<backUpImage->width;j++)
        {
            if(i < backUpImage->height/2 + 70)
            {
                IBackUp[i][j].h = 0;
                IBackUp[i][j].s = 0;
                IBackUp[i][j].v = 0;
            }
            else
            {
                IBackUp[i][j].h = I[i][j].h;
                IBackUp[i][j].s = I[i][j].s;
                IBackUp[i][j].v = I[i][j].v;
            }
        }
    }
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
    //bwImage is a pointer, so no need to reuse findCountours
    int findCountours = bwareaopen_(bwImage, 100);          
    //find contours of obstacles in image
    cvFindContours(bwImage, grayStorage, &contours);
    cvZero( bwImage );                      //redraw clean contours
    for( CvSeq* c=contours; c!=NULL; c=c->h_next)
    {
        cvDrawContours(bwImage, c, cvScalarAll(255), cvScalarAll(255), 8);
        //ignore obstacles/contours with are less than 100 pixels or bigger than 100000 pixels
        if ((cvContourArea(c, CV_WHOLE_SEQ) >= 60) && (cvContourArea(c, CV_WHOLE_SEQ) <= 100000))
        {
            cout << "Contour area: " << cvContourArea(c, CV_WHOLE_SEQ) << endl;     //area in pixels                        
            //find the x,y coordinate of the center of a contour
            cvMoments(c, &moment, 0);
            //centroid/moment of the contour/obstacle
            cout << "Contour center in x,y: " << moment.m10/moment.m00 << ", " << moment.m01/moment.m00 << endl;
            //The distance formula calculated by plotting points is given by:
            // Xc/Zc = Xp-cc(1)/Fc(1)
            // Yc/Zc = Yp-cc(2)/Fc(2)
            //For boat one Yc = 4.5 inches = 0.0635 meters
            //These formulas only work for 640X480 images
            // x,y coordinates of the obstacle from the bottom center of the image
            //Ignore everything less than 0.3 meters apart (anything too close to the boat)
            zObstacleDistance = 5*(yObstacleDistance*619.33108)/(X - (moment.m01/moment.m00));
            xObstacleDistance = 5*zObstacleDistance*((moment.m10/moment.m00)-324.36738)/618.62586;
            //copy data to be published
            xWaypoint_msg.data = xObstacleDistance;
            zWaypoint_msg.data = zObstacleDistance;
            //publish data
            Xwaypoint_info_pub.publish(xWaypoint_msg);
  					Zwaypoint_info_pub.publish(zWaypoint_msg);
            //try to ignore obstacle that are too close. Robot shall tell operator if there is
            //a problem with a close by obstacle
            //obstacle distance
            obstacleDistance = sqrt(pow(xObstacleDistance,2) + pow(yObstacleDistance,2) + pow(zObstacleDistance,2));
            //Just use the 2D angle
            obstacleHeading = tan((zObstacleDistance/xObstacleDistance)*PI/180);
            cout << "Obstacle polar coordinates: " << endl;
            cout << "z: " << zObstacleDistance << " x: " << xObstacleDistance << endl;
            cout << "Distance (meters) " << obstacleDistance << endl;
            cout << "Direction (degrees): " << obstacleHeading << endl << endl;
        }
    }
    /**************************************************************************/
    //deal with memory management. How do I get read of the arrays and pointers I am not using inside the callback function??????
	  try
    {
  	  //fprintf(stderr,"\n boatFront\n");
      cvShowImage("Boat Front", backUpImage);
      //cvShowImage("Color Segment", backUpImage);
      //cvShowImage("Obstacles", bwImage);
    }
    catch (sensor_msgs::CvBridgeException& e)
    {
      ROS_ERROR("Could not convert from '%s' to 'bgr8'.", msg->encoding.c_str());
    }
}

int main(int argc, char **argv)
{
  ros::init(argc, argv, "pototo_image_listener");
  ros::NodeHandle nh;
  ros::Rate loop_rate(0.001);
	cvNamedWindow("Boat Front",0);            //0 to maintains sizes regardless of image size
	cvResizeWindow("Boat Front",700,550);     // new width/heigh in pixels
    //   cvNamedWindow("Color Segment",0);            //0 to maintains sizes regardless of image size
	//cvResizeWindow("Color Segment",700,550);     // new width/heigh in pixels
   //     cvNamedWindow("Obstacle",0);            //0 to maintains sizes regardless of image size
	//cvResizeWindow("Obstacle",700,550);     // new width/heigh in pixels
        //cvWaitKey(0);
  
	//This is used for republishing the waypoints for the obstacles
    
	ROS_INFO("getting image");
	cvStartWindowThread();
	image_transport::ImageTransport it(nh);
		//image     //use this one for compressed images
		//camera/image
	image_transport::Subscriber sub = it.subscribe("image", 1, imageCallback);
	fprintf(stderr,"\n I am out of the call back function\n");
	ros::spin();
	cvDestroyWindow("Boat Front");
	//cvDestroyWindow("Color Segment");
	//cvDestroyWindow("Obstacles");
	loop_rate.sleep();
 return 0;
}
