function [k_c11 z] = ParameterIdentification(data, config, saveDir)
% [] = PARAMETERIDENTIFICATION( DATA, CONFIG, SAVEDIR )
% ═════════════════════════════════════════════════════════════════════════
% Kienzle parameter identification based on a least squares problem.
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
%                   ├─ b .......... Chip width [mm]
%                   ├─ h .......... Chip height [mm]
%                   ├─ A .......... Chip cross section [mm2]
%                   └─ {Fx,Fy,Fz}
%                      └─ mean .... Mean force value [N]
% CONFIG ...... Configuration struct, must contain the following fiels:
%                 ─ CONFIG
%                   └─ measurmentN .. Measurement ID, must be numeric and
%                                     matching the one in DATA.name
%
% SAVEDIR ..... Path to the location where the data has to be stored
%
% The calculated parameter as some additional informations are stored at
% the given SAVEDIR location.

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' Kienzle Parameter Identification\n');
fprintf('=====================================\n');

%% Configuration

forcesAvail = {'Fx','Fy','Fz'};

%% Problem Formulation
k_c11 = zeros(size(forcesAvail));
z     = zeros(size(forcesAvail));

fprintf('\nSOLVING LEAST SQUARES\n');
fprintf('----------------------------\n');

for force = 1:length(forcesAvail)
    y = [];
    H = [];
    for meas = 1:length(data)
        for i = 1:length(data{meas})
            if str2double(regexp(data{meas}{i}.name,'[0-9]*$','match','once')) ~= config{meas}(i).measurmentN
                fprintf(['  [!] Measurment ' data{meas}{i}.name ' did not matched config number. Skipped ...\n ']);
            elseif 1%config{meas}(i).useForKienzle
                y(end+1,1) =  log(data{meas}{i}.(forcesAvail{force}).mean /...
                                  data{meas}{i}.A);
                H(end+1,1) = -log(data{meas}{i}.h);
                H(end,2)   = 1;
            end
        end
    end
    param = H\y;
    
    B     = corr(y,H*param)^2/(corr(y)*corr(H*param));
    %B     = corr(y,H(:,1))^2/(corr(y)*corr(H(:,1)));
    r     = sign(param(1))*sqrt(abs(B));
    
    fprintf(['  [!] Correlation coeff ' forcesAvail{force} ': %1.3f\n'], r );
    
    k_c11(force) = exp(param(2));
    z(force)     = param(1);
end
        

%% Least squares results

fprintf('\nRESULTS\n');
fprintf('----------------------------\n');
fprintf('\n\t \t|  k_c11\t|  z\n')
fprintf('\t----------------------------------\n')
out = sprintf('\t#F%i\t|    %4.0f\t|  %1.2f\n',[(1:length(forcesAvail));k_c11;z]);

for i=1:length(forcesAvail)
    out = strrep(out,['#F' num2str(i)], forcesAvail(i));
    out = out{1};
end
fprintf(out);

%% Save
fprintf('\nSAVE DATA\n');
fprintf('----------------------------\n');

callerPath = pwd;
cd(saveDir);
fprintf('   >  Saving k_c11 ... ');
save('k_c11','k_c11');
fprintf(' done!\n');
fprintf('   >  Saving z ... ');
save('z','z');
fprintf(' done!\n');

fprintf('   >  Saving table ... ');

try
    id = fopen('KienzleParameters.txt', 'w');
    fprintf(id, ['\n\t \t|  k_c11\t|  z\n', '\t----------------------------------\n', out]);
    fclose(id);
    fprintf(' done!\n');
catch
    fprintf(' failed!\n');
end

cd(callerPath);

%% FI
fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');


