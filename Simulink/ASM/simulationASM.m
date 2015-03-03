function out = simulationASM(P, n, b)
%% SIMULATIONASM
%  ────────────────────────────────────────────────────────────────────────
%  Author:  SZ                  
%  Date:    20.03.2014                              
%  Version: V01R00
%  ────────────────────────────────────────────────────────────────────────
% Run ASM Simulation
%
% Copyright 2014 Inspire AG, ETHZ. All rights reserved

%  ╒═════════╤═════════════╤═════════════╤═══════════════════════════════╕
%  │ VERSION │ DATE        │ AUTHOR      │ DESCRIPTION                   │
%  ╞═════════╪═════════════╪═════════════╪═══════════════════════════════╡
%  │ V01R00  │ 20.03.2014  │ SZ          │ Initial creation              │
%  ├─────────┼─────────────┼─────────────┼───────────────────────────────┤
%  │         │             │             │                               │
%  ╘═════════╧═════════════╧═════════════╧═══════════════════════════════╛

%% Defaults
if nargin == 0
    P = 10e3;
    n = 14700;
    b = 0;
end

parameters
bearing.On = b;

%% OP
Tl = sum( bearing.f1.*bearing.P1.*bearing.dm )*1.3558;
Tv = sum( (bearing.nu0*1e6.*n>2000) .* 1.42e-5  .* bearing.f0.* (bearing.nu0*1e6.*n).^(2/3) .* (bearing.dm/0.0254).^3 + ...
          (bearing.nu0*1e6.*n<=2000).* 3.492e-3 .* bearing.f0.* (bearing.dm/0.0254).^3 ) ...
     *1.3558;
Ta = airfriction.A*(n*pi/30)^airfriction.k;
T  = bearing.On*(Tl + Tv) + P/(n*pi/30) + Ta;

[U, fs] = operationalPoint(T*n*pi/30, n);

input.U  = U;
input.fs = fs;
input.T  = T;
input.w0 = n*pi/30;

printInfo(['ASM Simulation: ' num2str(P/1e3) 'kW@' num2str(n) 'rpm'], 'title', '')

%% Simulation

printInfo('Simulation', 1, '')

input.stopSS = 1;

open('ASM_Losses');
set_param('ASM_Losses','IgnoredZcDiagnostic','none')

option = simset;
option.SrcWorkspace = 'current';
option.DstWorkspace = 'current';

simTime = 1/input.fs*5;

printInfo(['Maximum simulation time: ' num2str(simTime) 's'],'item','')

printInfo('Running Simulation', 'item', 'progress')
sim('ASM_Losses', simTime, option);
printInfo('Running Simulation', 'item', 'done')

if SimOutData.time(end)<simTime
    printInfo(['Steady state reached at t=' num2str(SimOutData.time(end)) 's'],'item','')
else
    printInfo('No steady state reached!','warning','')
end




%% Read values

printInfo('Analysis', 2, '')


out.time = SimOutData.time;
out.timeCur = SimOutCurrent.time;

out.raw  = {SimOutData, SimOutASM, SimOutData, SimOutVoltage};

out.QdotS  = SimOutData.signals(1).values;
out.QdotR  = SimOutData.signals(2).values;
out.QdotFe = SimOutData.signals(3).values;
out.QdotB1 = SimOutData.signals(4).values(:,1);
out.QdotB2 = SimOutData.signals(4).values(:,2);
out.QdotB3 = SimOutData.signals(4).values(:,3);
out.QdotAF = SimOutData.signals(5).values;


out.QdotTot = SimOutData.signals(1).values + ...
              SimOutData.signals(2).values + ...
              SimOutData.signals(3).values + ...
       repmat(SimOutData.signals(4).values(:,1) + ...
              SimOutData.signals(4).values(:,2) + ...
              SimOutData.signals(4).values(:,3) + ...
              SimOutData.signals(5).values,1,2);

out.IS  = SimOutCurrent.signals(1).values;
out.IR  = SimOutCurrent.signals(2).values;
out.IH  = SimOutCurrent.signals(3).values;
out.IFe = SimOutCurrent.signals(4).values;


out.omega = n*pi/30*ones(size(SimOutASM.signals(1).values));
out.Tem   = T      *ones(size(SimOutASM.signals(1).values));
out.Tfr   = SimOutASM.signals(1).values;
out.Taf   = SimOutASM.signals(3).values;
out.Tmech = out.Tem-out.Tfr-out.Taf;
out.s     = SimOutASM.signals(3).values;

out.U     = SimOutVoltage.signals.values(:,:);
out.PEl   = SimOutData.signals(6).values;
out.PMech = out.omega.*out.Tmech;

%% Steady state conditions
% Take the average over 10 periods:
tPeriod = 1/input.fs;

idxAvgASM = SimOutASM.time>=(SimOutASM.time(end)-tPeriod);
idxAvgCur = SimOutCurrent.time>=(SimOutCurrent.time(end)-tPeriod);
idxAvgDat = SimOutData.time>=(SimOutData.time(end)-tPeriod);

tPeriodASM = max(SimOutASM.time(idxAvgASM))    -min(SimOutASM.time(idxAvgASM));
tPeriodCur = max(SimOutCurrent.time(idxAvgCur))-min(SimOutCurrent.time(idxAvgCur));
tPeriodDat = max(SimOutData.time(idxAvgDat))   -min(SimOutData.time(idxAvgDat));


out.Analysis.QdotS  = mean(out.QdotS(idxAvgDat,:));
out.Analysis.QdotR  = mean(out.QdotR(idxAvgDat,:));
out.Analysis.QdotFe = mean(out.QdotFe(idxAvgDat,:));
out.Analysis.QdotB1 = mean(out.QdotB1(idxAvgDat,:));
out.Analysis.QdotB2 = mean(out.QdotB2(idxAvgDat,:));
out.Analysis.QdotB3 = mean(out.QdotB3(idxAvgDat,:));
out.Analysis.QdotAF = mean(out.QdotAF(idxAvgDat,:));

out.Analysis.QdotTot = mean(out.QdotTot(idxAvgDat,:));

out.Analysis.PEl   = trapz(SimOutData.time(idxAvgCur), out.PEl(idxAvgDat,:))/tPeriodDat;
out.Analysis.PMech = trapz(SimOutData.time(idxAvgCur), out.PMech(idxAvgDat,:))/tPeriodDat;
out.Analysis.n     = trapz(SimOutASM.time(idxAvgASM), out.omega(idxAvgASM))/tPeriodASM/pi*30;
out.Analysis.Tem   = mean(out.Tem);
out.Analysis.Tfr   = mean(out.Tfr);
out.Analysis.Taf   = mean(out.Taf);
out.Analysis.Tmech = mean(out.Tmech);

out.Analysis.IS  = sqrt(trapz(SimOutCurrent.time(idxAvgCur), out.IS(idxAvgCur,:).^2)/tPeriodCur);
out.Analysis.IR  = sqrt(trapz(SimOutCurrent.time(idxAvgCur), out.IR(idxAvgCur,:).^2)/tPeriodCur);
out.Analysis.IH  = sqrt(trapz(SimOutCurrent.time(idxAvgCur), out.IH(idxAvgCur,:).^2)/tPeriodCur);
out.Analysis.IFe = sqrt(trapz(SimOutCurrent.time(idxAvgCur), out.IFe(idxAvgCur,:).^2)/tPeriodCur);

% out.Analysis.IS  = mean(max(out.IS(idxAvgCur,:)));
% out.Analysis.IR  = mean(max(out.IR(idxAvgCur,:)));
% out.Analysis.IFe = mean(max(out.IFe(idxAvgCur,:)));

out.Analysis.heatInpShaft  = .5*(out.Analysis.QdotB1 + out.Analysis.QdotB2 + out.Analysis.QdotB3) + ...
                                 out.Analysis.QdotR;

% Heat input estimation
out.Analysis.heatInpFront  = ( .5*(out.Analysis.QdotB1 + out.Analysis.QdotB2) + ...
                               thres.front*out.Analysis.heatInpShaft) / areas.front;
out.Analysis.heatInpBack   = ( .5*out.Analysis.QdotB3 + ...
                               thres.back*out.Analysis.heatInpShaft) / areas.back;
out.Analysis.heatInpStator = ( out.Analysis.QdotS + out.Analysis.QdotFe + ...
                               out.Analysis.QdotAF + thres.coil*out.Analysis.heatInpShaft) / ...
                               areas.stator;

%% Table
fprintf(...
drawTable({'PWM', 'Plain'}, { 'Stator losses [W]', ...
                       'Rotor losses [W]', ...
                       'Iron Losses [W]', ...
                       'Bearing 1 losses [W]', ...
                       'Bearing 2 losses [W]', ...
                       'Bearing 3 losses [W]', ...
                       'Air friction losses [W]', ...
                       'Total losses [W]', ...
                       'Electrical power [W]', ...
                       'Mechanical power [W]', ...
                       'Voltage [V]', ...
                       'Frequency [Hz]', ...
                       'PWM frequency [Hz]', ...
                       'Speed [rpm]', ...
                       'Em. torque [Nm]', ...
                       'Fr. torque [Nm]', ...
                       'Air-Fr. torque [Nm]', ...
                       'Mech. torque [Nm]', ...
                       'Slip [-]', ...
                       'Stator current [A]', ...
                       'Rotor current [A]', ...
                       'Ih current [A]', ...
                       'IFe current [A]', ...
                       'Heat input front [W/m²]', ...
                       'Heat input back [W/m²]', ...
                       'Heat input stator [W/m²]', ...
                     }, ...
                     [  out.Analysis.QdotS; ...
                        out.Analysis.QdotR; ...
                        out.Analysis.QdotFe; ...
                        out.Analysis.QdotB1*[1 1]; ...
                        out.Analysis.QdotB2*[1 1]; ...
                        out.Analysis.QdotB3*[1 1]; ...
                        out.Analysis.QdotAF*[1 1]; ...
                        out.Analysis.QdotTot; ...
                        out.Analysis.PEl; ...
                        out.Analysis.PMech*[1 1]; ...
                        input.U*[1 1]; ...
                        input.fs*[1 1]; ...
                        pwm.f, NaN; ...
                        out.Analysis.n*[1 1]; ...
                        out.Analysis.Tem*[1 1]; ...
                        out.Analysis.Tfr*[1 1]; ...
                        out.Analysis.Taf*[1 1]; ...
                        out.Analysis.Tmech*[1 1]; ...
                        (input.fs-motor.p*out.Analysis.n/60)/input.fs*[1 1]; ...
                        out.Analysis.IS; ...
                        out.Analysis.IR; ...
                        out.Analysis.IH; ...
                        out.Analysis.IFe; ...
                        out.Analysis.heatInpFront; ...
                        out.Analysis.heatInpBack; ...
                        out.Analysis.heatInpStator; ...
                      ], 'full') ...
)

%% Plotting
% printInfo('Plotting', 3, '')

% 
% figure(1)
% subplot(4,1,1)
% plot(out.timeCur, out.IS); hold on
% xlabel('time [s]')
% ylabel('i_S [A]')
% subplot(4,1,2)
% plot(out.timeCur, out.IR)
% xlabel('time [s]')
% ylabel('i_R [A]')
% subplot(4,1,3)
% plot(out.timeCur, out.IH)
% xlabel('time [s]')
% ylabel('i_H [A]')
% subplot(4,1,4)
% plot(out.timeCur, out.IFe)
% xlabel('time [s]')
% ylabel('i_{Fe} [A]')
% 
% figure(2)
% plot(out.time, out.QdotS, 'k'); hold on
% plot(out.time, out.QdotR, 'r')
% plot(out.time, out.QdotFe, 'y')
% plot(out.time, out.QdotB1, 'b')
% plot(out.time, out.QdotB2, 'g')
% plot(out.time, out.QdotB3, 'c')
% 
% legend({'Q_S', 'Q_R', 'Q_{Fe}', 'Q_{B1}', 'Q_{B2}', 'Q_{B3}'});
% xlabel('time [s]')
% ylabel('[W]')
% 
% figure(3)
% plot(out.time, out.PEl, 'k'); hold on
% plot(out.time, out.PMech, 'r')
% legend({'P_{El}', 'P_{Mech}'});
% xlabel('time [s]')
% ylabel('[W]')

%% Add info

out.param.areas   = areas;
out.param.bearing = bearing;
out.param.input   = input;
out.param.motor   = motor;
out.param.pwm     = pwm;