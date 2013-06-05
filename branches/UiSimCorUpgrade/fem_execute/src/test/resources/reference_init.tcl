source Middle.tcl
source StaticAnalysisEnv.tcl
recorder Node -file tmp_disp.out -node 2 3 4 -dof 1 2 3 disp
recorder Node -file tmp_forc.out -node 2 3 4 -dof 1 2 3 reaction
