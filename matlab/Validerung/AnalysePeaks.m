%% ANALYSE: PEAKS
% ═════════════════════════════════════════════════════════════════════════
% Performs an analysis of simualtion data compared to measurement data, in
% order to provide the key values for model validation. Focusing on the
% effects of peaks due to acceleration
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 0.1
% ═════════════════════════════════════════════════════════════════════════
%
% For the sucessfull execution, two global variabels are required:
%
%   ─ MEASDIR:  Root folder of the measurements
%

%% Configuration
% Home and measurement directory
global MEASDIR;

if isempty(MEASDIR)
    fprintf('[!] MEASDIR not set!\n');
    return 
end

measDataStart = 451;

errCalc.processThreshold = 50;     %[W]
errCalc.readyThreshold   = 10;     %[W]

%% Load measurment data
measData = importdata([MEASDIR '/2011-12-05/Energy/data/2011_12_05_Schaublin42L_CYL_PR_K1_N01.txt']);

measID  = find(strcmp(measData.colheaders, 'CLT6:P')); 

measData.time   = measData.data(:,1)-measData.data(1,1);
measData.values = measData.data(:,measID);

measData.values = measData.values(measData.time>measDataStart);
measData.time   = measData.time(measData.time>measDataStart)-measDataStart;

measData = rmfield(measData, {'data', 'colheaders', 'textdata'});

fprintf('[+] Energy measurment data loaded\n');


%% Apply filter 

% SETTINGS:
% Length [s]:
    tau = 2;
% Slope [W/s]
    th = 40;
    
% Calculate length in steps
n = 2*round(tau/mean(diff(measData.time))/2);

measData.fvalues = measData.values;

% Find peak candidates
peaks = abs(diff(measData.fvalues))>th;

for i=2:length(measData.fvalues)-1
    if peaks(i)
        measData.fvalues(i) = measData.fvalues(i-1);
    end
end

measData.nvalues = measData.fvalues;

plot(measData.time, measData.values,'k'); hold on
plot(measData.time, measData.fvalues,'r')

%% Calculate error

totEngy  = trapz(measData.time,abs(measData.values));


% Total energy differnce
err = measData.values-measData.fvalues;

errPeak = err;

figure
hist(errPeak(errPeak~=0),100);

% errEngy/totEngy

%% State specific error

load('PeakAnalysis/TriggerVals');

triggerVals = interp1(trigger.time, trigger.values, measData.time, 'linear', 0);

processRng  = triggerVals>=errCalc.processThreshold;

% Ready range
readyRng    = (measData.values>=errCalc.readyThreshold)  & ~processRng;

% Standby range
standbyRng    = ~( processRng  | readyRng);

offRng  = ~( processRng  | readyRng | standbyRng);

% Time durations
processTime = diff(measData.time(1:2))*sum(processRng);
readyTime   = diff(measData.time(1:2))*sum(readyRng);
standbyTime = diff(measData.time(1:2))*sum(standbyRng);
offTime     = diff(measData.time(1:2))*sum(offRng);

% Define evaluation ranges, total times and names
errRng.names   = {'off', 'standby', 'ready', 'process'};
errRng.meas{1}  = offRng;
errRng.meas{2}  = standbyRng;
errRng.meas{3}  = readyRng;
errRng.meas{4}  = processRng;
errRng.duration = [ offTime, ... %warmupTime, ...
                    standbyTime, ...
                    readyTime, ...
                    processTime ];
                
energy  = [measData.values(1:end-1)  .* diff(measData.time); 0];
energyf = [measData.fvalues(1:end-1) .* diff(measData.time); 0];
                
for i=1:length(errRng.meas)
    tmp1 = sum(energyf(errRng.meas{i}));
    tmp2 = sum(energy(errRng.meas{i}));
    
    e = (tmp1-tmp2)/tmp2;
    
    fprintf('%s:\t%2.1f%%\n',errRng.names{i},e*100);
end

