%% PROCESSFILE: SCHAUBLIN 42L 05.12.2011
% 

%% FileCfg
FileName    = 'Measurment20111205_1';
FileComment = 'Prozess file zur Messung am 05.12.2011 an der Schaublin, Teil 1. Durchgeführt von AL und SZ.';

%% ProcessCfg
v_c = 100;  % [m/min]
l_c =  55;  % [mm]
  f = 0.15; % [mm/rev]

% Hier: Nur Werte bei der Bearbeitung angegeben. Haltezeit wird am Ende
% eingefügt.
%%
% $t_{halt}=t_{start}+\frac{l_c}{Z1_v}$

cut_times = [ 38 364 523-1.4  644-0.4  786  963  1089 1152 1276 1406-0.3];

% Leerläufe
empyt_runs     = [7];
empty_duration = [13];

%% X1_d
inputs.X1_d.unit        = 'mm';
inputs.X1_d.description = 'Diameter of the part at the cutting position (adjusted by the X1 axis)';
inputs.X1_d.time        = cut_times - [ 7 33 21 19 15 13 0 13 15 15 ];%[ 0 333 492  614  756  933  1122 1246 1376];
inputs.X1_d.values      = [ 50 38  35.6 32.8 29.6 26.0 22.0 22.0 17.6 12.8];
inputs.X1_d.init        = 60;

%% X1_ap
inputs.X1_ap.unit        = 'mm';
inputs.X1_ap.description = 'Cutting depth (adjusted by the X1 axis)';
inputs.X1_ap.values      = [0 1.0:0.2:1.8 0 2.0:0.2:2.6];

%% Haltezeit

% Spindeldrehzahl [rps]
n_proc = v_c*1000/60./(pi*inputs.X1_d.values);
% Dauer des Schnittprozesses [s]
t_proc = l_c ./ ( f*n_proc );
t_proc(empyt_runs) = empty_duration;
% Start-/stopzeit (rel.) [s]
t_start     = cut_times;
t_start_rot = inputs.X1_d.time;
t_halt      = cut_times + round(t_proc);

diam  = inputs.X1_d.values;
ap    = inputs.X1_ap.values;

for tiStep=1:length(cut_times)
    inputs.X1_d.time(tiStep*2-1)    = t_start_rot(tiStep);
    inputs.X1_d.time(tiStep*2)      = t_halt(tiStep);
    inputs.X1_ap.time(tiStep*2-1)   = t_start(tiStep);
    inputs.X1_ap.time(tiStep*2)     = t_halt(tiStep);
    inputs.X1_d.values(tiStep*2-1)  = diam(tiStep);
    inputs.X1_d.values(tiStep*2)    = 60;
    inputs.X1_ap.values(tiStep*2-1) = ap(tiStep);
    inputs.X1_ap.values(tiStep*2)   = 0;
end

%% C1_n
inputs.C1_n.unit        = 'rpm';
inputs.C1_n.description = 'Rotational speed of the spindle (C1)';
inputs.C1_n.time        = inputs.X1_d.time;
inputs.C1_n.values      = v_c*1000./(inputs.X1_d.values*pi);
inputs.C1_n.values(inputs.X1_d.values>55) = 0;

%% Z1_v
inputs.Z1_v.unit        = 'mm/min';
inputs.Z1_v.description = 'Speed forward of the tool (Z axis)';
% Achsen sind schon früher in regelung
inputs.Z1_v.time        = inputs.X1_d.time;% - [ 29 -133 29 -14 24 -16 18 -29 20 -33 18 -38 22 -36 12 -32 13 -24];
inputs.Z1_v.values      = f * inputs.C1_n.values;

%% AxisBrakeCtrl
inputs.AxisBrakeCtrl.unit        = '-';
inputs.AxisBrakeCtrl.description = 'Indicates if the axis brakes are engaged';
% Achsen sind schon früher in regelung
inputs.AxisBrakeCtrl.time        = inputs.X1_d.time - [ 29 -133 29 -14 24 -16 18 -29 20 -33 18 -38 21 -5 22 -36 12 -32 13 -24];
inputs.AxisBrakeCtrl.values      = 1*(0 == inputs.C1_n.values);

%% X1_v
inputs.X1_v.unit        = 'mm/min';
inputs.X1_v.description = 'Speed along the X-Axis';
inputs.X1_v.time        = [0];
inputs.X1_v.values      = [0];

%% LubFlow
inputs.LubFlow.unit        = 'kg/s';
inputs.LubFlow.description = 'Lubrication flow';
% Starts about 5s ahead, and ends when C1_n=0;
inputs.LubFlow.time        = inputs.C1_n.time + [ 1 -1 23 0 6 -1 7 0 6 0 5 -1 8 -0 3 -1 5 0 7 0];
inputs.LubFlow.values      = (inputs.C1_n.values>0)*10/60*883/1000;

%% FClamp
inputs.FClamp.unit        = 'N';
inputs.FClamp.description = 'Force applyed by the tailstock ';
inputs.FClamp.time        = [0];
inputs.FClamp.values      = [0];

%% Tool
inputs.Tool.unit        = '-';
inputs.Tool.description = 'Number of the tool';
inputs.Tool.time        = [0];
inputs.Tool.values      = [1];

%% ChipConv
inputs.ChipConv.unit        = '-';
inputs.ChipConv.description = 'Chip conveyor on/off';

inputs.ChipConv.time        = inputs.C1_n.time - [ 4 0 19 -1 25-11.4 -0.5 9 -1 14 -2 14 -1 8 0 11 0 7 -1.5 5 -1.5];
inputs.ChipConv.values      = 1*(inputs.C1_n.values>0);

% Add one empty run
inputs.ChipConv.time        = [inputs.ChipConv.time(1:2) inputs.ChipConv.time(1)+[161.8 177] inputs.ChipConv.time(3:end)];
inputs.ChipConv.values      = [inputs.ChipConv.values(1:2) inputs.ChipConv.values(1:2) inputs.ChipConv.values(3:end)];

%% Zero Movement
inputs.Zero.unit        = '-';
inputs.Zero.description = 'Zero movement element';
inputs.Zero.time        = [0];
inputs.Zero.values      = [0];

%% Create file
cd('..');
ProcessFileGenerator(FileName, inputs, FileComment, 0.2);
