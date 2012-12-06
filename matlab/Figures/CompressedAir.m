%% COMPRESEDAIR
% Creation of two report figures concerning the compressed air model
%% Parameter

p_c = 800000;
V_n = 1;
T_n = 293;
p_0 = 100000;

gamma = 1.4;

N = 100;

%% Ausgangspunkt
V_0 = V_n*p_c/p_0;

%% Isotherme kompression
V = linspace(V_0, V_n, N);
p = p_0*(V_0./V).^gamma;

V_ol = [V_0 V(end)];
p_ol = [p_0 p(end)];

%% Isochor
V = [V V_n];
p = [p p_c];

V_ol = [V_ol V_n];
p_ol = [p_ol p_c];

%% Isotherme expansion
p = [p linspace(p_c, p_0, N)];
V = [V V_n*(p_c./linspace(p_c, p_0, N)).^(1/gamma)];

V_ol = [V_ol V(end)];
p_ol = [p_ol p(end)];


%% Isobare expansion
V = [V V_0];
p = [p p_0];

%% Plotting
f = figure;
a = axes('Parent',f,...
         'XTickLabel',{'V_n', 'V_3', 'V_0'}, 'XTick', unique(sort(V_ol)),...
         'YTickLabel',{'p_0', 'p_c', 'p_2'}, 'YTick', unique(sort(p_ol/1e5)));
box(a,'on');
hold(a,'all');
grid on

plot(V,p/1e5, 'k'); hold on
plot(V_ol, p_ol/1e5, 'ok');
xlabel('V [m^3]');
ylabel('p [bar]');
axis([0 1.1*max(V) 0 1.1e-5*max(p)]);

%% Throttle

Pi_tr = 0.9;
Pi_cr = (2/(gamma+1))^(gamma/(gamma-1));
Pi    = [0 linspace(Pi_cr,Pi_tr,50)];
Pi_tt = sort([linspace(Pi_tr,2-Pi_tr,20),1]);
Pi_re = linspace(Pi_tr,1,10);

psi           = Pi.^(1/gamma).*sqrt(2*gamma./(gamma-1).*(1-Pi.^((gamma-1)/gamma))); 
psi_real      = Pi_re.^(1/gamma).*sqrt(2*gamma./(gamma-1).*(1-Pi_re.^((gamma-1)/gamma))); 
psi(Pi<Pi_cr) = sqrt(gamma*(2/(gamma+1))^((gamma+1)/(gamma-1)));


Pi_tr_sl = (psi(end)-psi(end-1))/(Pi(end)-Pi(end-1));
a        = (Pi_tr_sl*(Pi_tr-1)-psi(end))/2/(Pi_tr-1)^3;
b        = Pi_tr_sl-3*a*(Pi_tr-1)^2;

Pi       = [Pi Pi_tt];
psi      = [psi a*(Pi_tt-1).^3 + b*(Pi_tt-1)];

f = figure;
a = axes('Parent',f,...
    'XTickLabel',{'0', 'cr', 'tr', '1'}, 'XTick', [0 Pi_cr Pi_tr 1],...
    'YTickLabel',{'0', 'cr'}, 'YTick', [0 psi(1)]);
box(a,'on');
hold(a,'all');
grid on
plot(Pi,psi,'k');
plot(Pi_re, psi_real, '--k');
xlabel('\Pi [-]');
ylabel('\psi [-]');
axis([0 max(Pi) min(psi) 1.1*max(psi)]);
grid on



