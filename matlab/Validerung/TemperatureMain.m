function [] = TemperatureMain( setName )
% [] = TEMPERATUREYMAIN( SETNAME )
% ═════════════════════════════════════════════════════════════════════════
% Performs an analysis of simualtion data compared to measurement data, in
% order to provide the key values for model validation.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% For the sucessfull execution, two global variabels are required:
%
%   ─ HOMEDIR:  Root folder of the analysis
%
% The variable SETNAME referes to the folder in HOMEDIR name with the 
% following structure:
%
%   ─ SETNAME
%       ├─ temperature
%       │   ├─ measdata
%       │   │   └─ temperature.mat
%       │   ├─ plots [dir]
%       │   └─ config.m
%       └─ simdata
%           └─ simulation_output.dat
%
% Configuration of the analysis is done in the file "config.m". This file
% has to provide the following variables:
%
% tempDataStart             [num]       Start time for comparision
% tempDataStop              [num]       Stop time for comparision
% pairs                     [cell]      Cell array, each row has the
%                                       elements ID, Simulation,
%                                       Measurement, Name, Offset
%                                       see below for explanation
%
% The elements in the pairs cell have the following meaning:
%   ID:         Unique numerical identifier
%   Simulation: Name of the logged simulation signal in the from:
%               Component.Port (e.g.: Spindle.Torque)
%   Measurment:
%   Name:       User defined name. Used for plot titles and image saving
%   Offset:     Offset factor for the measurment. [°C->K]
%
% The generated plots and result are stored in the folder "plots"


%% NO CHANGES NEED FROM HERE!

%% Add path
addpath('.');


%% Path
% Creat std path

% Home and measurement directory
global HOMEDIR;

if isempty(HOMEDIR)
    fprintf('[!] HOMEDIR not set!\n');
    return 
end

simFile = [HOMEDIR '/' setName '/simdata/simulation_output.dat'];
plotDir = [HOMEDIR '/' setName '/temperature/plots/'];


%% Preprocess

% Load configuraiton
run([HOMEDIR '/' setName '/temperature/config']);

%% Load measurment data
load([HOMEDIR '/' setName '/temperature/measdata/temperature']);
tempRgn = (temperature.time>tempDataStart & temperature.time<tempDataStop);
fprintf('[+] Temperature measurment data loaded\n');

%% Load simulation data
simData = loadSimFile(simFile, pairs(:,2));

%% Do single plots
fHandler = [];
fTitles  = {};


%% Thermal
if exist('pairs')
    for pairID=1:size(pairs,1)
        fHandler(end+1) = figure;

        % Plot meas. data
        tempID  = find(strcmp(temperature.names, pairs{pairID,3}));
        plot( (temperature.time(tempRgn)-min(temperature.time(tempRgn))) / 3600 ,temperature.values(tempRgn,tempID)+pairs{pairID,5},':k'); hold on

        % Plot sim. data
        simID   = find(strcmp(simData.colheaders, pairs{pairID,2}));
        plot(simData.time/3600, simData.data(:,simID),'k');


        legend('Measurment','Simulation');
        xlabel('time [h]')
        ylabel('Temperature [K]');
        title(pairs{pairID,4});

        axis([0 max(simData.time)/3600 ...
              min(min(simData.data(:,simID))-0.1,min(temperature.values(tempRgn,tempID)+pairs{pairID,5}))*0.99...
              max(max(simData.data(:,simID))+0.1,max(temperature.values(tempRgn,tempID)+pairs{pairID,5}))*1.01]);

        fTitles{end+1}  = [ 'Single: ' pairs{pairID,4}];
    end
end

%% save
callerPath = pwd;
cd(plotDir);

for i=1:length(fHandler);
    fprintf(['   >  ' fTitles{i} ' ... ']);
    saveas(fHandler(i), fTitles{i}, 'fig');
    saveas(fHandler(i), fTitles{i}, 'png');
    saveas(fHandler(i), fTitles{i}, 'pdf');
    close(fHandler(i));
    fprintf(' done!\n');
end

cd(callerPath);

end
