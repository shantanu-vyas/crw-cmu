"MATLAB_CODE" folder:
      This MATLAB project is just used for algorithm testing and does not work with the robot. The attached images can be used
      as expamples to test the algortithm.
      To make it work just add all the files inside the MATLAB folder to a local MATLAB path to use it
      
"WaterRecognition" folder:
      This netbeans peject is used for algorithm testing as well, but by using the OpenCV (wich is the library we are
      using in ROS for image processing). The images attached with the folder are used as traning samples for the classifiers to learn how the
      water looks like. And then it compares this data with the new images taken on the fly in order to find water in the new images.
      To run the project just put the folder to the local workspace that you are. These images can also be used 
      using with netbeans and then compile it.
      
"learning_image_transport" folder:
      This is the main code that we use for image processing while running the boats. The file "my_subscriber.cpp"
      is the file used and it subscribe to the image compressed topic from the boat server. The rest of the files
      inside the "src" folder (like "my_publisher.cpp") are just used for testing and debugging.
      The images attached with the folder are used as traning samples for the classifiers to learn how the
      water looks like. And then it compares this data with the new images taken on the fly in order to find water in the new images.
      To use this subscriber, just put the "learning_image_transport" folder inside the "stacks" folder that is 
      located in your ROS folder. Then in the terminal run the command:
      
      rosrun learning_image_transport my_subscriber _image_transport:=compressed
      
      where "learning_image_transport" is the folder, "my_subcriber" is the .cpp file we are running our program from,
      and "_image_transport:=compressed" tells "my_subscriber" to subscribe to the topic "image/compressed" which publishes
      the images taken by the boat. And these are "compressed" images.

      
