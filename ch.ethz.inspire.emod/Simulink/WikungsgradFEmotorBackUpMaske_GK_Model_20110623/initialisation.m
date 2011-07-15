%% INITIALISATION
% INITIALISATION OF THE MODEL

%% Set charset
bdclose all
slCharacterEncoding('ISO-8859-1')

%% Open Model
addpath('Lib');
open('NDM200');

%% Config model
% Thermal Config
%for i=1:length( Configuration.Settings );
%    set_param('NDM200/TurningMachine',Configuration.Components{i},Configuration.Settings(i));
%end

% Control Config
%for i=1:length( Control.Settings );
%    set_param('NDM200/Control',Control.Components{i},Control.Settings(i,:));
%end
