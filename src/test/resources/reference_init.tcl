source Middle.tcl
source StaticAnalysisEnv.tcl
recorder Node -tcp 127.0.0.1 4114 -node 2 3 4 -dof 1 2 3 disp
recorder Node -tcp 127.0.0.1 4115 -node 2 3 4 -dof 1 2 3 reaction
