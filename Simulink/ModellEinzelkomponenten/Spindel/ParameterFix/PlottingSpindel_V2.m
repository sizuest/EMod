
%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_MainDrive.time;

 PowerMech       = Scp_MainDrive.signals(1).values(:,1)/1000;
 PowerVer        = Scp_MainDrive.signals(2).values(:,1)/1000;
 PowerEL         = Scp_MainDrive.signals(3).values(:,1)/1000;
 n               = Scp_MainDrive.signals(4).values(:,1);
 
% %% Power demand
figure()
 line(Time,[PowerMech, ...
            PowerVer, ...
            PowerEL, ...
        ]);
 ylabel('Leistung (P) [kW]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P Mechanisch', 'P Verlust', 'P Elekterisch')

 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
s = struct('Time', {Time}, 'P_mech', {PowerMech},'P_ver', {PowerVer}, 'P_El', {PowerEL},'n', {n})
%Time=str2double(s.Time)

% % open the file with write permission
fid = fopen('Spindel.txt', 'w');
fprintf(fid,'%s','Time')
fprintf(fid,'\t')
fprintf(fid,'%s','P_mech')
fprintf(fid,'\t')
fprintf(fid,'%s','P_ver')
fprintf(fid,'\t')
fprintf(fid,'%s','P_El')
fprintf(fid,'\t')
fprintf(fid,'%s\n','n')


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
fprintf(fid,'\n')
end
fclose(fid);