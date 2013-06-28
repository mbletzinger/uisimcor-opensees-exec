source Middle.tcl
source StaticAnalysisEnv.tcl

recorder Node -file tmp_disp.out -node 2 3 4 -dof 1 2 3 disp
recorder Node -file tmp_forc.out -node 2 3 4 -dof 1 2 3 reaction
pattern Plain 99003	Constant {
sp 2 1 130.20300000000000000E-009
sp 2 3 34.000120000000000000E-012
sp 3 1 120.03450000000000000E-009
sp 4 1 150.11000000000000000E-009

}
analyze 1
remove loadPattern 99003
