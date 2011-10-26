%% PARAMETERS Drives
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING
%% INITIALISATION
% INITIALISATION OF THE MODEL

%% Set charset
%bdclose all
%slCharacterEncoding('ISO-8859-1')
%% General
SimulTime = 60;
Ein=1;
PR=25;          % P Regler
Vorschub=1;     % Vorschub [m/s]
Kraft=100;      % Prozesskraft [N]
hsp=200;        % Spindelsteigung [mm];
r= 0.2;         % Reibung;
m=15;           % Masse des Motors etc. [kg]                                        
%% Job
%Job.time =[ 0, 6,13,21,25,30,36,43,49,53,56,60];
Job.time =[ 0, 1,2,3,4,5,7,9,11,13,15,17];
Job.Vorschub= [1,1,0.5,1,3,2,2,1,2,1.5,0.5,1];      % Vorschub [m/s]
%Job.Vorschub= [1,1,1,1,1,1,1,1,1,1,1,1];           % Vorschub [m/s]
Job.Kraft=[0,8,20,30,30,20,80,30,30,30,30,10];      % Prozesskraft [N]
%Job.Kraft=[0,0,0,0,0,0,0,0,0,0,0,0];               % Prozesskraft [N]
%% Antriebe
MaxDrehmoment = 38;       % Maximales Drehmoment [Nm]
MaxDrehzahl = 3000;       % Maximale Drehzahl [U/min]
Antriebe.Pmax = 1.5*4000; % Maximal Leistung der Spindel [W] 
Antriebe.Theta = .0016;   % Trägheitsmoment [m^2kg]
Antriebe.Tabelomega= [150	300	450	600	750	900	1050	1200	1350	1500	1650	1800	1950	2100	2250	2400	2550	2700	2850	3000];
Antriebe.TabelM = [0.475	0.95	1.425	1.9	2.375	2.85	3.325	3.8	  4.275	 4.75 	5.225	5.7	6.175 	6.65	7.125	7.6 	8.075	8.55 9.025 9.5];
Antriebe.Tabeleta=([ 
0.100	0.100	0.100	0.100	0.100	0.100	0.700	0.700	0.700	0.700	0.700	0.700	0.700	0.700	0.700	0.100	0.100	0.100	0.100	0.100
0.100	0.100	0.100	0.700	0.730	0.750	0.755	0.765	0.770	0.770	0.770	0.770	0.765	0.760	0.755	0.753	0.750	0.748	0.745	0.740
0.100	0.100	0.710	0.750	0.780	0.800	0.810	0.820	0.825	0.825	0.825	0.825	0.825	0.820	0.815	0.815	0.810	0.810	0.805	0.800
0.100	0.100	0.740	0.780	0.805	0.825	0.835	0.845	0.850	0.855	0.855	0.855	0.855	0.850	0.855	0.853	0.850	0.848	0.845	0.840
0.100	0.100	0.755	0.800	0.825	0.840	0.855	0.855	0.860	0.860	0.860	0.860	0.870	0.865	0.870	0.863	0.865	0.868	0.865	0.860
0.100	0.100	0.765	0.805	0.835	0.850	0.860	0.865	0.870	0.870	0.870	0.870	0.880	0.880	0.875	0.870	0.875	0.875	0.870	0.870
0.100	0.710	0.785	0.825	0.845	0.860	0.875	0.875	0.880	0.880	0.880	0.880	0.890	0.885	0.880	0.880	0.880	0.878	0.875	0.880
0.100	0.755	0.825	0.850	0.870	0.875	0.890	0.893	0.895	0.895	0.895	0.890	0.885	0.880	0.880	0.875	0.875	0.870	0.865	0.870
0.100	0.790	0.845	0.870	0.885	0.890	0.903	0.905	0.908	0.908	0.905	0.895	0.880	0.875	0.875	0.870	0.860	0.860	0.855	0.850
0.720	0.825	0.865	0.885	0.900	0.903	0.905	0.908	0.910	0.910	0.910	0.900	0.875	0.870	0.870	0.865	0.850	0.845	0.840	0.835
0.750	0.850	0.885	0.900	0.905	0.905	0.908	0.910	0.913	0.913	0.905	0.895	0.870	0.865	0.865	0.860	0.845	0.840	0.835	0.830
0.770	0.860	0.895	0.905	0.908	0.908	0.910	0.913	0.915	0.915	0.895	0.890	0.865	0.860	0.860	0.855	0.835	0.825	0.820	0.815
0.790	0.875	0.900	0.910	0.910	0.910	0.913	0.915	0.918	0.918	0.890	0.885	0.860	0.855	0.855	0.850	0.820	0.800	0.795	0.780
0.820	0.880	0.905	0.915	0.913	0.913	0.915	0.918	0.920	0.920	0.885	0.880	0.855	0.850	0.850	0.845	0.815	0.790	0.785	0.770
0.830	0.890	0.910	0.920	0.915	0.915	0.918	0.920	0.923	0.918	0.880	0.875	0.850	0.845	0.845	0.840	0.810	0.780	0.775	0.760
0.840	0.900	0.915	0.923	0.918	0.918	0.920	0.923	0.925	0.915	0.875	0.870	0.845	0.840	0.840	0.835	0.805	0.770	0.765	0.750
0.850	0.905	0.920	0.926	0.920	0.920	0.923	0.925	0.928	0.913	0.870	0.865	0.840	0.835	0.835	0.830	0.800	0.760	0.755	0.740
0.860	0.910	0.925	0.930	0.923	0.923	0.925	0.928	0.925	0.910	0.865	0.860	0.835	0.830	0.830	0.825	0.795	0.750	0.745	0.730
0.870	0.915	0.930	0.933	0.925	0.925	0.928	0.925	0.920	0.908	0.860	0.855	0.830	0.825	0.825	0.820	0.790	0.740	0.735	0.720
0.880	0.920	0.935	0.935	0.928	0.928	0.925	0.920	0.918	0.905	0.855	0.850	0.825	0.820	0.820	0.810	0.785	0.730	0.725	0.710
]);
