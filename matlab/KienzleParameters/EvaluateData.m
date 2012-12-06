function data = EvaluateData(data, config, dataDir)
% [] = EVALUATEDATA( DATA, CONFIG, DATADIR )
% ═════════════════════════════════════════════════════════════════════════
% Evaluates the measurement data of the force data series. The directions
% x, y, z are evaluated.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    21.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% The input arguments are the following:
% DATA ........ Struct with the measurement data and information with the
%               following fields:
%                 ─ DATA
%                   ├─ name ......... measurement name, must contain a 
%                   │                 measurement number
%                   ├─ ap ........... infeed rate [mm]
%                   ├─ kappa ........ angle of setting [deg]
%                   ├─ f ............ feed rate [mm/rev]
%                   └─ {Fx,Fy,Fz}
%                      ├─ time ...... time vector [s]
%                      └─ values .... values vector [N]
% CONFIG ...... Configuration struct, must contain the following fiels:
%                 ─ CONFIG
%                   ├─ measurmentN .. Measurement ID, must be numeric and
%                   │                 matching the one in DATA.name
%                   ├─ startTime .... Start time of used data window [s]
%                   └─ stopTime ..... Stop time of used data window [s]
%
% DATADIR ..... Path to the location where the data has to be stored
%
% The following information is added to the DATA struct:
%  ─ DATA
%    ├─ b .......... Chip width [mm]
%    ├─ h .......... Chip height [mm]
%    ├─ A .......... Chip cross section [mm2]
%    └─ {Fx,Fy,Fz}
%       └─ mean .... Mean force value within the selected data window
%
% The extendet data struct is saved at the given location DATADIR

%% CUT DATA

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' Evaluate Data\n');
fprintf('=====================================\n');


%% Config
forcesAvail = {'Fx','Fy','Fz'};


%% Avaiable Measurment sets

fprintf('\nCALCULATING MEAN VALUES\n');
fprintf('----------------------------\n');

%% Cut all data
for i = 1:length(data)
    if str2double(regexp(data{i}.name,'[0-9]*$','match','once')) ~= ...
            config(i).measurmentN
        fprintf(['  [!] Measurment ' data{i}.name ' did not matched config number. Skipped ...\n ']);
    elseif 1%config(i).useForKienzle
        fprintf(['  [+] Measurment ' data{i}.name ' evaluating ...']);
        
        data{i}.b  = data{i}.ap/sin(data{i}.kappa*pi/180);
        data{i}.h  = data{i}.f*sin(data{i}.kappa*pi/180);
        data{i}.A  = data{i}.b*data{i}.h;
        
        for force=1:length(forcesAvail)
            data{i}.(forcesAvail{force}).mean = ...
                mean(data{i}.(forcesAvail{force}).values(data{i}.(forcesAvail{force}).time>config(i).startTime & ...
                                                         data{i}.(forcesAvail{force}).time<config(i).stopTime ) );
        end
        
        fprintf(' done!\n');
    end
    
end

%% Save

fprintf('\nSAVE DATA\n');
fprintf('----------------------------\n');

callerPath = pwd;
cd(dataDir);
fprintf('   >  Saving data struct ... ');
save('Data','data');
fprintf(' done!\n');
cd(callerPath);

%% FI
fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');
