function [ data, times ] = loadFilterLog( filename )
%LOADFILTERDATA Loads data from Paul's filter.
%   
%   DATA = LOADFILTERDATA( FILENAME )
%
%   FILENAME = the name of the logfile to load
%   DATA = a matrix containing the data.
%

% Open the log file
fid = fopen(filename, 'r');
lines = textscan(fid, '%s', 'delimiter', '\n', 'bufsize', 65535);
fclose(fid);

% Start with empty data array
data = [];
times = [];

% Get each line from file
idx = 1;
while (idx < length(lines{1}))
    
    % Look for useful data
    while(idx < length(lines{1}))
        tline = lines{1}{idx}; idx = idx + 1;
        [obs, count] = sscanf(tline, 'Obs\tSensorTE\t%d\t%d\t%d\t%f\t%f\t%f\t%f\t%f');
    
        if (count == 8)
            break;
        end
    end
    
    
    % If data is not valid, ignore it
    while(idx < length(lines{1}))
        tline = lines{1}{idx}; idx = idx + 1;
        [~, badcount] = sscanf(tline, 'OUT OF EXTENT: %d %d %d %d');
        [bounds, goodcount] = sscanf(tline, 'Bounds for	%d	%d	%f - %f %f %f %f %f');
        
        if (goodcount == 8 || badcount > 0)
            break;
        end
    end
    if (badcount > 0)
        continue;
    end
    
    % Look for linear regression
    while(idx < length(lines{1}))
        tline = lines{1}{idx}; idx = idx + 1;
        [coeffs, count] = sscanf(tline, 'y   = %f * x + %f');
        
        if (count == 2)
            break;
        end
    end
    
    % Now look for gradient output
    while(idx < length(lines{1}))
        tline = lines{1}{idx}; idx = idx + 1;
        [filtgrads, count] = sscanf(tline, 'Gradient: SensorTE%dBoat@Unknown = %f for');

        if (count == 2)
            break;
        end
    end
    
    % Extract all the data variables
    i = obs(2) + 1;
    j = obs(3) + 1;
    m = obs(1) + 1;
    time = obs(8);
    lat = obs(4);
    long = obs(5);
    value = obs(6);
    grad = obs(7);
    lb = bounds(3);
    ub = bounds(4);
    regm = coeffs(1);
    regc = coeffs(2);
    filtgrad = filtgrads(2);
    
    % Generate a new time index if this is a new time
    timeidx = find(times == time);
    if (isempty(timeidx))
        timeidx = length(times) + 1;
        times(timeidx) = time;
    end
    
    % Insert the data into the matrix
    data(i,j,timeidx,m,:) = [time, lat, long, value, grad, lb, ub, regm, regc, filtgrad];
end



