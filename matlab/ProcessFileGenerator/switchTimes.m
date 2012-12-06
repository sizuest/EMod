function [ switchTimes ] = switchTimes( values, times, threshold )
% switchTimes = SWITCHTIMES( values, times, threshold)
% ═════════════════════════════════════════════════════════════════════════
% Finds the location and the number of threshold crossing
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 1.0
% ═════════════════════════════════════════════════════════════════════════
    wasHigh = values(1)>threshold;
    
    switchTimes = [];
    
    for i=2:1:length(values)
        switch wasHigh
            case 1
                if values(i)<threshold
                    switchTimes(end+1) = times(i);
                    wasHigh = 0;
                end
            case 0
                if values(i)>=threshold
                    switchTimes(end+1) = times(i);
                    wasHigh = 1;
                end
        end
    end
end

