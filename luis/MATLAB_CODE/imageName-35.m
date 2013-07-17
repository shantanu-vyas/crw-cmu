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

n = 30;      %number of items for an n x n matrix
% these variables are use to travers the picture by blocks of n x n pixels at
% a time. 
% Index(0,0) does not exist, so make sure kj and ki start from 1 (in the
% right way, of course)
% x and y are the dimensions of the local patch of pixels
x = X/2 + n; y = n; ix = 1; iy = 1;  
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
        if ((I(i,j,3) > 0.7)) %&& (I(i,j,2) < 0.24)) 
        %The HSV values vary between 0 and 1
            %I(i,j,1) = 85/255;     %H (color)
            %I(i,j,2) = 0.3;     %S (color intensity)
            I(i,j,3) = 0.8;       %V (brightness)
        end
    end
end
cropped = I;
%cropped = hsv2rgb(cropped);
%figure, imshow(cropped);

%% Grab a random patch of water below the horizon and compare every other
% pixel against it
% water patch sample (n X n matirx)
waterTraining(1:n,1:n) = cropped(round(X/1.2866)+200-30+(1:n),round(Y/7.0755)-30-100+(1:n),1);
%waterTraining(i,j,2) = cropped(i+949,j+949,2);
%waterTraining(i,j,3) = cropped(i+949,j+949,3);
        
%patch is green (this is for me to know where the water patch sample is)
cropped(round(X/1.286624)-30+200+(1:n),round(Y/7.07552)-30-100+(1:n),1) = 85/255;
cropped(round(X/1.286624)-30+200+(1:n),round(Y/7.07552)-30-100+(1:n),2) = 255/255;
cropped(round(X/1.286624)-30+200+(1:n),round(Y/7.07552)-30-100+(1:n),3) = 128/255;
% water patch sample (n X n matirx)
groundTraining(1:n,1:n) = cropped(round(X/4.7291)-50-30+(1:n),round(Y/7.78378)-30+(1:n),1);
%groundTraining(i,j,2) = cropped(i+149,j+849,2);
%groundTraining(i,j,3) = cropped(i+149,j+849,3);
        
%patch is red (this is for me to know where the ground patch sample is)
cropped(round(X/4.7291)-30-50+(1:n),round(Y/7.78378)-30+(1:n),1) = 0;
cropped(round(X/4.7291)-30-50+(1:n),round(Y/7.78378)-30+(1:n),2) = 1;
cropped(round(X/4.7291)-30-50+(1:n),round(Y/7.78378)-30+(1:n),3) = 1;

% Main loop. It traverses through the picture
%
while (x <= X) 
    %get a random sample
    % random sample taken from the picture. Must be determined whether
    % is is water or ground
    % Only using the Hue value from the picture
    % random sample taken from the picture. Must be determined whether
    % is water or ground
    sample(ix,iy:n) = cropped(x-n+ix,(y-n+iy):y,1);
    % Find the shortest distance between a pixel and the neighbors from each of
    % the training samples (sort of inefficient, but might do the job...sometimes)
    if ((ix == n))
        [~, waterDistance] = kNearestNeighbors(waterTraining,sample,n);
        %waterSum = sum(waterDistance);
        %waterMinDis = min(waterDistance);
        [~, groundDistance] = kNearestNeighbors(groundTraining,sample,n);
        %waterDistance
        for i = 1:n
            for j = 1:n
                if groundDistance(i,j) > waterDistance(i,j)
                    % mark water samples as green
                    cropped(x-n+i,y-n+j,1) = 85/255;
                    cropped(x-n+i,y-n+j,2) = 1;
                    cropped(x-n+i,y-n+j,3) = 128/255;
                %else
                    %red otherwise
                    %cropped(x-n+i,y-n+j,1) = 0;
                    %cropped(x-n+i,y-n+j,2) = 1;
                    %cropped(x-n+i,y-n+j,3) = 1;
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