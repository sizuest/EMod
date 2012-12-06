%% PROCESSFILE: SCHAUBLIN 42L 05.12.2011
% 

%% FileCfg
FileName    = 'Measurment20111215_Plan';
FileComment = 'Prozess file zur Messung am 15.12.2011 an der Schaublin, Teil 1: Plandrehen. Durchgeführt von AL und SZ.';

%% ProcessCfg
v_c   = 100;  % [m/min]
  f   = 0.1;  % [mm/rev]
n_max = 2000; % [rpm]
t_cyc = 11.4; % [s]
d     = 43;   % [mm]
f_cut = 4;  % [mm]
t_sample = 1; % [s]

t_cyc_slow = 14;


% Hier: Nur Werte bei der Bearbeitung angegeben. Haltezeit wird danach
% eingefügt.

% cyc_times = [ -0.091 1.44 2.44 3.03 4.44 5.96] * 1e3;
% cyc_cuts  = [ 60 74  38 114 123 162];

cyc_times = [ 1.44 2.44 3.03 4.44 5.96] * 1e3-1.44e3;
cyc_cuts  = [ 74  38 114 123 162];


%% Generic cut
X1_d.time    = 0:t_sample:t_cyc;
X1_d.values  = d;
X1_ap.values = f*v_c*1000./(2*pi*X1_d.values)/60;

for i=2:length(X1_d.time)
    n = min(v_c*1000./(pi*X1_d.values(i-1)), n_max)/60;
    X1_ap.values(i) = f*n;
    X1_d.values(i)  = max(X1_d.values(i-1)-2*X1_ap.values(i)*t_sample,0);
end



X1_d_slow.time    = 0:t_sample:t_cyc_slow;
X1_d_slow.values  = d;
X1_ap_slow.values = 2*f*v_c*1000./(pi*X1_d_slow.values)/60;

for i=2:length(X1_d_slow.time)
    n = min(v_c*1000./(pi*X1_d_slow.values(i-1)), 1000)/60;
    X1_ap_slow.values(i) = f*n;
    X1_d_slow.values(i)  = max(X1_d_slow.values(i-1)-2*X1_ap_slow.values(i)*t_sample,0);
end


%% X1_d, X1_ap

inputs.X1_d.unit        = 'mm';
inputs.X1_d.description = 'Diameter of the part at the cutting position (adjusted by the X1 axis)';
inputs.X1_d.time        = [0];
inputs.X1_d.values      = [60];

inputs.X1_ap.unit        = 'mm';
inputs.X1_ap.description = 'Cutting depth (adjusted by the X1 axis)';
inputs.X1_ap.values      = [0];

% for j=1:cyc_cuts(1)
%     inputs.X1_d.time    = [inputs.X1_d.time,    X1_d_slow.time+cyc_times(1)+(j-1)*t_cyc_slow];
%     inputs.X1_d.values  = [inputs.X1_d.values,  X1_d_slow.values];
%     inputs.X1_ap.values = [inputs.X1_ap.values, X1_ap_slow.values];
% end
% inputs.X1_d.time        = [inputs.X1_d.time,    cyc_times(1)+cyc_cuts(1)*t_cyc_slow];
% inputs.X1_d.values      = [inputs.X1_d.values,  60];
% inputs.X1_ap.values     = [inputs.X1_ap.values, 0];

% for i=2:length(cyc_times)
for i=1:length(cyc_times)
    
    for j=1:cyc_cuts(i)
        inputs.X1_d.time    = [inputs.X1_d.time,    X1_d.time+cyc_times(i)+(j-1)*t_cyc];
        inputs.X1_d.values  = [inputs.X1_d.values,  X1_d.values];
        inputs.X1_ap.values = [inputs.X1_ap.values, X1_ap.values];
    end
    inputs.X1_d.time        = [inputs.X1_d.time,    cyc_times(i)+cyc_cuts(i)*t_cyc];
    inputs.X1_d.values      = [inputs.X1_d.values,  60];
    inputs.X1_ap.values     = [inputs.X1_ap.values, 0];
end

inputs.X1_ap.time = inputs.X1_d.time;


%% C1_n
inputs.C1_n.unit        = 'rpm';
inputs.C1_n.description = 'Rotational speed of the spindle (C1)';
inputs.C1_n.time        = inputs.X1_d.time;
inputs.C1_n.values      = v_c*1000./(inputs.X1_d.values*pi);
inputs.C1_n.values(inputs.X1_d.values>55) = 0;
inputs.C1_n.values(inputs.C1_n.values>n_max) = n_max;

inputs.C1_n.values(inputs.C1_n.values>1000 & inputs.C1_n.time<cyc_times(2)) = 1000;

%% Z1_v
inputs.Z1_v.unit        = 'mm/min';
inputs.Z1_v.description = 'Speed forward of the tool (Z axis)';
% Achsen sind schon früher in regelung
inputs.Z1_v.time        = inputs.X1_d.time;
inputs.Z1_v.values      = f_cut * (inputs.C1_n.values>0) .* inputs.C1_n.values/60;

%% AxisBrakeCtrl
inputs.AxisBrakeCtrl.unit        = '-';
inputs.AxisBrakeCtrl.description = 'Indicates if the axis brakes are engaged';
% Achsen sind schon früher in regelung
inputs.AxisBrakeCtrl.time        = inputs.X1_d.time;
inputs.AxisBrakeCtrl.values      = 1*(0 == inputs.C1_n.values);


%% X1_v
inputs.X1_v.unit        = 'mm/min';
inputs.X1_v.description = 'Speed along the X-Axis';
inputs.X1_v.time        = inputs.X1_ap.time;
inputs.X1_v.values      = [0, diff(inputs.X1_ap.values)./diff(inputs.X1_ap.time)]*60;
inputs.X1_v.values(isinf(inputs.X1_v.values)) = 0;
inputs.X1_v.values(inputs.X1_v.values<0) = 0;

%% LubFlow
inputs.LubFlow.unit        = 'kg/s';
inputs.LubFlow.description = 'Lubrication flow';
% Starts about 5s ahead, and ends when C1_n=0;
inputs.LubFlow.time        = inputs.C1_n.time;
inputs.LubFlow.values      = (inputs.C1_n.values>0)*10*833/1000/60;

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
inputs.Tool.values      = [1];

%% ChipConv
inputs.ChipConv.unit        = '-';
inputs.ChipConv.description = 'Chip conveyor on/off';

inputs.ChipConv.time        = inputs.C1_n.time;
inputs.ChipConv.values      = 1*(inputs.C1_n.values>0);

%% Vertauschen von Z1_v und X1_v

%inputs.X1_v.values = diff(inputs.X1_d.values)./diff(inputs.X1_d.time)/2;

inputs.Z1_v.values  = inputs.X1_v.values;
inputs.X1_v.values  = zeros(size(inputs.X1_v.values));
inputs.X1_ap.values = (inputs.X1_ap.values~=0)*f_cut;

%% X1_d darf nicht null sein
inputs.X1_d.values(inputs.X1_d.values==0) = 1e-3;

%% Create file
cd('..');
ProcessFileGenerator(FileName, inputs, FileComment);
