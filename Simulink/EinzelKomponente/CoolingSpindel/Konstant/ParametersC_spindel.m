%% PARAMETERS
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING

%% General
SimulTime = 1000;
T_amb     = 298;    % [K] Ambient Temperature

%% Heatflow 
cp= 460;            % [J/kg/K] Internal heat capacity IRON
e=  0.5;            % emission
sb=5.67*(10^-8);    % [W/m^2/K^4] Stefan Boltzmann constant

MainDrive.m = 25;            % [kg] Mass
MainDrive.R  = 1/25/0.05;     % [W/K/m^2] Heat transfere resistance of surface
MainDrive.A  = 0.7;           % [m^2] surface area

q= 21/(1000*60)     %Flow [m3/s]
roh=995.64          %[kg/m3] water
K_W.m = q/roh
K_W.cp=4190                 %[J/kg/K] Water

%% Main Drive Cooling
p1=380;
n1=0.78;
p2=1100;
n2=0.7;
p=500;
n=0.8;
Venti_n2 = 273+40;


MainDriveCooling.epsilon = 0.8;
MainDriveCooling.Tmin = 30+273;
MainDriveCooling.Tmax = 40+273;
MainDriveCooling.Air.T_int = [10 20 30 40 50 60 70 80 90]+273;
MainDriveCooling.Air.T_ext = [10 20 30 40]+273;
MainDriveCooling.Air.P  = [     150, 0, 0,  0; ...
                                1500, 150, 0, 0; ...
                                3000, 1500, 150, 0; ...
                                4500, 3000, 1500, 150; ...
                                6000, 4500, 3000, 1500; ...
                                7500, 6000, 4500, 3000; ...
                                9000, 7500, 6000, 4500; ...
                                10500, 9000, 7500, 6000; ...
                                12000, 10500, 9000, 7500  ];
                            
  MainDriveCooling.Air.P_n2  = [200, 0, 0,  0; ...
                                2000, 200, 0, 0; ...
                                3500, 2000, 200, 0; ...
                                5000, 3500, 2000, 200; ...
                                6500, 5000, 3500, 2000; ...
                                8000, 6500, 5000, 3500; ...
                                9500, 8000, 6500, 5000; ...
                                11000, 9500, 8000, 6500; ...
                                12500, 11000, 9500, 8000  ];               

