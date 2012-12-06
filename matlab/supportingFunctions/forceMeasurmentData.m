function config = MeasurmentData(dataIn, configDir, tableContent, retrFields)
% [] = MEASUREMENT( DATA, CONFIGDIR, TABLECONTENT, RETRFIELDS )
% ═════════════════════════════════════════════════════════════════════════
% Interface function to retrieve additional data about a measurement. The
% user is assisted by graphical outputs. The gained data is stored as
% configuration file.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    21.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% The input arguments are the following:
% DATA ........ Struct with the measurement data with the following fields:
%                 ─ DATA
%                   ├─ name ......... measurement name, must contain a 
%                   │                 measurement number
%                   └─ {Fx,Fy,Fz}
%                      ├─ time ...... time vector [s]
%                      └─ values .... values vector [N]
% CONFIGDIR ... Path of the directory to save the configuration at
% TABLECONTENT  Cell with variable names to ask for. Must all be strings
%               and unique
% RETRFIELDS .. If set to 1, fucntion will only return available config
%               value names. (Optional parameter)
%
% The created configuration file "Config.csv" is stored at the given
% location



%% Config

% tableContent = {'X', 'startTime', 'stopTime', 'useForKienzle'};

% Dock all figures
defaultFigSetting = get(0,'DefaultFigureWindowStyle');
set(0,'DefaultFigureWindowStyle','docked') 

forcesAvail = {'Fx','Fy','Fz'};

%% Just return the fields
if nargin>3
    if retrFields
        config = ['measurmentN', tableContent];
        return;
    end
end

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' Measurment Data\n');
fprintf('=====================================\n');
    
%% Avaiable Measurment sets

fprintf('\nDATA AQUISITION\n');
fprintf('----------------------------\n');

%% Cut all data
for i = 1:length(dataIn)
    
    fprintf(['  [+] Measurment: ' dataIn{i}.name '\n']);
    
    config(i).measurmentN = str2num(regexp(dataIn{i}.name,'[0-9]*$','match','once'));
    
    % Plot Data to make desision easy

    f = figure; 
    for force = 1:length(forcesAvail)
        subplot(3,1,force); hold on; grid on;
        if force==1
            title(strrep(dataIn{i}.name,'_',' '));
        end
        plot(dataIn{i}.(forcesAvail{force}).time, ...
             dataIn{i}.(forcesAvail{force}).values, 'k');
        xlabel('Time [s]');
        ylabel('Force [N]');
    end
                 
    % Ask for value and save it
    for ask=1:length(tableContent)
        config(i).(tableContent{ask}) = ...
            input(['   > Value for ' tableContent{ask} ': ']);
    end  
    
    % close figure
    try
        close(f);
    end
end

%% Save

callerPath = pwd;

fprintf('\nSAVE DATA\n');
fprintf('----------------------------\n');

cd(configDir);
fprintf('   >  Saving configuration ... ');

csvwrite('Config.csv', squeeze(struct2cell(config)));
fprintf(' done!\n');
cd(callerPath);

clear meas* i curr* force* idx* max* *Middle

%% FI
set(0,'DefaultFigureWindowStyle',defaultFigSetting);

fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');

end
