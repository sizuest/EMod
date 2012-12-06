function [ err ] = InductionACMotorError( param, T, n, f, U, p)
%INDUCTIONACMOTORERROR Summary of this function goes here
%   Detailed explanation goes here
    R_r = param(1);
    R_s = param(2);
    L_r = param(3);
    L_s = param(4);
    L_m = param(5);
    if length(param)>5
        T_f = param(6);
    else
        T_f = 0;
    end
    
    T_f = 0;
    
    sigma = 1-L_m^2/L_s/L_r;


    T_calc = 3/2*p* (R_r*L_m^2*(f*2*pi-p*n*pi/30)).*U.^2./ ...
             ( (R_r*L_s*f*2*pi + R_s*L_r*(f*2*pi-p*n*pi/30)).^2 + ...
               (R_r*R_s-f*2*pi*sigma*L_s*L_r.*(f*2*pi-p*n*pi/30)).^2 )-T_f;
    
    if any(isnan(T_calc))
        err = 1e10;
        return
    end
    err = norm((T-T_calc));

end

