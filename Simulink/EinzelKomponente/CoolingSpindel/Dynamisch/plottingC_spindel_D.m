%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Cspindel.time;

%MainDrive heat flow
T_MainDrive          = Scp_Cspindel.signals(1).values(:,1);
Convection_MainDrive  = Scp_Cspindel.signals(2).values(:,1)/1000;
Radiation_MainDrive   = Scp_Cspindel.signals(3).values(:,1)/1000;
P_ElCooling_MainDrive= Scp_Cspindel.signals(4).values(:,1)/1000;
P_Cooling_MainDrive= Scp_Cspindel.signals(5).values(:,1)/1000;
P_ver_Cooling_MainDrive= Scp_Cspindel.signals(6).values(:,1)/1000;

%% Power demand 

figure()
line(Time,[P_ElCooling_MainDrive, ...
    P_Cooling_MainDrive, ...
    P_ver_Cooling_MainDrive,...
      ]);
title('Temperatur') 
ylabel('Power [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('P EL Cooling','P Cooling','P ver Coolling')

figure()
line(Time,[T_MainDrive, ...
      ]);
title('Temperatur') 
ylabel('Temperatur [K]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('T MainDrive','T Drives', 'T Cabinet')

%% Heat
%Main Drive
figure()
line(Time,[Convection_MainDrive, ...
       Radiation_MainDrive , ...
      ]);
title('MainDrive')  
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Convection Main Drive', 'Radiation Main Drive')

