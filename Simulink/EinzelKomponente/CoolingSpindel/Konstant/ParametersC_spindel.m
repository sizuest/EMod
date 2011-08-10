%% PARAMETERS
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING

%% General
SimulTime = 1000;
T_amb     = 296;    % [K] Ambient Temperature

%% Heatflow 
cp= 460;            % [J/kg/K] Internal heat capacity
R  = 25;            % [W/K/m^2] Heat transfere resistance of surface
e=  0.9;            % emission
sb=5.67*(10^-8);    % [W/m^2/K^4] Stefan Boltzmann constant

%%Heatflow MainDrive
MainDrive.m = 10;        % [kg] Mass
MainDrive.A  = 0.6;      % [m^2] surface area

%% Main Drive Cooling
p1=380;
n1=0.78;
p2=1100;
n2=0.7;
p=500;
n=0.8;
Venti_n2 = 273+40;
MainDrive.m = 40;            % [kg] Mass
MainDrive.R  = 25;            % [W/K/m^2] Heat transfere resistance of surface
MainDrive.A  = 0.7;           % [m^2] surface area
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

