%% EFFICIENCY MAP SPINDEL
% Berechnet aus einer Anzahl Betriebpunkte die Parameter für den Willans
% Approach mit LMS. Berechnet und formatiert anschliessend die Werte für
% das .xml file
clc
%clear all

%% Version
vers = 2.0;

%% Parameter
   

U = [295; 400; 400];
I = [ 43;  32;  22];

P_el = U.*I*sqrt(3);        % Leistungsaufnahme an den Betriebspunkten [W]
T    = [95; 25; 13];        % Drehmoment an den Betriebpunkten [N]
n    = [1500; 5700; 8000];  % Drehzahl an den Betriebpunkten [rpm]
f    = [54; 200; 276];      % El. Frequenz [Hz]
p    = 2;                   % Polpaarzahl

P_amp = 0;

n_map = 100:100:6000;       % Punkte Drehzahl für die map [rpm]
P_map = [50:50:200 400:200:15e3];       % Punkte Leistung für die map [W]P_max = 15e3;
T_max = 100;
P_max = 15e3;
name  = 'Kessler_000101561';


%% Pre-Processing
P_mech = T.*(n*pi/30);

%% Processing
y = P_el;
H = [P_mech];
% 
% % y = P_el./P_mech - 1;
% % H = [T./n, T.*n, 1./n./T];
% 
x = (H'*H)^-1*H'*y;
e   = x(1);


resStr = [ '\t' sprintf('e =\t %3.2f\n',e)];

% [P_tmp, n_tmp] = meshgrid(P_map, n_map);
% 
% eta_map = P_tmp./(P_tmp*e);
% eta_map(eta_map<0) = 1;
% eta_map(eta_map>1) = 1;
% 
% [C,h]=contour(P_tmp/1e3,n_tmp,eta_map*100); hold on;
% set(h,'LevelStep',10,'ShowText','on','TextStep',get(h,'LevelStep'));
% xlabel('Power [kW]');
% ylabel('Speed [rmp]');
% zlabel('\eta [%]');
% title('Kessler Spindel');


param_init  = [1; 1; 0.01; 0.01; 0.01; 0];
param_limit = [2; 2; 0.1; 0.1; 0.1; 1]; 
options = optimset('Algorithm','interior-point','MaxFunEvals',1000000);
param = fmincon(@(param) InductionACMotorError(param, T, n, f, U, p), ...
                param_init, ...
                -eye(length(param_init)), zeros(length(param_init),1), ...
                zeros(length(param_init)), zeros(length(param_init),1),...
                [zeros(length(param_init)-1,1); 1], param_limit, [], options);

R_r = param(1);
R_s = param(2);
L_r = param(3);
L_s = param(4);
L_m = param(5);
a=0;
if length(param)>5
    Tf   = param(6);
else
    Tf = 0;
end
% TODO: Wird 'a' gebraucht?

resStr = [ '\t' sprintf('R_r =\t %3.2f\n',R_r) ...
           '\t' sprintf('R_s =\t %3.2f\n',R_s) ...
           '\t' sprintf('L_r =\t %3.2f\n',L_r) ...
           '\t' sprintf('L_s =\t %3.2f\n',L_s) ...
           '\t' sprintf('L_m =\t %3.2f\n',L_m) ...
           '\t' sprintf('T_f =\t %3.2f\n',Tf)];
       
fprintf(resStr);



% Calculate map
%P_el_map  = 1/e*(repmat(P_map,length(n_map),1)+P_0);
T_map     = repmat(P_map,length(n_map),1)./repmat(n_map'*pi/30,1,length(P_map));
omega_map = repmat(n_map',1,length(P_map))*pi/30;
f_map     = repmat(interp1(n,f,n_map,'linear','extrap')',1,length(P_map));

%eta_map  = repmat(P_map,length(n_map),1)./P_el_map;

% eta_map = (a*T_map./repmat(n_map',1,length(P_map))+b*repmat(n_map',1,length(P_map)).*T_map+c./repmat(n_map',1,length(P_map))./T_map +1).^-1;

eta_map = (f_map*2*pi/p./omega_map + R_r*R_s./(L_m^2*(f_map*2*pi-p*omega_map).*p.*omega_map) + ...
           R_s/R_r*L_r^2/L_m^2*(f_map*2*pi-p*omega_map)./p./omega_map + ...
           P_amp./(T_map)./omega_map + Tf./T_map).^-1;
       
eta_map(eta_map<0) = (T_map(eta_map<0)+Tf).*omega_map(eta_map<0)./P_max;
eta_map(eta_map>1) = 1;
       
%eta_map = T_map.*omega_map ./ (T_map.*omega_map./eta_map + a*omega_map.^2);
       
   
%% Post-Processing
T_plot     = linspace(0,max(max(T_map)),length(n_map))';

omega_plot = omega_map(:,1);
f_plot     = repmat(interp1(n,f,n_map,'linear','extrap'),length(T_plot),1);


[omega_plot, T_plot] = meshgrid(omega_plot, T_plot);

T_plot(T_plot.*omega_plot>P_max) = P_max./omega_plot(T_plot.*omega_plot>P_max);
T_plot(T_plot>T_max) = T_max;

eta_plot = (f_plot*2*pi/p./omega_plot + R_r*R_s./(L_m^2*(f_plot*2*pi-p*omega_plot).*p.*omega_plot) + ...
            R_s/R_r*L_r^2/L_m^2*(f_plot*2*pi-p*omega_plot)./p./omega_plot + ...
            P_amp./(T_plot)./omega_plot  + Tf./(T_plot) ).^-1;

eta_plot(T_plot.*omega_plot>P_max) = NaN;
eta_plot(T_plot>T_max) = NaN;


% Show results
eta_plot(T_plot == 0)=0;
eta_plot(eta_plot<0 | eta_plot>1)=NaN;
[C,h]=contour(omega_plot/pi*30,T_plot,eta_plot); hold on;
set(h,'LevelStep',.1,'ShowText','on','TextStep',get(h,'LevelStep')*2);
plot([ omega_plot(1,:)/pi*30 omega_plot(1,end)/pi*30], [min(T_max,P_max./omega_plot(1,:)) 0], 'k', 'LineWidth', 2);

axis([ 0 omega_plot(1,end)/pi*30*1.1 0 T_max*1.1]);

xlabel('Speed [rmp]');
ylabel('Torque [Nm]');
zlabel('\eta [%]');
title('Kessler Spindel');

%% Generate xml
try
    id = fopen(['Motor_' name '.xml'], 'w');
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
             '\tGenerated with EfficencyMapSpindel version ' num2str(vers) '\n' ...
              resStr '\n\n' ...
             '\tParameters\n' ...
             '\t----------\n' ...
             '\tPowerNominal [W]\n' ...
             '\t  Nominal mechanical power of motor.\n' ...
             '\tRotspeedNominal [rpm]\n' ...
             '\t  Nominal rotational speed of motor.\n' ...
             '\tPowerSamples [W]\n' ...
             '\t  Power samples used for linear interpolation of the efficiency.\n' ...
             '\t  The PowerSamples must be sorted, smallest value first.\n' ...
             '\tRotspeedSamples [rpm]\n' ...
             '\t  Rotational speed samples used for linear interpolation of the efficiency.\n' ...
             '\t  The RotspeedSamples must be sorted, smallest value first.\n' ...
             '\tEfficiencyMatrix [1]\n' ...
             '\t  The matrix value at the position p,r (EfficiencyMatrix[p,r])\n' ...
             '\t  corresponds to the motor efficiency when the motor is running\n' ...
             '\t  at power PowerSamples[p] and at rotational speed RotspeedSamples[r].\n' ...
             '  </comment>\n'] );
% write data
fprintf(id, ['  <entry key="PowerNominal">' num2str(P_mech(1)) '</entry>\n']);
fprintf(id, ['  <entry key="RotspeedNominal">' num2str(n(1))   '</entry>\n']);
fprintf(id, ['  <entry key="PowerSamples">' num2str(P_map(1)) ]);
for i=2:length(P_map)
    fprintf(id, [', ' num2str(P_map(i))]);
end
fprintf(id, ';</entry>\n');
fprintf(id, ['  <entry key="RotspeedSamples">' num2str(n_map(1)) ]);
for i=2:length(n_map)
    fprintf(id, [', ' num2str(n_map(i))]);
end
fprintf(id, ';</entry>\n');
fprintf(id, '  <entry key="EfficiencyMatrix">' );
for i=1:length(P_map)
    fprintf(id, num2str(eta_map(1,i)));
    for j=2:length(n_map)
        fprintf(id, [', ' num2str(eta_map(j,i))]);
    end
    fprintf(id, ';\n');
end
fprintf(id, '  </entry>\n');
fprintf(id, ['  <entry key="FrictionTorque">' num2str(round(10*Tf)/10)   '</entry>\n']);
fprintf(id, '</properties>');

fclose(id);

%%
interp2(n_map, P_map, eta_map', 800, 753)
