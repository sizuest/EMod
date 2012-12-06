%% p-V Map

%% Parameters

map.p = [827370 779108 723950 655002 565370 475738 358527 241317];  % [Pa]
map.m = [0 0.166 0.333 0.5 0.666 0.833 1 1.166];                    % [kg/s]
rho   = 883;                                                        % [kg/m3]

%% Preprocessing
map.V = map.m/rho;

%% Plotting
plot(map.V*1000,map.p/1e5,'k');
xlabel('Vdot [l/s]');
ylabel('p [bar]')
