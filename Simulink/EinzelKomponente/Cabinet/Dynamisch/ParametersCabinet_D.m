%% PARAMETERS
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING

%% General
SimulTime = 1000;
T_amb     = 296;    % [K] Ambient Temperature

%% Shedule
Schedule.time =           [0, 100,900,1000];
Schedule.signals.values = [0, 4,4,0]';

%% Job
Job.time =       [ 0, 150,190,230,240,320,330,400,405,475,480,600];
Job.P_MainDrive  = [ 20, 40, 15,10, 9,5,13,5,12,9,2,1]*1000; %Verlust Main Drive
%% Heatflow 
cp= 460;            % [J/kg/K] Internal heat capacity
R  = 25;            % [W/K/m^2] Heat transfere resistance of surface
e=  0.9;             % emission
sb=5.67*(10^-8);     % [W/m^2/K^4] Stefan Boltzmann constant      

%% CNC
% *Cabinet*
CNC.Cabinet.m  = 200;     % [kg] Mass inside the cabinet
CNC.Cabinet.A  = 2+2.5+5; % [m^2] surface area


CNC.Cooling.Tmin = 40+273;
CNC.Cooling.Tmax = 45+273;
CNC.Cooling.T_int = [20 30 40 50]+273;
CNC.Cooling.T_ext = [20 30 40 50]+273;
CNC.Cooling.Qdot  = [1650, 1283, 912,  550; ...
                     2250, 1883, 1517, 1150; ...
                     2850, 2483, 2117, 1750; ...
                     3450, 3083, 2717, 2350];
CNC.Cooling.epsilon = 2;

% *SimuDrive*
CNC.SimoDrive.OpPointsP   = [0 89e3];
CNC.SimoDrive.OpPointsEta = [.4 .92];
CNC.SimoDrive.PStandby    = 500;