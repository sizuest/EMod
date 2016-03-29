
%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Antriebe.time;

 PowerMech         = Scp_Antriebe.signals(1).values(:,1);
 PowerVer          = Scp_Antriebe.signals(2).values(:,1);
 PowerEL           = Scp_Antriebe.signals(3).values(:,1);
 
 n                 = Scp_Antriebe.signals(4).values(:,1);
  
  
 T                  = Scp_Waermefluss.signals(1).values(:,1);
 C                  = Scp_Waermefluss.signals(2).values(:,1);
 R                  = Scp_Waermefluss.signals(3).values(:,1);
 TL                 = Scp_Waermefluss.signals(4).values(:,1);
 
% %% Power demand
figure()
 line(Time,[PowerMech , ...
            PowerVer, ...
            PowerEL, ...
        ]);
 title('Leistung Antriebe')
 ylabel('Leistung (P) [W]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P mech', 'P Ver', 'P el')
 
 
 figure ()
  line(Time,[C, ...
            R, ...
        ]);
 ylabel('Leistung (P) [W]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('Konvection Antriebe', 'Strahlung Antriebe')
 
  figure()
 plot(Time,[T,...
            TL,...
                ]);
 title('Temperature Antriebe')
 legend('Temperatur Antriebe')
 ylabel('Temperatur [K]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 
 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)

s = struct('Time', {Time}, 'P_mech', {PowerMech},'P_ver', {PowerVer}, 'P_El', {PowerEL},'n', {n}, 'Temperatur', {T},'Konvektion', {C},'Strahlung', {R},'TemperaturLuft', {TL})
%Time=str2double(s.Time)

% % open the file with write permission
fid = fopen('Antriebe.txt', 'w');
fprintf(fid,'%s','Time')
fprintf(fid,'\t')
fprintf(fid,'%s','P_mech')
fprintf(fid,'\t')
fprintf(fid,'%s','P_ver')
fprintf(fid,'\t')
fprintf(fid,'%s','P_El')
fprintf(fid,'\t')
fprintf(fid,'%s','n')
fprintf(fid,'\t')
fprintf(fid,'%s','T')
fprintf(fid,'\t')
fprintf(fid,'%s','C')
fprintf(fid,'\t')
fprintf(fid,'%s','R')
fprintf(fid,'\t')
fprintf(fid,'%s\n','TL')



for i=1:length(s.Time);
fprintf(fid,'%f', s.Time(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.P_mech(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.P_ver(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.P_El(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.n(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.Temperatur(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.Konvektion(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.Strahlung(i));
fprintf(fid,'\t')
fprintf(fid,'%f',s.TemperaturLuft(i));
fprintf(fid,'\n')
end
fclose(fid);;