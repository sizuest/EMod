function [  ] = GetDirTree( path, name, files, maxlevel )
%GETDIRTREE(PATH, NAME, FILES, MAXLEVEL)
% ═════════════════════════════════════════════════════════════════════════
% Reads the the directory PATH with all subdirectories and creates a tree
% for latex report. Output file is saved at NAME.tex. If argument FILES is
% true, files will also be listed. The procedure is stopped, if MAXLEVEL
% depth is reached.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    16.03.2012                All rights reserved
% Version: 0.1
% ═════════════════════════════════════════════════════════════════════════
%
% Requires the package "dirtree" in latex for compilation

if nargin<4
    maxlevel = inf;
end

% empty output
output = ['\\dirtree{%%\n' ...
    '.1\t\\textbf{' name '}.'...
    dirtree(path, files, 2, maxlevel) ...
    '\n}'];

try
    id = fopen([ name '.tex'], 'w');
    fprintf(id, output);
    fclose(id);
catch e
    error('Can''t open file');
end

end

%% Recursive function 

function [out] = dirtree(path, files, level, maxlevel)
    out = '';
    obj = dir(path);
    
    if level > maxlevel
        return;
    end
    
    for i=1:length(obj)
        if isempty(regexp(obj(i).name,'[.]{1,2}$','ONCE'))
            if obj(i).isdir
                if level == 2
                    out = strcat(out, '\n .', num2str(level), '\t\\textbf{', ...
                        strrep(strrep(obj(i).name,'.','{.}'),'_','\\_'), '}.' );
                else
                    out = strcat(out, '\n .', num2str(level), '\t', ...
                        strrep(strrep(obj(i).name,'.','{.}'),'_','\\_'), '.' );
                end
                out = strcat(out, dirtree([path '/' obj(i).name], files, level+1, maxlevel));
            elseif files
                out = strcat(out, '\n.', num2str(level), '\t\\emph{', ...
                    strrep(strrep(obj(i).name,'.','{.}'),'_','\\_'), '}.' );
            end
        end
    end
end

