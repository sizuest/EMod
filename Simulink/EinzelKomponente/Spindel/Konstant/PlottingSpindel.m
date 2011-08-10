
%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_MainDrive.time;

 PowerMech       = Scp_MainDrive.signals(1).values(:,1)/1000;
 PowerVer        = Scp_MainDrive.signals(2).values(:,1)/1000;
 PowerEL         = Scp_MainDrive.signals(3).values(:,1)/1000;
 n               = Scp_MainDrive.signals(4).values(:,1);
 
 T                 = Scp_MainDriveT.signals(1).values(:,1);
 C                 = Scp_MainDriveT.signals(2).values(:,1)/1000;
 R                 = Scp_MainDriveT.signals(3).values(:,1)/1000;

% %% Power demand
figure()
 line(Time,[PowerMech, ...
            PowerVer, ...
            PowerEL, ...
        ]);
 ylabel('Leistung (P) [KW]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P mech', 'P ver', 'P el')
 
 
 figure ()
 line(Time,[C, ...
            R, ...
        ]);
 ylabel('Leistung (P) [KW]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('Convection', 'Radiation')
 
 figure ()
 line(Time,[T, ...
        ]);
 ylabel('Temperatur [K]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('Temperatur MainDrive')
 
 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)

