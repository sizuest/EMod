function [ us, fs ] = operationalPoint( P, n )
%OPERATIONALPOINT Summary of this function goes here
%   Detailed explanation goes here

parameters

wr = n*pi/30;
T  = P/wr;


p = [-6*motor.k^2*motor.Lh^2*pi, ...
      4*motor.Rr*(motor.Lh+motor.Ls)^2*pi^2*T + 3*motor.k^2*motor.Lh^2*motor.p*wr, ...
      0, ...
      motor.Rr*motor.Rs^2*T];

  
cand_fs = roots(p);

[~, idx] = min(abs(imag(cand_fs)));

fs = cand_fs(idx);
us = fs * motor.k;

if us>motor.Umax
    
   us=motor.Umax;
   
   p = [ 4*motor.Rr*(motor.Lh+motor.Ls)^2*pi^2*T, ...
        -6*motor.Lh^2*pi*us^2, ...
         motor.Rr*motor.Rs^2*T+3*motor.Lh^2*motor.p*wr*us^2];

    cand_fs = roots(p);
    fs = min(cand_fs);
    
end

end

