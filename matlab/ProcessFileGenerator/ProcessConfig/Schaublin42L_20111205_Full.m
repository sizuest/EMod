%% PROCESSFILE: SCHAUBLIN 42L 05.12.2011
% 

%% FileCfg
FileName    = 'Measurment20111205';
FileComment = 'Prozess file zur Messung am 05.12.2011 an der Schaublin. Durchgeführt von AL und SZ.';

%% ProcessCfg
v_c = 100;  % [m/min]
l_c =  55;  % [mm]
  f = 0.15; % [mm/rev]

% Hier: Nur Werte bei der Bearbeitung angegeben. Haltezeit wird am Ende
% eingefügt.
%%
% $t_{halt}=t_{start}+\frac{l_c}{Z1_v}$

cut_times = [ 38 364-1.4 523  644  786  963  1086 1152 1276 1406 ...
              1578 1768 ...
              3868 4114 4268 4567 4718 4884 ...
              5145 5250 5427 5646 5786 ...
              5960 6097 6266 6443 6725 7260 7490 7614 ...
              7777 7836 8060 8239 ...
              8417 8579 ...
              8691 8783 8918];
% Leerläufe
empyt_runs     = [7];
empty_duration = [13];


%% X1_d
inputs.X1_d.unit        = 'mm';
inputs.X1_d.description = 'Diameter of the part at the cutting position (adjusted by the X1 axis)';
inputs.X1_d.time        = cut_times - [ 7 33 21 19 15 13 0 13 15 15 ...
                                        5 14 ...
                                        42 18 20 15 15 16 ...
                                        8 5 14 17 15 ...
                                        5 5 13 12 21 18 20 14 ...
                                        13 5 18 20 ...
                                        13 27 ...
                                        4 12 10];
inputs.X1_d.values      = [ 50 38  35.6 32.8 29.6 26.0 22.0 22.0 17.6 12.8 ...
                            40 34.8 ...
                            32.8 30.8 25.6 20.8 16.4 12.4 ...
                            40 38 43.4 31.2 28.4 ...
                            40 38 35.6 33.6 29.2 24.4 22.0 20.0 ...
                            40 38 34.0 28.8 ...
                            26.8 23.2 ...
                            21.2 18.0 15.2];
inputs.X1_d.init        = 60;

%% X1_ap
inputs.X1_ap.unit        = 'mm';
inputs.X1_ap.description = 'Cutting depth (adjusted by the X1 axis)';
inputs.X1_ap.values      = [0 1.0:0.2:1.8 0 2.0:0.2:2.4 ...
                            1.0 2.6 ...
                            1.0 1.0 2.6:-0.2:2.0 ...
                            1.0 1.0 1.8:-0.2:1.4 ...
                            1.0 1.0 1.2 1.0 2.2 2.4 1.2 1.0 ...
                            1.0 1.0 2.0 2.6 ...
                            1.0 1.8 ...
                            1.0 1.6 1.4];

%% Haltezeit

% Spindeldrehzahl [rps]
n_proc = v_c*1000/60./(pi*inputs.X1_d.values);
% Dauer des Schnittprozesses [s]
t_proc = l_c ./ ( f*n_proc );
t_proc(empyt_runs) = empty_duration;
% Start-/stopzeit (rel.) [s]
t_start     = cut_times;
t_start_rot = inputs.X1_d.time
t_halt      = cut_times + round(t_proc);

diam  = inputs.X1_d.values;
ap    = inputs.X1_ap.values;

for tiStep=1:length(t_halt)
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
inputs.Z1_v.time        = inputs.X1_d.time; % - [ 29 -133 29 -14 24 -16 18 -29 20 -33 18 -38 22 -36 12 -32 13 -24 ...
%                                                6  -1 28 42 ...
%                                                65 -1 37 -6 56 -47 59 -14 50 -15 61 -12 ...
%                                                49 -14 46 -15 47 -19 35 -15 46 -16 ...
%                                                42 -14 27 -13 65 -24 27 -17 29 -31 43 -40 93 -9 46 -7 ...
%                                                33 -27 0 -25 90 -12 94 -55 ...
%                                                43 -17 58 -23 ...
%                                                26  0 31 -9 34 -19];
inputs.Z1_v.values      = f * inputs.C1_n.values;

%% AxisBrakeCtrl
inputs.AxisBrakeCtrl.unit        = '-';
inputs.AxisBrakeCtrl.description = 'Indicates if the axis brakes are engaged';
% Achsen sind schon früher in regelung
% inputs.AxisBrakeCtrl.time        = inputs.X1_d.time - [ 29 -133 29 -14 24 -16 18 -29 20 -33 18 -38 13 -5 22 -36 12 -32 13 -24 ...
%                                                6  -1 28 42 ...
%                                                65 -1 37 -6 56 -47 59 -14 50 -15 61 -12 ...
%                                                49 -14 46 -15 47 -19 35 -15 46 -16 ...
%                                                42 -14 27 -13 65 -24 27 -17 158 -31 43 -40 93 -9 46 -7 ...
%                                                33 -27 0 -25 90 -12 94 -55 ...
%                                                43 -17 58 -23 ...
%                                                26  0 31 -9 34 -19];
% inputs.AxisBrakeCtrl.values      = 1*(0 == inputs.C1_n.values);
inputs.AxisBrakeCtrl.time        = [2,206.294946193695,302.012083530426,403.421648979187,478.266547203064,564.285471916199,607.294934272766,695.632997035980,751.503131866455,833.516273498535,856.285988807678,872.309122085571,931.341717243195,1018.41479587555,1067.74917936325,1105.48787403107,1117.29439306259,1202.68082618713,1249.48524093628,1318.84854078293,1377.67030572891,1438.81120777130,1535.16083717346,1544.85904932022,1560.67135143280,1610.63822698593,1672.83328294754,1727.64926433563,1740.50993728638,1811.34905147553,1811.55988264084,1834.75125932693,2312.49362134933,2328.51675415039,3768.70125389099,3784.93521738052,3803.69914960861,3894.56718063355,4077.14656496048,4140.60660552979,4212.07821178436,4328.03509616852,4508.50617361069,4594.52509832382,4668.52667379379,4739.57661867142,4823.48723649979,4905.28954744339,5096.93465232849,5180.84527015686,5224.90888595581,5280.77902126312,5380.71277189255,5463.56923627853,5599.76586723328,5685.15230035782,5740.17911243439,5820.71643924713,5925.49929618835,6000.34419393539,6070.97247791290,6128.52925872803,6201.26584959030,6314.69276523590,6371.61705350876,6405.34996509552,6416.10233068466,6482.51400089264,6548.29317855835,6649.91357517242,6650.12440586090,6775.77950191498,6924.83680534363,6925.04763650894,6925.68012857437,6929.68591165543,6936.85415554047,6938.32997035980,6940.64910793304,6940.85993862152,6941.49243068695,6944.23322963715,6952.45562696457,6954.14227247238,6956.46141052246,6978.38780307770,7002.42250251770,7007.90410089493,7010.64489984512,7015.28317499161,7218.10230684280,7326.04762411118,7397.94089221954,7513.47611474991,7567.87043523788,7634.70376634598,7745.81154441834,7887.06811189652,7950.10649108887,8095.36884212494,8137.74581241608,8313.57861471176,8374.29785585403,8452.09438323975,8522.30100584030,8614.85568237305,8665.45504999161,8708.46451234818,8752.73895883560,8804.18164920807,8884.50814533234,8947.75735473633;];
inputs.AxisBrakeCtrl.values      = repmat([0 1], 1, length(inputs.AxisBrakeCtrl.time)/2);

%% X1_v
inputs.X1_v.unit        = 'mm/min';
inputs.X1_v.description = 'Speed along the X-Axis';
inputs.X1_v.time        = [0];
inputs.X1_v.values      = [0];

%% LubFlow
inputs.LubFlow.unit        = 'kg/s';
inputs.LubFlow.description = 'Lubrication flow';
% Starts about 5s ahead, and ends when C1_n=0;
% inputs.LubFlow.time        = inputs.C1_n.time + [ 1 -1 23 0 6 -1 7 0 6 0 5 -1 8 0 3 -1 5 0 7 0 ...
%                                                   5 0 10 0 ...
%                                                   13 0 11 0 15 0 10 0 9 0 9 0 ...
%                                                   7 0 5 0 9 0 10 0 10 0 ...
%                                                   5 0 5 0 10 0 10 0 16 0 12 0 14 0 8 0 ...
%                                                   12 0 4 0 12 0 14 0 11 0 23 0 4 0 9 0 7 0];
inputs.LubFlow.time        = [32,71.8470015525818,354.149306774139,390.201355934143,507.634055137634,546.005241870880,632.024167060852,666.178740024567,775.389041423798,806.381154060364,954.595135211945,980.527310848236,1095.85170269012,1100.91163969040,1142.44528722763,1166.47998666763,1265.99207639694,1287.70763826370,1397.55043172836,1414.83854913712,1573.17240333557,1605.42950057983,1716.74810886383,1724.75967550278,1757.64926433563,1791.38217639923,3855.62554216385,3889.99094581604,4103.14078187943,4134.97621726990,4253.04140806198,4285.29850482941,4557.27010583878,4580.88314390183,4709.06820821762,4729.09712457657,4875.62446022034,4892.49091577530,5137.47618722916,5171.84159088135,5245.21067380905,5276.20278644562,5417.88101577759,5450.34894323349,5635.66912698746,5667.08290100098,5776.08237218857,5806.44199228287,5955.07763481140,5987.54556226730,6092.11758852005,6123.10970115662,6256.35470247269,6290.08761405945,6400.98456144333,6404.56868314743,6433.66331958771,6465.49875497818,6709.00821161270,6745.27109146118,7248.52396821976,7277.19694328308,7476.64278364182,7505.31575870514,7605.88200139999,7627.80839395523,7764.84834814072,7804.69534969330,7831.68167924881,7862.67379188538,8047.78314495087,8082.99187135696,8224.67010068893,8258.82467365265,8407.88197755814,8435.50079870224,8555.88512754440,8595.09963750839,8687.44348335266,8705.78575420380,8774.51656150818,8794.96713924408,8910.71319246292,8928.63380193710;];
inputs.LubFlow.values      = repmat([1 0], 1, length(inputs.LubFlow.time)/2);
% inputs.LubFlow.values      = (inputs.C1_n.values>0)*10/60*883/1000;

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

% inputs.ChipConv.time        = inputs.C1_n.time - [ 4 0 22 1 25 2 9 1 13 0 13 1 8 0 11 0 7 0 5 1 ...
%                                                    0 0 9 0 ...
%                                                    0 0 15 0 28 0 16 0 8 0 17 0 ...
%                                                    12 0 7 0 12 0 0 0 9 0 ...
%                                                    10 0 7 0 11 0 12 0 12 0 19 0 10 0 10 0 ...
%                                                    1 0 1 0 10 0 10 0 ...
%                                                    10 0 11 0 ...
%                                                    3 0 8 0 8 0];
% inputs.ChipConv.values      = 1*(inputs.C1_n.values>0);
inputs.ChipConv.time        = [27,72.9610924720764,189.128807067871,204.308617115021,309.513135433197,309.723966121674,312.675596237183,391.104615688324,487.665075778961,547.119332313538,615.639309406281,667.081999778748,757.317538738251,807.284413814545,936.523632049561,981.641401290894,1076.93687677383,1077.14770793915,1080.09933757782,1101.18240737915,1128.37956714630,1167.59407711029,1253.82383298874,1289.03255939484,1386.22551107407,1415.95263957977,1539.92109012604,1543.71604299545,1570.70237207413,1606.54359102249,1698.67660617828,1725.03044319153,1745.69185161591,1792.49626684189,3827.43416547775,3890.89420557022,4081.48515701294,4081.69598770142,4082.75014114380,4135.87947702408,4220.00092554092,4286.20176506043,4536.87946510315,4537.30112648010,4538.35527992249,4581.99723434448,4695.84581136704,4730.00038433075,4851.43886661530,4893.39417552948,5125.72960519791,5172.74485063553,5238.10236740112,5277.10604619980,5401.49615812302,5451.25220298767,5627.29583597183,5667.98616075516,5762.43831348419,5807.34525203705,5945.65019035339,5988.65965270996,6085.64177370071,6124.01296091080,6242.49981355667,6290.99087381363,6389.44881010056,6404.62862014771,6419.17593860626,6419.38676929474,6422.76006031036,6466.61284542084,6692.41252326965,6746.38518190384,7223.28422117233,7278.10020303726,7459.83626461029,7506.21901845932,7590.12963628769,7628.92248439789,7760.69167089462,7805.59860992432,7830.05497074127,7863.57705163956,8032.45244073868,8083.89513111115,8209.76105833054,8259.93876409531,8394.65958023071,8436.61488962174,8541.81940793991,8542.03023862839,8543.50605344772,8596.21372795105,8684.13012886047,8706.89984464645,8763.61330223084,8796.08122968674,8900.44242525101,8929.74789237976;];
inputs.ChipConv.values      = repmat([1 0], 1, length(inputs.ChipConv.time)/2);


% Add one empty run
% inputs.ChipConv.time        = [inputs.ChipConv.time(1:2) inputs.ChipConv.time(1)+[161.8 177] inputs.ChipConv.time(3:end)];
% inputs.ChipConv.values      = [inputs.ChipConv.values(1:2) inputs.ChipConv.values(1:2) inputs.ChipConv.values(3:end)];


%% Create file
%cd('..');
ProcessFileGenerator(FileName, inputs, FileComment);
