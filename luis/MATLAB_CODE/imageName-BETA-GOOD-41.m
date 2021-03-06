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
figure, imshow(cropped);

n = 20;      % number of items for an n x n matrix
             % if n gets changed, then the algorthm might have to be
             % recalibrated. Try to keep it constant

% these variables are use to travers the picture by blocks of n x n pixels at
% a time. 
% Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
% right way, of course)
% x and y are the dimensions of the local patch of pixels
x = X/2+n+100; y = n; ix = 1; iy = 1;  
% M controls the x axis (up and down); N controls the y axis (left and
% right)
Mw = -50; Nw = 300; Mg = -300; Ng = 0;
% ground sample
groundTrainingHue = zeros(n);
groundTrainingSat = zeros(n);
groundTrainingVal = zeros(n);
% water sample
waterTrainingHue = zeros(n);
waterTrainingSat = zeros(n);
waterTrainingVal = zeros(n);
% n x n sample patch taken from the picture
sampleHue = zeros(n);
sampleSat = zeros(n);
sampleVal = zeros(n);
resampleHue = zeros(n);
resampleSat = zeros(n);
resampleVal = zeros(n);

% Stores the vote for each channel (whether it belongs to water or not
% 1 is part of water, 0 not part of water
% if sum of votes is bigger than 1, then it belongs to water
comparator = [0,0,0];

%counter = 0;

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
%{
I = cropped;
for i = X:-1:1          
    for j = Y/3:-1:1
        % look on how bright the object is
        if ((cropped(i,j,3) > 0.7) && (cropped(i,j,2) < 0.24)) 
        %The HSV values vary between 0 and 1
            %cropped(i,j,1) = 85/255;     %H (color)
            cropped(i,j,2) = 0.3;         %S (color intensity)
            cropped(i,j,3) = 0.6;         %V (brightness)
        end
    end
end
cropped = I;
%}
%cropped = hsv2rgb(cropped);
%figure, imshow(cropped);

%% Grab a random patch of water below the horizon and compare every other
% pixel against it
% The results of the water detection depend on where in the picture the
% training samples are located. Maybe adding more training samples will
% help improve this?

% water patch sample (n X n matirx)
waterTrainingHue(1:3*n,1:n) = cropped(round(X/1.2866)+round(X/5.237)+(1:3*n) + Mw,round(Y/7.0755)+round(Y/21.01622)+(1:n) + Nw,1);
waterTrainingSat(1:3*n,1:n) = cropped(round(X/1.2866)+round(X/5.237)+(1:3*n) + Mw,round(Y/7.0755)+round(Y/21.01622)+(1:n) + Nw,2);
waterTrainingVal(1:3*n,1:n) = cropped(round(X/1.2866)+round(X/5.237)+(1:3*n) + Mw,round(Y/7.0755)+round(Y/21.01622)+(1:n) + Nw,3);        
%patch is green (this is for me to know where the water patch sample is)
cropped(round(X/1.286624)+round(X/5.237)+(1:3*n) + Mw,round(Y/7.07552)+round(Y/21.01622)+(1:n) + Nw,1) = 85/255;
cropped(round(X/1.286624)+round(X/5.237)+(1:3*n) + Mw,round(Y/7.07552)+round(Y/21.01622)+(1:n) + Nw,2) = 255/255;
cropped(round(X/1.286624)+round(X/5.237)+(1:3*n) + Mw,round(Y/7.07552)+round(Y/21.01622)+(1:n) + Nw,3) = 128/255;

% ground patch sample (n X n matirx)
% Detecting the horizon in the picture might be an excellent visual aid to
% choose where (above the horaizon) you can take a ground training sample
% from. The ground pixel sample can be at a constant distance from the
% horizon
groundTrainingHue(1:3*n,1:n) = cropped(round(X/4.7291)+round(X/8.3176)+(1:3*n) + Mg,round(Y/7.78378)+round(Y/16.54468)+(1:n) + Ng,1);
groundTrainingSat(1:3*n,1:n) = cropped(round(X/4.7291)+round(X/8.3176)+(1:3*n) + Mg,round(Y/7.78378)+round(Y/16.54468)+(1:n) + Ng,2);
groundTrainingVal(1:3*n,1:n) = cropped(round(X/4.7291)+round(X/8.3176)+(1:3*n) + Mg,round(Y/7.78378)+round(Y/16.54468)+(1:n) + Ng,3);        
%patch is red (this is for me to know where the ground patch sample is)
cropped(round(X/4.7291)+round(X/8.3176)+(1:3*n) + Mg,round(Y/7.78378)+round(Y/16.54468)+(1:n) + Ng,1) = 0;
cropped(round(X/4.7291)+round(X/8.3176)+(1:3*n) + Mg,round(Y/7.78378)+round(Y/16.54468)+(1:n) + Ng,2) = 1;
cropped(round(X/4.7291)+round(X/8.3176)+(1:3*n) + Mg,round(Y/7.78378)+round(Y/16.54468)+(1:n) + Ng,3) = 1;

% Main loop. It traverses through the picture
%
while (x <= X) 
    %get a random sample
    % random sample taken from the picture. Must be determined whether
    % is is water or ground
    % Only using the Hue value from the picture
    % random sample taken from the picture. Must be determined whether
    % is water or ground
    sampleHue(ix,iy:n) = cropped(x-n+ix,(y-n+iy):y,1);
    sampleSat(ix,iy:n) = cropped(x-n+ix,(y-n+iy):y,2);
    sampleVal(ix,iy:n) = cropped(x-n+ix,(y-n+iy):y,3);
    % Find the shortest distance between a pixel and the neighbors from each of
    % the training samples (sort of inefficient, but might do the job...sometimes)
    if ((ix == n))
        %HSV for water sample
        [~, waterDistanceHue] = kNearestNeighbors(waterTrainingHue,sampleHue,n);
        [~, waterDistanceSat] = kNearestNeighbors(waterTrainingSat,sampleSat,n);
        [~, waterDistanceVal] = kNearestNeighbors(waterTrainingVal,sampleVal,n);
        %HSV for ground sample
        [~, groundDistanceHue] = kNearestNeighbors(groundTrainingHue,sampleHue,n);
        [~, groundDistanceSat] = kNearestNeighbors(groundTrainingSat,sampleSat,n);
        [~, groundDistanceVal] = kNearestNeighbors(groundTrainingVal,sampleVal,n);
        %waterDistance
        for i = 1:n
            for j = 1:n
                if ((groundDistanceHue(i,j) > waterDistanceHue(i,j))) 
                    % mark water samples as green
                    comparator(1,1) = 1;
                else
                    comparator(1,1) = 0;
                end
                if ((groundDistanceSat(i,j) > waterDistanceSat(i,j)))% && (sampleSat(i,j) <= 0.2))
                    % mark water samples as green
                    comparator(1,2) = 1;
                else
                    comparator(1,2) = 0;
                end
                
                if ((groundDistanceVal(i,j) < waterDistanceVal(i,j)))% && (sampleVal(i,j) >= 0.7))
                    % mark water samples as green
                    comparator(1,3) = 1;
                else
                    comparator(1,3) = 0;
                end
                if sum(comparator,2) > 1
                % classify pixel as water
                    %if resampleHue(n,n) == 0
                        resampleHue(i,j) = sampleHue(i,j);
                    %end
                    %if resampleSat(n,n) == 0
                        resampleSat(i,j) = sampleSat(i,j);
                    %end
                    %if resampleVal(n,n) == 0
                        resampleVal(i,j) = sampleVal(i,j);
                    %end
                    cropped(x-n+i,y-n+j,1) = 85/255;
                    cropped(x-n+i,y-n+j,2) = 1;
                    cropped(x-n+i,y-n+j,3) = 128/255;
                end 
                %elseif (((cropped(x-n+i,y-n+j,3) >= 0.7) && (cropped(x-n+i,y-n+j,2) <= 0.24)) && (groundDistance1(i,j) > waterDistance1(i,j))) 
                 %   cropped(x-n+i,y-n+j,1) = 85/255;
                  %  cropped(x-n+i,y-n+j,2) = 1;
                   % cropped(x-n+i,y-n+j,3) = 128/255;
                %else
                    %red otherwise
                 %   cropped(x-n+i,y-n+j,1) = 0;
                  %  cropped(x-n+i,y-n+j,2) = 1;
                   % cropped(x-n+i,y-n+j,3) = 1;
            end
        end
    end     
    % iteration goes from left-right, top-bottom
    if ix < n
       ix = ix + 1;
    else
        if (y < Y/3)
            y = y + n;
            if y > Y/3
                y = Y/3;
            end
        elseif (y == Y/3)
            x = x + n;
            y = n;
        end
        ix = 1;
    end
    %counter = counter + 1        
end

% use a set of data  that has been confirmed as water to resample everthing
waterTrainingHue = resampleHue;
waterTrainingSat = resampleSat;
waterTrainingVal = resampleVal;
x = X/2+n+100; y = n; ix = 1; iy = 1;
while (x <= X) 
    %get a random sample
    % random sample taken from the picture. Must be determined whether
    % is is water or ground
    % Only using the Hue value from the picture
    % random sample taken from the picture. Must be determined whether
    % is water or ground
    sampleHue(ix,iy:n) = cropped(x-n+ix,(y-n+iy):y,1);
    sampleSat(ix,iy:n) = cropped(x-n+ix,(y-n+iy):y,2);
    sampleVal(ix,iy:n) = cropped(x-n+ix,(y-n+iy):y,3);
    % Find the shortest distance between a pixel and the neighbors from each of
    % the training samples (sort of inefficient, but might do the job...sometimes)
    if ((ix == n))
        %HSV for water sample
        [~, waterDistanceHue] = kNearestNeighbors(waterTrainingHue,sampleHue,n);
        [~, waterDistanceSat] = kNearestNeighbors(waterTrainingSat,sampleSat,n);
        [~, waterDistanceVal] = kNearestNeighbors(waterTrainingVal,sampleVal,n);
        %HSV for ground sample
        [~, groundDistanceHue] = kNearestNeighbors(groundTrainingHue,sampleHue,n);
        [~, groundDistanceSat] = kNearestNeighbors(groundTrainingSat,sampleSat,n);
        [~, groundDistanceVal] = kNearestNeighbors(groundTrainingVal,sampleVal,n);
        %waterDistance
        for i = 1:n
            for j = 1:n
                % if sample has not been classified as water...
                if ((sampleHue(i,j)~=85/255) && (sampleSat(i,j)~=1) && (sampleVal(i,j)~=128/255))
                    if ((groundDistanceHue(i,j) > waterDistanceHue(i,j))) 
                        % mark water samples as green
                        comparator(1,1) = 1;
                    else
                        comparator(1,1) = 0;
                    end
                    if ((groundDistanceSat(i,j) > waterDistanceSat(i,j)))% && (sampleSat(i,j) <= 0.2))
                        % mark water samples as green
                        comparator(1,2) = 1;
                    else
                        comparator(1,2) = 0;
                    end
                
                    if ((groundDistanceVal(i,j) < waterDistanceVal(i,j)))% && (sampleVal(i,j) >= 0.7))
                        % mark water samples as green
                        comparator(1,3) = 1;
                    else
                        comparator(1,3) = 0;
                    end
                    if sum(comparator,2) > 1
                        cropped(x-n+i,y-n+j,1) = 85/255;
                        cropped(x-n+i,y-n+j,2) = 1;
                        cropped(x-n+i,y-n+j,3) = 128/255;
                    end 
                    %elseif (((cropped(x-n+i,y-n+j,3) >= 0.7) && (cropped(x-n+i,y-n+j,2) <= 0.24)) && (groundDistance1(i,j) > waterDistance1(i,j))) 
                    %   cropped(x-n+i,y-n+j,1) = 85/255;
                    %  cropped(x-n+i,y-n+j,2) = 1;
                    % cropped(x-n+i,y-n+j,3) = 128/255;
                    %else
                        %red otherwise
                    %   cropped(x-n+i,y-n+j,1) = 0;
                    %  cropped(x-n+i,y-n+j,2) = 1;
                    % cropped(x-n+i,y-n+j,3) = 1;
                end
            end
        end
    end     
    % iteration goes from left-right, top-bottom
    if ix < n
       ix = ix + 1;
    else
        if (y < Y/3)
            y = y + n;
            if y > Y/3
                y = Y/3;
            end
        elseif (y == Y/3)
            x = x + n;
            y = n;
        end
        ix = 1;
    end
    %counter = counter + 1        
end
%}
cropped = hsv2rgb(cropped);
figure, imshow(cropped);

%{
%% Segmenting image with a theshhold
obstacles = rgb2gray(cropped);
%filter the image to reduce some noise/reflectance?
obstacles = wiener2(obstacles, [2 1]);
threshold = 50;            %based on object brightness/darkness
%threshold = graythresh(obstacles);
%finding edges with the 'canny'
%obstacles = edge(obstacles, 'canny', 60/255, 1.5);
% Do a "hole fill" to get rid of any background pixels inside the blobs.
obstacles = imfill(obstacles, 'holes');
%eliminate from the picture anything fewer than 200 pixels
obstacles = bwareaopen(obstacles, 50);

%% Clean Image from objects touching borders
%obstacles = imclearborder(obstacles);

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
%}