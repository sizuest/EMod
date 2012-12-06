function data = loadSimFile(name, signalnames)

% DATA = LOADSIMFILE( NAME, SIGNALNAMES )
% ═════════════════════════════════════════════════════════════════════════
% Loads the desired signal names from a given simulation results file (in
% general "simulation_output.dat"
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    21.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% The inputs are the following:
% NAME .......... Path to the simualtion file
% SIGNALNAMES ... Cell array with the desired signal names
%
% The created output is a struct:
%  ─ DATA
%    ├─ time ......... Time vector [s]
%    ├─ data ......... Data field
%    └─ colheaders ... Signal names

%% Read File 
fprintf(['[+] Loading simulation file ...']);
tmp   = importdata(name,'\t',3);
% Read missing col headers
heads = textread(name,'%s',size(tmp.textdata,2)*2-1,'whitespace','\t');

tmp.textdata(1,1:end) = heads(1:size(tmp.textdata,2));
tmp.textdata(2,2:end) = heads(size(tmp.textdata,2)+1:end);

tmp = tmp.textdata;
fprintf([' done!\n']);

%% Form output

skipped = '';

% Time vector
data.time = str2double(tmp(4:end,1));

% Signals
% for i=2:size(tmp,2)
%     % Check if its a numeric signal
%     if(isempty(strfind(tmp{1,i},'-Sim')))
%         fprintf(['[+] Adding: ' tmp{1,i} '...']);
%         try
%             unit   = tmp{3,i}; 
%             values = str2double(tmp(4:end,i));
% 
%             eval(['data.' tmp{2,i} '.unit = unit;']);
%             eval(['data.' tmp{2,i} '.values = values;']);
%             fprintf([' done!\n']);
%         catch
%             fprintf([' FAILED!\n']);
%         end
%     else
%         skipped = [skipped ', ' tmp{1,i}];
%     end   
% end

includeIDs = [];

for i=2:size(tmp,2)
    % Check if signal is requested
    if(any(strcmp(tmp{2,i},signalnames)))
        includeIDs(end+1) = i;
    else
        skipped = [skipped ', ' tmp{1,i}];
    end
    
end

% Import
fprintf(['[+] Adding ' num2str(length(includeIDs)) ' signals ...']);
data.data       = str2double(tmp(4:end,includeIDs));
data.colheaders = tmp(2,includeIDs);
fprintf([' done!\n']);

%% FI


fprintf(['[!] Skipped: ' skipped '\n']);

end
