%% EFFICIENCY MAP FANUC SERVOS
% Berechnet aus den Angaben von GE Fanuc die Effizienz an verschiedenen
% Betriebspunkten


%% Plotting parameters
max_axis_torque = 30;
max_axis_omega  = 2100;
close all

%% ALPHA C6/2000
%% - Parameters
% Config for alpha6/2000

max_speed = 2000; % [rmp]
max_torq  = 22;   % [Nm]
max_alow_torque = 22;
max_pwr   = 22000*pi/30; % [W]
kappa_a   = 1.68; % [Nm/A]
kappa_i   = .56;  % [Vs/rad]
p         = 3;    % Number of pole pairs
T_f       = 0.3;  % [Nm]
R_a       = 1.52; % [Ohm]

P_elamp   = 0.13*230; %[W]
P_thamp   = 37.7; % [W]

N_omega   = 100;
N_torque  = 100;

%% - PreProcessing

omega  = repmat(linspace(0,max_speed*pi/30,N_omega)', 1,N_torque);
torque = repmat(linspace(0,max_torq,       N_torque),N_omega,1);


%% - Processing
P_m = omega.*torque;
%P_m(P_m>max_pwr)=NaN;

T_a = torque+T_f;

T_a(P_m>=max_pwr)=NaN

I_a = T_a/kappa_a;

U_i = kappa_i*omega;
U_r = I_a*R_a;

U_a = U_i+U_r;

P_elm = I_a.*U_a*p;
P_thm = I_a.*U_r*p;

P_el  = P_elm+P_elamp;
P_th  = P_thm+P_thamp;

eta = P_m./P_el;

max_torque_line = [max_pwr./omega(1:end-1,1); 0];
max_torque_line(max_torque_line>max_alow_torque) = max_alow_torque;



%% - Plotting

figure(2)
hold on;
plot(omega(:,1)*30/pi, max_torque_line,'--k','LineWidth',3);
[C,h]=contour(omega*30/pi,torque,eta*100);
set(h,'LevelStep',10,'ShowText','on','TextStep',get(h,'LevelStep')*2); 
axis([0 max_axis_omega 0 max_axis_torque]);
xlabel('Speed [rmp]');
ylabel('Torque [Nm]');
zlabel('\eta [%]');
title('\alpha C6/2000');
hold off;

%% ALPHA C12/3000
%% - Parameters
% Config for alpha12/3000

max_speed = 2000; % [rmp]
max_torq  = 25;   % [Nm]
max_alow_torque = 25;
max_pwr   = 25000*pi/30; % [W]
kappa_a   = 2.04; % [Nm/A]
kappa_i   = 0.68; % [Vs/rad]
p         = 3;    % Number of pole pairs

T_f       = 0.8;  % [Nm]
R_a       = 1.10; % [Ohm]

P_elamp   = 0.13*230; %[W]
P_thamp   = 47.3; % [W]

N_omega   = 100;
N_torque  = 100;

%% - PreProcessing

omega  = repmat(linspace(0,max_speed*pi/30,N_omega)', 1,N_torque);
torque = repmat(linspace(0,max_torq,       N_torque),N_omega,1);


%% - Processing
P_m = omega.*torque;
%P_m(P_m>max_pwr)=NaN;

T_a = torque+T_f;

T_a(P_m>=max_pwr)=NaN

I_a = T_a/kappa_a;

U_i = kappa_i*omega;
U_r = I_a*R_a;

U_a = U_i+U_r;

P_elm = I_a.*U_a*p;
P_thm = I_a.*U_r*p;

P_el  = P_elm+P_elamp;
P_th  = P_thm+P_thamp;

eta = P_m./P_el;

max_torque_line = [max_pwr./omega(1:end-1,1); 0];
max_torque_line(max_torque_line>max_alow_torque) = max_alow_torque;


%% - Plotting

figure(1)
hold on;
plot(omega(:,1)*30/pi, max_torque_line,'--k','LineWidth',3);
[C,h]=contour(omega*30/pi,torque,eta*100);
set(h,'LevelStep',10,'ShowText','on','TextStep',get(h,'LevelStep')*2); 
axis([0 max_axis_omega 0 max_axis_torque]);
xlabel('Speed [rmp]');
ylabel('Torque [Nm]');
zlabel('\eta [%]');
title('\alpha C12/3000');

hold off;

%% ALPHA M9/3000
%% - Parameters
% Config for alphaM9/3000

max_speed = 3000; % [rmp]
max_torq  = 37;   % [Nm]
max_alow_torque = 37;
max_pwr   = 74000*pi/30; % [W]
kappa_a   = 0.86; % [Nm/A]
kappa_i   = 0.29; % [Vs/rad]
p         = 3;    % Number of pole pairs

T_f       = 0.3;  % [Nm]
R_a       = 0.181; % [Ohm]

P_elamp   = 0.13*230; %[W]
P_thamp   = 33.3; % [W]

N_omega   = 100;
N_torque  = 100;

%% - PreProcessing

omega  = repmat(linspace(0,max_speed*pi/30,N_omega)', 1,N_torque);
torque = repmat(linspace(0,max_torq,       N_torque),N_omega,1);


%% - Processing
P_m = omega.*torque;
%P_m(P_m>max_pwr)=NaN;

T_a = torque+T_f;

T_a(P_m>=max_pwr)=NaN

I_a = T_a/kappa_a;

U_i = kappa_i*omega;
U_r = I_a*R_a;

U_a = U_i+U_r;

P_elm = I_a.*U_a*p;
P_thm = I_a.*U_r*p;

P_el  = P_elm+P_elamp;
P_th  = P_thm+P_thamp;

eta = P_m./P_el;

max_torque_line = [max_pwr./omega(1:end-1,1); 0];
max_torque_line(max_torque_line>max_alow_torque) = max_alow_torque;


%% - Plotting

figure(3)
hold on;
plot(omega(:,1)*30/pi, max_torque_line,'--k','LineWidth',3);
[C,h]=contour(omega*30/pi,torque,eta*100);
set(h,'LevelStep',10,'ShowText','on','TextStep',get(h,'LevelStep')*2); 
axis([0 3100 0 40]);
xlabel('Speed [rmp]');
ylabel('Torque [Nm]');
zlabel('\eta [%]');
title('\alpha M9/3000');

hold off;
