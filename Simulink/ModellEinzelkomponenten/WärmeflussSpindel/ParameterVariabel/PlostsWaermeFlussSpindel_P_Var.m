%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS
Time = Scp_MainDrive.time;

 PowerMech       = Scp_MainDrive.signals(1).values(:,1)/1000;
 PowerVer        = Scp_MainDrive.signals(2).values(:,1)/1000;
 PowerEL         = Scp_MainDrive.signals(3).values(:,1)/1000;
 n               = Scp_MainDrive.signals(4).values(:,1);
 Drehmoment      = Scp_MainDrive.signals(5).values(:,1)/1000;
 Drehzahl        = Scp_MainDrive.signals(6).values(:,1)/1000;
  
% %% Power demand
figure()
 line(Time,[PowerMech, ...
            PowerVer, ...
            PowerEL, ...
        ]);
 ylabel('Leistung (P) [KW]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P mech', 'P ver', 'P el')
 
 
 
 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)

 
 
 % %% Drehmoment
figure()
 line(Time,[Drehmoment, ...   
        ]);
 ylabel('Drehmoment [Nm]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('Drehmoment')
 
 
 
 % %% Power demand
figure()
 line(Time,[Drehzahl, ...
        ]);
 ylabel('Drehzahl [U/min]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('Drehzahl')
%% Read Results
close all
Time = Scp_HeatFlow.time;

%Spindel Wärmefluss
Temp       = Scp_HeatFlow.signals(1).values(:,1);
Convection = Scp_HeatFlow.signals(2).values(:,1)/1000;
Radiation  = Scp_HeatFlow.signals(3).values(:,1)/1000;

%% Wärmefluss
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


s = struct('Time', {Time}, 'P_mech', {PowerMech},'P_ver', {PowerVer}, 'P_El', {PowerEL},'n', {n},'Drehmoment', {Drehmoment},'Drehzahl', {Drehzahl},'Konvektion', {Convection},'Strahlung', {Radiation},'T_Spindel', {Temp})
%Time=str2double(s.Time)

% % open the file with write permission
fid = fopen('Spindel_Temperatur.txt', 'w');
fprintf(fid,'%s','Time');
fprintf(fid,'\t');
fprintf(fid,'%s','P_mech');
fprintf(fid,'\t');
fprintf(fid,'%s','P_ver');
fprintf(fid,'\t');
fprintf(fid,'%s','P_El');
fprintf(fid,'\t');
fprintf(fid,'%s','n');
fprintf(fid,'\t');
fprintf(fid,'%s','Drehmoment');
fprintf(fid,'\t');
fprintf(fid,'%s','Drehzahl');
fprintf(fid,'\t');
fprintf(fid,'%s','Konvektion');
fprintf(fid,'\t');
fprintf(fid,'%s','Strahlung');
fprintf(fid,'\t');
fprintf(fid,'%s\n','T_Spindel');


for i=1:length(s.Time);
fprintf(fid,'%f', s.Time(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.P_mech(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.P_ver(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.P_El(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.n(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.Drehmoment(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.Drehzahl(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.Konvektion(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.Strahlung(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.T_Spindel(i));
fprintf(fid,'\n');
end
fclose(fid);