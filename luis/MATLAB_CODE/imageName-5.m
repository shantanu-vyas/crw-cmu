function y = imageName(image)

%% Obstacle detection on the water surface
% image is the file to be read
%I = imread(image);
I = image;
[X Y] = size(I);
figure, imshow(I);

%% Crop the image to the ROI
cropped = imcrop(image,[0 550 Y Y/9.5]);
%iptsetpref ImshowBorder Light
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
        if (I(i,j,1) < 90) && (I(i,j,2) < 98) && (I(i,j,3) < 100)
            I(i,j,1) = 1;
            I(i,j,2) = 1;
            I(i,j,3) = 1;
        else
            I(i,j,1) = 100;
            I(i,j,2) = 255;
            I(i,j,3) = 90;
        end
    end
end

%cropped = I;
%figure, imshow(cropped);

%% Segmenting image with a theshhold
threshold = 200;
obstacles = im2bw(cropped, threshold/255); %3.8
%figure, imshow(obstacles);
%eliminate from the picture anything fewer than 200 pixels
obstacles = bwareaopen(obstacles,60);
% Do a "hole fill" to get rid of any background pixels inside the blobs.
obstacles = imfill(obstacles, 'holes');
%figure, imshow(obstacles);

%% Clean Image from objects touching borders
obstacles = imclearborder(obstacles);
%figure, imshow(obstacles);

%% Find Obstacles
% bwboundaries() returns a cell array, where each cell contains the row/column coordinates for the obstacle in the image.
[B,L] = bwboundaries(obstacles,'noholes');
numRegions = max(L(:))
% Label each blob with a specific color.
%labeledImage = bwlabel(L, 8);
%figure, imshow(label2rgb(labeledImage));

%% Region Extraction - connected region properties
stats = regionprops(L,'all');

%% Analyse Eccentricity
% May have to look for objects of different shapes.
% for that may add other props other than 'Eccentricity'
eccentricityShape = [stats.Eccentricity];
areaShape = [stats.Area];
eccNum = find(eccentricityShape < 1);
areaNum = find(areaShape > 60);

%% Display selected images
% Display region boundaries over cropped images
% bound obstacles of certain areas
for index=1:length(areaNum)
    bound = B{areaNum(index)};
    line(bound(:,2),bound(:,1),'Color','r','LineWidth',2)
end
% bound obstacles of certain eccentricity
%for index=1:length(eccNum)
 %   bound = B{eccNum(index)};
  %  line(bound(:,2),bound(:,1),'Color','k','LineWidth',2)
%end