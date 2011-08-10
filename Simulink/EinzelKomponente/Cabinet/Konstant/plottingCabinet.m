%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Cabinet.time;


%Cabinet heat flow
T_Cabinet= Scp_Cabinet.signals(1).values(:,1);
Convection_Cabinet= Scp_Cabinet.signals(2).values(:,1)/1000;
Radiation_Cabinet= Scp_Cabinet.signals(3).values(:,1)/1000;
P_ElCooling_Cabinet=Scp_Cabinet.signals(4).values(:,1)/1000;
P_Cooling_Cabinet=Scp_Cabinet.signals(5).values(:,1)/1000;


%% Evaluation

%PowerMean = mean(Scp_MainDrive.signals(1).values);


%% Power demand 
%Cabinet
figure()
line(Time,[
        P_ElCooling_Cabinet,...
        P_Cooling_Cabinet,...
      ]);
title('Cabinet')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('P ElCooling Cabinet','P Cooling Cabinet')


figure()
line(Time,[T_Cabinet,...
      ]);
title('Temperatur') 
ylabel('Temperatur [K]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('T Cabinet')


%Cabinet
figure()
line(Time,[Convection_Cabinet, ...
       Radiation_Cabinet, ...
      ]);
title('Cabinet')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Convection Cabinet', 'Radiation Cabinet')


