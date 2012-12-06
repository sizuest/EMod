function [ err ] = WillansError( vars, P_el, P_mech )
%WILLANSERROR Summary of this function goes here
%   Detailed explanation goes here
    e   = vars(1);
    P_f = vars(2);
    
    err = sum(abs(P_mech -(e*P_el-P_f)));

end

