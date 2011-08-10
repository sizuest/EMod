
%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Drives.time;

 PowerMech         = Scp_Drives.signals(1).values(:,1);
 PowerVer          = Scp_Drives.signals(2).values(:,1);
 PowerEl           = Scp_Drives.signals(3).values(:,1);
 
 n                 = Scp_Drives.signals(4).values(:,1);
  
  
 T                  = Scp_DrivesT.signals(1).values(:,1);
 C                  = Scp_DrivesT.signals(2).values(:,1);
 R                  = Scp_DrivesT.signals(3).values(:,1);
 
% %% Power demand
figure()
 line(Time,[PowerMech , ...
            PowerVer, ...
            PowerEl, ...
        ]);
 title('Power Drives')
 ylabel('Leistung (P) [W]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P mech', 'P Ver', 'P el')
 
 
 figure ()
  line(Time,[C, ...
            R, ...
        ]);
 ylabel('Leistung (P) [W]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('Convection Drive', 'Radation Drive')
 
  figure()
 plot(Time,T)
 title('Temperature Drives')
 legend('Temperatur Drive')
 ylabel('Temperatur [K]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 
 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)

