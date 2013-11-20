%% PROCESSFILE


%% Force
inpfields.Force.unit = 'NEWTON'
inpfields.Force.description = 'Force acting on the piston'
inpfields.Force.time   = [0 1 10    20     25   35    40 50 60 65]
inpfields.Force.values = [0 0 0 10000 10000 10000 50000  0 0  0]
inpfields.Force.init = 0

%% Control
inpfields.PumpCtrl.unit = 'NONE'
inpfields.PumpCtrl.description = 'Pump ON/OFF'
inpfields.PumpCtrl.time   = [0 10 20 30 40 50 60 65]
inpfields.PumpCtrl.values = [0  1 1  1  1  1  0  0]
inpfields.PumpCtrl.init = 0

%% AmbienteTemperature
inpfields.AmbTemperature.unit = 'KELVIN'
inpfields.AmbTemperature.description = 'AmbienteTemperature'
inpfields.AmbTemperature.time   = 0
inpfields.AmbTemperature.values = 293
inpfields.AmbTemperature.init = 293

%% Velocity
inpfields.Velocity.unit = 'MM_MIN'
inpfields.Velocity.description = 'Piston movement speed'
inpfields.Velocity.time   = [0 10    20     25   35    40 50    60 65]
inpfields.Velocity.values = [0 0     6000   0    6000  0  6000  0  0]
inpfields.Velocity.init = 0

%% Create file
cd('..');
ProcessFileGenerator('Testprocess1', inpfields, ...
                     'TODO');
