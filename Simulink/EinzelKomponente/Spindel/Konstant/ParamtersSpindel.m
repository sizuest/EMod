%% PARAMETERS
% INITIALISATION OF THE MODEL PARAMETER
% STATUS: TESTING

%% General
SimulTime = 1000;
T_amb= 295;    % [K] Ambient Temperature

%% Wärmefluss
MainDrive.A=0.7;     % [m^2]
MainDrive.m=30;      % [Kg] 
R=25;
cp= 460;            % [J/kg/K] Internal heat capacity
e=  0.9;            % [m^2] surface area
sb=5.67*(10^-8);    % [W/m^2/K^4] surface area
%% Main Drive Cooling
p1=380;
n1=0.78;
p2=1100;
n2=0.7;
p=500;
n=0.8;
Venti_n2 = 273+40;
MainDrive.m = 40;            % [kg] Mass
MainDrive.R  = 25;            % [W/K/m^2] Heat transfere resistance of surface
MainDrive.A  = 0.7;           % [m^2] surface area
MainDriveCooling.epsilon = 0.8;
MainDriveCooling.Tmin = 30+273;
MainDriveCooling.Tmax = 40+273;
MainDriveCooling.Air.T_int = [10 20 30 40 50 60 70 80 90]+273;
MainDriveCooling.Air.T_ext = [10 20 30 40]+273;
MainDriveCooling.Air.P  = [     150, 0, 0,  0; ...
                                1500, 150, 0, 0; ...
                                3000, 1500, 150, 0; ...
                                4500, 3000, 1500, 150; ...
                                6000, 4500, 3000, 1500; ...
                                7500, 6000, 4500, 3000; ...
                                9000, 7500, 6000, 4500; ...
                                10500, 9000, 7500, 6000; ...
                                12000, 10500, 9000, 7500  ];
                            
  MainDriveCooling.Air.P_n2  = [200, 0, 0,  0; ...
                                2000, 200, 0, 0; ...
                                3500, 2000, 200, 0; ...
                                5000, 3500, 2000, 200; ...
                                6500, 5000, 3500, 2000; ...
                                8000, 6500, 5000, 3500; ...
                                9500, 8000, 6500, 5000; ...
                                11000, 9500, 8000, 6500; ...
                                12500, 11000, 9500, 8000  ];
%% Main Drive
MainDrive.Pmax = 1.5*41600;  % [W] Maximum mechanical shaft power
MainDrive.Theta = .01;    % [m^2 kg] Main drive inertia
MainDrive.Tabelomega= [100	200	300	400	500	600	700	800	900	1000	1100	1200	1300	1400	1500	1600	1700	1800	1900	2000	2100	2200	2300	2400	2500	2600	2700	2800	2900	3000	3100	3200	3300	3400	3500	3600	3700	3800	3900	4000	4100	4200	4300	4400	4500	4600	4700	4800	4900	5000	5100	5200	5300	5400	5500	5600	5700	5800	5900	6000	6100	6200	6300	6400	6500];
MainDrive.TabelM = [100	200	300	400	500	600	700	800	900	1000	5000	10000	15000	20000	25000	30000	35000	40000	41600];
MainDrive.Tabeleta=([     
0.36	0.39	0.41	0.41	0.42	0.42	0.42	0.43	0.43	0.43	0.60	0.60	0.60	0.60	0.60	0.60	0.60	0.60	0.60
0.38	0.47	0.50	0.53	0.54	0.55	0.56	0.56	0.57	0.57	0.60	0.60	0.60	0.60	0.60	0.60	0.60	0.60	0.60
0.34	0.46	0.52	0.55	0.58	0.59	0.61	0.62	0.63	0.63	0.69	0.69	0.69	0.69	0.69	0.69	0.69	0.69	0.69
0.30	0.43	0.50	0.54	0.58	0.60	0.62	0.63	0.65	0.65	0.73	0.74	0.74	0.74	0.74	0.74	0.74	0.74	0.74
0.26	0.39	0.47	0.53	0.56	0.59	0.62	0.63	0.65	0.66	0.76	0.78	0.78	0.78	0.78	0.78	0.78	0.78	0.78
0.23	0.36	0.45	0.50	0.55	0.58	0.60	0.62	0.64	0.66	0.78	0.80	0.81	0.81	0.81	0.81	0.81	0.81	0.81
0.21	0.33	0.42	0.48	0.52	0.56	0.59	0.61	0.63	0.65	0.80	0.82	0.83	0.83	0.83	0.83	0.83	0.83	0.83
0.19	0.31	0.39	0.45	0.50	0.54	0.57	0.59	0.62	0.63	0.80	0.83	0.84	0.85	0.85	0.85	0.85	0.85	0.85
0.17	0.29	0.37	0.43	0.48	0.52	0.55	0.58	0.60	0.62	0.81	0.84	0.85	0.86	0.86	0.86	0.86	0.86	0.86
0.16	0.27	0.35	0.41	0.46	0.50	0.53	0.56	0.58	0.60	0.81	0.85	0.86	0.86	0.87	0.87	0.87	0.87	0.87
0.15	0.25	0.33	0.39	0.44	0.48	0.52	0.54	0.57	0.59	0.81	0.85	0.86	0.87	0.88	0.88	0.88	0.88	0.88
0.13	0.23	0.31	0.37	0.42	0.46	0.50	0.53	0.55	0.57	0.81	0.85	0.87	0.88	0.88	0.88	0.88	0.88	0.88
0.13	0.22	0.30	0.36	0.40	0.45	0.48	0.51	0.54	0.56	0.81	0.85	0.87	0.88	0.89	0.89	0.89	0.89	0.89
0.12	0.21	0.28	0.34	0.39	0.43	0.46	0.50	0.52	0.54	0.80	0.86	0.87	0.88	0.89	0.89	0.90	0.90	0.90
0.11	0.20	0.27	0.32	0.37	0.41	0.45	0.48	0.51	0.53	0.80	0.86	0.88	0.89	0.89	0.90	0.90	0.90	0.90
0.10	0.19	0.25	0.31	0.36	0.40	0.43	0.47	0.49	0.52	0.80	0.86	0.88	0.89	0.89	0.90	0.90	0.90	0.91
0.10	0.18	0.24	0.30	0.34	0.38	0.42	0.45	0.48	0.50	0.79	0.85	0.88	0.89	0.90	0.90	0.90	0.91	0.91
0.09	0.17	0.23	0.28	0.33	0.37	0.40	0.44	0.46	0.49	0.79	0.85	0.88	0.89	0.90	0.90	0.91	0.91	0.91
0.09	0.16	0.22	0.27	0.32	0.36	0.39	0.42	0.45	0.47	0.78	0.85	0.88	0.89	0.90	0.90	0.91	0.91	0.91
0.08	0.15	0.21	0.26	0.30	0.34	0.38	0.41	0.43	0.46	0.77	0.85	0.88	0.89	0.90	0.90	0.91	0.91	0.91
0.08	0.14	0.20	0.25	0.29	0.33	0.36	0.39	0.42	0.45	0.77	0.85	0.87	0.89	0.90	0.90	0.91	0.91	0.91
0.07	0.14	0.19	0.24	0.28	0.32	0.35	0.38	0.41	0.43	0.76	0.84	0.87	0.89	0.90	0.91	0.91	0.91	0.91
0.07	0.13	0.18	0.23	0.27	0.31	0.34	0.37	0.40	0.42	0.76	0.84	0.87	0.89	0.90	0.91	0.91	0.91	0.92
0.07	0.13	0.18	0.22	0.26	0.30	0.33	0.36	0.39	0.41	0.75	0.84	0.87	0.89	0.90	0.90	0.91	0.91	0.92
0.06	0.12	0.17	0.21	0.25	0.29	0.32	0.35	0.38	0.40	0.74	0.83	0.87	0.89	0.90	0.90	0.91	0.91	0.92
0.06	0.12	0.16	0.21	0.24	0.28	0.31	0.34	0.37	0.39	0.74	0.83	0.86	0.88	0.90	0.90	0.91	0.91	0.92
0.06	0.11	0.16	0.20	0.24	0.27	0.30	0.33	0.36	0.38	0.73	0.83	0.86	0.88	0.90	0.90	0.91	0.91	0.92
0.06	0.11	0.15	0.19	0.23	0.26	0.29	0.32	0.35	0.37	0.72	0.82	0.86	0.88	0.89	0.90	0.91	0.91	0.92
0.05	0.10	0.15	0.19	0.22	0.26	0.29	0.31	0.34	0.36	0.72	0.82	0.86	0.88	0.89	0.90	0.91	0.91	0.92
0.05	0.10	0.14	0.18	0.22	0.25	0.28	0.30	0.33	0.35	0.71	0.81	0.86	0.88	0.89	0.90	0.91	0.91	0.92
0.05	0.10	0.14	0.18	0.21	0.24	0.27	0.30	0.32	0.34	0.70	0.81	0.85	0.88	0.89	0.90	0.91	0.91	0.91
0.05	0.09	0.13	0.17	0.20	0.24	0.26	0.29	0.31	0.34	0.70	0.81	0.85	0.87	0.89	0.90	0.91	0.91	0.91
0.05	0.09	0.13	0.17	0.20	0.23	0.26	0.28	0.31	0.33	0.69	0.80	0.85	0.87	0.89	0.90	0.91	0.91	0.91
0.05	0.09	0.13	0.16	0.19	0.22	0.25	0.28	0.30	0.32	0.69	0.80	0.85	0.87	0.89	0.90	0.91	0.91	0.91
0.04	0.09	0.12	0.16	0.19	0.22	0.24	0.27	0.29	0.31	0.68	0.79	0.84	0.87	0.88	0.90	0.90	0.91	0.91
0.04	0.08	0.12	0.15	0.18	0.21	0.24	0.26	0.29	0.31	0.67	0.79	0.84	0.87	0.88	0.89	0.90	0.91	0.91
0.04	0.08	0.12	0.15	0.18	0.21	0.23	0.26	0.28	0.30	0.67	0.79	0.84	0.86	0.88	0.89	0.90	0.91	0.91
0.04	0.08	0.11	0.15	0.17	0.20	0.23	0.25	0.27	0.30	0.66	0.78	0.83	0.86	0.88	0.89	0.90	0.91	0.91
0.04	0.08	0.11	0.14	0.17	0.20	0.22	0.25	0.27	0.29	0.66	0.78	0.83	0.86	0.88	0.89	0.90	0.91	0.91
0.04	0.07	0.11	0.14	0.17	0.19	0.22	0.24	0.26	0.28	0.65	0.78	0.83	0.86	0.88	0.89	0.90	0.91	0.91
0.04	0.07	0.10	0.13	0.16	0.19	0.21	0.24	0.26	0.28	0.64	0.77	0.83	0.86	0.87	0.89	0.90	0.90	0.91
0.04	0.07	0.10	0.13	0.16	0.19	0.21	0.23	0.25	0.27	0.64	0.77	0.82	0.85	0.87	0.89	0.90	0.90	0.91
0.04	0.07	0.10	0.13	0.16	0.18	0.20	0.23	0.25	0.27	0.63	0.76	0.82	0.85	0.87	0.88	0.90	0.90	0.90
0.03	0.07	0.10	0.13	0.15	0.18	0.20	0.22	0.24	0.26	0.63	0.76	0.82	0.85	0.87	0.88	0.89	0.90	0.90
0.03	0.07	0.10	0.12	0.15	0.17	0.20	0.22	0.24	0.26	0.62	0.76	0.81	0.85	0.87	0.88	0.89	0.90	0.90
0.03	0.06	0.09	0.12	0.15	0.17	0.19	0.21	0.23	0.25	0.62	0.75	0.81	0.84	0.87	0.88	0.89	0.90	0.90
0.03	0.06	0.09	0.12	0.14	0.17	0.19	0.21	0.23	0.25	0.61	0.75	0.81	0.84	0.86	0.88	0.89	0.90	0.90
0.03	0.06	0.09	0.12	0.14	0.16	0.19	0.21	0.23	0.25	0.61	0.74	0.81	0.84	0.86	0.88	0.89	0.90	0.90
0.03	0.06	0.09	0.11	0.14	0.16	0.18	0.20	0.22	0.24	0.60	0.74	0.80	0.84	0.86	0.88	0.89	0.90	0.90
0.03	0.06	0.09	0.11	0.14	0.16	0.18	0.20	0.22	0.24	0.60	0.74	0.80	0.84	0.86	0.87	0.89	0.89	0.90
0.03	0.06	0.08	0.11	0.13	0.15	0.18	0.20	0.22	0.23	0.59	0.73	0.80	0.83	0.86	0.87	0.88	0.89	0.90
0.03	0.06	0.08	0.11	0.13	0.15	0.17	0.19	0.21	0.23	0.59	0.73	0.79	0.83	0.85	0.87	0.88	0.89	0.89
0.03	0.06	0.08	0.10	0.13	0.15	0.17	0.19	0.21	0.23	0.58	0.73	0.79	0.83	0.85	0.87	0.88	0.89	0.89
0.03	0.05	0.08	0.10	0.13	0.15	0.17	0.19	0.20	0.22	0.58	0.72	0.79	0.83	0.85	0.87	0.88	0.89	0.89
0.03	0.05	0.08	0.10	0.12	0.14	0.16	0.18	0.20	0.22	0.57	0.72	0.79	0.82	0.85	0.87	0.88	0.89	0.89
0.03	0.05	0.08	0.10	0.12	0.14	0.16	0.18	0.20	0.22	0.57	0.72	0.78	0.82	0.85	0.86	0.88	0.89	0.89
0.03	0.05	0.08	0.10	0.12	0.14	0.16	0.18	0.19	0.21	0.56	0.71	0.78	0.82	0.84	0.86	0.88	0.89	0.89
0.03	0.05	0.07	0.10	0.12	0.14	0.16	0.17	0.19	0.21	0.56	0.71	0.78	0.82	0.84	0.86	0.87	0.88	0.89
0.03	0.05	0.07	0.09	0.11	0.13	0.15	0.17	0.19	0.21	0.55	0.70	0.77	0.81	0.84	0.86	0.87	0.88	0.89
0.02	0.05	0.07	0.09	0.11	0.13	0.15	0.17	0.19	0.20	0.55	0.70	0.77	0.81	0.84	0.86	0.87	0.88	0.88
0.02	0.05	0.07	0.09	0.11	0.13	0.15	0.17	0.18	0.20	0.55	0.70	0.77	0.81	0.84	0.86	0.87	0.88	0.88
0.02	0.05	0.07	0.09	0.11	0.13	0.15	0.16	0.18	0.20	0.54	0.69	0.77	0.81	0.83	0.85	0.87	0.88	0.88
0.02	0.05	0.07	0.09	0.11	0.13	0.14	0.16	0.18	0.19	0.54	0.69	0.76	0.80	0.83	0.85	0.87	0.88	0.88
0.02	0.05	0.07	0.09	0.11	0.12	0.14	0.16	0.18	0.19	0.53	0.69	0.76	0.80	0.83	0.85	0.86	0.88	0.88
0.02	0.04	0.07	0.09	0.10	0.12	0.14	0.16	0.17	0.19	0.53	0.68	0.76	0.80	0.83	0.85	0.86	0.87	0.88

]);
