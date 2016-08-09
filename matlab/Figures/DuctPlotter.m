function [ ] = DuctPlotter( ductCfgPath , filename, outputType)
%FLOWCHARTPLOTTER(PATH, NAME, TYPE)
% =========================================================================
% Creates a flowchart of the machine 
% configuration located at the folder PATH (Machine.xml and IOLinking.txt
% are required). The output consits of a graphviz file NAME (default:
% 'flowchart') and a image of the format TYPE (default: ps, others: jpg,
% png, eps)
% -------------------------------------------------------------------------
% Author:  sizuest                   Copyright (c) 2012 by Inspire AG, ETHZ
% Date:    15.03.2012                All rights reserved
% Version: 1.1
% =========================================================================
%
% To compile the figure, dot is used. If the command is not available, no
% image will be created

if nargin<2
    filename = 'flowchart';
end
if nargin<3
    outputType = 'pdf';
end


%% Read files

fprintf(' >  Reading duct configuration ...');
ductFile =  xmlread( ductCfgPath );

%% Parse Duct
% Go through the whole file and create a struct, global vars for components
% and simulators will be filled up
ductStruct = parseChildNodes(ductFile);
ductStruct = removeUnusedFields(ductStruct);

fprintf(' done!\n');

[elements, connections] = getDuctElements(ductStruct.Children(4).Children, 'MAIN');

% for i=1:length(elements)
%     fprintf('%s (%s)\n', elements{i}.name, elements{i}.type)
% end




%% Create dot file

% Color configurations --------------------
col.node    = 'black';
col.edge    = 'black';

% EOCONF -----------------------------------

fprintf(' >  Generating graphiviz file ...');

gv_out = 'digraph g {\nrankdir=LR;\nsplines=true;\noverlap=scale;\nnode [shape=plaintext];\n ';

for i=1:length(elements)
    
    gv_out = strcat(gv_out, ...
        [elements{i}.link ' [ label=<\n<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0"><TR><TD>' ...
        '<TABLE COLOR="' col.node '" BORDER="1" CELLBORDER="0" CELLSPACING="0"><TR><TD>' ...
        '<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0">']);

    gv_out = strcat(gv_out, ...
            ['<TR><TD PORT="In" ALIGN="left"><FONT COLOR="' col.node '">&nbsp;</FONT></TD></TR>']);


    gv_out = strcat(gv_out, [ '</TABLE></TD><TD><B><FONT COLOR="' col.node '">' elements{i}.name '</FONT></B></TD><TD><TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0">']);       


    gv_out = strcat(gv_out, ...
            ['<TR><TD PORT="Out" ALIGN="right"><FONT COLOR="' col.node '">&nbsp;</FONT></TD></TR>']);

    gv_out = strcat(gv_out, '</TABLE></TD></TR></TABLE></TD></TR>');
    gv_out = strcat(gv_out, ['<TR><TD><FONT COLOR="' col.node '"><I>model: ' elements{i}.type '</I></FONT></TD></TR>']);
    
    if(~isempty(elements{i}.profile))
        tmp = '';
        fields = fieldnames(elements{i}.profile.props);
        for s=1:length(fields)
            tmp = [tmp num2str(elements{i}.profile.props.(fields{s})) 'x'];
        end
        tmp = tmp(1:end-1);
            
        gv_out = strcat(gv_out, ...
                ['<TR><TD><FONT COLOR="' col.node '">Profile: ' tmp ' (' strrep(elements{i}.profile.type,'hp','') ')</FONT></TD></TR>']);
    end
    
    if(~isempty(elements{i}.isolation))            
        gv_out = strcat(gv_out, ...
                ['<TR><TD><FONT COLOR="' col.node '">Isolation: ' elements{i}.isolation.type ' (' elements{i}.isolation.thickness ')</FONT></TD></TR>']);
    end

    if(~isempty(elements{i}.props))
        p = fieldnames(elements{i}.props);
        for j=1:length(p)
            gv_out = strcat(gv_out, ...
                ['<TR><TD><FONT COLOR="' col.node '">' p{j} ': ' elements{i}.props.(p{j}) '</FONT></TD></TR>']);
        end
    end

    gv_out = strcat(gv_out, '</TABLE>\n> ];\n');
end

for i=1:length(connections)   
    gv_out = strcat(gv_out, [connections{i} '[color="' col.edge '", style="solid"]\n']);
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

%% Remove unused fields
function structOut = removeUnusedFields(structIn)
    structOut = structIn;
    if ~isempty(structOut.Children)
        structOut.Children = structOut.Children(1);
    end
    
    n=0;
    
    for i=1:length(structIn.Children)
        if~strcmp(structIn.Children(i).Name, '#text')
            isFitting = false;
            if ~isempty(structIn.Children(i).Attributes)
                for j=1:length(structIn.Children(i).Attributes)
                    if strcmp(structIn.Children(i).Attributes(j).Value,'ductFitting')
                        isFitting = true;
                    end
                end
            end
            
            if ~isFitting
                n = n+1;
                structOut.Children(n) = structIn.Children(i);
            end
        end
    end
    
    for i=1:length(structOut.Children)
        structOut.Children(i) = removeUnusedFields(structOut.Children(i));
    end
    
        
end

%% makeFieldName
function out = makeFieldName(str)
    out = strrep(str, 'ü', 'ue');
    out = strrep(out, 'ö', 'oe');
    out = strrep(out, 'Ö', 'Oe');
    out = strrep(out, 'ä', 'ae');
    out = strrep(out, ' ', '');
    out = regexprep(out, '^([1-9])', 'a$1');
end

%% Get Duct Elements

function [elements, connections, lastElement] = getDuctElements(ductStruct, branch, lastElement)
    if nargin<3
        lastElement = {};
    end

    elements = {};
    connections = {};
    for i=1:length(ductStruct)
        idxName    = find(strcmp({ductStruct(i).Children.Name}, 'name'));
        idxProfile = find(strcmp({ductStruct(i).Children.Name}, 'profile'));
        idxIsolation = find(strcmp({ductStruct(i).Children.Name}, 'isolation'));
        
        a.type = ductStruct(i).Attributes(end).Value;
        a.name = ductStruct(i).Children(idxName).Children.Data;
        a.props = [];
        a.profile = [];
        a.isolation = [];
        a.link = makeFieldName([branch '_' a.name]);
        
        if ~isempty(lastElement)
            for j=1:length(lastElement) 
                connections{end+1} = [lastElement{j} ':Out -> ' a.link ':In'];
            end
        end
        
        lastElement = {a.link};
        
        if strcmp(a.type, 'ductBypass')
            [tmp1, tmp2, tmp3] = getDuctElements(ductStruct(i).Children(3).Children(4).Children, [a.link '_1'], {a.link});
            elements = [elements tmp1];
            connections = [connections tmp2];
            [tmp1, tmp2, tmp4] = getDuctElements(ductStruct(i).Children(4).Children(4).Children, [a.link '_2'], {a.link});
            elements = [elements tmp1];
            connections = [connections tmp2];
            
            lastElement = [tmp3, tmp4];

        else %if ~strcmp(a.type, 'ductElbowFitting')
            a.profile.type = ductStruct(i).Children(idxProfile).Attributes.Value;
            for j=1:length(ductStruct(i).Children(idxProfile).Children)
                a.profile.props.(makeFieldName(ductStruct(i).Children(idxProfile).Children(j).Name)) = ductStruct(i).Children(idxProfile).Children(j).Children.Data;
            end
            
            if ~isempty(idxIsolation)
                for j=1:length(ductStruct(i).Children(idxIsolation).Children)
                    a.isolation.(makeFieldName(ductStruct(i).Children(idxIsolation).Children(j).Name)) = ductStruct(i).Children(idxIsolation).Children(j).Children.Data;
                end 
            end
            
            
            for j=1:length(ductStruct(i).Children)
                if ~any([idxIsolation idxProfile idxName]==j)
                    a.props.(makeFieldName(ductStruct(i).Children(j).Name)) = ductStruct(i).Children(j).Children.Data;
                end
            end   
        end
        
        elements{end+1} = a;

    end
end

%% FUNCTION FROM MATLAB CENTERAL
% ----- Local function PARSECHILDNODES -----
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
end
end

% ----- Local function MAKESTRUCTFROMNODE -----
function nodeStruct = makeStructFromNode(theNode)
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
end

% ----- Local function PARSEATTRIBUTES -----
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


