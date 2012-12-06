function [] = ProcessFileGenerator( name, inp_fields, description, minSampleTi )
% PROCESSFILEGENERATOR( name, inp_fields, description, minSampleTi )
% ═════════════════════════════════════════════════════════════════════════
% Generates a process file with the name NAME, each
% signal is given as a field in inp_fied. The signals name is equal to the
% field name. Each signal field contains a time [mat], a values [mat], a
% initial condition [double] (opt.) unit [cell] and a description [cell] 
% field.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% Example:
% inpfields.C1_n.unit        = 'rpm'
%               .description = 'Rotational speed of the spindle (C1)'
%               .time        = [0 2 3 6]
%               .values      = [0 1000 1500 1000]
%               .init        = 1

if nargin<3
    description='@TODO';
elseif nargin<4
    minSampleTi = 1;
else
    % Add line breaks
    cperl = 50;
    tmp   = '';
    while any(strfind(description,' ')>cperl)
        k = strfind(description,' ');
        k = k(k>cperl);
        tmp = [tmp description(1:k(1)) '\n      '];
        description = description(k(1)+1:end);
    end
    description = [tmp description];
end

fprintf('\n\n');
fprintf('=====================================\n');
fprintf(' PROCESS FILE GENERATOR\n');
fprintf('=====================================\n');
%% Read input
signal_names = fieldnames(inp_fields);
fprintf(['Name:        ' name '\n']);
fprintf(['Signals:     ' regexprep(cell2mat(strcat(signal_names,', ')'),',$','') '\n']);
fprintf(['Description: ' description '\n']);
    

%% Check inputs
fprintf('\nPREPROCESSING\n');
fprintf('----------------------------\n');

req_fields = {'unit', 'description', 'time', 'values' };
opt_fields = {'init'};
% no error has occured
err = 0;

for f=1:length(signal_names)
    fprintf(['- Checking ' signal_names{f} ' ...\n']);
    field_err = 0;
    
    % Check fields
    for r=1:length(req_fields)
        if ~isfield(inp_fields.(signal_names{f}), req_fields{r});
            fprintf(['  [!] ' signal_names{f} ' must have a "' req_fields{r} '" field!\n']);
            err = 1;
        end
    end
    for r=1:length(opt_fields)
        if ~isfield(inp_fields.(signal_names{f}), opt_fields{r});
            inp_fields.(signal_names{f}) = setfield(inp_fields.(signal_names{f}), ...
                                                    opt_fields{r}, 0);
        end
    end
    
    % Check content
    if ~field_err
        if ~ischar(inp_fields.(signal_names{f}).unit)
            fprintf(['  [!] Field ' signal_names{f} '.unit must be a string!\n']);
            err = 1;
        end
        if ~ischar(inp_fields.(signal_names{f}).description)
            fprintf(['  [!] Field ' signal_names{f} '.description must be a string!\n']);
            err = 1;
        end
        if ~isnumeric(inp_fields.(signal_names{f}).time)
            fprintf(['  [!] Field ' signal_names{f} '.time must be numeric!\n']);
            err = 1;
        end
        if ~isnumeric(inp_fields.(signal_names{f}).values)
            fprintf(['  [!] Field ' signal_names{f} '.values must be numeric!\n']);
            err = 1;
        end
        if length(inp_fields.(signal_names{f}).time)~=length(inp_fields.(signal_names{f}).values)
            fprintf(['  [!] Fields ' signal_names{f} '.time and values must have same size!\n']);
            err = 1;
        end
    end
end

if err
    fprintf(' [E] Error occured in input check!\n');
    return
end 
%% Create unique time vector

fprintf('\nPROCESSING\n');
fprintf('----------------------------\n');

fprintf('- Creating unique time vector ...\n');

% Create time vector over all signals
all_times = [];
for f=1:length(signal_names)
    all_times = unique(sort([all_times inp_fields.(signal_names{f}).time]));
end

% Calculate sample time
all_steps  = all_times(2:end)-all_times(1:end-1);
sampleTime = vecgcd(all_steps);

% Limit sample time
if sampleTime<minSampleTi
    sampleTime = minSampleTi;
end

time = min(all_times):sampleTime:max(all_times);
clear('all_*');

fprintf(['  [+] Sample time is set to: ' num2str(sampleTime) '\n']);

fprintf('- Converting data to new time vector ...\n');
% Convert all signals to new time vector
for f=1:length(signal_names)
    fprintf(['  > Converting ' signal_names{f} ' ...\n']);
    if length(inp_fields.(signal_names{f}).time)>1
        tmp = timeseries(inp_fields.(signal_names{f}).values,...
                   inp_fields.(signal_names{f}).time);
        tmp = resample(tmp, time, 'zoh');
        inp_fields.(signal_names{f}).values = squeeze(tmp.Data);
        inp_fields.(signal_names{f}).values(...
            isnan(inp_fields.(signal_names{f}).values)) = inp_fields.(signal_names{f}).init;
    else
        inp_fields.(signal_names{f}).values = repmat(inp_fields.(signal_names{f}).values,1,length(time));
    end
    inp_fields.(signal_names{f}).time   = time - time(1);
end

clear('tmp');

fprintf('  [+] All signals have been converted to new sample time\n');

%% Set first time entry to zero
time = time - time(1);

%% Check file
% All values must be non-infinite and not NaN
for f=1:length(signal_names)
    %fprintf(id,['  <entry key="' signal_names{f} '">' sprintf('%f ', inp_fields.(signal_names{f}).values) '</entry>\n']);
    if any(isnan(inp_fields.(signal_names{f}).values))
        fprintf(['  [!] NaN detected in ' signal_names{f}  '\n']);
    end
    if any(isinf(inp_fields.(signal_names{f}).values))
        fprintf(['  [!] Inf detected in ' signal_names{f}  '\n']);
    end
        

end



%% Write file

fprintf('\nSAVING\n');
fprintf('----------------------------\n');


try
    id = fopen([ 'process_' name '.xml'], 'w');
catch
    error('Can''t open file');
end

% write header
fprintf(id, '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n');
fprintf(id, '<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">\n');
fprintf(id, '<properties>\n');
fprintf(id, '  <comment>\n');
fprintf(id, '    Process definition\n');
fprintf(id, '    ==================\n');  
fprintf(id,['    Name                    : ' name '\n']);
fprintf(id,['    Duration                : ' num2str(max(time)) '\n']);
fprintf(id, '\n');
fprintf(id, '    Process description:\n');
fprintf(id,['      ' description '\n']);
fprintf(id, '\n');
fprintf(id, '    Process parameters:\n');
fprintf(id, '      SamplePeriod  [s]           : Sample period (used in this file) of the time vectors of the process parameters\n');
for f=1:length(signal_names)
    fprintf(id,['      ' signal_names{f}  blanks(14-length(signal_names{f})) '[' inp_fields.(signal_names{f}).unit ']' blanks(8-length(inp_fields.(signal_names{f}).unit)) '    : ' inp_fields.(signal_names{f}).description '\n']);
end
fprintf(id, '  </comment>\n');
fprintf(id,['  <entry key="SamplePeriod">' num2str(sampleTime) '</entry>\n']);
for f=1:length(signal_names)
    fprintf(id,['  <entry key="' signal_names{f} '">' sprintf('%f ', inp_fields.(signal_names{f}).values) '</entry>\n']);
end
fprintf(id, '</properties>\n');

fclose(id);

fprintf(' [+] Config file has been created\n');

%% FI
fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');

end

