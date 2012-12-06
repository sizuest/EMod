function data = ChirpRemove(data, dataDir, maxStepFirst, maxStepAdd)

% DATA = CHIRPREMOVE( DATA, DATADIR, maxStepFirst, maxStepAdd )
% ═════════════════════════════════════════════════════════════════════════
% Removes chirp from measurement data
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    21.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% The input arguments are the following:
% DATA .......... Struct with the measurement data and information with the
%                 following fields:
%                 ─ DATA
%                   ├─ time ... time vector [s]
%                   └─ values . values vector [K]
% DATADIR ....... Path to the location where the data has to be stored
% MAXSTEPFIRST .. Maximum allowed change per time step, if the existing
%                 data is ok
% MAXSTEPADD .... How much is the temperature allowed to change over time.
%                 (temperature gradient)
%
% The extendet data struct is saved at the given location DATADIR

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' CHIRP REMOVE\n');
fprintf('=====================================\n');

callerPath=pwd;

% Dock all figures
defaultFigSetting = get(0,'DefaultFigureWindowStyle');
set(0,'DefaultFigureWindowStyle','docked') 

%% Go trough data

time = data.time;

for col=1:size(data.values,2)
    
   curData = data.values(:,col);
   
   lastOK = 1;
   wasNan = 0;
   maxStep = 0;
   newData = nan(size(curData));
   
   for i=2:length(curData)
       
       % Calc time since last good value
       timeStep = time(i)-time(lastOK);
       
       % Calc max difference
       if i==lastOK+1
           maxStep = maxStepFirst*timeStep;
       else
           maxStep = maxStep + maxStepAdd*timeStep;
       end
       
       % Check distance
       if wasNan
           if ~isnan(curData(i))
               lastOK=i;
               wasNan = 0;
           end
       elseif isnan(curData(i))
            wasNan = 1;
            if lastOK==i-1
                newData(i) = NaN;
                newData(i-1) = curData(i-1);
            else
                newData(lastOK:i-1) = interp1(time([lastOK i-1]), curData([lastOK i-1]), time(lastOK:i-1)); 
                lastOK = i;
            end
       elseif abs(curData(i)-curData(lastOK))<=maxStep
           newData(lastOK:i) = interp1(time([lastOK i]), curData([lastOK i]), time(lastOK:i)); 
           lastOK = i;
       end
       
   end
   
   if ~all(curData(~isnan(curData)) == newData(~isnan(curData)))
       f = figure;
       plot(time, curData, 'k'); hold on
       plot(time, newData, 'r');

       s = input('Keep new data vector? [1/0]: ');
       if s
           data.values(:,col) = newData;
       end

       close(f);
   end
    
end

plot(data.time,data.values);
legend(data.names);

%% SAVE DATA
fprintf('\nSAVE DATA\n');
fprintf('----------------------------\n');

cd(dataDir);

fprintf('   >  Saving data struct ... ');
save('Data','data');
fprintf(' done!\n');
cd(callerPath);

%% FI
set(0,'DefaultFigureWindowStyle',defaultFigSetting);

fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');

end
