%% PARAMETERS
%  ────────────────────────────────────────────────────────────────────────
%  Author:  SZ                  
%  Date:    08.10.2014                              
%  Version: V01R00
%  ────────────────────────────────────────────────────────────────────────
% ASM parameters
%
% Copyright 2014 Inspire AG, ETHZ. All rights reserved

%  ╒═════════╤═════════════╤═════════════╤═══════════════════════════════╕
%  │ VERSION │ DATE        │ AUTHOR      │ DESCRIPTION                   │
%  ╞═════════╪═════════════╪═════════════╪═══════════════════════════════╡
%  │ V01R00  │ 08.10.2014  │ SZ          │ Initial creation              │
%  ├─────────┼─────────────┼─────────────┼───────────────────────────────┤
%  │         │             │             │                               │
%  ╘═════════╧═════════════╧═════════════╧═══════════════════════════════╛

%% Motor
motor.J    = .00117;
motor.Rs   = .114;
motor.Rr   = .107;
motor.Ls   = .177e-3;
motor.Lr   = .267e-3;
motor.Lh   = 4.07e-3;
motor.p    = 2;
motor.k    = .66;        % Us/fs, where fs<fb
motor.Umax = 380;

%% Inputs
input.U  = 330;
input.fs = 500;
input.T  = 10e3/(14700/30*pi);
input.w0 = 14700/30*pi;


%% PWM
pwm.Umax = 400;
pwm.f    = 20e3;