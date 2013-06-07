%% Configuration Parameters for Hybrid Simulatoin 
%
%%

function [Sys, MDL, AUX] = SimConfig			
MDL = MDL_RF; AUX = MDL_AUX;        			% Type definition. Do not delete this line. 
%% Project Information  
% =================================================================================================
% Simulation configuration parameters for MOST example
%
% Unit: mm, N, sec
%
% by Oh-Sung Kwon, os.kwon@utoronto.ca
% University of Toronto
% 
% Last updated on 29/07/2011 
% =================================================================================================

%% System Configuration 
% Define number of substructure modulues, auxilary modules and nodes.

% Number of substructure modules. (Analytical model or experimental specimen)
Sys.Num_RF_Module     	= 3;

% Number of auxilary modules. (Camera or data acquisition system)
Sys.Num_AUX_Module		= 0;

% Total number of effective nodes. Effective nodes are nodes at the interface of substructures  
% or nodes subjected to external forces (inertial force from mass, static force, or displacement) 
Sys.Num_Node        	= 3;

% --------------------------------------------------------------------------------------------------
%% Stiffness Evaluation
% Define parameters for initial stiffness evaluation. 

% Evaluate Stiffness?
% 1 (Yes) to run stiffness evaluation test,
% 0 (No) to read stiffness matrix from file. In this case, user should
% provide stiffness matrices of each substructure in the files MDL01_K.txt, MDL02_K.txt, etc.
Sys.Eval_Stiffness 	= 1;

% Number of Stiffness Test
% If stiffness is evaluated experimentally, the evaluation need to be done 
% several times and the average of the results are used as the initial 
% stiffness. This parameter is used when Sys.Eval_Stiffness = 1
Sys.Num_Test_Stiffness  = 1;

% --------------------------------------------------------------------------------------------------
%% Time History Curves 
% These curves will be used as acceleration history, displacement history, or force history.
% The input file should have two columns of (time, curve). At least one curve should be defined.

Sys.TimeHistoryCurve{1}	= load('acc475C.dat');

% --------------------------------------------------------------------------------------------------
%% Non-Transient Loads 
% TimeHistoryCuve in the above can be assigned to a specific DOF as a time-dependent static load. 
% Static analysis is carried out in this stage. The load at the end of the stage is kept constant
% during transient analysis stage. 

% Sys.TimeHistoryLoad{ind}= {NodeID, DOF, Type('D' or 'F'), ScaleFactor, CurveID};
Sys.TimeHistoryLoad{1}  =   {};

% Time increment 
Sys.Static_Step_dt 	= 0;	

% Number of non-transient steps
% Sys.Static_Step_num * Sys.Static_Step_dt should be smaller than the maximum time in the time history curve. 
Sys.Static_Step_num 	= 0;


% --------------------------------------------------------------------------------------------------
%% Transient Loads 
% Acceleration at the supports are defined in this section. The unit of acceleration should be 
% consistent with the units of mass, time, and force. (i.e. mass*acc = force). In the future version, 
% transient force or displacement will be able to be applied to a structure.  

% Sys.Base_Acceleration_dof = {ScaleFactor CurveID}
Sys.Base_Acceleration_x     = {9.81        1      };
Sys.Base_Acceleration_y     = {};	
Sys.Base_Acceleration_z     = {};

% Dynamic analysis time steps
Sys.Dynamic_Step_dt 	= 0.01;

% Number of dynamic analysis steps
% Sys.Dynamic_Step_num * Sys.Dynamic_Step_dt should be smaller than the shortest time curve. 
Sys.Dynamic_Step_num 	= 500;

% --------------------------------------------------------------------------------------------------
%% Dynamic Integration Scheme
% Following integration schemes are implemented. Additional integration schemes could be easily added by a user. 
%
%  AlphaOS: Alpha-Operator Splitting scheme,  Combescure, D., and Pegon, P. (1997)
%           Sys.IntParam(1) = alpha
%           Alpha = (0 ~ 1/3). In most cases, alpha = 0.05 works.
%
%  Chang2011: Chang, Yang, and Hsu (2011)
%           Sys.IntParam(1) = beta; Sys.IntParam(2) = gamma
% Refer the above paper for definition of parameters. If 'Chang2011' method is used, the mass matrix should not 
% have zero diagonal terms as inverse of mass matrix is required. (Eq.29 in the reference) Beta = 1/4, 
% Gamma = 1/2 supposed to have unconditional stability based on Chang (2002).

Sys.IntScheme = 'AlphaOS';		% Parameters for Alpha-OS scheme
Sys.IntParam(1) = 0.05;

%Sys.IntScheme = 'Chang2011';
%Sys.IntParam(1) = 1/4;		% Beta
%Sys.IntParam(2) = 1/2;		% Gamma

% --------------------------------------------------------------------------------------------------
%% Rayleigh Damping
% xi_1 and xi_2: Damping ratio, Tn_1, Tn_2: Target period
Sys.xi_1 		= 0.00;
Sys.Tn_1 		= 0.00;
Sys.xi_2 		= 0.00;
Sys.Tn_2 		= 0.00;

% --------------------------------------------------------------------------------------------------
%% Lumped Masses 
% Lumped masses are assigned to each DOF of each node. 
% Sys.Node_Mass{Node number} = x, y, z, rx, ry, rz directional mass

% Sys.Node_Mass = {};	% if there are no nodal masses
Sys.Node_Mass{1} 	= [2.54628081981000, 0, 0, 0, 0, 1E-10];
Sys.Node_Mass{2} 	= [5.49705587697000, 0, 0, 0, 0, 0];
Sys.Node_Mass{3} 	= [2.54628081981000, 0, 0, 0, 0, 0];

% --------------------------------------------------------------------------------------------------
%% Graphic User Interface for SimCor
% Enable GUI for SimCor?
% 1 (Yes) enable the GUI for SimCor. This option slows down the simulation a little bit but user can
% pause and resume simulation. 
% 0 (No) disable the GUI for SimCor. This option connects substructure modules, and run simulation 
% automatically without user interruption. Preferable option for debugging purpose and analytical 
% simulation. 
Sys.EnableGUI       	= 1;         % Use GUI for SimCor


% --------------------------------------------------------------------------------------------------
%% Basic Substructure Module Parameters   
% Following parameters should be defined for each substructure module.

% Create objects of MDL_RF
MDL(1) 			= MDL_RF;
MDL(2) 			= MDL_RF;
MDL(3) 			= MDL_RF;

% Name of each module. It does not affect simulation but used as an
% identifier in log files. 
MDL(1).name 		= 'LeftCol'; 	% Module ID of this module is 1
MDL(2).name 		= 'Middle'; 	% Module ID of this module is 2
MDL(3).name 		= 'RightCol'; 	% Module ID of this module is 3

% URL of each module
%MDL(1).URL  		= '127.0.0.1:11997';
%MDL(2).URL  		= '127.0.0.1:11998';
%MDL(3).URL  		= '127.0.0.1:11999';

% Communication protocol for each module. The name of the communication
% protocols are somewhat misleading, and will be changed in the future
% version. TCPIP protocol uses binary format to exchange data. This
% protocol is recommended as it is most efficient and reliable. The format
% of data communication are defined in a separate document. LabView1 
% protocol is based on ASCII format and the format was initially developed 
% by NEES. LabView2 is a simplified version introduced in SimCor. NTCP
% seems to be no longer supported and involves a large overhead as all
% communication goes through NEESPOP server. 
%       tcpip     : binary communication using TCPIP
%       LabView1  : ASCII communication with LabView plugin format
%       LabView2  : same as LabView1 but 'execute' and 'query' commands are not used
%       NTCP      : communicate through NEESPOP server
%       elastick  : use stiffness matrix to calculate force. SimCor will load stiffness matrix of each 
%                   module. No network communication and substructure models are necessary. This option
%                   is primarily for debugging purpuse in SimCor.
%       fem_executor : Run one or more analytical modules locally with the FemExecutor java library. 
MDL(1).protocol 	= 'fem_executor';
MDL(2).protocol 	= 'fem_executor';
MDL(3).protocol 	= 'fem_executor';


% Module 1: Left column ----------------------------------------------------------------------------
MDL(1).node    		= [1];           % Control point node number
MDL(1).EFF_DOF 		= [1 0 0 0 0 1]; % Effective DOF for CP 1


% Module 2: Middle column and beams ----------------------------------------------------------------
MDL(2).node    		= [1 2 3];       % Control point node number
MDL(2).EFF_DOF 		= [1 0 0 0 0 1   % Effective DOF for CP 1
               		   1 0 0 0 0 0    % Effective DOF for CP 2
               		   1 0 0 0 0 0];  % Effective DOF for CP 3

% Module 3: Right column ---------------------------------------------------------------------------
MDL(3).node    		= [3];           % Control point node number
MDL(3).EFF_DOF 		= [1 0 0 0 0 0]; % Effective DOF for CP 3


% Dismplacement for stiffness evaluation of each module
% Del_t: Translation, Del_r: Rotation in radian
MDL(1).DEL_t 		= 0.005;
MDL(2).DEL_t 		= 0.005;
MDL(3).DEL_t 		= 0.005;
             		
MDL(1).DEL_r 		= 0.002;
MDL(2).DEL_r 		= 0.002;
MDL(3).DEL_r 		= 0.002;

% Enable GUI for each module?
% GUI for each module can only display the data.
% GUI for each module can not control the hybrid simulation.
% Yes (1) enable the GUI for each module
% No  (0) disable the GUI for each module
MDL(1).EnableGUI 	= 0;
MDL(2).EnableGUI 	= 0;
MDL(3).EnableGUI 	= 0;

% --------------------------------------------------------------------------------------------------
%% Advanced Substructure Module Parameters
% These parameters need to be redefined in the following situations.
%     (1) To change coordinate system between SimCor and substructure module
%     (2) To apply scale factors in displacement and measured forces
%     (3) To define limits based on measured force or displacement (for tolerance and safety)
%     (4) When it is necessary to resume experiment due to unexpected circumstances.

% Coordinate transformation - ----------------------------------------------------------------------
% If transformation is required, define transformation matrix. 
for i=1:Sys.Num_RF_Module 
	MDL(i).TransM = [];
end

% Scale factor for displacement, rotation, force, and moment
% Experimental specimens are not always in full scale. Use this factors to apply scale factors. 
% The displacement scale factors are multiplied before they are sent to module. Measured force and 
% moments are DIVIDED with the scale factors before used in the PSD algorithm.
for i=1:Sys.Num_RF_Module 
	MDL(i).ScaleF = [1 1 1 1];	
end

% Relaxation check ---------------------------------------------------------------------------------
% If this parameter is 1, SimCor send commend to retrieve data and check relaxation just before 
% the execution of proposed command. In most cases, and if hybrid simulation runs at a certain rate, 
% relation is not an issue. 
for i=1:Sys.Num_RF_Module 
	MDL(i).CheckRelax  = 0;		% Module i
	% if MDL(i).CheckLimit=1, define the following variables. 
  	% Variable size should be (number of control nodes)* 6 array
  	%
  	% Displacement variation ratio (not increment)
  	% MDL(i).MES_D_inc = [ a b c d e f    
  	%                      ...        ];
  	% Force variaiton ratio (not increment)
  	% MDL(i).MES_F_inc = [ a b c d e f    
  	%                      ...        ];
end

% Check displacement and force limit ---------------------------------------------------------------
% At every steps, check if the displacement or force are approaching to the capacity of the 
% equipments' stroke or force capacity.
for i=1:Sys.Num_RF_Module 
  	MDL(i).CheckLimit  = 0;   % Module i
  	% if MDL(i).CheckLimit=1, define the following variables. 
  	% Variable size should be (number of control nodes)* 6 array
  	%
  	% Displacement increment limit(not ratio)
  	% MDL(i).TGT_D_inc = [ a b c d e f    
    	%                     ...        ];
    	% Displacement limit
  	% MDL(i).CAP_D_tot = [ a b c d e f    
    	%                     ...        ];
    	% Force limit
  	% MDL(i).CAP_F_tot = [ a b c d e f    
    	%                     ...        ];
    	% Displacement tolerance (ratio)
  	% MDL(i).TOL_D_inc = [ a b c d e f    
    	%                     ...        ];
end

% Define restarting point of a module. 
% Previous test data should exist up to the restarting point.
% MDL(2).birth_step = 100;			% the step that experiment will resume.
% MDL(2).prev_hist  = 'MDL02_recv.pre';	    	% previous test history which will be used until 


% __________________________________________________________________________________________________
%
% Auxiliary module configuration
% __________________________________________________________________________________________________

% AUX(1)                = MDL_AUX;
% AUX(1).URL            = '127.0.0.1:12000';
% AUX(1).protocol       = 'labview1';
% AUX(1).name           = 'Camera';     % Module ID of this mdoule is 1


