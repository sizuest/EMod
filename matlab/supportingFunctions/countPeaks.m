function [ N, loc ] = countPeaks( data, th )
%COUNTPEAKS Conts the peaks in DATA, where a peak is defined as
%data(i-1)<data(i) & data(i)>data(i+1)

data(data<th) = NaN;

d   = sign(diff(data(:)))';

loc = find([0 d]>0 & [d 0]<=0);

N   = length(loc);

end
