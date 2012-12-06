function data = preprocessRawTemperatureData(rawDataDir, dataDir)
% DATA = PREPROCESSRAWTEMPERATURE( RAWDATADIR, DATADIR )
% ═════════════════════════════════════════════════════════════════════════
% Preprocesses the measurement data of the temperature data series.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    21.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% RAWDATADIR ... Source folder. All csv files will be loaded
% DATADIR ...... Directory to store the retrieved data

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' PREPROCESS RAW DATA\n');
fprintf('=====================================\n');
fprintf(['Folder: ' rawDataDir '\n\n'])

callerPath=pwd;


%% Load Files

files    = dir(rawDataDir);
ltsFiles = {};
csvFiles = {};

for i=1:length(files)
    if regexp(files(i).name, '_ref.lts$')
        ltsRef = files(i).name
    elseif regexp(files(i).name, '.lts$')
        ltsFiles{end+1} = files(i).name;
    elseif regexp(files(i).name, '_ref.csv$')
        csvRef = files(i).name
    elseif regexp(files(i).name, '.csv$')
        csvFiles{end+1} = files(i).name;
    end
end

cd(rawDataDir);

% CSV Files
fprintf('\nLOADING CSV FILES\n');
fprintf('----------------------------\n');
fprintf(['   <  ' num2str(length(csvFiles)) ' found\n']);

for i=1:length(csvFiles)
    fprintf(['   <  ' csvFiles{i} ' loading ...']);
    
    tmp = importdata(csvFiles{i},';',4);
        
    data{i}.time   = tmp.data(:,1);
    data{i}.values = tmp.data(:,2:end);
    
    for j=3:size(tmp.textdata,2)
        data{i}.names{j-2}  = tmp.textdata{4,j};
    end
    
    data{i}.StartTime = regexp(tmp.textdata{1,1},'([0-9]{2}).([0-9]{2}).([0-9]{2,4}).([0-9]{2}).([0-9]{2}).([0-9]{2})','match','once');    
    
    fprintf(' done!\n');
end

%% Merge

fprintf('\nMERGE DATA\n');
fprintf('----------------------------\n');

mData  = [];
mNames = {};
mTime  = [];
mOff   = [];

for i=1:length(data)
    
    % calculate offset
    off   = 0%datenum(data{i}.StartTime,'DD.MM.YY hh:mm:ss');
    mOff  = [mOff off];
    mTime = [mTime; data{i}.time+off];
    mNames = [mNames data{i}.names];
end

mTime = unique(sort(mTime))-min(mOff);
mOff  = mOff-min(mOff);

for i=1:length(data)
    
    mask = sum((repmat(data{i}.time',length(mTime),1) == ...
                repmat(mTime,1,length(data{i}.time)) ), 2);
    
    newData = interp1(data{i}.time+mOff(i), data{i}.values, mTime);
    
    newData(mask==0,:) = NaN;
    
    mData = [mData, newData];
end

clear data;

data.time   = mTime;
data.values = mData;
data.names  = mNames;

plot(data.time,data.values);
legend(data.names);


%% Save

fprintf('\nSAVE DATA\n');
fprintf('----------------------------\n');

cd(dataDir);

fprintf('   >  Saving data struct ... ');
save('Data','data');
fprintf(' done!\n');
cd(callerPath);

%% FI
fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');

end
