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

%%Heatflow Drive
Drive.m = 20;       % [kg] Mass
Drive.A  = 0.6;     % [m^2] surface area
