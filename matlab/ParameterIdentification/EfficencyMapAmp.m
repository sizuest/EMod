%% EFFICIENCY MAP Amplifier
% Berechnet und formatiert anschliessend die Werte für
% das .xml file
clc
%clear all

%% Version
vers = 1.0;

%% Parameter
   
% P_amp  = 200;
P_amp  = 30;
P_off =  0;
% P_map = 0:500:15e3;
P_map = 0:100:5e3;
% name  = 'FanucSPM-26';
name  = 'FanucSVU1-80';


%% Processing

P_tot   = P_map+P_amp;
eta_map = P_map./P_tot;

eta_map(eta_map<=0) = 1;
eta_map(eta_map>1)  = 1;

%% Plotting
plot(P_tot/1000,[0 eta_map(2:end)], 'k');
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
             '\tP_on=' num2str(P_amp) 'W, P_off=' num2str(P_off) 'W\n\n' ...
             '\tParameters\n' ...
             '\t----------\n' ...
             '\tPowerSamples [W]\n' ...
             '\t  Vector of power samples.\n' ...
             '\tEfficiency [rpm]\n' ...
             '\t  Vector of corresponding efficencies.\n' ...
             '\tPowerLossStatic [W]\n' ...
             '\t  Static power demand	.\n' ... ...
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
fprintf(id, ['  <entry key="PowerLossStatic">' num2str(P_off) '</entry>\n' ]);
fprintf(id, '</properties>');

fclose(id);
