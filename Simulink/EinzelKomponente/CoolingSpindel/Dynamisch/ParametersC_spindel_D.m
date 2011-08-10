%% PARAMETERS
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING

%% General
SimulTime = 1000;
T_amb     = 290;    % [K] Ambient Temperature

%% Shedule
Schedule.time =           [0, 100,900,1000];
Schedule.signals.values = [0, 4,4,0]';

%% Job
Job.time =       [ 0, 150,160,230,240,320,330,400,405,475,480,600];
Job.P_verMainDrive  = [ 15, 18, 15,10, 9,5,2,5,1,0.5,2,1]*1000; %Verlust Main Drive
%% Heatflow 
cp= 460;            % [J/kg/K] Internal heat capacity
R  = 25;            % [W/K/m^2] Heat transfere resistance of surface
e=  0.9;                % emission
sb=5.67*(10^-8);        % [W/m^2/K^4] Stefan Boltzmann constant

%%Heatflow MainDrive
MainDrive.m = 40;            % [kg] Mass
MainDrive.A  =0.7;           % [m^2] surface area
%% Main Drive Cooling
p1=380;                     %Power Ventilator n1
n1=0.78;                    % Wirkungsgrad n1
p2=1100;                    %Power Ventilator n2
n2=0.7;                     % Wirkungsgrad n2
p=500;                      % Power Pumpe
n=0.8;                      % Wirkungsgrad Pumpe
Venti_n2 = 273+40;


%Hydac 
MainDriveCooling.epsilon = 0.8;
MainDriveCooling.Tmin = 40+273;
MainDriveCooling.Tmax = 50+273;
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

