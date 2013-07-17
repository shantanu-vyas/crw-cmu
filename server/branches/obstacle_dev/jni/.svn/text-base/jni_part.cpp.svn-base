#include <jni.h>

#include <opencv2/core/core.hpp>

#include <opencv2/imgproc/imgproc.hpp>

#include <opencv2/features2d/features2d.hpp>

#include "opencv2/video/video.hpp"

#include "opencv2/highgui/highgui.hpp"

#include "opencv2/flann/flann.hpp"

#include "opencv2/nonfree/nonfree.hpp"

#include <vector>

#include <cv.h>

#include <math.h>

#include "string.h"

#include <sys/time.h>

#include <android/log.h>

#define APPNAME "AirboatServer"

using namespace std;

using namespace cv;

extern "C" {

double desiredWidth = 320;//480;//320;//320;//225; //210;//225//240;//480;
double desiredHeight = 240;//320;//240;//240;//150; //140;//150//120;//320;//120->reflections are not detected (height is too small)
double nativeWidth = 720; //720;
double nativeHeight = 480; //480;
const int SZ = 300;//800;//680;//3400;//800//195;// 450;//323; //78;//90;
int N = 5; //30;//reset after N frames
vector<Point2f> nextPts;
vector<Point2f> prevPts;
vector<uchar> stats;
vector<float> err;
string msg;
int rightTrajCounter = 0, leftTrajCounter = 0, middleTrajCounter = 0;
float Xs = 0, Ys = 0;
float prevXs = 0, prevYs = 0;
//for reflection detection
OrbFeatureDetector orbdetector(100, 2.0f, 1, 7/*3*/, 0, 2, 1, 7/*3*/);//40, 2.0f, 1, 3, 0, 2, 1, 3);
OrbDescriptorExtractor orbextractor;
vector<DMatch> matches_refl;
vector<KeyPoint> upperKP, lowerKP;
Mat upper_desc, lower_desc;
int moveDecision = 0;
jintArray obstacles;
Mat mbgra;
Mat gs_cropped_old;
vector<KeyPoint> corners2;
vector<KeyPoint> corners2_next;
char buffer[50];
vector<Point2f> alltrajectories[SZ];
uchar validTrajectories[SZ];
int trajLen[SZ];
vector<Mat> frames;
Mat gs_cropped, upper, lower;
float startROI,endROI;
int motionDecision=0;
int lastMotionDecision=0;
GFTTDetector gfttd(SZ, 0.01, 2, 5);
BFMatcher matcher(NORM_HAMMING2);
Mat temp;
Mat gs, gs_orig;
vector<Point2f> upperleftBins;
vector<Point2f> lowerrightBins;
int const B=12;
int binCounts[B*4];
int obstaclesL;
int obstaclesM;
int obstaclesR;
bool avoidingManeuver=false;
int binSizeW, binSizeH;
vector<Rect> waterPatches;
int memoryObstacle = 0; //-1 left, +1 right, 0 forward is clear
//vector<int> obstacle_memory;

//PARAMETERS TO TWEAK///////////////
int binObstacleTH=5;
int startROIbias=10;
double histCompTh=0.5;
double histCompTh2=0.85;
int move_fwdTH=2;//try 3
int move_turnTH=3;//try 3
double reflTH=110.0;
///////////////////////////////////

Mat computeHistogram(Mat src)
{
	  Mat hsv;
	  cvtColor(src, hsv, CV_BGR2HSV);

	  /// Using 30 bins for hue and 32 for saturation
	  int h_bins = 30/*50*/; int s_bins = 32/*60*/;
	  int histSize[] = { h_bins, s_bins };

	  // hue varies from 0 to 256, saturation from 0 to 180
	  float h_ranges[] = { 0, 256 };
	  float s_ranges[] = { 0, 180 };

	  const float* ranges[] = { h_ranges, s_ranges };

	  // Use the 0-th and 1-st channels
	  int channels[] = { 0, 1 };

	  /// Histograms
	  MatND hist;

	  /// Calculate the histograms for the HSV images
	  calcHist( &hsv, 1, channels, Mat(), hist, 2, histSize, ranges, true, false );
	  normalize( hist, hist, 0, 1, NORM_MINMAX, -1, Mat() );

	  return hist;
}

void doColorHistogramComparison(Mat mbgra)
{
     binSizeW = /*80*/(desiredWidth/12)/desiredWidth * nativeWidth;
     binSizeH = ((desiredHeight-startROI-endROI)/4) /desiredHeight * nativeHeight;
     vector<MatND> waterHistograms;
     vector<MatND> otherHistograms;
     vector<Rect>  otherHistogramsRegions;
     waterPatches.clear();
     __android_log_write(ANDROID_LOG_INFO, "VOO", "initializing vars");
     for(int i=0; i<B; i++)//12x40 or 6x80
     {
 	      //water histograms
 	      if(i>=4 && i<=7)
 	      {
 	    	  Mat patch_other1 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight), binSizeW, binSizeH));
			  MatND hist01 = computeHistogram(patch_other1);

			  Mat patch_other2 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH, binSizeW, binSizeH));
			  MatND hist02 = computeHistogram(patch_other2);

			  Mat patch_other3 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH, binSizeW, binSizeH));
			  MatND hist03 = computeHistogram(patch_other3);

			  otherHistograms.push_back(hist01);
			  otherHistograms.push_back(hist02);
			  otherHistograms.push_back(hist03);
			  otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight), binSizeW, binSizeH));
			  otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH, binSizeW, binSizeH));
			  otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH, binSizeW, binSizeH));

			  //water patches
// 	    	  Mat patch_water = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH, binSizeW, binSizeH));
// 	    	  MatND hist1 = computeHistogram(patch_water);

 	    	  Mat patch_water2 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH, binSizeW, binSizeH));
 	    	  MatND hist2 = computeHistogram(patch_water2);

// 	    	  waterHistograms.push_back(hist1);
 	    	  waterHistograms.push_back(hist2);
// 	    	  waterPatches.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH, binSizeW, binSizeH));
 	    	  waterPatches.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH, binSizeW, binSizeH));
 	      }
 	      else
 	      {
 	    	  Mat patch1 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight), binSizeW, binSizeH));
			  MatND hist1 = computeHistogram(patch1);

			  Mat patch2 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH, binSizeW, binSizeH));
			  MatND hist2 = computeHistogram(patch2);

			  Mat patch3 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH, binSizeW, binSizeH));
			  MatND hist3 = computeHistogram(patch3);

			  otherHistograms.push_back(hist1);
			  otherHistograms.push_back(hist2);
			  otherHistograms.push_back(hist3);
			  otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight), binSizeW, binSizeH));
			  otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH, binSizeW, binSizeH));
			  otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH, binSizeW, binSizeH));

//			  if(i==2 || i==3 || i==8 || i==9)
//			  {
//				  Mat patch4 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH, binSizeW, binSizeH));
//				  MatND hist4 = computeHistogram(patch4);
//				  waterHistograms.push_back(hist4);
//				  waterPatches.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH, binSizeW, binSizeH));
//				  //otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH, binSizeW, binSizeH));
//			  }
//			  else
//			  {
				  Mat patch4 = mbgra(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH, binSizeW, binSizeH));
				  MatND hist4 = computeHistogram(patch4);
				  otherHistograms.push_back(hist4);
				  otherHistogramsRegions.push_back(Rect(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH, binSizeW, binSizeH));
//			  }
 	      }
     }
     __android_log_write(ANDROID_LOG_INFO, "VOO", "done computing histograms");

     //check if reflections lie in patches. If so, then count patch as water patches because reflections should only be on water regions.
     for(int i=0; i<otherHistograms.size(); i++)
     {
    	 //check if reflections are in the histogram region. if so then do count as water patch
    	 Rect region = otherHistogramsRegions[i];
    	 bool reflection = false;
    	 for(int r=0; r<matches_refl.size(); r++)
    	 {
    		DMatch dm = matches_refl[r];
			if (dm.distance < reflTH)//120)
			{
				Point pp2 = Point2f((lowerKP[dm.trainIdx].pt.x),
								(((lowerKP[dm.trainIdx].pt.y + nativeHeight/2+1))));
				if((pp2.x > region.x && pp2.x < (region.x+region.width))
						&& (pp2.y > region.y && pp2.y<(region.y+region.height)))
				{
					  rectangle(mbgra, Point2f(region.x, region.y), Point2f(region.x+region.width, region.y+region.height),Scalar(255,0,0,100), 15);
					  waterPatches.push_back(region);
					  reflection=true;
				}
					//reflection=true;
			}
    	 }

    	 if(reflection)
    		 continue;
//    	 if(reflection)
//    	 {
//			  Rect patch = otherHistogramsRegions[i];
//			  rectangle(mbgra, Point2f(patch.x, patch.y), Point2f(patch.x+patch.width, patch.y+patch.height),Scalar(255,0,0,100), 15);
//			  waterPatches.push_back(patch);
//    		 continue;
//    	 }

		 MatND hist1 = otherHistograms[i];

		  double res = 0.0;
		  double minRes = 20;
		  __android_log_print(ANDROID_LOG_INFO, "VOO", "}}}}}} water patches size = %d", waterHistograms.size());
		  __android_log_print(ANDROID_LOG_INFO, "VOO", "}}}}}} other patches size = %d", otherHistograms.size());
		  for(int j=0; j<waterHistograms.size(); j++)
		  {
			  MatND hist2 = waterHistograms[j];
			  res = compareHist(hist1, hist2, CV_COMP_BHATTACHARYYA);
			  if(res<minRes)
				  minRes = res;
		  }
		  res = minRes;///= waterHistograms.size();

		  if(res < histCompTh)//0.65)//the less the better (the more correlation) - i.e water patch
		  {
			  Rect patch = otherHistogramsRegions[i];
			  rectangle(mbgra, Point2f(patch.x, patch.y), Point2f(patch.x+patch.width, patch.y+patch.height),Scalar(255,0,0,100), 15);
			  waterPatches.push_back(patch);
		  }
		  else if(res > histCompTh2) //mark as obstacle if threshold is HIGH, like >0.9 (only in middle region)
		  {
//			  if(i<16)
//				  obstaclesL++;
//			  else
			  if(i>=16 && i<=23)
				  obstaclesM++;
//			  else if(i>23)
//				  obstaclesR++;

			  Rect patch = otherHistogramsRegions[i];
			  rectangle(mbgra, Point2f(patch.x, patch.y), Point2f(patch.x+patch.width, patch.y+patch.height),Scalar(255,255,0,200), 4);
		  }
     }
}

void renderAndComputeGrid(Mat mbgra, int framenum)
{
//	if(framenum==0)
//	{
		upperleftBins.clear();
		lowerrightBins.clear();
//	}

  __android_log_write(ANDROID_LOG_INFO, "VOO", "display grids");
  for(int i=0; i<B; i++)//12x40 or 6x80
  {
	  rectangle(mbgra, Point2f(i*binSizeW,startROI/desiredHeight * nativeHeight),
				Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH), Scalar(255,0,255,255), 2);
	  rectangle(mbgra, Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH),
			Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH), Scalar(255,0,255,255), 2);

	  if(i>=4 && i<=7)
	  {
		  rectangle(mbgra, Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH),
					Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH), Scalar(0,0,255,255), 2);
		  rectangle(mbgra, Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH),
		  					Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+4*binSizeH), Scalar(0,0,255,255), 2);
	  }
	  else
	  {
		  rectangle(mbgra, Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH),
							Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH), Scalar(255,0,255,255), 2);
		  rectangle(mbgra, Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH),
		 							Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+4*binSizeH), Scalar(255,0,255,255), 2);
	  }
//	  if(i>=2 && i<=9 )
//	  {
//		  rectangle(mbgra, Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH),
//							Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+4*binSizeH), Scalar(0,0,255,255), 2);
//	  }
//	  else
//	  {
//		  rectangle(mbgra, Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH),
//							Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+4*binSizeH), Scalar(255,0,255,255), 2);
//	  }
//
//	  if(framenum==0)
//	  {
		  upperleftBins.push_back(Point2f(i*binSizeW,startROI/desiredHeight * nativeHeight));
		  lowerrightBins.push_back(Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH));

		  upperleftBins.push_back(Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+binSizeH));
		  lowerrightBins.push_back(Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH));

		  upperleftBins.push_back(Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+2*binSizeH));
		  lowerrightBins.push_back(Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH));

		  upperleftBins.push_back(Point2f(i*binSizeW,(startROI/desiredHeight * nativeHeight)+3*binSizeH));
		  lowerrightBins.push_back(Point2f((i+1)*binSizeW,(startROI/desiredHeight * nativeHeight)+4*binSizeH));
//	  }
  }
	  __android_log_write(ANDROID_LOG_INFO, "VOO", "done displaying grids");
}

void setMSG()
{
   if(motionDecision==0)
		msg="do nothing (stop)";
	else if(motionDecision==1)
		msg="turn right";
	else if(motionDecision==2)
		msg="u-turn right";
	else if(motionDecision==3)
		msg="turn left";
	else if(motionDecision==4)
		msg="go forward";
	else if(motionDecision==5)
		msg="u-turn left";
}

void reinitTrajectories()
{
	for(int i=0; i<SZ; i++)
	{
		alltrajectories[i].clear();
		validTrajectories[i]=0;
		trajLen[i]=0;
	}
}

int obstacleAvoidance()
{
	for(int i=0; i<16; i=i+4)
	{
		obstaclesL+= binCounts[i];
		obstaclesL+= binCounts[i+1];//*2;//added weights to signify how close the obstacles are
		obstaclesL+= binCounts[i+2];//*2;
		obstaclesL+= binCounts[i+3]*2;
	}
	for(int i=16; i<32; i=i+4)
	{
		obstaclesM+= binCounts[i];
		obstaclesM+= binCounts[i+1];//*2;//weight increases inversely proportionally to depth
		obstaclesM+= binCounts[i+2];//*2;
		obstaclesM+= binCounts[i+3];//*2;
	}
	for(int i=32; i<48; i=i+4)
	{
		obstaclesR+= binCounts[i];
		obstaclesR+= binCounts[i+1];//*2;
		obstaclesR+= binCounts[i+2];//*2;
		obstaclesR+= binCounts[i+3]*2;
	}
	__android_log_print(ANDROID_LOG_INFO, "VOO", "OO: L:%d, M:%d, R:%d", obstaclesL, obstaclesM, obstaclesR);

	lastMotionDecision = motionDecision;
//	if(avoidingManeuver && (lastMotionDecision==1 || lastMotionDecision==3))// || motionDecision==2 || motionDecision==5))
//	{//if previous motion was a hard-turn avoidance maneuver then dont do anything
//		avoidingManeuver=false;
//		motionDecision=0;
//	}
//	else
	if(obstaclesM>move_fwdTH)
	{//if obstacles detected infront of boat, turn in the direction with least expected obstacles
		//avoidingManeuver=true;
		if(obstaclesR>obstaclesL)
		{
			motionDecision=3;//complete left turn
			memoryObstacle=1;
		}
		else
		{
			motionDecision=1;//complete right turn
			memoryObstacle=-1;//obstacle detected on left
		}
	}
	else if((obstaclesL-obstaclesR)>move_turnTH)// && obstaclesR<=1)
	{//if obstacles on left outweight the obstacles on the right, then turn right (half-turn)
		//avoidingManeuver=true;
		motionDecision=2;//half right
		memoryObstacle=-1;
	}
	else if((obstaclesR-obstaclesL)>move_turnTH)//(obstaclesR>=4 && obstaclesL<=1)
	{//if obstacles on right outweigh the obstacles on the left, then turn left (half-turn)
		//avoidingManeuver=true;
		motionDecision=5;//half left
		memoryObstacle=1;
	}
	else //just move forward
	{
		motionDecision=4;//forward
		memoryObstacle=0;
	}


	//with memory:
//	if(lastMotionDecision==motionDecision)
//		return motionDecision;
//	else
//		return 4;

	return motionDecision;


	//using obstacle memory:
	//push back -1, 0, +1 - depending on if obstacle on left, none, right, respectively
	//if two consecutive -1's or +1's are encountered then turn in opositte direction
	//or
	//if lastmotiondecision and current motion decision are the same then do that
}

void render(Mat mbgra)
{
    //rectangle(mbgra, Point(0,startROI/desiredHeight * nativeHeight),Point(nativeWidth, nativeHeight-1-(endROI/desiredHeight * nativeHeight)), Scalar(255,0,0,255),2);//roi
	//rectangle(mbgra, Point(0,startROI/desiredHeight * nativeHeight),Point(nativeWidth/3, nativeHeight-1-(endROI/desiredHeight * nativeHeight)), Scalar(255,0,0,255),2);//left roi
	//rectangle(mbgra, Point(nativeWidth*2/3,startROI/desiredHeight * nativeHeight),Point(nativeWidth, nativeHeight-1-(endROI/desiredHeight * nativeHeight)), Scalar(255,0,0,255),2);//right roi

    //for reflections upper and lower ROIs of image
	__android_log_write(ANDROID_LOG_INFO, "VOO", "dividing frame into upper and lower");
	rectangle(mbgra, Point(0,20),Point(nativeWidth, nativeHeight/2), Scalar(255,100,100,20),1);//roi
	rectangle(mbgra, Point(0,nativeHeight/2+1),Point(nativeWidth, nativeHeight), Scalar(255,100,100,50),1);//roi

	__android_log_write(ANDROID_LOG_INFO, "VOO", "display trajectories");
	for(int i=0; i<SZ; i++)
	{
		if(validTrajectories[i]==1)
		{
			vector<Point2f> traj = alltrajectories[i];
			for(int j=0; j<traj.size()/*trajLen[j]*/; j++)
				circle(mbgra, Point2f(traj[j].x / desiredWidth * nativeWidth, (traj[j].y+startROI)/desiredHeight * nativeHeight), 3, Scalar(0,255,0,255), 1);
		}
	}

	sprintf(buffer, "L:%d - M:%d - R:%d", obstaclesL, obstaclesM, obstaclesR);
	putText(mbgra, buffer, Point(20,50), CV_FONT_NORMAL, 2, Scalar(255,0,255,255), 2);
	sprintf(buffer, "startROIbias: %d - histTH: %.2f - movefwdTH: %d"/* - moveturnTH: %d"*/ , startROIbias, histCompTh, move_fwdTH);//, move_turnTH);
	putText(mbgra, buffer, Point(20,100), CV_FONT_NORMAL, 1, Scalar(255,0,255,255), 1);


	setMSG();

	putText(mbgra, msg, Point((desiredWidth/2-50)/desiredWidth * nativeWidth,(desiredHeight-15)/desiredHeight *nativeHeight), CV_FONT_NORMAL, 1, Scalar(255,0,0,255), 1);
}

void computeReflections(Mat mbgra)
{
	temp = Mat(gs_orig.cols, gs_orig.rows, CV_8UC1);
	gs_orig.copyTo(temp);
	upper = temp(Rect(0,20,nativeWidth, nativeHeight/2));
	__android_log_write(ANDROID_LOG_INFO, "VOO", "upper done");
	lower = temp(Rect(0,nativeHeight/2+1,nativeWidth, nativeHeight-nativeHeight/2-1));
	__android_log_write(ANDROID_LOG_INFO, "VOO", "lower done");
	__android_log_write(ANDROID_LOG_INFO, "VOO", "computing reflections");
	upperKP.clear();
	__android_log_write(ANDROID_LOG_INFO, "VOO", "computing upper orb features");
	orbdetector.detect(upper, upperKP);
	orbextractor.compute(upper, upperKP, upper_desc);
	lowerKP.clear();
	__android_log_write(ANDROID_LOG_INFO, "VOO", "computing lower orb features");
	orbdetector.detect(lower, lowerKP);
	orbextractor.compute(lower, lowerKP, lower_desc);
	if (upperKP.size() > 0 && lowerKP.size() > 0)
		matcher.match(upper_desc, lower_desc, matches_refl);

	for(int r=0; r<matches_refl.size();r++)
	{
		DMatch dm = matches_refl[r];
		if (dm.distance < reflTH)//120)
		{
			Point pp1 = Point2f((upperKP[dm.queryIdx].pt.x),
							(upperKP[dm.queryIdx].pt.y));
			Point pp2 = Point2f((lowerKP[dm.trainIdx].pt.x),
							(((lowerKP[dm.trainIdx].pt.y + nativeHeight/2+1))));
			if(abs(pp1.x - pp2.x) < 20)
			{
				circle(mbgra, pp2, 50, Scalar(255, 0, 255, 255), 3);
				circle(mbgra, pp1, 50, Scalar(255, 0, 255, 255), 3);
				line(mbgra, pp1, pp2, Scalar(255,0,255,255), 2);
				for(int t=0; t<SZ; t++)
				{
					if(validTrajectories[t]==1)
					{
						vector<Point2f> traj = alltrajectories[t];
						Point feature = Point(traj[traj.size()-1].x / desiredWidth * nativeWidth,
								(traj[traj.size()-1].y+startROI)/desiredHeight * nativeHeight);
						if (abs(feature.x-pp2.x) < 50 && abs(feature.y-(pp2.y)) < 50) //threshold to see if they are approximately vertical
						{
							__android_log_write(ANDROID_LOG_INFO, "VOO", ">>>>> REFL NEAR FEATURE - NULLIFYING!!!!!!!!!");
							validTrajectories[t]=0;
						}
					}
				}
			}
		}
	}
}

void pruneSmallTrajectories()
{
	for(int i=0; i<SZ; i++)
	{
		vector<Point2f> traj = alltrajectories[i];
		if(traj.size()<2)
			validTrajectories[i]=0;
	}
}

void countBins(Mat mbgra)
{
	for(int i=0; i<B*4; i++)
		binCounts[i]=0;

	for(int i=0; i<SZ; i++)
	{
		//remove features inside water patches
		if(validTrajectories[i]==1)
		{
			vector<Point2f> traj = alltrajectories[i];
			Point2f feature(traj[traj.size()-1].x / desiredWidth * nativeWidth, (traj[traj.size()-1].y+startROI) / desiredHeight * nativeHeight);
			for(int k=0; k<waterPatches.size(); k++)
			{
				Rect wpatch = waterPatches[k];
				if((feature.x > wpatch.x && feature.x < (wpatch.x+wpatch.width))
						&& (feature.y > wpatch.y && feature.y < (wpatch.y+wpatch.height)))
				{
					__android_log_write(ANDROID_LOG_INFO, "VOO", "found one!!!!!!!!!!!");
					validTrajectories[i]=0;
				}
			}
		}

		if(validTrajectories[i]==1)
		{
			vector<Point2f> traj = alltrajectories[i];
			Point2f feature(traj[traj.size()-1].x / desiredWidth * nativeWidth, (traj[traj.size()-1].y+startROI) / desiredHeight * nativeHeight);
			for(int j=0; j<B*4; j++)
			{
				if((feature.x > upperleftBins[j].x && feature.x < lowerrightBins[j].x) && (feature.y > upperleftBins[j].y && feature.y < lowerrightBins[j].y))
				{
					__android_log_print(ANDROID_LOG_INFO, "VOO", "%%%%%%%%% >>>>>>>>>>>       setting bin %d", traj.size());
					binCounts[j]+=traj.size();
				}
			}
		}
	}
	__android_log_write(ANDROID_LOG_INFO, "VOO", "done counting bins");
	__android_log_print(ANDROID_LOG_INFO, "VOO", "done counting bins %d, %d", upperleftBins.size(), lowerrightBins.size());

	for(int i=0; i<B*4; i++)
	{
		__android_log_print(ANDROID_LOG_INFO, "VOO", ">>>>>>> bin %d --> %d", i, binCounts[i]);
		if(binCounts[i]>=binObstacleTH)//movementThreshold)
		{
			__android_log_write(ANDROID_LOG_INFO, "VOO", "here");
			rectangle(mbgra, Point2f(upperleftBins[i].x, upperleftBins[i].y), Point2f(lowerrightBins[i].x, lowerrightBins[i].y), Scalar(255,255,0,200), 2);
			binCounts[i]=1;//to just add up the number of bins for motion deliberation
		}
		else
			binCounts[i]=0;//so that it is not used in obstacle avoidance
	}
}

JNIEXPORT jintArray JNICALL Java_edu_cmu_ri_airboat_server_AirboatObstAvoidanceView_FindFeatures(
		JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv,
		jintArray bgra, int framenum)
		{

    jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
    jint*  _bgra = env->GetIntArrayElements(bgra, 0);
    Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
    Mat mbgra(height, width, CV_8UC4, (unsigned char *)_bgra);
    //Please make attention about BGRA byte order
    //ARGB stored in java as int array becomes BGRA at native level

    cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);//convert image

    nativeWidth=width;
    nativeHeight=height;
    __android_log_print(ANDROID_LOG_INFO, "VOO", "read frame = %d, %d", width, height);
    gs = Mat(desiredHeight, desiredWidth, CV_8UC1);
    gs_orig = Mat(nativeHeight, nativeWidth, CV_8UC1);
    cvtColor(mbgra, gs_orig, CV_BGR2GRAY);
    resize(mbgra, gs, Size(desiredWidth, desiredHeight), 0, 0);
    startROI=desiredHeight/2+startROIbias;//-10;//-10;//60;
    endROI=20;
    gs_cropped = gs(Rect(0, startROI, desiredWidth, desiredHeight-startROI-endROI));
    frames.push_back(gs_cropped);
	__android_log_write(ANDROID_LOG_INFO, "VOO", "initializing trajectories");

	//initialize variables
	reinitTrajectories();

    //show grid and compute their locations
    renderAndComputeGrid(mbgra, framenum);

    obstaclesL=0; obstaclesM=0; obstaclesR=0;

    //if frames buffer is full, process them
    if(frames.size()==N)
    {
    	frames[0].copyTo(gs_cropped_old);
		gfttd.detect(frames[0], corners2);//good features to track
		for(int i=0; i<corners2.size(); i++)
			circle(mbgra, Point(corners2[i].pt.x / desiredWidth * nativeWidth, (corners2[i].pt.y+startROI)/desiredHeight * nativeHeight), 3, Scalar(0,0,255,255), 4);
		frames[0].copyTo(gs_cropped_old);

		__android_log_write(ANDROID_LOG_INFO, "VOO", "start loop");
		for(int i=1; i<N; i++)
		{
			frames[i].copyTo(gs_cropped);
			nextPts.clear(); prevPts.clear(); stats.clear(); err.clear();
			for(int i=0; i<corners2.size(); i++)
				prevPts.push_back(Point2f(corners2[i].pt.x, corners2[i].pt.y));

			__android_log_print(ANDROID_LOG_INFO, "VOO", "computing optical flow - prevPts size = %d", prevPts.size());
			if(prevPts.size()>0)
				calcOpticalFlowPyrLK(gs_cropped_old, gs_cropped, prevPts, nextPts, stats, err,
						Size(15, 15), 3,
						TermCriteria(TermCriteria::COUNT + TermCriteria::EPS, 5, 0.01));

			__android_log_write(ANDROID_LOG_INFO, "VOO", "pushing trajectories");
			for(int j=0; j<nextPts.size(); j++)
			{
				if(stats[j]==1 && err[j]<6.0)
				{
					alltrajectories[j].push_back(Point2f(nextPts[j].x, nextPts[j].y));
					validTrajectories[j]=1;
					trajLen[j]++;
				}
			}

			gs_cropped.copyTo(gs_cropped_old);
			corners2.clear();
		    for(int j=0; j<nextPts.size(); j++)
				corners2.push_back(KeyPoint(Point2f(nextPts[j].x, nextPts[j].y), 1));
		}

		__android_log_write(ANDROID_LOG_INFO, "VOO", "starting function calls");

		//compute reflections using ORB (rotation-invariant fast features)
		computeReflections(mbgra);

		//prune out small trajectories
		//pruneSmallTrajectories();

		//do color histogram comparison
		__android_log_write(ANDROID_LOG_INFO, "VOO", "histogram comparison");
		doColorHistogramComparison(mbgra);//determines whether bin is water or not (all in waterpatches vector)

		//count bins occupied by obstacles
		countBins(mbgra);//assigns 1 or 0 to bin corresponding to obstacle or free, respectively

		//motion deliberation
		motionDecision = obstacleAvoidance();

		//clear frames buffer
		frames.clear();
    }

    //render everything on to mbgra which is returned to JAVA code to be displayed
    render(mbgra);

	env->ReleaseIntArrayElements(bgra, _bgra, 0);
	env->ReleaseByteArrayElements(yuv, _yuv, 0);

////////////////////////////////////////////////////////////////////////////////////////////////
const int size = desiredWidth;
obstacles = env->NewIntArray(size);
if (obstacles == NULL)
	return NULL; /* out of memory error thrown */

// fill a temp structure to use to populate the java int array
jint fill[size];
fill[0] = motionDecision;
// move from the temp structure to the java structure
env->SetIntArrayRegion(obstacles, 0, size, fill);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////


return obstacles;

}

}
