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
Job.time =       [ 0, 150,160,230,240,320,330,400,405,475,480,600];
Job.P_verDrive  = [ 1, 0.5, 0.8,2, 1.5,3,0.4,0.8,4,3.5,2,1]*1000;
%% Heatflow 
cp= 460;            % [J/kg/K] Internal heat capacity
R  = 25;            % [W/K/m^2] Heat transfere resistance of surface
e=  0.9;            % emission
sb=5.67*(10^-8);    % [W/m^2/K^4] Stefan Boltzmann constant

%%Heatflow Drive
Drive.m = 20;       % [kg] Mass
Drive.A  = 0.6;     % [m^2] surface area
