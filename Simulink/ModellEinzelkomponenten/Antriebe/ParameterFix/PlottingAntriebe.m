
%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Antriebe.time;

 PowerMech         = Scp_Antriebe.signals(1).values(:,1);
 PowerVer          = Scp_Antriebe.signals(2).values(:,1);
 PowerEL           = Scp_Antriebe.signals(3).values(:,1);
 n                  = Scp_Antriebe.signals(4).values(:,1);

% %% Power demand
figure()
 line(Time,[PowerMech , ...
            PowerVer, ...
            PowerEL, ...
        ]);
 title('Power Drives')
 ylabel('Leistung (P) [W]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P mech', 'P Ver', 'P el')
 
 
 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)


s = struct('Time', {Time}, 'P_mech', {PowerMech},'P_ver', {PowerVer}, 'P_El', {PowerEL},'n', {n})
%Time=str2double(s.Time)

% % open the file with write permission
fid = fopen('Antriebe.txt', 'w');
fprintf(fid,'%s','Time');
fprintf(fid,'\t');
fprintf(fid,'%s','P_mech');
fprintf(fid,'\t');
fprintf(fid,'%s','P_ver');
fprintf(fid,'\t');
fprintf(fid,'%s','P_El');
fprintf(fid,'\t');
fprintf(fid,'%s\n','n');


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
fprintf(fid,'\n');
end
fclose(fid);;