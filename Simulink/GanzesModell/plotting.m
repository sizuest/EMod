%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_MainDrive.time;

%Main Drive
P_Tot                   = Scp_MainDrive.signals(1).values(:,1)/1000;
P_mech_MainDrive         =Scp_MainDrive.signals(2).values(:,1)/1000;
P_Verlust_MainDrive      = Scp_MainDrive.signals(3).values(:,1)/1000;
P_El_MainDrive          = Scp_MainDrive.signals(4).values(:,1)/1000;
n_MainDrive          = Scp_MainDrive.signals(5).values(:,1);

%Drives
P_mech_Drives         =Scp_Drive.signals(1).values(:,1)/1000;
P_Verlust_Drives      = Scp_Drive.signals(2).values(:,1)/1000;
P_El_Drives          = Scp_Drive.signals(3).values(:,1)/1000;
n_Drives          = Scp_Drive.signals(4).values(:,1);

%MainDrive heat flow
T_MainDrive          = Scp_MainDriveT.signals(1).values(:,1);
Convection_MainDrive         = Scp_MainDriveT.signals(2).values(:,1)/1000;
Radiation_MainDrive   = Scp_MainDriveT.signals(3).values(:,1)/1000;
P_ElCooling_MainDrive= Scp_MainDriveT.signals(4).values(:,1)/1000;
P_Cooling_MainDrive= Scp_MainDriveT.signals(5).values(:,1)/1000;

%Drives heat flow
T_Drives= Scp_Drives.signals(1).values(:,1);
Convection_Drives= Scp_Drives.signals(2).values(:,1)/1000;
Radiation_Drives= Scp_Drives.signals(3).values(:,1)/1000;

%Cabinet heat flow
T_Cabinet= Scp_Cabinet.signals(1).values(:,1);
Convection_Cabinet= Scp_Cabinet.signals(2).values(:,1)/1000;
Radiation_Cabinet= Scp_Cabinet.signals(3).values(:,1)/1000;
P_ElCooling_Cabinet=Scp_Cabinet.signals(4).values(:,1)/1000;
P_Cooling_Cabinet=Scp_Cabinet.signals(5).values(:,1)/1000;


%% Evaluation

%PowerMean = mean(Scp_MainDrive.signals(1).values);


%% Power demand 
%Maschin
figure()
area(Time,[
        P_ElCooling_Cabinet,...
        P_ElCooling_MainDrive,...
        P_El_Drives,...
        P_El_MainDrive,...
        P_Tot, ... 
      ]);
title('Maschin')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('P Cooling Cabinet','P Cooling Main Drive', 'P Drives', 'P Main Drive', 'P TOT')

%MainDrive
figure()
line(Time,[P_Tot, ...
       P_mech_MainDrive , ...
       P_Verlust_MainDrive, ...
       P_El_MainDrive,...
       P_ElCooling_MainDrive,...
       P_Cooling_MainDrive,...
      ]);
title('MainDrive')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('P tot','Power Mech', 'Power Verlust', 'Power El','Power el Cooling', 'Power Cooling')

%Drives
figure()
line(Time,[P_mech_Drives , ...
       P_Verlust_Drives, ...
       P_El_Drives,...
      ]);
title('Drive') 
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Power Mech', 'Power Verlust', 'Power El')



figure()
line(Time,[n_MainDrive, ...
            n_Drives,...
      ]);
ylabel('n','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Wirkungsgrad mainDrive','Wirkungsgrad Drives')


figure()
line(Time,[T_MainDrive, ...
           T_Drives,...
           T_Cabinet,...
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

%Drives
figure()
line(Time,[Convection_Drives, ...
       Radiation_Drives , ...
      ]);
title('Drive')  
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Convection Drive', 'Radiation Drive')

%Cabinet
figure()
line(Time,[Convection_Cabinet, ...
       Radiation_Cabinet, ...
      ]);
title('Cabinet')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Convection Cabinet', 'Radiation Cabinet')



% figure ()
% plot(Time, [Scp_Main.signals(2).values(:,1),Scp_Main.signals(2).values(:,2),Scp_Main.signals(2).values(:,3),Scp_Main.signals(2).values(:,4)])
% legend('Convection','Extrated', 'Q Spindel', 'Q Kuehlung Spindel')
% ylabel('Q [W]','FontSize',12) 
% xlabel('Zeit [s]','FontSize',12)
% figure()
% plot(Time,[Scp_Main.signals(3).values(:,2),Scp_Main.signals(3).values(:,1),Scp_Main.signals(3).values(:,3)])
% legend('Temperatur Cabinet (CNC)','Housing', 'Temperatur Spindel')
% ylabel('Temperatur [K]','FontSize',12) 
% xlabel('Zeit [s]','FontSize',12)
% figure ()
% plot(Time,Scp_Main.signals(4).values(:,1))
% legend('Wirkungsgrad')
% ylabel('n','FontSize',12) 
% xlabel('Zeit [s]','FontSize',12)

