%% FORCE MEASURMENT ERROR
% Gibt eine Abschätzung über den Fehler nach der Mttelung über N Messwerte

f    = 0.15:0.05:0.45;  % Forschub [mm/rev]
ap   = 1.00:0.50:3.0;   % Schnittiefe [mm]

v_c  = 100;             % Schnittgeschw. [m/min]
l    = 400;             % Bearbeitete Länge [mm]
M    = 1;               % Frequenz der Messung

dact = 40;              % Aktueller Durchmesser [mm]

%% Check all possibilities

p = sqrt(2*v_c*100/6*repmat(f',1,length(ap))./(dact-2*repmat(ap,length(f),1))/l/M);

%% Show results
figure(1)
plot(ap,p);
grid on
xlabel('a_p [mm]');
ylabel('k [-]');

s = cell(length(f),1);
for i=1:length(f)
    s{i}=['f=' num2str(f(i)) 'mm/rev'];
end

legend(s);
