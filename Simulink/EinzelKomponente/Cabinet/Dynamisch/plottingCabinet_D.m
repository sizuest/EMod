%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Cabinet.time;


%Cabinet heat flow
El_tot = Scp_Cabinet.signals(1).values(:,1)/1000;
T_Cabinet_aussen = Scp_Cabinet.signals(2).values(:,1);
T_Cabinet= Scp_Cabinet.signals(3).values(:,1);
Convection_Cabinet= Scp_Cabinet.signals(4).values(:,1);
Radiation_Cabinet= Scp_Cabinet.signals(5).values(:,1);
P_ElCooling_Cabinet=Scp_Cabinet.signals(6).values(:,1)/1000;
P_Cooling_Cabinet=Scp_Cabinet.signals(7).values(:,1)/1000;


%% Evaluation

%PowerMean = mean(Scp_MainDrive.signals(1).values);


%% Power demand 
%Cabinet

figure()
line(Time,[
        El_tot,...
      ]);
title('Cabinet')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('P tot')

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
           T_Cabinet_aussen,... 
      ]);
title('Temperatur') 
ylabel('Temperatur [K]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('T Cabinet', 'T Cabinet aussen')


%Cabinet
figure()
line(Time,[Convection_Cabinet, ...
       Radiation_Cabinet, ...
      ]);
title('Cabinet')
ylabel('Leistung (P) [W]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Convection Cabinet', 'Radiation Cabinet')


