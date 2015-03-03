
%% Config

% Mechanic power
P = [0     10    10    10    7     5    10    2     4    8     2     0];
% rotational speed
n = [42000 14700 17200 34500 41500 6000 15000 40000 4000 12000 42000 100];

P = 0;
n = 42000;

Results = {};

%% Loop

diary('simlog.txt')

for i=1:length(P)
    Results{end+1} = simulationASM( P(i)*1e3, n(i), 1);
end

diary off
close all

%% Plot 
QdotFrontPerArea  = [];
QdotBackPerArea   = [];
QdotStatorPerArea = [];
QdotPWMPerArea    = [];
QdotPWM           = [];
PEl               = [];


for i=1:length(P)
    QdotFrontPerArea(i)  = Results{i}.Analysis.heatInpFront(1);
    QdotBackPerArea(i)   = Results{i}.Analysis.heatInpBack(1);
    QdotStatorPerArea(i) = Results{i}.Analysis.heatInpStator(1);
    QdotPWMPerArea(i)    = Results{i}.Analysis.heatInpFront(1)  - Results{i}.Analysis.heatInpFront(2) + ...
                           Results{i}.Analysis.heatInpBack(1)   - Results{i}.Analysis.heatInpBack(2) + ...
                           Results{i}.Analysis.heatInpStator(1) - Results{i}.Analysis.heatInpStator(2);
    QdotPWM(i)           = [1 -1]*Results{i}.Analysis.QdotTot';
    PEl(i)               = Results{i}.Analysis.PEl(1);
    PMech(i)             = Results{i}.Analysis.PMech(1);
end

% [Pm, nm] = meshgrid(sort(P), sort(n));
[Pm, nm] = meshgrid(linspace( min(P), max(P), 10), ...
                    linspace( min(n), max(n), 10) );

% Fm   = scatteredInterpolant(P', n', QdotFrontPerArea');
% Bm   = scatteredInterpolant(P', n', QdotBackPerArea');
% Sm   = scatteredInterpolant(P', n', QdotStatorPerArea');
% PWMm = scatteredInterpolant(P', n', QdotPWMPerArea');
% PWM  = scatteredInterpolant(P', n', QdotPWM');

Fm   = createFit(P', n', QdotFrontPerArea');
Bm   = createFit(P', n', QdotBackPerArea');
Sm   = createFit(P', n', QdotStatorPerArea');
PWMm = createFit(P', n', QdotPWMPerArea');
PWM  = createFit(P', n', QdotPWM');
PElm = createFit(P', n', PEl');
PMechm = createFit(P', n', PMech');


plot_limits1 = [ min(P) max(P) ...
                 min(n) max(n) ...
                 0      max(QdotFrontPerArea+QdotBackPerArea+QdotStatorPerArea)/1000 ...
                 0      max(QdotFrontPerArea+QdotBackPerArea+QdotStatorPerArea)/1000 ];
plot_limits2 = [ min(P) max(P) ...
                 min(n) max(n) ...
                 0      max(QdotFrontPerArea*Results{1}.param.areas.front+QdotBackPerArea*Results{1}.param.areas.back+QdotStatorPerArea*Results{1}.param.areas.stator)/1000 ...
                 0      max(QdotFrontPerArea*Results{1}.param.areas.front+QdotBackPerArea*Results{1}.param.areas.back+QdotStatorPerArea*Results{1}.param.areas.stator)/1000 ];

figure
subplot(2,2,1)
surf( Pm, nm, Fm(Pm,nm)/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{front} [kW/m²]');
axis(plot_limits1)

subplot(2,2,2)
surf( Pm, nm, Bm(Pm,nm)/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{back} [kW/m²]');
axis(plot_limits1)

subplot(2,2,3)
surf( Pm, nm, Sm(Pm,nm)/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{stator} [kW/m²]');
axis(plot_limits1)

subplot(2,2,4)
surf( Pm, nm, (Fm(Pm,nm)+Bm(Pm,nm)+Sm(Pm,nm))/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{total} [kW/m²]');
axis(plot_limits1)

figure
subplot(2,2,1)
surf( Pm, nm, Fm(Pm,nm)*Results{1}.param.areas.front/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{front} [kW]');
axis(plot_limits2)

subplot(2,2,2)
surf( Pm, nm, Bm(Pm,nm)*Results{1}.param.areas.back/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{back} [kW]');
axis(plot_limits2)

subplot(2,2,3)
surf( Pm, nm, Sm(Pm,nm)*Results{1}.param.areas.stator/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{stator} [kW]');
axis(plot_limits2)

subplot(2,2,4)
surf( Pm, nm, (Fm(Pm,nm)*Results{1}.param.areas.front+...
               Bm(Pm,nm)*Results{1}.param.areas.back+...
               Sm(Pm,nm)*Results{1}.param.areas.stator)/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{total} [kW]');
axis(plot_limits2)

figure
subplot(2,2,1)
surf( Pm, nm, PWMm(Pm,nm)/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{PWM} [kW/m^2]');
% axis([min(P) max(P) min(n) max(n) 0 max(QdotPWMPerArea)/1000 0 max(QdotPWMPerArea)/1000]);

subplot(2,2,2)
surf( Pm, nm, PWM(Pm,nm)/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('Q_{PWM} [kW]');

subplot(2,2,3)
surf( Pm, nm, PElm(Pm,nm)/1e3 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('P_El [kW]');


subplot(2,2,4)
surf( Pm, nm, PElm(Pm,nm)./(PMechm(Pm,nm)+(Fm(Pm,nm)*Results{1}.param.areas.front+...
                                           Bm(Pm,nm)*Results{1}.param.areas.back+...
                                           Sm(Pm,nm)*Results{1}.param.areas.stator))-1 );
xlabel('P [kW]')
ylabel('n [rpm]')
zlabel('error [1]');

%% Create overview
A = [];
for i=1:length(Results)
    A(i,:) = [ P(i), ...
               Results{i}.Analysis.n(1), ...
               Results{i}.Analysis.QdotTot(1), ...
               Results{i}.Analysis.heatInpFront(1)/1e3, ...
               Results{i}.Analysis.heatInpBack(1)/1e3, ...
               Results{i}.Analysis.heatInpStator(1)/1e3];
end
               

fprintf(drawTable({'P [kW]', 'n [rpm]', 'Qdot [kW]', 'front [kW/m2]', 'coil [kW/m2]', 'back [kW/m2]'}, ...
                   {}, A,'full'));


