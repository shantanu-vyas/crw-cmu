function y = imageName(image)

%% Obstacle detection on the water surface
% image is the file to be read
%I = imread(image);
I = image;
[X Y] = size(I);
%figure, imshow(I);

%% Crop the image to the ROI
cropped = imcrop(image,[0 550 Y Y/9.50]);
figure, imshow(cropped);

%% First detect the boat (know where it ends).
% assume water color is 90-98-100 RGB (or +/- that range)
% work with reflexion
[X Y] = size(cropped);
I = cropped;
for i = X/2:X          
    for j = 1:Y/3
        
        %the third element in the array is the channel
        %if pixel is different than previous, then thats the boat
        %if (I(i,j,1)<90)&&(I(i,j,2)<80)&&(I(i,j,3)<100)
        %if (I(i,j,1) < 90) && (I(i,j,2) < 98) && (I(i,j,3) < 100)
            %I(i,j,1) = 1;
            %I(i,j,2) = 1;
           % I(i,j,3) = 1;
        %else
         %   I(i,j,1) = 100;
          %  I(i,j,2) = 255;
           % I(i,j,3) = 90;
        %end
    end
end

%cropped = I;
%figure, imshow(cropped);

%% Segmenting image by thelhold
threshold = 200;
obstacles = im2bw(cropped, 0.38);
%figure, imshow(obstacles);
obstacles = bwareaopen(obstacles,100);
figure, imshow(obstacles);

%% Find Obstacles
[B,L] = bwboundaries(obstacles, 'noholes');
numRegions = max(L(:));
imshow(label2rgb(L));

%% Detect things in front of the boat (like water and obstacles)
% Put a mask to everything else the boat shall not worry about
%figure, imshow(I);
%I = rgb2gray(I);
%% only transform the section where the obstacles are
%for i = X/2:X/1.42          
 %   for j = 1:Y/3
        % transform each pixel to from RGB to HSV
  %      I(i,j,1) = rgb2hsv(I(i,j,1));
   %     I(i,j,2) = rgb2hsv(I(i,j,2));
    %    I(i,j,3) = rgb2hsv(I(i,j,3));
    %end
%end
%cropped = rgb2hsv(obstacles);
%% Detect edges/obstacles
%BW2 = edge(I,'canny',0.5);
%figure, imshow(obstacles);