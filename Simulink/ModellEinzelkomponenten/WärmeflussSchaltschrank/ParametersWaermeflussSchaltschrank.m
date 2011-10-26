%% PARAMETERS
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING

%% Allgemeine Paramter
SimulTime      = 1200;
T_Umgebung     = 296;    % Umgebungstemperatur [K] 
%% Spindel & Achsen
P_SA= 60000;             %Leistungsbezug Spindel und Achsen [W] 
%% Wärmefluss
cp.Stahl= 460;      % Wärmecapazität Stahl [J/kg/K] 
cp.Luft=1007;       % Wärmecapazität Luft [J/kg/K] 
e=0.6;              % Emission
sb=5.67*(10^-8);    % Stefan Boltzmann konstant[W/m^2/K^4] 
roh.Luft=1.2;       % Dichte Luft [kg/m^3]
roh.Stahl=8750;     % Dichte Stahl [kg/m^3]



%%Schaltschrank Wärmefluss
Pr=0.7;                  % Prandtl-Zahl
f1=0.3448;               % Einfluss Prandtl-Zahl

a=0.51;                  % Breite Schaltschrank [m]
b=2.6;                   % Länge Schaltschrank [m]
c=1.9;                   % Höhe Schaltschrank [m]
l=2*c;                   % Anströmlänge Seitenwände [m] 
l1=(a*b/(2*(a+b)));      % Anströmlänge Boden und Decke (nicht verwendet Korregiert in der Oberfläche)[m]
geometrie=l+l1;
r1=0;                    % Blechdicke [m]
r2=0.003;                % Blechdicke [m]
A.Schaltschrank=2*a*c+1.5*a*b+b*c;                      % Oberfläche Schaltschrank [m^2] (1.5 Korrektur für Unterseite)
A.Schaltschrank1=2*(a*c+a*b+b*c);                       % Oberfläche Schaltschrank [m^2] 
m.Schaltschrank=A.Schaltschrank1*(r2-r1)*roh.Stahl;     % Masse Schaltschrank [kg]
m.Luft_Schaltschrank=a*b*c*roh.Luft;                    % Masse Luft im Schaltschrank [kg] 
Temperatur.Tabel=[0	10	20	30	40	50	60	70	80	90	100	120	140	160	180	200];
Waermeleitfaehigkeit.Tabel=[0.0244	0.0251	0.0259	0.0266	0.0274	0.0281	0.0288	0.0295	0.0302	0.0309	0.0316	0.0330	0.0343	0.0357	0.0370	0.0382];
KinematischeViskositaet.Tabel=[1.35E-05	1.44E-05	1.53E-05	1.63E-05	1.72E-05	1.82E-05	1.92E-05	2.03E-05	2.13E-05	2.24E-05	2.35E-05	2.57E-05	2.80E-05	3.04E-05	3.29E-05	3.54E-05
];

%% 
% *Schaltschrank Regler*
Schaltschrank_Kuehlung.Tmin = 28+273;   % Regler T min [K] 
Schaltschrank_Kuehlung.Tmax = 32+273;   % Regeler T max [K] 

%%Rittal Kühlaggregat 
EinRittal=1;
P_Rittal=1200;                                                 % Leistungsaufnahme Rittal [W]
Anzahl=2;                                                      % Anzahl Rittalschränke
VentilatorRittal=80;                                           % Leistungsaufnahme Rittal Ventilatoren [W]
n.Rittal=0.9;                                                  % Wirkungsgrad Rittal
Schaltschrank_Kuehlung.T_int = [20 30 40 50]+273;              % Temperatur im Schaltschrank [K] 
Schaltschrank_Kuehlung.T_ext = [20 30 40 50]+273;              % Temeratur in der Umgebung [K] 
Schaltschrank_Kuehlung.Qdot  = [1650, 1283, 912,  550; ...     % Kuehlleistung Rittal [W]
                     2250, 1883, 1517, 1150; ...
                     2850, 2483, 2117, 1750; ...
                     3450, 3083, 2717, 2350];

% *SimuDrive*
CNC.SimuDrive.MaxP        = 89000;          % Maximale Leistung Simudrive [W]
CNC.SimuDrive.OpPointsP   = [0 5e3 89e3];   % Leistung Simudrive [W]
CNC.SimuDrive.OpPointsEta = [.98 0.95 .97]; % Wirkungsgrad Siumudrive
CNC.SimuDrive.PStandby    = 500;            % Leistungsverbrauch ohne Last [W]
