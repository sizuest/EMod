%% PROCESSFILE: SCHAUBLIN 42L 09.12.2011
% 

v_c = 100; % [m/min]
a_p = 1.2; % [mm]
l_c =  55; % [mm]


cut_times  = [0  248  401 530 ...
              713 932 1314 1458 ...
              1791 1955 2086 2207];
f          = [0.10 0.12 0.18 0.20 ...
              0.12 0.18 0.20 0.10 ...
              0.20 0.18 0.12 0.10];

% Leerläufe
empyt_runs     = [];
empty_duration = [];

%% X1_d
inputs.X1_d.unit        = 'mm';
inputs.X1_d.description = 'Diameter of the part at the cutting position (adjusted by the X1 axis)';
inputs.X1_d.time        = cut_times;
inputs.X1_d.values      = [37.6 35.2 32.8 30.4 ...
                           28.0 25.6 23.2 20.8 ...
                           38.6 36.2 33.8 31.4];


%% X1_ap
inputs.X1_ap.unit        = 'mm';
inputs.X1_ap.description = 'Cutting depth (adjusted by the X1 axis)';
inputs.X1_ap.time        = inputs.X1_d.time;
inputs.X1_ap.values      = a_p*ones(size(inputs.X1_d.values ));
%% Haltezeit

% Spindeldrehzahl [rps]
n_proc = v_c*1000/60./(pi*inputs.X1_d.values);
% Dauer des Schnittprozesses [s]
t_proc = l_c ./ ( f.*n_proc );
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
    inputs.Z1_v.time(tiStep*2-1)    = t_start_rot(tiStep);
    inputs.Z1_v.time(tiStep*2)      = t_halt(tiStep);
    inputs.Z1_v.values(tiStep*2-1)  = f(tiStep)*v_c*1000/(diam(tiStep)*pi);
    inputs.Z1_v.values(tiStep*2)    = 0; 
end

%% Z1_v
inputs.Z1_v.unit        = 'mm/min';
inputs.Z1_v.description = 'Speed forward of the tool (Z axis)';
% Achsen sind schon früher in regelung
% inputs.Z1_v.time        = inputs.X1_d.time;% - [ 29 -133 29 -14 24 -16 18 -29 20 -33 18 -38 22 -36 12 -32 13 -24];

%% C1_n
inputs.C1_n.unit        = 'rpm';
inputs.C1_n.description = 'Rotational speed of the spindle (C1)';
inputs.C1_n.time        = inputs.X1_d.time;
inputs.C1_n.values      = v_c*1000./(inputs.X1_d.values*pi);
inputs.C1_n.values(inputs.X1_d.values>42) = 0;

%% AxisBrakeCtrl
inputs.AxisBrakeCtrl.unit        = '-';
inputs.AxisBrakeCtrl.description = 'Indicates if the axis brakes are engaged';
% Achsen sind schon früher in regelung
inputs.AxisBrakeCtrl.time        = inputs.X1_d.time;
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
inputs.LubFlow.time        = inputs.C1_n.time-5;
inputs.LubFlow.time(inputs.C1_n.values==0) = inputs.C1_n.time(inputs.C1_n.values==0);
inputs.LubFlow.values      = (inputs.C1_n.values>0)*10/60*883/1000;

%% FClamp
inputs.FClamp.unit        = 'N';
inputs.FClamp.description = 'Force applyed by the tailstock ';
inputs.FClamp.time        = [0];
inputs.FClamp.values      = [0];

%% Zero
inputs.Zero.unit        = '-';
inputs.Zero.description = 'Zero element';
inputs.Zero.time        = [0];
inputs.Zero.values      = [0];

%% Tool
inputs.Tool.unit        = '-';
inputs.Tool.description = 'Number of the tool';
inputs.Tool.time        = [0];
inputs.Tool.values      = [2];

%% ChipConv
inputs.ChipConv.unit        = '-';
inputs.ChipConv.description = 'Chip conveyor on/off';
% Starts about 30s ahead
inputs.ChipConv.time        = inputs.C1_n.time-30;
inputs.ChipConv.time(inputs.C1_n.values==0) = inputs.C1_n.time(inputs.C1_n.values==0);
inputs.ChipConv.values      = 1*(inputs.C1_n.values>0);

%% Create file
cd('..');
ProcessFileGenerator('Measurment20111209', inputs, ...
                     'Prozess file zur Messung am 09.12.2011 an der Schaublin. Durchgeführt von AL und SZ.');
