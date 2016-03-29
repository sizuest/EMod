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
title('Schrank')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Leistungsaufnahme Rittal','Kühlleistung Rittal')


figure()
line(Time,[T_Cabinet,...
      ]);
title('Temperatur') 
ylabel('Temperatur [K]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('T Schrank')


%Cabinet
figure()
line(Time,[Convection_Cabinet, ...
       Radiation_Cabinet, ...
      ]);
title('Cabinet')
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Konvektion Schrank', 'Strahlung Schrank')


%%Printing
s = struct('Time', {Time}, 'P_ELKuehlung', {P_ElCooling_Cabinet},'P_Kuehelleistung', {P_Cooling_Cabinet}, 'P_Konvektion', {Convection_Cabinet},'P_Strahlung', {Radiation_Cabinet},'T_Schrank', {T_Cabinet})
%Time=str2double(s.Time)

% % open the file with write permission
fid = fopen('Spindel_Temperatur.txt', 'w');
fprintf(fid,'%s','Time');
fprintf(fid,'\t');
fprintf(fid,'%s','P_ELKuehlung');
fprintf(fid,'\t');
fprintf(fid,'%s','P_Kuehelleistung');
fprintf(fid,'\t');
fprintf(fid,'%s','P_Konvektion');
fprintf(fid,'\t');
fprintf(fid,'%s','P_Strahlung');
fprintf(fid,'\t');
fprintf(fid,'%s\n','T_Schrank');


for i=1:length(s.Time);
fprintf(fid,'%f', s.Time(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.P_ELKuehlung(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.P_Kuehelleistung(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.P_Konvektion(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.P_Strahlung(i));
fprintf(fid,'\t');
fprintf(fid,'%f',s.T_Schrank(i));
fprintf(fid,'\n');
end
fclose(fid);
