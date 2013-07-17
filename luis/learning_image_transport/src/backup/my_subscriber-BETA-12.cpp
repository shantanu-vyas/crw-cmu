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


void imageCallback(const sensor_msgs::ImageConstPtr& msg)
{
	//bridge that will transform the message (image) from ROS code back to "image" code
  sensor_msgs::CvBridge bridge;
  fprintf(stderr, "\n callBaack funtion \n");
  //publish data (obstacle waypoints) back to the boat
  //ros::NodeHandle n;
  //std_msgs::Float32 xWaypoint_msg;         // X coordinate obstacle message
  //std_msgs::Float32 yWaypoint_msg;         // Y coordinate obstacle message
  //publish the waypoint data             
  //ros::Publisher waypoint_info_pub = n.advertise<std_msgs::Float32>("waypoint_info", 1000);
  //ros::Publisher Ywaypoint_info_pub = n.advertise<std_msgs::Float32>("waypoint_info", 1000);
  //std::stringstream ss;
  
  /***********************************************************************/
  //live image coming streamed straight from the boat's camera
  IplImage* boatFront = bridge.imgMsgToCv(msg, "bgr8");
  IplImage* backUpImage = bridge.imgMsgToCv(msg, "bgr8");
  boatFront->origin = IPL_ORIGIN_TL;   //sets image origin to top left corner
  //Crop the image to the ROI
  //cvSetImageROI(boatFront, cvRect(0,0,boatFront->height/0.5,boatFront->width/1.83));
  int X = boatFront->height;
  int Y = boatFront->width;
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
  
  int pixelsNumber = 30;  //number of pixels for an n x n matrix and # of neighbors
  const int arraySize = pixelsNumber;
  const int threeArraySize = pixelsNumber;
  int pixelCount;
  //if n gets changed, then the algorithm might have to be
  //recalibrated. Try to keep it constant
  //these variables are used for the k nearest neighbors
  //int accuracy;
  //reponses for each of the classifications
  float responseWaterH, responseWaterS, responseWaterV; 
  float responseGroundH, responseGroundS, responseGroundV;
  //float responseSkyH, responseSkyS, responseSkyV;
  float averageHue = 0.0;
  float averageSat = 0.0;
  float averageVal = 0.0;
  CvMat* trainClasses = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
  CvMat* trainClasses2 = cvCreateMat( pixelsNumber, 1, CV_32FC1 );
  //for (int i = 0; i < pixelsNumber/2; i++)
  //{
    //  cvmSet(trainClasses, i,0,1);
     // cvmSet(trainClasses2, i,0,1);
  //}
  //for (int i = pixelsNumber/2; i < pixelsNumber; i++)
  //{
    //  cvmSet(trainClasses, i,0,2);
     // cvmSet(trainClasses2, i,0,2);
  //}
  //for (int i =0; i<pixelsNumber;i++)
  //{
    //   cout << cvmGet(trainClasses,i,0);
      // cout << cvmGet(trainClasses2,i,0);   
  //}
  //CvMat sample = cvMat( 1, 2, CV_32FC1, _sample );
  //used with the classifier 
  //CvMat* nearestWaterH = cvCreateMat(1, pixelsNumber, CV_32FC1);
  //CvMat* nearestWaterS = cvCreateMat(1, pixelsNumber, CV_32FC1);
  //CvMat* nearestWaterV = cvCreateMat(1, pixelsNumber, CV_32FC1);
  //CvMat* nearestGroundH = cvCreateMat(1, pixelsNumber, CV_32FC1);
  //CvMat* nearestGroundS = cvCreateMat(1, pixelsNumber, CV_32FC1);
  //CvMat* nearestGroundV = cvCreateMat(1, pixelsNumber, CV_32FC1);
  //these variables are use to traverse the picture by blocks of n x n pixels at
  //a time. 
  //Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
  //right way, of course)
  //x and y are the dimensions of the local patch of pixels
  int x = ceil((boatFront->height)/10); //(boatFront->height)/2.5 + pixelsNumber + 99; 
  int y = 0;//pixelsNumber-1; 
  int ix = 0;                                                             
  int iy = 0; 
  int skyX = 0; 
  int skyY = 0;
  //M controls the x axis (up and down); N controls the y axis (left and
  //right)
  int Mw = -550; 
  int Nw = 1300; 
  int Mg = -350; 
  int Ng = 700;
  int row1 = 0;
  int column1 = 0;
  int row2 = 0;
  int column2 = 0;
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
  CvMat* resampleHue = cvCreateMat(arraySize,arraySize,CV_32FC1);
  CvMat* resampleSat = cvCreateMat(arraySize,arraySize,CV_32FC1);
  CvMat* resampleVal = cvCreateMat(arraySize,arraySize,CV_32FC1);
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
 // for(int i = 0; i < 3; i++)
  //{   
      //comparator[i] = 0;
    //  for(int j = 0; j < 3; j++)
     // {
       //   comparatorTwo[i][j] = 0;
     // }
  //}
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
    wont go below the horizon*/
  
  //convert from RGB to HSV
  cvCvtColor(boatFront, boatFront, CV_BGR2HSV);
  cvCvtColor(backUpImage, backUpImage, CV_BGR2HSV);
  HsvImage I(boatFront);
  HsvImage IBackUp(backUpImage);
  fprintf(stderr,"\n About to do Sky detection\n");
  //Sky detection
  for (int i=0; i<boatFront->height/3;i++)
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
              I[i][j].h = 0.3*180;       //H (color)
              I[i][j].s = 0.3*180;          //S (color intensity)
              I[i][j].v = 0.6*180;          //V (brightness)
              if (skyY == pixelsNumber-1)
              {
                 if (skyX == pixelsNumber-1)
                   skyX = 0;
                 else
                   skyX = skyX + 1;
                 skyY = 0;
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
  cvSetImageROI(imageSample2, cvRect(0,0,imageSample2->height/0.5,imageSample2->width/1.83));
  cvCvtColor(imageSample2, imageSample2, CV_BGR2HSV);
  HsvImage I2(imageSample2);
  IplImage* imageSample3 = cvLoadImage("20110805_032259.jpg");
  cvSetImageROI(imageSample3, cvRect(0,0,imageSample3->height/0.5,imageSample3->width/1.83));
  cvCvtColor(imageSample3, imageSample3, CV_BGR2HSV);
  HsvImage I3(imageSample3);
  IplImage* imageSample4 = cvLoadImage("20110805_032301.jpg");
  cvSetImageROI(imageSample4, cvRect(0,0,imageSample4->height/0.5,imageSample4->width/1.83));
  cvCvtColor(imageSample4, imageSample4, CV_BGR2HSV);
  HsvImage I4(imageSample4);
  IplImage* imageSample5 = cvLoadImage("20110805_032303.jpg");
  cvSetImageROI(imageSample5, cvRect(0,0,imageSample5->height/0.5,imageSample5->width/1.83));
  cvCvtColor(imageSample5, imageSample5, CV_BGR2HSV);
  HsvImage I5(imageSample5);
  IplImage* imageSample6 = cvLoadImage("20110805_032305.jpg");
  cvSetImageROI(imageSample6, cvRect(0,0,imageSample6->height/0.5,imageSample6->width/1.83));
  cvCvtColor(imageSample6, imageSample6, CV_BGR2HSV);
  HsvImage I6(imageSample6);
  IplImage* imageSample7 = cvLoadImage("20110805_032307.jpg");
  cvSetImageROI(imageSample7, cvRect(0,0,imageSample7->height/0.5,imageSample7->width/1.83));
  cvCvtColor(imageSample7, imageSample7, CV_BGR2HSV);
  HsvImage I7(imageSample7);
  IplImage* imageSample8 = cvLoadImage("20110805_032309.jpg");
  cvSetImageROI(imageSample8, cvRect(0,0,imageSample8->height/0.5,imageSample8->width/1.83));
  cvCvtColor(imageSample8, imageSample8, CV_BGR2HSV);
  HsvImage I8(imageSample8);
  IplImage* imageSample9 = cvLoadImage("20110805_032953.jpg");
  cvSetImageROI(imageSample9, cvRect(0,0,imageSample9->height/0.5,imageSample9->width/1.83));
  cvCvtColor(imageSample9, imageSample9, CV_BGR2HSV);
  HsvImage I9(imageSample9);
  IplImage* imageSample10 = cvLoadImage("20110805_032957.jpg");
  cvSetImageROI(imageSample10, cvRect(0,0,imageSample10->height/0.5,imageSample10->width/1.83));
  cvCvtColor(imageSample10, imageSample10, CV_BGR2HSV);
  HsvImage I10(imageSample10);
  fprintf(stderr,"\n Grab water samples\n");
  //grab water samples from each picture
  for (int i=0; i < threeArraySize; i++)
  {
  	fprintf(stderr,"\n patch is pink (this is for me to know where the ground patch sample is\n");
      for (int j=0; j < arraySize; j++)
      {
          row1 = ceil(X/1.2866)+ceil(X/5.237)+i+ceil(-X/3.534545455);
          row1 = x + i;
        	//if (row1 > X-1)
	          //  row1 = X-1;
          column1 = ceil(Y/7.0755)+ceil(Y/21.01622)+j+ceil(X/1.495384615);
         // if (column1 > Y-1)
          	//	column1 = Y-1;
          averageHue = (I1[row1][column1].h + I2[row1][column1].h + I3[row1][column1].h + I4[row1][column1].h + I5[row1][column1].h + 	
          I6[row1][column1].h + I7[row1][column1].h + I8[row1][column1].h + I9[row1][column1].h + I10[row1][column1].h) / 10;
          averageSat = (I1[row1][column1].s + I2[row1][column1].s + I3[row1][column1].s + I4[row1][column1].s + I5[row1][column1].s + 
          I6[row1][column1].s + I7[row1][column1].s + I8[row1][column1].s + I9[row1][column1].s + I10[row1][column1].s) / 10;
          averageVal = (I1[row1][column1].v + I2[row1][column1].v + I3[row1][column1].v + I4[row1][column1].v + I5[row1][column1].v + 
          I6[row1][column1].v + I7[row1][column1].v + I8[row1][column1].v + I9[row1][column1].v + I10[row1][column1].v) / 10;   
          fprintf(stderr,"\n water patch sample (n X n matrix)\n");
          cvmSet(waterTrainingHue,i,j,averageHue);
          cvmSet(waterTrainingSat,i,j,averageSat);
          cvmSet(waterTrainingVal,i,j,averageVal);  
          fprintf(stderr,"\n patch is red (this is for me to know where the ground patch sample is\n");
          //I[row1][column1].h = 0;
          //I[row1][column1].s = 255;
          //I[row1][column1].v = 255;
      }
  }
  
  fprintf(stderr,"\n Order water samples in ascending\n");
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
	
	/***********************************************************************/
  //Grab a random patch of water below the horizon and compare every other
  //pixel against it
  //The results of the water detection depend on where in the picture the
  //training samples are located. Maybe adding more training samples will
  //help improve this?
  fprintf(stderr,"\n Random patch\n");
  // Main loop. It traverses through the picture
  skyX = 0; 
  skyY = 0;
  
  //The distance formula calculated by plotting points is given by:
  /*********** distance = 0.0006994144*(1.011716711^x)     *****************/
  //cout << "Distance: " << distancePixels << endl;
  fprintf(stderr,"\n Painting water red!!!!!\n");
  while (x < boatFront->height/3)//(x < boatFront->height/1.158)
  {
      //get a random sample taken from the picture. Must be determined whether
      //it is water or ground
      for (int i = 0; i<pixelsNumber;i++)
      {
        //for (int j=0;j<pixelsNumber;j++)
        //{
        if(y<=Y-5){
          cvmSet(sampleHue,0,i,I[x][y+i].h);
          cvmSet(sampleSat,0,i,I[x][y+i].s);
          cvmSet(sampleVal,0,i,I[x][y+i].v);
        //}
        }
        else fprintf(stderr,"\n Reached the end of big Y\n");
      //}
      //Find the shortest distance between a pixel and the neighbors from each of
      //the training samples (sort of inefficient, but might do the job...sometimes)
      //if (ix == pixelsNumber-1)
      //{
          //HSV for water sample
          // learn classifier
              for (int i=0;i<pixelsNumber;i++)
              {
                  for (int j=0;j<pixelsNumber;j++)
                  {
                      if ((minH <= cvmGet(sampleHue,0,j)) || (maxH >= cvmGet(sampleHue,0,j)))
                          //mark water samples as green
                          comparator[0] = 1;
                      else
                          comparator[0] = 0;
                      if (((minS <= cvmGet(sampleSat,0,j)) || (maxS <= cvmGet(sampleSat,0,j))))
                      //mark water samples as green
                          comparator[1] = 1;
                      else
                          comparator[1] = 0;
                      if ((minV <= cvmGet(sampleVal,0,j)) || (maxV <= cvmGet(sampleVal,0,j)))
                      //mark water samples as green
                          comparator[2] = 1;
                      else
                          comparator[2] = 0;
                      //count votes
                      for (int i3=0; i3 < 3; i3++)
                          votesSum = votesSum + comparator[i3];
                      //sky detection 
                      if (votesSum > 1) //&& ((sampleSat[i][j] - sampleVal[i][j]) <= 0.1*180)
                      {
                      // classify pixel as water 
                          I[x-pixelsNumber+i][y-pixelsNumber+j].h = 0;
                          I[x-pixelsNumber+i][y-pixelsNumber+j].s = 0;
                          I[x-pixelsNumber+i][y-pixelsNumber+j].v = 0;
                          pixelCount++;
                          //fprintf(stderr,"\n Pixel Painted %d\n");
                          //ROS_INFO("pixel count %d", pixelCount);
                          
                      }
                      votesSum = 0;
                        //ix = 0;
         	 						for(int i = 0; i < 3; i++)
    									{   
    	    							comparator[i] = 0;
    									}
                  }
          }
          if (y < Y-1)
              y = y + pixelsNumber-1;
          /** edge pixels ***
          if (y > Y-1)
              y = Y-1;
              	****/
          else //if (y == Y-1)
          {
              x = x + pixelsNumber-1; /// Because the image comes in flipped
              y = pixelsNumber-1;
          }
        }
    }
  //traverse through the image one more time, divide the image in grids of
    // 500x500 pixels, and see how many pixels of water are in each grid. If
    // most of the pixels are labeled water, then mark all the other pixels
    // as water as well    
    //int counter = 0;
    int xDivisor = 100;
    int yDivisor = 100;
    votesSum = 0;
    column1 = 0;
    row1 = 0;
    x = ceil(boatFront->height/10);
    //obstacleDistance = 0;
    y = 0;
    int counter = 0;
    
    //The problem lies somewhere below this line!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    while (x < boatFront->height/3)//(x < boatFront->height/1.158)
    {
        //get a random sample taken from the picture. Must be determined whether
        //it is water or ground
        for (int i = 0; i < ceil(boatFront->height/xDivisor); i++)
        {
            for(int j = 0; j < ceil(boatFront->width/yDivisor); j++)
            {
                cvmSet(resampleHue,i,j,I[x+i][y+j].h);
                cvmSet(resampleSat,i,j,I[x+i][y+j].s);
                cvmSet(resampleVal,i,j,I[x+i][y+j].v);
                if(cvmGet(resampleHue,i,j)==0 && cvmGet(resampleSat,i,j)==0 && cvmGet(resampleVal,i,j)==0)
                {
                    votesSum++;
                }
            }
        }
        if (votesSum > ((boatFront->height/xDivisor)*(boatFront->width/yDivisor)*(7.5/9)))
        {   
        // if bigger than 4/5 the total number of pixels in a square, then consider the entire thing as water  
        // We might need to use other smaller quantities (like 5/6 maybe?)
            for (int i = 0; i < ceil(boatFront->height/xDivisor);i++)
            {
                for (int j = 0; j < ceil(boatFront->width/yDivisor); j++)
                {
                    row1 = x + i;
                    if (row1 > X-1)
                        row1 = X-1;
                    column1 = y+j;
                    I[row1][column1].h = 0;
                    I[row1][column1].s = 255;
                    I[row1][column1].v = 255;
                }
            }
        }
        else
        {   
        // If not water, eliminate all red pixels and turn those pixels
        // back to the original color. These pixels shall, then, be marked
        // as obstacles
            for (int i = 0; i < ceil(boatFront->height/xDivisor);i++)
            {
                for (int j = 0; j < ceil(boatFront->width/yDivisor); j++)
                {
                    row1 = x + i;
                    if (row1 > X-1)
                        row1 = X-1;
                    column1 = y+j;
                    if (column1 > Y-1)
                        column1 = Y-1;
                    //the darker the color, the closer the object to the boat
                    //I[row1][column1].h = 128;    
                    //I[row1][column1].s = 255;   
                    //I[row1][column1].v = 255 - counter;
                    I[row1][column1].h = IBackUp[row1][column1].h;
                    I[row1][column1].s = IBackUp[row1][column1].s;
                    I[row1][column1].v = IBackUp[row1][column1].v;
                    //counter = counter + 20;
                }
            }
            //The distance formula calculated by plotting points is given by:
    /***********  distance = (1.76e-11)*pow(pixels,3.99)  *****************/
    /***********  pixel = (513.9332077469)pow(distance,0.240675506  *****************/
    
            // Convert from pixel distance to normal distance in meters
            /*if(obstacleDistance > sqrt(pow(xObstacleDistance,2) + pow(yObstacleDistance,2)))
            {
                // x,y coordinates of the obstacle
                xObstacleDistance = (1.76e-11)*pow(((boatFront->height/xDivisor)+x)/2, 3.99) ;
                yObstacleDistance = (1.76e-11)*pow(((boatFront->width/yDivisor)+y)/2, 3.99);
                //xWaypoint_msg = xObstacleDistance;
                //yWaypoint_msg = yObstacleDistance;
                //publish position data
                //waypoint_info_pub.publish(xWaypoint_msg);
                //waypoint_info_pub.publish(yWaypoint_msg);
                //ROS_INFO("Obstacle coordinates: X = %f meters, Y = %f meters", xObstacleDistance, yObstacleDistance);  
                obstacleDistance = sqrt(pow(xObstacleDistance,2) + pow(yObstacleDistance,2));
                //ROS_INFO("Obstacle distance from: %f", obstacleDistance);
            }
            //cout << "Distance to Obstacle is: " << obstacleDistance << endl << endl;
         */   
        }
        y = y + boatFront->width/xDivisor;
        if (y > Y-1)
        {
            x = x + boatFront->height/yDivisor;
            y = 0;
            counter = counter + 30;
        }
        votesSum = 0;
        
    }
    
  fprintf(stderr,"\n About to color\n");
  cvCvtColor(boatFront, boatFront, CV_HSV2BGR);
  cvCvtColor(backUpImage, backUpImage, CV_HSV2BGR);
  /**************************************************************************/
	try
  {
  	fprintf(stderr,"\n boatFront\n");
    cvShowImage("Boat Front", boatFront);
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
  //while(ros::ok())
  //{
  	cvNamedWindow("Boat Front",0);            //0 to maintains sizes regardless of image size
  	cvResizeWindow("Boat Front",700,550);     // new width/heigh in pixels
  
  	//This is used for republishing the waypoints for the obstacles
    
  
  	ROS_INFO("getting image");
  	cvStartWindowThread();
  	image_transport::ImageTransport it(nh);
  	//image/compressed
  	//camera/image
  	image_transport::Subscriber sub = it.subscribe("image", 1, imageCallback);
  	//sleep(4);
  	fprintf(stderr,"\n I am out of the callbakc function\n");
  	ros::spin();
  	cvDestroyWindow("Boat Front");
  	loop_rate.sleep();
  //}
  	//while(1);
   return 0;
}
