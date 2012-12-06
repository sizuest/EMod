%% PROCESSFILE: SCHAUBLIN 42L 11.11.2011
% 


%% C1_n
inputs.C1_n.unit        = 'rpm';
inputs.C1_n.description = 'Rotational speed of the spindle (C1)';
inputs.C1_n.time        = [0 1135 1360 1380 1400 1427 1500 1527 1560 1588 1657];
inputs.C1_n.values      = [0  600  630  660  690  750  910  700  100 1000    0];

%% Z1_v
inputs.Z1_v.unit        = 'mm/min';
inputs.Z1_v.description = 'Speed forward of the tool (Z axis)';
inputs.Z1_v.time        = [0];
inputs.Z1_v.values      = [0];

%% X1_d
inputs.X1_d.unit        = 'mm';
inputs.X1_d.description = 'Diameter of the part at the cutting position (adjusted by the X1 axis)';
inputs.X1_d.time        = [0];
inputs.X1_d.values      = [0];

%% X1_ap
inputs.X1_ap.unit        = 'mm';
inputs.X1_ap.description = 'Cutting depth (adjusted by the X1 axis)';
inputs.X1_ap.time        = [0];
inputs.X1_ap.values      = [0];

%% X1_v
inputs.X1_v.unit        = 'mm/min';
inputs.X1_v.description = 'Speed along the X-Axis';
inputs.X1_v.time        = [0];
inputs.X1_v.values      = [0];

%% LubFlow
inputs.LubFlow.unit        = 'rpm';
inputs.LubFlow.description = 'Rotational speed of the spindle (C1)';
inputs.LubFlow.time        = [0];
inputs.LubFlow.values      = [0];

%% FClamp
inputs.FClamp.unit        = 'N';
inputs.FClamp.description = 'Force applyed by the tailstock ';
inputs.FClamp.time        = [0  990 1670];
inputs.FClamp.values      = [0 2060    0];

%% Temperature
inputs.Temperature.unit        = 'K';
inputs.Temperature.description = 'Temperature of the shop floor';
inputs.Temperature.time        = [0];
inputs.Temperature.values      = [293];

%% Pressure
inputs.Pressure.unit        = 'Pa';
inputs.Pressure.description = 'Ambient pressure of the shop floor';
inputs.Pressure.time        = [0];
inputs.Pressure.values      = [101300];

%% Zero
inputs.Zero.unit        = '-';
inputs.Zero.description = 'Zero element';
inputs.Zero.time        = [0];
inputs.Zero.values      = [0];

%% Tool
inputs.Tool.unit        = '-';
inputs.Tool.description = 'Number of the tool';
inputs.Tool.time        = [0];
inputs.Tool.values      = [16];

%% ChipConv
inputs.ChipConv.unit        = '-';
inputs.ChipConv.description = 'Chip conveyor on/off';
inputs.ChipConv.time        = [0 50 300];
inputs.ChipConv.values      = [0  1   0];

%% Create file
cd('..');
ProcessFileGenerator('TestMeasurment20111110', inputs, ...
                     'Prozess file zur Testmessung am 10.11.2011 an der Schaublin. Durchgeführt von AL und SZ.');
