function [ data ] = loadVehicleLog( filename )
%LOADVEHICLELOG Loads a vehicle's log and converts it to a data matrix
%   
%   DATA = LOADVEHICLELOG( FILENAME )
%
%   FILENAME = the name of the logfile to load
%   DATA = a matrix containing time, 2D position, and sensor readings
%           in the form [time, x, y, theta, cond, diel, temp]
%

% Open the log file
fid = fopen(filename, 'r');
lines = textscan(fid, '%s', 'delimiter', '\n');
fclose(fid);

% Set starting pose
pose = zeros(3);

% Start with empty data array
data = [];

% Get each line from file
for i = 1:length(lines{1})
    tline = lines{1}{i};
    
    % Split log file into pieces
    datum = regexp(tline,'[ {:\[\]\,}]+','split');
    
    % Ignore short data strings
    if (length(datum) >= 11)
    
        % Look for the commands of interest
        result = strcmp(datum(6), {'POSE', 'TE'});

        % Handle pose messages by updating location
        if (result(1))
            pose = [str2double(datum(7)), str2double(datum(8)), str2double(datum(13))];
        end

        % Handle sensor messages by adding a new data entry
        if (result(2))
            data = [data; [...
                    str2double(datum(1)), ... % time
                    pose, ... % current pose
                    str2double(datum(8)), ... % conductivity?
                    str2double(datum(9)), ... % dielectric?
                    str2double(datum(10)) ... % temperature
                ]]; %#ok<AGROW>
        end
    end
end

end

