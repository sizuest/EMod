function data = ForceDriftCorrection( data, dataDir )
% [] = FORCEDRIFTCORRECTION( DATA, DATADIR )
% ═════════════════════════════════════════════════════════════════════════
% Compensates the drift during long force measurements. Each measurement
% with a sucessfull drift correction is labled, to avoid multiple
% corrections.
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
%                   └─ {Fx,Fy,Fz}
%                      ├─ time ...... time vector [s]
%                      └─ values .... values vector [N]
% DATADIR ..... Path to the location where the data has to be stored
%
% The new force value series will be stored in the DATA struct, where the
% field "DriftCorrection" is added as well. This struct is stored at the
% given location.

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' FORCE DRIFT CORRECTION\n');
fprintf('=====================================\n');

availForces = {'Fx','Fy','Fz'};

%% Drift correction
fprintf('\nPROCESSING DATA\n');
fprintf('----------------------------\n');

for meas=1:length(data)
    if isfield(data{meas}, 'DriftCorrection')
        fprintf(['  [!] ' data{meas}.name ': refused [allready done]\n']);
    else
        data{meas} = setfield(data{meas},'DriftCorrection',struct);
        fprintf(['  [+] ' data{meas}.name ' ...']);
        for force = 1:length(availForces)
            
            n = min(length(data{meas}.(availForces{force}).time),sum(data{meas}.(availForces{force}).time<5));
            
            fprintf([' (' num2str(n) 'th order filter) ...']);
            
            % Filter data
            a = 1;
            b = ones(n,1)/n;

            fData = filter(b,a, data{meas}.(availForces{force}).values);
       
            
            % Determine gradient per time
            off  = -fData(1);
            grad = (min(fData)-off) / ...
                    max(data{meas}.(availForces{force}).time);
                
            % Remove drift
            data{meas}.(availForces{force}).values = ...
                data{meas}.(availForces{force}).values - grad*data{meas}.(availForces{force}).time;
            
            % Save value
            data{meas}.DriftCorrection.(availForces{force}) = grad;
        end
        
        fprintf(' done!\n');
    end
end







%% Save

fprintf('\nSAVE DATA\n');
fprintf('----------------------------\n');

% Save current path
callerPath=pwd;

cd(dataDir);

fprintf('   >  Saving data struct ... ');
save('Data','data');
fprintf(' done!\n');
cd(callerPath);

%% FI
fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');
