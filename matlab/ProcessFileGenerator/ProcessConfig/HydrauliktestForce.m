%% PROCESSFILE


%% Force
inpfields.Force.unit = 'NEWTON'
inpfields.Force.description = 'Force acting on the piston'
inpfields.Force.time   = [0 1 30    120    125   200    250    290 300]
inpfields.Force.values = [0 0 15000 0      20000 0      25000  0   0]
inpfields.Force.init = 0

%% Control
inpfields.PumpCtrl.unit = 'NONE'
inpfields.PumpCtrl.description = 'Pump ON/OFF'
inpfields.PumpCtrl.time   = [0 300]
inpfields.PumpCtrl.values = [1 0]
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
inpfields.Velocity.time   = [0 10    30     31   120   121 125   126  200  201 250  251 290  291 300]
inpfields.Velocity.values = [0 0     6000   0    6000  0   6000  0    6000 0   6000 0   6000 0   0]
inpfields.Velocity.init = 0
%% MassFlowOut
inpfields.MassFlowOut.unit = 'KG_S'
inpfields.MassFlowOut.description = 'MassFlow demanded from the pump'
inpfields.MassFlowOut.time   = [0           60      180         240         300   360          420         480          540     660      720    780          840    900          960          1200]
inpfields.MassFlowOut.values = [0.450266667 0.4532  0.454666667 0.459066667 0.462 0.464933333  0.469333333 0.472266667  0.4752  0.4796   0.484  0.486933333  0.4928 0.495733333  0.500133333  0]
inpfields.MassFlowOut.init = 0
%% RotSpeed
inpfields.RotSpeed.unit = 'RPM'
inpfields.RotSpeed.description = 'Rotational speed of the pump'
inpfields.RotSpeed.time   = [0 60    180   240  300  360  420   480  540  660  720  780  840  900  960  1200]
inpfields.RotSpeed.values = [0 2000  3000  2000 2000 2000 2000  2000 2000 2000 2000 2000 2000 2000 2000 0                                ]
inpfields.RotSpeed.init = 0

%% PressureOut
inpfields.PressureOut.unit = 'PA'
inpfields.PressureOut.description = 'Pressure in the tank'
inpfields.PressureOut.time   = [0 60     180   240  300  360  420   480  540  660  720  780  840  900  960  1200]
inpfields.PressureOut.values = [100000 100000 100000  100000 100000 100000 100000  100000 100000 100000 100000 100000 100000 100000 100000 100000]
inpfields.PressureOut.init = 0

%% ValveCtrl
inpfields.ValveCtrl.unit = 'NONE'
inpfields.ValveCtrl.description = 'Pressure in the tank'
inpfields.ValveCtrl.time   = [0 300]
inpfields.ValveCtrl.values = [1 0]
inpfields.ValveCtrl.init = 0

%% Create file
cd('..');
ProcessFileGenerator('ValidationNDM200', inpfields, ...
                     'TODO');
