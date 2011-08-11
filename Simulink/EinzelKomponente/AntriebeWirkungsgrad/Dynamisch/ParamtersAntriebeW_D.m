%% PARAMETERS Drives
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING
%% INITIALISATION
% INITIALISATION OF THE MODEL

%% Set charset
%bdclose all
%slCharacterEncoding('ISO-8859-1')
%% General
SimulTime = 600;
T_amb     = 296;    % [K] Ambient Temperature

%% Shedule
Schedule.time =           [0, 100,600];
Schedule.signals.values = [4, 4,4]';

%% Job Drives
Job.time =       [ 0, 150,160,230,240,320,330,400,405,475,480,600];
%% Drives
Job.Vorschub= [1,1,1,1,3,1,1,1,1,1,1,1];
Job.Kraft=[0,30,30,30,30,30,30,30,30,30,30,0];
hsp=200;        %Spindelsteigung [mm];
r= 0.2;         %Reibung;
%% Heatflow
cp= 460;                % [J/kg/K] Internal heat capacity
R  = 25;                % [W/K/m^2] Heat transfere resistance of surface
e=  0.9 ;               % Emission coefficient
sb=5.67*(10^-8);        % [W/m^2/K^4] Boltzmann constant
Drive.A  = 0.7;               % [m^2] surface area
Drive.m = 25;           % [kg] Mass
%% Drive
Drive.Pmax = 2*4000;  % [W] Maximum mechanical shaft power
Drive.Theta = .03;    % [m^2 kg] Main drive inertia
Drive.Tabelomega= [150	300	450	600	750	900	1050	1200	1350	1500	1650	1800	1950	2100	2250	2400	2550	2700	2850	3000];
Drive.TabelM = [0.475	0.95	1.425	1.9	2.375	2.85	3.325	3.8	  4.275	 4.75 	5.225	5.7	6.175 	6.65	7.125	7.6 	8.075	8.55 9.025 9.5];

Drive.Tabeleta=([   
0.100	0.100	0.100	0.100	0.100	0.100	0.700	0.700	0.700	0.700	0.700	0.700	0.700	0.700	0.700	0.100	0.100	0.100	0.100	0.100
0.100	0.100	0.100	0.700	0.730	0.750	0.755	0.765	0.770	0.770	0.770	0.770	0.765	0.760	0.755	0.753	0.750	0.748	0.745	0.740
0.100	0.100	0.710	0.750	0.780	0.800	0.810	0.820	0.825	0.825	0.825	0.825	0.825	0.820	0.815	0.815	0.810	0.810	0.805	0.800
0.100	0.100	0.740	0.780	0.805	0.825	0.835	0.845	0.850	0.855	0.855	0.855	0.855	0.850	0.855	0.853	0.850	0.848	0.845	0.840
0.100	0.100	0.755	0.800	0.825	0.840	0.855	0.855	0.860	0.860	0.860	0.860	0.870	0.865	0.870	0.863	0.865	0.868	0.865	0.860
0.100	0.100	0.765	0.805	0.835	0.850	0.860	0.865	0.870	0.870	0.870	0.870	0.880	0.880	0.875	0.870	0.875	0.875	0.870	0.870
0.100	0.710	0.785	0.825	0.845	0.860	0.875	0.875	0.880	0.880	0.880	0.880	0.890	0.885	0.880	0.880	0.855	0.855	0.865	0.860
0.100	0.755	0.825	0.850	0.870	0.875	0.890	0.893	0.895	0.895	0.895	0.890	0.885	0.880	0.880	0.875	0.845	0.845	0.855	0.850
0.100	0.790	0.845	0.870	0.885	0.890	0.903	0.905	0.908	0.908	0.905	0.895	0.880	0.875	0.875	0.870	0.840	0.840	0.845	0.840
0.720	0.825	0.865	0.885	0.900	0.903	0.905	0.908	0.910	0.910	0.910	0.900	0.875	0.870	0.870	0.865	0.835	0.830	0.835	0.830
0.750	0.850	0.885	0.900	0.905	0.905	0.908	0.910	0.913	0.913	0.905	0.895	0.870	0.865	0.865	0.860	0.830	0.820	0.825	0.820
0.770	0.860	0.895	0.905	0.908	0.908	0.910	0.913	0.915	0.915	0.895	0.890	0.865	0.860	0.860	0.855	0.825	0.810	0.805	0.800
0.790	0.875	0.900	0.910	0.910	0.910	0.913	0.915	0.918	0.918	0.890	0.885	0.860	0.855	0.855	0.850	0.820	0.800	0.795	0.780
0.820	0.880	0.905	0.915	0.913	0.913	0.915	0.918	0.920	0.920	0.885	0.880	0.855	0.850	0.850	0.845	0.815	0.790	0.785	0.770
0.830	0.890	0.910	0.920	0.915	0.915	0.918	0.920	0.923	0.918	0.880	0.875	0.850	0.845	0.845	0.840	0.810	0.780	0.775	0.760
0.840	0.900	0.915	0.923	0.918	0.918	0.920	0.923	0.925	0.915	0.875	0.870	0.845	0.840	0.840	0.835	0.805	0.770	0.765	0.750
0.850	0.905	0.920	0.926	0.920	0.920	0.923	0.925	0.928	0.913	0.870	0.865	0.840	0.835	0.835	0.830	0.800	0.760	0.755	0.740
0.860	0.910	0.925	0.930	0.923	0.923	0.925	0.928	0.925	0.910	0.865	0.860	0.835	0.830	0.830	0.825	0.795	0.750	0.745	0.730
0.870	0.915	0.930	0.933	0.925	0.925	0.928	0.925	0.920	0.908	0.860	0.855	0.830	0.825	0.825	0.820	0.790	0.740	0.735	0.720
0.880	0.920	0.935	0.935	0.928	0.928	0.925	0.920	0.918	0.905	0.855	0.850	0.825	0.820	0.820	0.810	0.785	0.730	0.725	0.710
]);