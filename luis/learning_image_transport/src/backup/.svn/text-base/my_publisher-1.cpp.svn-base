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

#include <opencv/cvwimage.h>
#include <opencv/highgui.h>

#include <cv_bridge/CvBridge.h>


int main(int argc, char** argv)
{
  ros::init(argc, argv, "image_publisher");
  ros::NodeHandle nh;
  image_transport::ImageTransport it(nh);
  image_transport::Publisher pub = it.advertise("camera/image", 10);

  cv::WImageBuffer3_b image( cvLoadImage("farm.jpg", CV_LOAD_IMAGE_COLOR) );
  sensor_msgs::ImagePtr msg = sensor_msgs::CvBridge::cvToImgMsg(image.Ipl(), "bgr8");
  
  ros::Rate loop_rate(0.5);
  int counter = 1;
  while (nh.ok()) 
  {
  	ROS_INFO("%d", counter);
		if (counter == 1)
		  cv::WImageBuffer3_b image( cvLoadImage("farm.jpg", CV_LOAD_IMAGE_COLOR) );
		if (counter == 2)
			cv::WImageBuffer3_b image( cvLoadImage("farm.jpg", CV_LOAD_IMAGE_COLOR) );
		if (counter == 3)
			cv::WImageBuffer3_b image( cvLoadImage("farm.jpg", CV_LOAD_IMAGE_COLOR) );
		if (counter == 4)
			cv::WImageBuffer3_b image( cvLoadImage("farm.jpg", CV_LOAD_IMAGE_COLOR) );	
		counter++;
    if (counter > 4)
    	counter = 1;		
    sensor_msgs::ImagePtr msg = sensor_msgs::CvBridge::cvToImgMsg(image.Ipl(), "bgr8");
    //image_transport::Publisher pub = it.advertise("camera/image", 1);
    pub.publish(msg);
    ros::spinOnce();
    loop_rate.sleep();
  }
}








/*
int main(int argc, char** argv)
{
  ros::init(argc, argv, "image_publisher");
  ros::NodeHandle nh;
  image_transport::ImageTransport it(nh);
  //image_transport::Publisher pub = it.advertise("camera/image", 1);

  cv::WImageBuffer3_b image( cvLoadImage("crazy-obstacles.jpg", CV_LOAD_IMAGE_COLOR) );
  //cout << argv[1];  
  sensor_msgs::ImagePtr msg = sensor_msgs::CvBridge::cvToImgMsg(image.Ipl(), "bgr8");
  
  ros::Rate loop_rate(0.5);
  int counter = 1;
  while (nh.ok()) 
  {
    ROS_INFO("%d", counter);
		if (counter == 1)
		  cv::WImageBuffer3_b image( cvLoadImage("lilypad.jpg", CV_LOAD_IMAGE_COLOR) );
		if (counter == 2)
			cv::WImageBuffer3_b image( cvLoadImage("DSC_0135.JPG", CV_LOAD_IMAGE_COLOR) );
		if (counter == 3)
			cv::WImageBuffer3_b image( cvLoadImage("crazy-obstacles.jpg", CV_LOAD_IMAGE_COLOR) );
		if (counter == 4)
			cv::WImageBuffer3_b image( cvLoadImage("green-water.jpg", CV_LOAD_IMAGE_COLOR) );		
    sensor_msgs::ImagePtr msg = sensor_msgs::CvBridge::cvToImgMsg(image.Ipl(), "bgr8");
    image_transport::Publisher pub = it.advertise("camera/image", 5);
    pub.publish(msg);
    ros::spinOnce();
    loop_rate.sleep();
    counter++;
    if (counter > 4)
    	counter = 1;
  }
  return 0;
}*/
