%% Papiercomputer
% ═════════════════════════════════════════════════════════════════════════
% Performs an analysis of simualtion data compared to measurement data, in
% order to provide the key values for model validation. Creating table and
% figure of papercomputer analysis.
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 0.1
% ═════════════════════════════════════════════════════════════════════════

%% Load data
load('PaperComputer/PaperComputerData')


%% Calculation
B = sum(data.weights,1);
E = sum(data.weights,2);

% weight
W = sum(B)/length(B);

%% Plotting

f = figure;

plot(E,B,'ks','MarkerFaceColor','k'); hold on
grid on

xlabel('influence I')
ylabel('persuasibility P');

axis([0 max(E)*1.1 0 max(B)*1.1]);

textoffset = max(E) * 0.01;

for i=1:length(B)
    text(E(i)+textoffset, B(i), data.shortnames{i})
end

set(gca, 'XTick', W, 'YTick', W, 'XTickLabel' , '', 'YTickLabel', '');

%% Table

cols = 'lc';
for i=1:length(B)
    cols = [cols 'c'];
end

tex_out = [ '\\begin{table}\n', ...
            '\t\\centering\n', ...
            '\t\\footnotesize\n', ...
            '\t\\begin{tabular}{' cols '}\n', ...
            '\t\\toprule', ...
            '\t\tInfluence of $\\downarrow$ on $\\rightarrow$&\t'];
for i=1:length(data.names)
    tex_out = [ tex_out '\t& \\rotatebox{90}{' data.names{i} '}'];
end
tex_out = [ tex_out '\\\\\n' ...
            '\t\\midrule\n' ];

for i=1:length(data.names)
    tex_out = [ tex_out '\t\t' data.names{i} '\t& ' data.shortnames{i} '\t& '];

    for j=1:length(B)
        if j==i
            tex_out = [ tex_out '-- ' ];
        else
            tex_out = [ tex_out num2str(data.weights(i,j)) ];
        end
        if j==length(B)
            tex_out = [tex_out '\\\\\n'];
        else
            tex_out = [tex_out '\t& '];
        end
    end
end

tex_out = [ tex_out '\t\t\\midrule\n' ];

tex_out = [ tex_out '\t\tPersuasibility \t& \t& '];
for j=1:length(B)
    tex_out = [ tex_out num2str(B(j))];
    if j==length(B)
        tex_out = [tex_out '\\\\\n'];
    else
        tex_out = [tex_out '\t& '];
    end
end

tex_out = [ tex_out '\t\tInfluence \t& \t& '];
for j=1:length(B)
    tex_out = [ tex_out num2str(E(j))];
    if j==length(B)
        tex_out = [tex_out '\\\\\n'];
    else
        tex_out = [tex_out '\t& '];
    end
end

tex_out = [tex_out '\t\tLimit\t& ' sprintf('%1.1f', W) '\\\\\n'];


tex_out = [ tex_out  '\t\t\\bottomrule\n' ...
                     '\t\\end{tabular}\n' ...
                     '\t\\normalsize\n' ...
                     '\t\\caption[\\TODO]{\\TODO}\n' ...
                     '\\end{table}\n'];




%% Saving

try
    id = fopen([ 'PaperComputerOut.tex'], 'w');
    fprintf(id, tex_out);
    fclose(id);
catch
    error('Can''t open file');
end
