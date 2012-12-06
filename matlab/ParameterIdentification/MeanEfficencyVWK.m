%% MEAN EFFICIENCY VWK
% Berechnet die Mittlere Effizient der Pumpe über Kennlinie 
clc

%% Parameter
P_el_pump = 135;
P_el_comp = 740;
Q_dot_max = 2346;


V_op  = 12/60/1000;
map.V = [0 13 ] / 60 / 1000;
map.p = [37.5 7]*1000*9.81;
map.n = 100;

%% Processing

V = linspace(min(map.V), max(map.V), map.n);
p = interp1(map.V,map.p,V,'linear');

P_mech = V.*p;

eta = P_mech/P_el_pump;

disp(['eta=' num2str(interp1(V,eta,V_op))]);
disp(['epsilon=' num2str(Q_dot_max/P_el_comp)]);

%% Plotting
plot(map.V*1000, map.p/1e5)
xlabel('Vdot [l/s]');
ylabel('p_{pump} [bar]');

