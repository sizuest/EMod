%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = Scp_Main.time;

Power.Total            = Scp_Main.signals(1).values(:,1)/1000;
Power.CNC              = Scp_Main.signals(1).values(:,2)/1000;
Power.MainDriveCooling = Scp_Main.signals(1).values(:,3)/1000;
Power.Housing          = Scp_Main.signals(1).values(:,4)/1000;
Power.ProcessCooling   = Scp_Main.signals(1).values(:,5)/1000;
Power.CabinetCooling   = Scp_Main.signals(1).values(:,6)/1000;
%Power.AirCompressor    = Scp_Main.signals(1).values(:,7)/1000;
Power.HydraulicPump    = Scp_Main.signals(1).values(:,8)/1000;

%% Evaluation

PowerMean = mean(Scp_Main.signals(1).values);


%% Power demand
area(Time,[ Power.CabinetCooling, ...
       Power.MainDriveCooling, ...
       Power.HydraulicPump, ...
       Power.ProcessCooling, ...
       Power.CNC, ...
       %Power.AirCompressor,
       ]);
ylabel('Leistung (P) [KW]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
legend('Cabinet Cooling', 'Main drive cooling', 'hydraulic pump', 'Process conditioning', 'CNC', 'Air compressor')


figure ()
plot(Time, [Scp_Main.signals(2).values(:,1),Scp_Main.signals(2).values(:,2),Scp_Main.signals(2).values(:,3),Scp_Main.signals(2).values(:,4)])
legend('Convection','Extrated', 'Q Spindel', 'Q Kuehlung Spindel')
ylabel('Q [W]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
figure()
plot(Time,[Scp_Main.signals(3).values(:,2),Scp_Main.signals(3).values(:,1),Scp_Main.signals(3).values(:,3)])
legend('Temperatur Cabinet (CNC)','Housing', 'Temperatur Spindel')
ylabel('Temperatur [K]','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)
figure ()
plot(Time,Scp_Main.signals(4).values(:,1))
legend('Wirkungsgrad')
ylabel('n','FontSize',12) 
xlabel('Zeit [s]','FontSize',12)

