%% Thermal Measurment
% ═════════════════════════════════════════════════════════════════════════
% Reads in thermal measurement raw data
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════

%% Path
baseDir    = '/home/simon/Dropbox/MT Simon Züst/Testbench/Messungen/Schaublin/42LTMI3/';

% Force
rawForceDataDir = '/Kraft/rawdata/';
forceDataDir    = '/Kraft/data/';
forceConfigDir  = '/Kraft/config/';

% Temperature
rawTemperatureDataDir = '/Temperatur/rawdata/';
temperatureDataDir    = '/Temperatur/data/';
temperatureConfigDir  = '/Temperatur/config/';

saveDir    = '/ThermalMeasurment/';


%% Settings
measurmentSets = {'2011-12-15'};

doPreprocessRawData = 0;
doDriftCorrection   = 0;
doChirpRemove       = 0;
doMeasurmentConfig  = 0;
doDataEvaluation    = 0;
doPlotting          = 1;

noForce = 1;

%% Add as path
addpath('/home/simon/Dropbox/MT Simon Züst/Matlab/ThermalMeasurment');

%% Preprocessing

% Force Data
data.force       = cell(1,length(measurmentSets));
% Temperature Data
data.temperature = cell(1,length(measurmentSets));


if doPreprocessRawData
    for i=1:length(measurmentSets)
        data.force{i} = preprocessRawForceData(measurmentSets{i},...
                                    [baseDir measurmentSets{i} rawForceDataDir] , ...
                                    [baseDir measurmentSets{i} forceDataDir], ...
                                    {'U', 'R', 'VC', 'L', 'X'},{'U', 'R', 'vc', 'l', 'X'});
        data.temperature{i} = preprocessRawTemperatureData(...
                                    [baseDir measurmentSets{i} rawTemperatureDataDir] , ...
                                    [baseDir measurmentSets{i} temperatureDataDir]);
    end
else
    for i=1:length(measurmentSets)
        % Force
        if noForce
            tmp.data = {};
        else
            tmp = load([baseDir measurmentSets{i} forceDataDir 'Data.mat']);
        end
        data.force{i} = tmp.data;
        % Temperature
        tmp = load([baseDir measurmentSets{i} temperatureDataDir 'Data.mat']);
        data.temperature{i} = tmp.data;
    end
end

% Temperature data.force


clear i;

%% Drift Correction

if doDriftCorrection
    for i=1:length(measurmentSets)
        data.force{i} = ForceDirftCorrection(data.force{i}, ...
                                             [baseDir measurmentSets{i} forceDataDir]);
    end
end

%% Chirp remove

if doChirpRemove
    for i=1:length(measurmentSets)
        data.temperature{i} = ChirpRemove(data.temperature{i}, ...
                                         [baseDir measurmentSets{i} temperatureDataDir], ...
                                         0.1, 0.001);
    end
end



%% Measurment Configuration

config = cell(1,length(measurmentSets));

if doMeasurmentConfig
    for i=1:length(measurmentSets)
        config{i} = forceMeasurmentData(data.force{i},...
                                   [baseDir measurmentSets{i} forceConfigDir], ...
                                   {'startTime', 'stopTime', 'useForEvaluation'});
    end
elseif ~noForce
    for i=1:length(measurmentSets)
        % Load CSV
        tmp    = csvread([baseDir measurmentSets{i} forceConfigDir 'Config.csv']);
        % Get field names
        fields = forceMeasurmentData([],[],{'startTime', 'stopTime', 'useForEvaluation'},1);
        % Make a cell
        tmp = mat2cell(tmp, ones(1,length(fields)), ones(1,length(data.force{i})));
        % Make a struct
        config{i} = cell2struct(tmp, fields);
    end
end


%% Evaluate Data

if doDataEvaluation
    
end

%% Plot

plots = {};

if doPlotting
 
    time = [0; cumsum(ceil(diff(data.temperature{1,1}.time)/100))];
    
    values = data.temperature{1,1}.values;
    
    ticks = find(diff(time)>2)+[0 1];
    
    values(ticks(1):ticks(2),:) = NaN;
    
    gap   = round(5400/mean(diff(data.temperature{1,1}.time)));
    ticks = [ 0:gap:ticks(1), ticks(2):gap:length(data.temperature{1,1}.time) ]+1;
    
    ticks = unique(sort(ticks));
    
    plot(time, values);
    set(gca, 'XTick', time(ticks), 'XTickLabel', ceil(data.temperature{1,1}.time(ticks)/3600) );
    legend(data.temperature{1,1}.names)
    
    xlabel('time [h]');
    ylabel('\vartheta [°C]');
    
    grid on;
end



