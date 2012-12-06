%% TAILSTOCK ANALYSIS
% Goal: identify linear approximation: P(t) = k* F_clamp(t)

%% Measuremetns

% Power
P = [35, 65, 100, 165, 230]';

% Force
F = [106, 206, 307, 401, 503]'*10;

%% LS

k = F\P;
disp(k);
