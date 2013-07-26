function [] = plotSurface( data, axis )
%PLOTSURFACE Creates a plot of given vehicle sensor
%   [] = PLOTSURFACE( DATA, AXIS )
%
%   DATA = a matrix containing time, 2D position, and sensor readings
%           in the form [time, x, y, theta, cond, diel, temp]
%   AXIS = the index of the column of data to display, e.g.
%           5 = COND
%           6 = DIEL
%           7 = TEMP
%

labels = {'Time','Easting','Northing','Yaw','Conductivity','Dielectric','Temperature'};
validTime = (data(:,2) ~= 0) & (data(:,3) ~= 0);
data = data(validTime, :);

figure;
fo = fit( [data(:,2), data(:,3)], data(:,axis), 'linearinterp');
plot( fo );
%plot3(data(:,2), data(:,3), data(:,axis) );
xlabel( 'Easting' );
ylabel( 'Northing' );
zlabel( labels{axis} );

end

