%% PROCESSFILE: SCHAUBLIN 42L 05.12.2011
% 

%% FileCfg
FileName    = 'Water_42000_100';
FileComment = 'Prozess file zur Messung Wasser mit 42krpm und 100rpm';


%% RotSpeed

inputs.RotSpeed.unit        = 'rpm';
inputs.RotSpeed.description = 'Rotational speed of the spindle';
inputs.RotSpeed.time        = cumsum([0 1 1800 3600 3600 1800]);
inputs.RotSpeed.values      = [0 0 42000 0 100 0];

%% Torque

inputs.Torque.unit        = 'Nm';
inputs.Torque.description = 'Spindle torque';
inputs.Torque.time        = 0;
inputs.Torque.values      = 0;

%% InletTemperature

inputs.InletTemperature.unit        = 'K';
inputs.InletTemperature.description = 'Coolant inlet temperature';
inputs.InletTemperature.time        = 0;
inputs.InletTemperature.values      = 19+273.15;

%% FlowRate

inputs.FlowRate.unit        = 'l/min';
inputs.FlowRate.description = 'Coolant flow rate';
inputs.FlowRate.time        = 0;
inputs.FlowRate.values      = 5.6;



%% Create file
cd('..');
ProcessFileGenerator(FileName, inputs, FileComment);
