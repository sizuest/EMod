%% KIENZLE MAIN
% ═════════════════════════════════════════════════════════════════════════
% Calculation of the kienzle parameters for all three direction based on
% measurement data
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% Please set the tasks to be performed inside the settings section of the
% file

%% Settings
global MEASDIR;
baseDir    = MEASDIR;
rawDataDir = '/Kraft/rawdata/';
dataDir    = '/Kraft/data/';
configDir  = '/Kraft/config/';

saveDir    = '/KienzleParameters/';

% measurmentSets = {'2011-12-05','2011-12-09'};
measurmentSets = {'2011-12-09'};


doPreprocessRawData = 0;
doMeasurmentConfig  = 0;
doDataEvaluation    = 0;
doPlotting          = 0;
doKienzleParameter  = 1;

%% Add as path
addpath('/home/simon/Dropbox/MT Simon Züst/Matlab/KienzleParameters');

%% Preprocessing

data = cell(1,length(measurmentSets));

if doPreprocessRawData
    for i=1:length(measurmentSets)
        data{i} = preprocessRawForceData(measurmentSets{i},...
                                    [baseDir measurmentSets{i} rawDataDir] , ...
                                    [baseDir measurmentSets{i} dataDir], ...
                                    {'AP', 'F', 'VC'},{'ap', 'f', 'vc'});
    end
else
    for i=1:length(measurmentSets)
       tmp = load([baseDir measurmentSets{i} dataDir 'Data.mat']);
       data{i} = tmp.data;
    end
end

clear i;

%% Measurment Configuration

config = cell(1,length(measurmentSets));

if doMeasurmentConfig
    for i=1:length(measurmentSets)
        config{i} = forceMeasurmentData(data{i},...
                                   [baseDir measurmentSets{i} configDir], ...
                                   {'X', 'startTime', 'stopTime', 'useForKienzle'});
    end
else
    for i=1:length(measurmentSets)
        % Load CSV
        tmp    = csvread([baseDir measurmentSets{i} configDir 'Config.csv']);
        % Get field names
        fields = forceMeasurmentData([],[],{'X', 'startTime', 'stopTime', 'useForKienzle'},1);
        % Make a cell
        tmp = mat2cell(tmp, ones(1,length(fields)), ones(1,length(data{i})));
        % Make a struct
        config{i} = cell2struct(tmp, fields);
    end
end


%% Evaluate Data

if doDataEvaluation
    for i=1:length(measurmentSets)
        data{i} = EvaluateData(data{i}, config{i}, ...
                               [baseDir measurmentSets{i} dataDir]);
    end
end

%% Kienzle Parameters

if doKienzleParameter
    [k_c11 z] = ParameterIdentification(data, config, [baseDir saveDir]);
else
    k_c11 = load([baseDir saveDir 'k_c11.mat']);
    z     = load([baseDir saveDir 'z.mat']);
    k_c11 = k_c11.k_c11;
    z     = z.z;
end

%% Plot

plots = {};

if doPlotting
    plots = PlotData(data, config, k_c11, z, [baseDir saveDir]);
end



