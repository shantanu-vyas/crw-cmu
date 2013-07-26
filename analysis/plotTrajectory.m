function [] = plotTrajectory( data, multiplier )
%PLOTTRAJECTORY Creates an animation of the boat motion over time
%
%   [] = PLOTTRAJECTORY( DATA, MULTIPLIER )
%
%   DATA = a matrix containing time, 2D position, and sensor readings
%           in the form [time, x, y, theta, cond, diel, temp]
%   MULTIPLIER = time multiplier, where a value of 1.0 is real time, higher
%           than 1.0 is faster than real time, and lower than 1.0 is 
%           correspondingly slower than real time.
%

% Ignore pre-GPS locations
validTime = (data(:,2) ~= 0) & (data(:,3) ~= 0);
data = data(validTime, :);

% Create a new figure
figure;
hold on;
xlabel( 'Easting' );
ylabel( 'Northing' );

% Plot position as a circle with a line coming out
plot( data(1,2), data(1,3), 'bo' );
plot( [data(1,2), data(1,2) + cos(data(1,4))], ...
    [data(1,3), data(1,3) + sin(data(1,4))], ...
    'r-' );
time = data(1,1);
title(['Time = ' num2str(time)]);
axis([min(data(:,2)) - 5, max(data(:,2)) + 5, min(data(:,3)) - 5, max(data(:,3)) + 5]);

% Plot new locations in real time
for i = 1:size(data,1);
    pause((data(i,1) - time)./1000./multiplier);
    time = data(i,1);
    disp(time);
    
    plot( data(i,2), data(i,3), 'bo' );
    plot( [data(i,2), data(i,2) + cos(data(i,4))], ...
            [data(i,3), data(i,3) + sin(data(i,4))], ...
            'r-' );
    title(['Time = ' num2str(time)]);
end

end

