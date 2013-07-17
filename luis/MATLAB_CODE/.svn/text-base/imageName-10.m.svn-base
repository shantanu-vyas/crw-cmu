function y = imageName(image)
%% Obstacle detection on the water surface
% image is the photo file to be read
%I = imread(image);
[X Y] = size(image);
%figure, imshow(image);

%% Crop the image to the ROI
cropped = imcrop(image,[0 550 Y Y/9.5]);
%iptsetpref ImshowBorder Light
figure, imshow(cropped);

%% Convert from RGB to HSV to control the brightness of the objects.
% work with reflexion
cropped = rgb2hsv(cropped);
[X Y] = size(cropped);
I = cropped;
for i = X:-1:1          
    for j = Y/3:-1:1
        % look on how bright the object is
        %if (I(i,j,1) ~= I(X/2,(Y/3)-10,1) && I(i,j,2) ~= I(X/2,(Y/3)-100,2) && I(i,j,3) ~= I(X/2,(Y/3)-10,3))
            %I(i,j,1) = 0;     %H (color)
            %I(i,j,2) = 0;     %S (color intensity)
            I(i,j,3) = 0;       %V (brightness)
        %else
         %   I(i,j,1) = 100;
          %  I(i,j,2) = 255;
           % I(i,j,3) = 90;
        %end
    end
end
cropped = I;
%cropped = hsv2rgb(cropped);
figure, imshow(cropped);

%% Segmenting image with a theshhold
obstacles = rgb2gray(cropped);
obstacles = wiener2(obstacles, [10 3]);
threshold = 50;            %based on object brightness/darkness
%threshold = graythresh(obstacles);
obstacles = im2bw(obstacles, threshold/255); %3.8
%figure, imshow(obstacles);
%eliminate from the picture anything fewer than 200 pixels
obstacles = bwareaopen(obstacles, 60);
%finding edges with the 'canny'
obstacles = edge(obstacles, 'canny', 80/255);
% Do a "hole fill" to get rid of any background pixels inside the blobs.
obstacles = imfill(obstacles, 'holes');
%figure, imshow(obstacles); 

% fill a gap in the pen's cap
%se = strel('disk',5);
%obstacles = imclose(obstacles,se);

%% Clean Image from objects touching borders
%obstacles = imclearborder(obstacles);
%figure, imshow(obstacles);

%% Find Obstacles
% bwboundaries() returns a cell array, where each cell contains the row/column coordinates for the obstacle in the image.
[B,L] = bwboundaries(obstacles,'noholes');
numRegions = max(L(:))
% Label each blob with a specific color.
labeledImage = bwlabeln(L, 8);
figure, imshow(label2rgb(labeledImage));

%% Region Extraction - connected region properties
stats = regionprops(L,'all');

%% Analyse Eccentricity and Area
% May have to look for objects of different shapes.
% for that may add other props other than 'Eccentricity' and 'Area'
eccentricityShape = [stats.Eccentricity];
areaShape = [stats.Area];
eccNum = find(eccentricityShape < 1);
areaNum = find(areaShape > 80);

%% Display selected images
% Display region boundaries over cropped images
% bound obstacles of certain areas and eccentricity
for index=1:length(areaNum)
    bound = B{areaNum(index)};
    line(bound(:,2),bound(:,1),'Color','r','LineWidth',2)
end
% bound obstacles of certain eccentricity
%for index=1:length(eccNum)
 %   bound = B{eccNum(index)};
  %  line(bound(:,2),bound(:,1),'Color','r','LineWidth',2)
%end