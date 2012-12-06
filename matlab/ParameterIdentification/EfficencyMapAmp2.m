%% EFFICIENCY MAP Amplifier
% Berechnet und formatiert anschliessend die Werte für
% das .xml file
clc
%clear all

%% Version
vers = 2.1;

%% Parameter
% SPINDEL   
P_dmd  = [15e3 18.5e3 22e3 ];
P_loss = [189  247    349 ];
P_ctrl =  30;
P_map  = 0:100:22e3;
name   = 'FanucSPM-26';

% X1/Z1 ACHSE
% P_dmd  = [1.4e3 2.8e3];
% P_loss = [73    106];
% P_ctrl =  29.9;
% P_map  = 0:100:5e3;
% name   = 'FanucSVM2-20-20';

% REVOLVER
% P_dmd  = [1.4e3 2.8e3 3.8e3];
% P_loss = [70    91    106];
% P_ctrl =  29.9;
% P_map  = 0:100:5e3;
% name   = 'FanucSVM1-80';


% P_dmd  = [1.4e3 2.8e3];
% P_loss = [73    106];
% P_ctrl =  0;
% P_map  = 0:100:5e3;
% name   = 'FanucSVU1-20';

%% Processing

P_tot = P_dmd+P_loss;
eta   = P_dmd./P_tot;
eta(isnan(eta)) = 0;
eta_map = interp1(P_dmd,eta,P_map, 'linear','extrap');

eta_map(eta_map<=0) = 1;
eta_map(eta_map>1)  = 1;

%% Plotting
plot(P_map/1000,[0 eta_map(2:end)], 'k');
xlabel('P_{dmd} [kW]')
ylabel('\eta [1]')
title(name)

%% Generate xml
try
    id = fopen(['Amplifier_' name '.xml'], 'w');
catch
    error('Can''t open file');
end

% write header
fprintf(id, '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n');
fprintf(id, '<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">\n');

% write properties
fprintf(id, '<properties>\n');
% write comment
fprintf(id, ['  <comment>\n' ...
             '\tModel parameter definition\n' ...
             '\t==========================\n' ...
             '\tModel : Motor\n' ...
             '\tType  : ' name  '\n\n'...
             '\tGenerated with EfficencyMapAmp version ' num2str(vers) '\n' ...
             '\tP_dmd=[' num2str(P_dmd) '] W, P_loss=[' num2str(P_loss) '] W, P_ctrl=' num2str(P_ctrl) '\n\n' ...
             '\tParameters\n' ...
             '\t----------\n' ...
             '\tPowerSamples [W]\n' ...
             '\t  Vector of power samples.\n' ...
             '\tEfficiency [rpm]\n' ...
             '\t  Vector of corresponding efficencies.\n' ...
             '\tPowerCtrl [W]\n' ...
             '\t  Static control power	.\n' ... ...
             '  </comment>\n'] );
% write data
fprintf(id, ['  <entry key="PowerSamples">' num2str(P_map(1)) ]);
for i=2:length(P_map)
    fprintf(id, [', ' num2str(P_map(i))]);
end
fprintf(id, ';  </entry>\n');
fprintf(id, ['  <entry key="Efficiency">' num2str(eta_map(1)) ]);
for i=2:length(eta_map)
    fprintf(id, [', ' num2str(eta_map(i))]);
end
fprintf(id, '  </entry>\n');
fprintf(id, ['  <entry key="PowerCtrl">' num2str(P_ctrl) '</entry>\n' ]);
fprintf(id, '</properties>');

fclose(id);
