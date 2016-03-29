%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_HeatFlow.time;

%MainDrive heat flow
Temp       = Scp_HeatFlow.signals(1).values(:,1);
Convection = Scp_HeatFlow.signals(2).values(:,1)/1000;
Radiation  = Scp_HeatFlow.signals(3).values(:,1)/1000;

%% HeatFlow
figure()
line(Time,[Temp, ...
      ]);
title('Temperatur') 
ylabel('Temperatur [K]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Temperatur')

figure()
line(Time,[Convection, ...
       Radiation, ...
      ]);
title('Konvection, Strahlung')  
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Konvektion', 'Strahlung')

