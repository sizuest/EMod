
%% PLOTTING
% PLOTTING OF THE SIMULATION RESULTS

%% Read Results
close all
Time = ScopeData.time;

 PowerMech.Total            = ScopeData.signals(1).values(:,1);
 PowerEL.Total              = ScopeData.signals(2).values(:,1);
 Q                          = ScopeData.signals(3).values(:,1);
 n                          = ScopeData.signals(4).values(:,1);
% %% Evaluation
 
 PowerMean = mean(ScopeData.signals(1).values);
 
 
% %% Power demand
 area(Time,[PowerMech.Total , ...
            PowerEL.Total, ...
        ]);
 ylabel('Leistung (P) [W]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 legend('P mech', 'P el')
 
 
 figure ()
 plot(Time,Q)
 legend('Q Vorschub ')
 ylabel('Q [W]','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)
 figure()
 plot(Time,n)
 legend('Wirkungsgrad')
 ylabel('Wirkungsgrad','FontSize',12) 
 xlabel('Zeit [s]','FontSize',12)

