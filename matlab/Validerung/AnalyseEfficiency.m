%% ANALYSIS: EFFICENCY
% ═════════════════════════════════════════════════════════════════════════
% Performs an analysis of simualtion data compared to measurement data, in
% order to provide the key values for model validation.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 0.1
% ═════════════════════════════════════════════════════════════════════════
%
% For the sucessfull execution, two global variabels are required:
%
%   ─ HOMEDIR:  Root folder of the analysis
%   ─ MEASDIR:  Root folder of the measurements
%
% The variable SETNAME referes to the folder in HOMEDIR name with the 
% following structure:
%
%   ─ SETNAME
%       ├─ analysis
%       │   └─ efficency [dir]
%       └─ simdata
%           └─ simulation_output.dat
% 
% The generated plots and result are stored in the folder "analysis"

%% Config
setName = '20111205';

% Home and measurement directory
global HOMEDIR;
global MEASDIR;

if isempty(HOMEDIR)
    fprintf('[!] HOMEDIR not set!\n');
    return 
elseif isempty(MEASDIR)
    fprintf('[!] MEASDIR not set!\n');
    return 
end

forceDir = [MEASDIR '/2011-12-05/Kraft'];
% Folders/Files
forceDataFile  = [forceDir '/data/Data.mat'];
forceCfgFile   = [forceDir '/config/Config.csv'];
energyDataFile = [MEASDIR '/2011-12-05/Energy/data/2011_12_05_Schaublin42L_CYL_PR_K1_N01.txt'];

plotDir    = [HOMEDIR '/' setName '/analysis/efficency'];

%% Measured power

measurement.pel = ... Measured Fanuc power
                  [1193, 1401, 1504, 1656, 1956, 2045, 2321, 2580, 2768, ...
                   2763, 2347, 2253, 2184, 2058, 1899, 1449, 1342, 1131, ...
                   2279, 2546, 1307, 1226, 2117, 2596, 1921, 1731, 1515] - ...
                  ... Estimated axis power
                  [  32,   41,   52,   65,   81,  101,  124,  154,  155, ...
                    161,  142,  126,  113,   77,   65,   53,   40,   33, ...
                    116,  139,   44,   36,   95,  159,   83,   71,   60 ];


%% Load maps

amplifier = load('FanucMap/AmpMap');
spindle   = load('FanucMap/SpindleMap');

% Use same power vector
amplifier.eta_map = interp1( amplifier.P_map, amplifier.eta_map, spindle.P_map , {}, 'extrap');
amplifier.P_map   = spindle.P_map;

%% Calculate fanuc estimated map

fanuc.omega_map = spindle.omega_map(:,1);
fanuc.P_map     = spindle.P_map';

fanuc.eta_map   = spindle.eta_map .* repmat(amplifier.eta_map, size(spindle.eta_map,1), 1);

%% Calculate measured mechanical powers

forcesData = load(forceDataFile);
forcesData = forcesData.data;
forcesCfg  = load(forceCfgFile);

% Sort data chronological
[~,idx] = sort(forcesCfg(1,:));

forcesCfg  = forcesCfg(:,idx);
forcesData = forcesData(idx);

% Read out mean force and speed
tmp1 = zeros(size(forcesData));
tmp2 = zeros(size(forcesData));
for i=1:length(forcesData);
    tmp1(i) = forcesData{i}.Fz.mean;
    tmp2(i) = forcesData{i}.vc;
end

measurement.torque   = tmp1 .* forcesCfg(2,:)/2000;            % [Nm]
measurement.rotspeed = tmp2 ./ (forcesCfg(2,:)/1000*pi) * pi / 30;  % [rad/s]

measurement.rotspeed(isnan(measurement.torque)) = [];
measurement.pel(isnan(measurement.torque))      = [];
measurement.torque(isnan(measurement.torque))   = [];

measurement.pmech = measurement.torque .* measurement.rotspeed;

%% Calculate estimated efficencies

effEstimated = interp2(fanuc.P_map, fanuc.omega_map, fanuc.eta_map, ...
                        measurement.pmech, measurement.rotspeed );
                    
%% Calculate measured efficencies

effMeasured = measurement.pmech ./ measurement.pel;

%% Claculate mean error
sum((effEstimated./effMeasured-1).*measurement.pel)./sum(measurement.pel)

%% Plotting
figure
plot(effMeasured*100, effEstimated*100, 'k+'); hold on
xlabel('measured [%]');
ylabel('estimated [%]');

grid on

% Draw contour with iso relative error lines
x = linspace( floor(min(effMeasured) *99), ceil(max(effMeasured) *101), 20 )/100;
y = linspace( floor(min(effEstimated)*99), ceil(max(effEstimated)*101), 20 )/100;

[X,Y] = meshgrid(x,y);

err = (Y-X)./X;

[C,h] = contour(X*100,Y*100,err*100,'--k');
set(h,'ShowText','on','TextStep',get(h,'LevelStep')*1)
% colormap gray

axis( 100*[min(x), max(x), ...
           min(y), max(y)]);

