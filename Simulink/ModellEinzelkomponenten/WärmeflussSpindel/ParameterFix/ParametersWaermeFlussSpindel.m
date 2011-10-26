%% PARAMETERS
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING

%% General
SimulTime      = 5000;
T_Umgebung     = 296;    % [K] Umgebungstemperatur

%% Wärmefluss
cp.Stahl= 460;      % Wärmecapazität Stahl [J/kg/K] 
cp.Luft=1007;       % Wärmecapazität Luft [J/kg/K]
m.Luft=5;           % Masse Luft[kg] 
e=0.6;              % Emission
sb=5.67*(10^-8);    % Stefan Boltzmann konstant[W/m^2/K^4] 



%%Waermefluss Spindel
A.Spindel= 0.6;      % Oberfläche [m^2] 
m.Spindel =23;       % Masse [kg]
d=0.2;               % Durchmesser [m] 
l=pi/2*d;            % Anströmlänge [m]
geometrie=l;
Pr=0.7;              % Prandtl-Zahl
f3=0.325;            % Einfluss Prandtl-Zahl
Temperatur.Tabel=[0	10	20	30	40	50	60	70	80	90	100	120	140	160	180	200];
Waermeleitfaehigkeit.Tabel=[0.0244	0.0251	0.0259	0.0266	0.0274	0.0281	0.0288	0.0295	0.0302	0.0309	0.0316	0.0330	0.0343	0.0357	0.0370	0.0382];
KinematischeViskositaet.Tabel=[1.35E-05	1.44E-05	1.53E-05	1.63E-05	1.72E-05	1.82E-05	1.92E-05	2.03E-05	2.13E-05	2.24E-05	2.35E-05	2.57E-05	2.80E-05	3.04E-05	3.29E-05	3.54E-05
];
