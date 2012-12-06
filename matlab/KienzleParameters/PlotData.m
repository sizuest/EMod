function fHandlers = PlotData( data, config, k_c11, z, saveDir )
% [] = PLOTDATA( DATA, CONFIG, K_C11, Z, SAVEDIR )
% ═════════════════════════════════════════════════════════════════════════
% Plots the data required during the analysis, for all three directions x,
% y, z.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    21.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
%
% The input arguments are the following:
% DATA ........ Struct with the measurement data and information with the
%               following fields:
%                 ─ DATA
%                   ├─ ap ........... infeed rate [mm]
%                   ├─ A ........     Cross section chip [mm2]
%                   ├─ h ............ Chip height [mm]
%                   └─ {Fx,Fy,Fz}
%                      └─ mean ...... Mean measure force [N]
% CONFIG ...... Configuration struct, must contain the following fiels:
%                 ─ CONFIG
%                   └─ useForKienzle. Bool, if the measurement shall be
%                                     included
% K_C11 ....... Vector with the identified Kienzle parameters for all three
%               dircetions
% Z ........... Same as above
% SAVEDIR ..... Path to the location where the data has to be stored


%% Config
forcesAvail = {'Fx','Fy','Fz'};
forcesNames = {'F_p','F_f', 'F_c'};

%%

fprintf('\nPLOTTING DATA\n');
fprintf('----------------------------\n');

fHandlers = [];
fTitles   = {};

%% Plot

fprintf('  [+] Plotting log(F/A) over log(h) ...');
for force = 1:length(forcesAvail)
    
    Kienzle.h = [];
    fHandlers(end+1) = figure;
    fTitles{end+1}   = ['LogLog_' forcesNames{force} '_over_h'];
    
    for meas = 1:length(data)
        for i = 1:length(data{meas})
      
            if config{meas}(i).useForKienzle
                plot(log(data{meas}{i}.h),...
                     log(data{meas}{i}.(forcesAvail{force}).mean./data{meas}{i}.A), ...
                     'ok'); hold on; grid on;
                 Kienzle.h(end+1) = data{meas}{i}.h;
            else
%                 plot(log(data{meas}{i}.h),...
%                      log(data{meas}{i}.(forcesAvail{force}).mean./data{meas}{i}.A), ...
%                      '+k'); hold on; grid on;
            end
            
        end
    end
    
    xlabel('log(h)');
    ylabel(['log(' forcesNames{force} ')']);
    
    Kienzle.h = sort(unique(Kienzle.h));
    Kienzle.F = Kienzle.h.^-z(force)*k_c11(force);
    loglog(log(Kienzle.h), log(Kienzle.F), '--k');
    axis([min(log(Kienzle.h))*1.01 max(log(Kienzle.h))*0.99 min(log(Kienzle.F))*.9 max(log(Kienzle.F))*1.1]);
end
fprintf(' done!\n');

fHandlers(end+1) = figure;
fTitles{end+1}   = 'Force_over_ap';

fprintf('  [+] Plotting F/A over ap ...');
for meas = 1:length(data)
    for i = 1:length(data{meas})
        for force = 1:length(forcesAvail)
            
            subplot(3,1,force); hold on; grid on;
            if config{meas}(i).useForKienzle
                plot(data{meas}{i}.ap,...
                     data{meas}{i}.(forcesAvail{force}).mean./data{meas}{i}.A, ...
                     'ok');
            end
             
            if (meas==1 && i==1)
                xlabel('h [mm]');
                ylabel([forcesNames{force} ' [N/mm^2]']);
                title(forcesAvail{force});
            end
        end
    end
end
fprintf(' done!\n');

%% Save
fprintf('\nSAVE PLOTS\n');
fprintf('----------------------------\n');

callerPath = pwd;
cd(saveDir);

for i=1:length(fHandlers);
    fprintf(['   >  ' fTitles{i} ' ... ']);
    saveas(fHandlers(i), fTitles{i}, 'fig');
    saveas(fHandlers(i), fTitles{i}, 'png');
    saveas(fHandlers(i), fTitles{i}, 'pdf');
    fprintf(' done!\n');
end

cd(callerPath);


%% FI
fprintf('\n\n ALL DONE\n');
fprintf('=====================================\n');

end
