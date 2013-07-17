function y = imageName(image)

%% traverses through the image pixels. Trying to look for intensity
% differences. Also, trying the identify where the boat image begins in the
% picture
I = imread('boatObstacle.jpg');
[X Y] = size(I);
%figure, imshow(I);

%% First detect the boat (know where it ends).
% assume water color is 90-98-100 RGB (or +/- that range)
%work with reflexion
for i = X/2:X/1.42          
    for j = 1:Y/3
        
        % the third element in the array is the channel
        %if pixel is different than previous, then thats the boat
        %if (I(i,j,1)<90)&&(I(i,j,2)<80)&&(I(i,j,3)<100)
        if (I(i,j,1) < 90) && (I(i,j,2) < 98) && (I(i,j,3) < 100)
            I(i,j,1)=1;
             I(i,j,2)=1;
              I(i,j,3)=1;
        else
             I(i,j,1)=100;
             I(i,j,2)=255;
              I(i,j,3)=90;
        end
    end
end

%% Detect things in front of the boat (like water and obstacles)
% Put a mask to everything else the boat shall not worry about
figure, imshow(I);
%I = rgb2gray(I);

%BW2 = edge(I,'canny',0.5);
%figure, imshow(BW2);