
%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Drives.time;

 PowerMech         = Scp_Drives.signals(1).values(:,1)/1000;
 PowerVer          = Scp_Drives.signals(2).values(:,1)/1000;
 PowerEl           = Scp_Drives.signals(3).values(:,1)/1000;
 
 n                  = Scp_Drives.signals(4).values(:,1);
 
%% Power demand
figure()
 line(Time,[PowerMech , ...
            PowerVer, ...
            PowerEl, ...
        ]);
 ylabel('Leistung (P) [KW]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P mech', 'P Ver', 'P el')
 
 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)

