function y = imageName(image)
%% Obstacle detection on the water surface
% image is the photo file to be read
%I = imread(image);
[X Y] = size(image);
%figure, imshow(image);

%% Crop the image to the ROI
%cropped = imcrop(image,[0 950 Y Y/9.5]);
% image,x ,y , width of box, height of box
cropped = imcrop(image,[0 0 Y Y/5.5]);
[X Y] = size(cropped);
%iptsetpref ImshowBorder Light
%figure, imshow(cropped);

n = 30;      %number of items for an n x n matrix
% these variables are use to travers the picture by blocks of n x n pixels at
% a time. 
% Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
% right way, of course)
x = X/2 + n; y = n; ki = x-n; kj = 1;   
% ground sample
groundTraining = zeros(n);
% water sample
waterTraining = zeros(n);
% n x n sample patch taken from the picture
sample = zeros(n);

counter = 0;

%% Evaluate and find the horizon line in the picture. This will gives us a
% divition where above the horizon is (most likely) to be ground, and
% below is (most likely) to be water
% draw a white line that will represent the horizon
%for j = Y/3:-1:1
 %   cropped(X/2,j,2) = 0.1;
%end        

%% Convert from RGB to HSV to control the brightness of the objects.
% work with reflexion
cropped = rgb2hsv(cropped);
%figure, imshow(cropped);
%for i = 300:-1:1               % X (more or less) = 820 pixels
%   for j = Y/3:-1:1
        %cropped(i,j,2) = 0;
        %cropped(i,j,3) = 1;
    %end
%end 
I = cropped;
for i = X:-1:1          
    for j = Y/3:-1:1
        % look on how bright the object is
        if I(i,j,3) > 0.7
        %The HSV values vary between 0 and 1
            %I(i,j,1) = 0;     %H (color)
            %I(i,j,2) = 0.1;     %S (color intensity)
            I(i,j,3) = 0.8;       %V (brightness)
        end
    end
end
%cropped = I;
%cropped = hsv2rgb(cropped);
%figure, imshow(cropped);

%% Grab a random patch of water below the horizon and compare every other
% pixel against it
% water patch sample (n X n matirx)
waterTraining(1:n,1:n) = cropped(1099-n+(1:n),1099-n+(1:n),1);
%waterTraining(i,j,2) = cropped(i+949,j+949,2);
%waterTraining(i,j,3) = cropped(i+949,j+949,3);
        
%patch is green (this is for me to know where the water patch sample is)
cropped(1099-n+(1:n),1099-n+(1:n),1) = 85/255;
cropped(1099-n+(1:n),1099-n+(1:n),2) = 255/255;
cropped(1099-n+(1:n),1099-n+(1:n),3) = 128/255;
% water patch sample (n X n matirx)
groundTraining(1:n,1:n) = cropped(299-30+(1:n),999-30+(1:n),1);
%groundTraining(i,j,2) = cropped(i+149,j+849,2);
%groundTraining(i,j,3) = cropped(i+149,j+849,3);
        
%patch is red (this is for me to know where the ground patch sample is)
cropped(299-30+(1:n),999-30+(1:n),1) = 0;
cropped(299-30+(1:n),999-30+(1:n),2) = 1;
cropped(299-30+(1:n),999-30+(1:n),3) = 1;
% Main loop. It traverses through the picture
[X Y] = size(cropped);
 
while ((ki < X)) 
    %get a random sample
    % random sample taken from the picture. Must be determined whether
    % is is water or ground
    % Only using the Hue value from the picture
    %sample(i1,j1) = cropped(ki+i1-1,kj+j1,1);
    sample(1:n,1:n) = cropped(ki+(1:n),kj+(1:n),1);
    %sample(i,j,2) = cropped(i+100,j+100,2);
    %sample(i,j,3) = cropped(i+100,j+100,3);
    %ki = x; kj = y;
    %size(sample)
    
    %{
    
    %[xs,ys] = size(sample)
    % Find the shortest distance between a pixel and the neighbors from each of
    % the training samples (sort of inefficient, but might do the job...sometimes)
    [~, waterDistance] = kNearestNeighbors(waterTraining,sample,n);
    %waterSum = sum(waterDistance);
    %waterMinDis = min(waterDistance);
    [~, groundDistance] = kNearestNeighbors(groundTraining,sample,n);
    %[x y] = size(groundDistance);
    %[x y] = size(waterDistance);
    %groundSum = sum(groundDistance);
    %groundMinDis = min(groundDistance);
    %groundDistance
    %waterDistance
    for i2 = 1:n
        for j2 = 1:n
            if groundDistance(i2,j2) > waterDistance(i2,j2)
                % mark water samples as green
                cropped(ki+1+i2,kj+1+j2,1) = 85/255;
                cropped(ki+1+i2,kj+1+j2,2) = 1;
                cropped(ki+1+i2,kj+1+j2,3) = 128/255;
            %else
                % red otherwise
             %   cropped(ki+1+i2,kj+1+j2,1) = 0;
              %  cropped(ki+1+i2,kj+1+j2,2) = 1;
               % cropped(ki+1+i2,kj+1+j2,3) = 1;
            end
        end
    end 
    % iteration goes from left-right, top-bottom
    %ki = x - n; kj = y - n;
    
    %}
    
    if ((ki == x) && (kj == y) && (y < Y) && (x < X))
        y = y + n;
        %x = n;
        ki = x - n;
        kj = y - n;
    elseif ((ki == x) && (kj == y) && (x < X) && (y < Y))
        y = n;
        x = x + n;
        ki = x - n;
        kj = 1;
   % else
    %    ki = X;
     %   kj = Y;
    end
    counter = counter + 1        
end

cropped = hsv2rgb(cropped);
figure, imshow(cropped);

%% Segmenting image with a theshhold
obstacles = rgb2gray(cropped);
%filter the image to reduce some noise/reflectance?
obstacles = wiener2(obstacles, [2 1]);
threshold = 50;            %based on object brightness/darkness
%threshold = graythresh(obstacles);
%figure, imshow(obstacles);
%finding edges with the 'canny'
%obstacles = edge(obstacles, 'canny', 60/255, 1.5);
% Do a "hole fill" to get rid of any background pixels inside the blobs.
obstacles = imfill(obstacles, 'holes');
%figure, imshow(obstacles); 
%eliminate from the picture anything fewer than 200 pixels
obstacles = bwareaopen(obstacles, 50);

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
%labeledImage = bwlabeln(L, 8);
%figure, imshow(label2rgb(labeledImage));

%% Region Extraction - connected region properties
stats = regionprops(L,'all');

%% Analyse Eccentricity and Area
% May have to look for objects of different shapes.
% for that may add other props other than 'Eccentricity' and 'Area'
eccentricityShape = [stats.Eccentricity];
areaShape = [stats.Area];
eccNum = find(eccentricityShape < 1);
areaNum = find(areaShape > 100);

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