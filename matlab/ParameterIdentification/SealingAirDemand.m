%% SEALING AIR

c_d = 0.5;
gamma = 1.4;
R = 287;
rho = 1.2;
A = 0.05*pi*30e-6;
p_c = 3.6*1.5e5%8e5;
T_amb = 296;

%%
Psi_cr = (2/(gamma+1))^(gamma/(1-gamma))
Psi = 8^(-1/gamma)*sqrt(2*gamma/(gamma-1)*(1-8^((1-gamma)/gamma)))

%%
Vdot = c_d*A*p_c/sqrt(R*T_amb)*Psi
