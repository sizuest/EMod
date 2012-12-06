function data=preprocessRawForceData(measurmentSet,rawDataDir, dataDir, ...
                                     nameStrgs, nameVars)
% [] = PREPROCESSRAWFORCEDATA( SETNAME, RAWDATADIR, DATADIR, 
%                              STRNAMES, VARNAMES )
% ═════════════════════════════════════════════════════════════════════════
% Preproessment of the raw force measurement data. Returns a struct with
% the retrived data. Variables can be read out of the measurement files.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    21.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
% 
% The input parameters are
% SETNAME ..... Name of the measurement
% RAWDATADIR .. Directory containing the raw data. all tdms files in this
%               directory will be loaded
% DATADIR ..... Directory to save the retrieved data at
% STRNAMES .... Cell with stringnames to be used as identifier for
%               variables in the file names.
%               Example: read X=70.5
%                filename = myMeasurment_X70.5_test.tdm
%                STRNAMES = {'X'};
% VARNAMES .... Variable names to save the retrieved variables at. Must be
%               of the same length as STRNAMES

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' PREPROCESS RAW DATA\n');
fprintf('=====================================\n');
fprintf(['Folder: ' rawDataDir '\n\n'])

if nargin>3
    if length(nameStrgs) ~= length(nameVars)
        error('Placeholders and variables cells must have same size')
    end
end

%% Load Data
fprintf('\nLOADING TDMS FILES\n');
fprintf('----------------------------\n');
files = dir(rawDataDir);

tdmsFiles={};
for i=1:length(files)
    if regexp(files(i).name, '.tdms$')
        tdmsFiles{end+1} = [rawDataDir files(i).name];
    end
end

fprintf(['  [+] ' num2str(length(tdmsFiles)) ' found\n']);

clear files

% Save current path
callerPath=pwd;

% Jump into raw data dir
%cd(rawDataDir);

% Load data
rawData = convertTDMS(false, tdmsFiles);
clear tdmsFiles

fprintf('  [+] All files imported!\n');


% Jump back to caller
%cd(callerPath);

%% Generate required Data format:


fprintf('\nGENERATING DATA STRUCT\n');
fprintf('----------------------------\n');

% Create cell for data
data = cell(1,length(rawData));
% Create cell for config
% MeasurmentName  | X  |StartTime  | StopTime | Use for Kienzle
% -------------------------------------------------------------
% ...             |    |            |          |

measFields = { 'kappa', 'KSM',...
              'Fx', 'Fy', 'Fz', 'name' };
          
kappa = input('   Please enter the value for kappa in degrees: ');
KSM   = input('   Has cooling fluid be used [1/0]?: ');         

for i=1:length(rawData)
    
    % Create struct
    measurment = struct;   
    
    for j = 1:length(nameStrgs);
        measurment = setfield(measurment,nameVars{j},...
                              str2double(strrep(regexp(rawData(i).FileNameShort,[nameStrgs{j} '[0-9]*[\.]*[0-9]*'],'match'),nameStrgs{j},'')) ...
                             );
    end
        
    % Read all specific values
%     ap = str2double(strrep(regexp(rawData(i).FileNameShort,'AP[0-9]*.[0-9]*','match'),'AP',''));
%     vc = str2double(strrep(regexp(rawData(i).FileNameShort,'VC[0-9]*','match'),'VC',''));
%     f  = str2double(strrep(regexp(rawData(i).FileNameShort,'F[0-9]*.[0-9]*','match'),'F',''));
    
    % Write name
    name = [measurmentSet ':' rawData(i).FileNameShort(1:end-5)];
    
    try
    
        % Read measurment vectors
        Fx.values = rawData(i).Data.MeasuredData(1).Data;
        Fy.values = rawData(i).Data.MeasuredData(2).Data;
        Fz.values = rawData(i).Data.MeasuredData(3).Data;

        Fx.time   = (0:rawData(i).Data.MeasuredData(1).Sample_Rate:(rawData(i).Data.MeasuredData(1).Sample_Rate*(rawData(i).Data.MeasuredData(1).Total_Samples-1)))';
        Fy.time   = (0:rawData(i).Data.MeasuredData(2).Sample_Rate:(rawData(i).Data.MeasuredData(2).Sample_Rate*(rawData(i).Data.MeasuredData(2).Total_Samples-1)))';
        Fz.time   = (0:rawData(i).Data.MeasuredData(3).Sample_Rate:(rawData(i).Data.MeasuredData(3).Sample_Rate*(rawData(i).Data.MeasuredData(3).Total_Samples-1)))';

    catch
        fprintf(['  [!] ' name ' failed during preprocessing\n']);
        Fx.values = NaN;
        Fy.values = NaN;
        Fz.values = NaN;
        Fx.time   = NaN;
        Fy.time   = NaN;
        Fz.time   = NaN;
    end
    % Will be done later
    Fx.mean   = NaN;
    Fy.mean   = NaN;
    Fz.mean   = NaN;

    % Write all standart values
    for j=1:length(measFields)
        measurment = setfield(measurment,measFields{j}, eval(measFields{j}) );
    end
    
    % Save struct to data
    data{i} = measurment;
    
    fprintf(['  [+] ' name ' added\n']);
        
end

clear('ap', 'X', 'f', 'vc', 'kappa', 'KSM', 'b', 'h', 'A',...
              'Fx', 'Fy', 'Fz', 'name', 'UseForKienzle');
clear field meas* Field i j 

%% Save

fprintf('\nSAVE DATA\n');
fprintf('----------------------------\n');

cd(dataDir);
fprintf('   >  Saving imported data ... ');
save('ImportedRawData','rawData');
fprintf(' done!\n');

fprintf('   >  Saving data struct ... ');
save('Data','data');
fprintf(' done!\n');
cd(callerPath);

%% FI
fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');

end
