function [ ] = FlowchartPlotter( machineCfgPath , filename, outputType)
%FLOWCHARTPLOTTER(PATH, NAME, TYPE)
% ═════════════════════════════════════════════════════════════════════════
% Creates a flowchart of the machine 
% configuration located at the folder PATH (Machine.xml and IOLinking.txt
% are required). The output consits of a graphviz file NAME (default:
% 'flowchart') and a image of the format TYPE (default: ps, others: jpg,
% png, eps)
% ─────────────────────────────────────────────────────────────────────────
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 1.1
% ═════════════════════════════════════════════════════════════════════════
%
% To compile the figure, dot is used. If the command is not available, no
% image will be created

if nargin<2
    filename = 'flowchart';
end
if nargin<3
    outputType = 'ps';
end

%% Globals
global MACHINECOMPONENTS;
global SIMCONTROLLERS;

MACHINECOMPONENTS = {};
SIMCONTROLLERS    = {};

%% Paths
% Read config files
machineFilePath = [machineCfgPath '/Machine.xml'];
linkingFilePath = [machineCfgPath '/IOLinking.txt'];

%% Read files

fprintf(' >  Reading machine configuration ...');
machineFile =  xmlread( machineFilePath );
linkingFile = fileread( linkingFilePath );

%% Parse Machine
% Go through the whole file and create a struct, global vars for components
% and simulators will be filled up
[~] = parseChildNodes(machineFile);

fprintf(' done!\n');

% Get machine components, loop trough all components and read properties
% name, model type and parameter type
for i=1:length(MACHINECOMPONENTS)
    % Variable to store result
    clear tmp
    
    for j=1:length(MACHINECOMPONENTS{i}.Children)
        % If the type is name, get the value
        if strcmpi(MACHINECOMPONENTS{i}.Children(j).Name,'name')
            tmp.name = MACHINECOMPONENTS{i}.Children(j).Children.Data;
        % If type is the componet describtion, go trough
        elseif strcmpi(MACHINECOMPONENTS{i}.Children(j).Name,'component')
            % Find type, model type is stored as attribute
            for k=1:length(MACHINECOMPONENTS{i}.Children(j).Attributes)
                if strcmpi(MACHINECOMPONENTS{i}.Children(j).Attributes(k).Name, 'xsi:type')
                    tmp.model = MACHINECOMPONENTS{i}.Children(j).Attributes(k).Value;
                end
            end
            % Get additional informations
            for k=1:length(MACHINECOMPONENTS{i}.Children(j).Children)
                if regexp( MACHINECOMPONENTS{i}.Children(j).Children(k).Data, ...
                        '<gain>[\S]*</gain>')
                    tmp.props.gain = ...
                        regexprep( MACHINECOMPONENTS{i}.Children(j).Children(k).Data, ...
                        '<gain>([\S]*)</gain>','$1');
                else
                   tmp.props.(MACHINECOMPONENTS{i}.Children(j).Children(k).Name) = ...
                        MACHINECOMPONENTS{i}.Children(j).Children(k).Children.Data;
                end
            end
        end
    end
    
    tmp.inputs = {};
    tmp.outputs = {};
    
    machineComponent.(tmp.name) = tmp;
end

% Get Simulators, same as above
simController = cell(size(SIMCONTROLLERS));

for i=1:length(SIMCONTROLLERS)
    for k=1:length(SIMCONTROLLERS{i}.Attributes)
        if strcmpi(SIMCONTROLLERS{i}.Attributes(k).Name, 'xsi:type')
            simController{i}.controller = SIMCONTROLLERS{i}.Attributes(k).Value;
        end
    end
    for j=1:length(SIMCONTROLLERS{i}.Children)    
        simController{i}.(SIMCONTROLLERS{i}.Children(j).Name) = ...
            SIMCONTROLLERS{i}.Children(j).Children.Data;
    end
    
end

fprintf('[+] %d machine components added\n', length(fieldnames(machineComponent)));
fprintf('[+] %d simulation controllers added\n', length(simController));

%% Parse IOLinking

% remove all comments and empty lines
linkingFile = regexprep( linkingFile, '\#[^\n]+\n', '\n');
linkingFile = regexprep( linkingFile, '[\s]+\n', '\n');

% remove .Plus, .Sum, .Minus
% linkingFile = regexprep( linkingFile, {'\.Plus', '\.Sum', '\.Minus' }, '');

% get sources and targets
linkSources = regexprep( linkingFile, '([\S]+)[\s]*=[\s]*([\S]+)', '$2');
linkTargets = regexprep( linkingFile, '([\S]+)[\s]*=[\s]*([\S]+)', '$1');
linkSources = regexp(linkSources, '[^\n\s]+','match')';
linkTargets = regexp(linkTargets, '[^\n\s]+','match')';

% Add sources to machine components
for i=1:length(linkSources)
    
    if regexp(linkSources{i},'\.');
        % component
        c = regexprep(linkSources{i},  '\.[\S]+','');
        % port
        p = regexprep(linkSources{i}, '[\S]+\.','');
        machineComponent.(c).outputs{end+1} = p;
    end
end

% Add targets to machine components
for i=1:length(linkTargets)
    
    if regexp(linkTargets{i},'\.');
        % component
        c = regexprep(linkTargets{i},  '\.[\S]+','');
        % port
        p = regexprep(linkTargets{i}, '[\S]+\.','');
        
        if strcmpi('Plus',p)
            p = ['Plus' num2str( length(machineComponent.(c).inputs) )];
            linkTargets{i} = strcat(c,'.',p);
        end
        machineComponent.(c).inputs{end+1} = p;
    end
end
        
f = fieldnames(machineComponent);
for i=1:length(f)
    machineComponent.(f{i}).inputs  = unique(machineComponent.(f{i}).inputs );
    machineComponent.(f{i}).outputs = unique(machineComponent.(f{i}).outputs );
end

% invert equality
linkingFile = strcat(linkSources, '->', linkTargets);%regexprep( linkingFile, '([\S]+)[\s]*=[\s]*([\S]+)', '$2 -> $1');


% do foo.bla -> "foo":bla
linkingFile = regexprep( linkingFile, '->([\S]+)\.([\S]+)', '-> "$1":$2');
linkingFile = regexprep( linkingFile, '([\S]+)\.([\S]+)->', '"$1":$2 ->');

% Create cell with one line per element
linksText = regexp(linkingFile, '[^\n]+','match')';

fprintf('[+] %d connections added\n', length(linksText));

%% Find empty outputs and ask for name


f = fieldnames(machineComponent);
for i=1:length(f)
    if isempty(machineComponent.(f{i}).outputs)
        %machineComponent.(f{i}).outputs = input([f{i} ' has no outputs. Outputs [CellStr]:'] );
        machineComponent.(f{i}).outputs = {'PTotal'};
        fprintf('[!] %s output "PTotal" added\n', f{i});
    end
end


%% Create dot file

% Color configurations --------------------
col.thermal.node   = 'red';
col.thermal.edge   = 'red';

col.elmech.node    = 'black';
col.elmech.edge    = 'black';

col.sim.node       = 'blue';
col.sim.edge       = 'blue';

% EOCONF -----------------------------------

fprintf(' >  Generating graphiviz file ...');

gv_out = 'digraph g {\nrankdir=LR;\nsplines=true;\noverlap=scale;\nnode [shape=plaintext];\n ';

f = fieldnames(machineComponent);
for i=1:length(f)
    
    % Color selection
    if ~isempty(regexp(machineComponent.(f{i}).model, '(free|forced)HeatTransfere', 'ONCE')) || ...
            ~isempty(regexp(machineComponent.(f{i}).model, '(homog|layer)Storage', 'ONCE')) || ...
            ~isempty(regexp(f{i}, '(HeatLoss|Thermal)', 'ONCE'))
        tmp = col.thermal.node ;
    else
        tmp = col.elmech.node ;
    end
    
    % Sum block
    if strcmpi('sum', machineComponent.(f{i}).model)

        gv_out = strcat(gv_out, ...
            [f{i} ' [ label=<\n<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0"><TR><TD>' ...
            '<TABLE COLOR="' tmp '" BORDER="1" CELLBORDER="0" CELLSPACING="0"><TR><TD>' ...
            '<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0">']);
        p = machineComponent.(f{i}).inputs;
        for j=1:length(p)
            if strfind(p{j},'Plus')
                gv_out = strcat(gv_out, ...
                    ['<TR><TD PORT="' p{j} '" ALIGN="left"><FONT COLOR="' tmp '">+</FONT></TD></TR>']);
            else
                gv_out = strcat(gv_out, ...
                    ['<TR><TD PORT="' p{j} '" ALIGN="left"><FONT COLOR="' tmp '">-</FONT></TD></TR>']);
            end
        end
        gv_out = strcat(gv_out, ...
            ['</TABLE></TD>' ...
            '<TD><TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0"><TR><TD PORT="Sum" ALIGN="right"><FONT COLOR="' tmp '">=</FONT></TD></TR></TABLE></TD></TR></TABLE></TD></TR>' ...
            '<TR><TD><FONT COLOR="' tmp '">' f{i} '</FONT></TD></TR></TABLE>\n> ];\n']);
    else

        gv_out = strcat(gv_out, ...
            [f{i} ' [ label=<\n<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0"><TR><TD>' ...
            '<TABLE COLOR="' tmp '" BORDER="1" CELLBORDER="0" CELLSPACING="0"><TR><TD>' ...
            '<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0">']);

        p = machineComponent.(f{i}).inputs;
        for j=1:length(p)
            gv_out = strcat(gv_out, ...
                ['<TR><TD PORT="' p{j} '" ALIGN="left"><FONT COLOR="' tmp '">' p{j} '</FONT></TD></TR>']);
        end

        gv_out = strcat(gv_out, [ '</TABLE></TD><TD><B><FONT COLOR="' tmp '">' f{i} '</FONT></B></TD><TD><TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0">']);       


        p = machineComponent.(f{i}).outputs;
        for j=1:length(p)
            gv_out = strcat(gv_out, ...
                ['<TR><TD PORT="' p{j} '" ALIGN="right"><FONT COLOR="' tmp '">' p{j} '</FONT></TD></TR>']);
        end
        
        gv_out = strcat(gv_out, '</TABLE></TD></TR></TABLE></TD></TR>');
        gv_out = strcat(gv_out, ['<TR><TD><FONT COLOR="' tmp '"><I>model: ' machineComponent.(f{i}).model '</I></FONT></TD></TR>']);
        
        p = fieldnames(machineComponent.(f{i}).props);
        for j=1:length(p)
            gv_out = strcat(gv_out, ...
                ['<TR><TD><FONT COLOR="' tmp '">' p{j} ': ' machineComponent.(f{i}).props.(p{j}) '</FONT></TD></TR>']);
        end

        gv_out = strcat(gv_out, '</TABLE>\n> ];\n');
    end
end

for i=1:length(simController)
    gv_out = strcat(gv_out, ...
       [simController{i}.name ' [ label=<<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0"><TR><TD><FONT COLOR="' col.sim.node '"><B>' simController{i}.name '</B></FONT></TD></TR><TR><TD><I><FONT COLOR="' col.sim.node '">' simController{i}.controller '</FONT></I></TD></TR></TABLE>> ];\n']);
end

for i=1:length(linksText)
    
    if isempty(regexp(linksText{i}{1}, '[\S]+":[\S]+ ->', 'ONCE'))
        tmp = col.sim.edge;
    elseif ~isempty(regexp(linksText{i}{1}, 'Thermal_[\S]+":[\S]+ ->', 'ONCE')) || ...
            ~isempty(regexp(linksText{i}{1}, ':PLoss', 'ONCE')) || ...
            ~isempty(regexp(linksText{i}{1}, 'HeatLoss', 'ONCE'))|| ...
            ~isempty(regexp(linksText{i}{1}, ':PThermal', 'ONCE'))
        tmp = col.thermal.edge;
    else
        tmp = col.elmech.edge;
    end
    
    if ~isempty(regexp(linksText{i}{1}, ':Temperature', 'ONCE'))
        style = 'dashed';
    else
        style = 'solid';
    end
    
    gv_out = strcat(gv_out, [linksText{i}{1} '[headport=w, tailport=e, color="' tmp '", style="' style '"]\n']);
end


gv_out = [gv_out '}'];

% Saving
try
    id = fopen([ filename '.gv'], 'w');
    fprintf(id, gv_out);
    fclose(id);
catch e
    error('Can''t open file');
end

fprintf(' done!\n'); 


%% Tex output
fprintf(' >  Generating latex file ...');

% Models
tex_out = [ '\\begin{table}\n', ...
            '\t\\centering\n', ...
            '\t\\footnotesize\n', ...
            '\t\\begin{tabular}{lllp{5cm}}\n', ...
            '\t\\toprule', ...
            '\t\tComponent\t& Model type\t& Parameter set\t& Attributes\\\\\n\t\t\\midrule'];
        
f = fieldnames(machineComponent);
for i=1:length(f)
    tex_out = strcat(tex_out, ...
        ['\t\t ' f{i} '\t& ' machineComponent.(f{i}).model '\t& ']);
    
    if isfield(machineComponent.(f{i}).props, 'type')
        tex_out = strcat(tex_out, machineComponent.(f{i}).props.type);
    else
        tex_out = strcat(tex_out, '--');
    end
    
    tex_out = strcat(tex_out, '\t&');
    
    p = fieldnames(machineComponent.(f{i}).props);
    for j=1:length(p)
        tex_out = strcat(tex_out, ...
            [p{j} ': ' machineComponent.(f{i}).props.(p{j}) '\\newline\t']);
    end
    
    tex_out = strcat(tex_out(1:end-11), '\\\\\n');
end

tex_out = strcat(tex_out,[  '\t\t\\bottomrule\n' ...
                            '\t\\end{tabular}\n' ...
                            '\t\\normalsize\n' ...
                            '\t\\caption[\\TODO]{\\TODO}\n' ...
                            '\\end{table}\n']);

                        
tex_out = strrep(tex_out,'_','\\_');
% Saving
try
    id = fopen([ filename '.tex'], 'w');
    fprintf(id, tex_out);
    fclose(id);
catch e
    error('Can''t open file');
end

fprintf(' done!\n'); 

%% Create image

try
    s = system(['dot -T' outputType ' ' filename '.gv -o '  filename '.' outputType ]);
    if s==0
        fprintf('[+] Image created\n');
    else
        fprintf('[!] Dot returned an error\n');
    end
catch e
    fprintf('[!] Call of dot failed. Please compile the file manual\n    error:%s\n', e.message);
end

end

%% FUNCTION FROM MATLAB CENTERAL
% ----- Subfunction PARSECHILDNODES -----
function children = parseChildNodes(theNode)
% Recurse over node children.
children = [];
if theNode.hasChildNodes
   childNodes = theNode.getChildNodes;
   numChildNodes = childNodes.getLength;
   allocCell = cell(1, numChildNodes);

   children = struct(             ...
      'Name', allocCell, 'Attributes', allocCell,    ...
      'Data', allocCell, 'Children', allocCell);

    for count = 1:numChildNodes
        theChild = childNodes.item(count-1);
        children(count) = makeStructFromNode(theChild);
    end
    
    nodeOk = true(size(children));
    for count = 1:numChildNodes
        if isempty(children(count).Attributes) && ...
                isempty(children(count).Children) && ...
                strcmp('',regexprep(children(count).Data,'\s',''))
            nodeOk(count) = false;
        end
    end
    children = children(nodeOk);    
end
end

% ----- Subfunction MAKESTRUCTFROMNODE -----
function nodeStruct = makeStructFromNode(theNode)
    global MACHINECOMPONENTS;
    global SIMCONTROLLERS;
% Create structure of node info.

    nodeStruct = struct(                        ...
       'Name', char(theNode.getNodeName),       ...
       'Attributes', parseAttributes(theNode),  ...
       'Data', '',                              ...
       'Children', parseChildNodes(theNode));

    if any(strcmp(methods(theNode), 'getData'))
       nodeStruct.Data = char(theNode.getData); 
    else
       nodeStruct.Data = '';
    end

    if strcmpi('machineComponent',nodeStruct.Name)
        MACHINECOMPONENTS{end+1} = nodeStruct;
    elseif strcmpi('simController',nodeStruct.Name)
        SIMCONTROLLERS{end+1} = nodeStruct;
    end
end

% ----- Subfunction PARSEATTRIBUTES -----
function attributes = parseAttributes(theNode)
% Create attributes structure.

attributes = [];
if theNode.hasAttributes
   theAttributes = theNode.getAttributes;
   numAttributes = theAttributes.getLength;
   allocCell = cell(1, numAttributes);
   attributes = struct('Name', allocCell, 'Value', ...
                       allocCell);

   for count = 1:numAttributes
      attrib = theAttributes.item(count-1);
      attributes(count).Name = char(attrib.getName);
      attributes(count).Value = char(attrib.getValue);
   end
end
end
