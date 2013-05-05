source LeftCol.tcl
source StaticAnalysisEnv.tcl
recorder Node -file tmp_disp.out -node -dof 1 2 3 disp
recorder Node -file tmp_forc.out -node -dof 1 2 3 reaction
pattern Plain 999999	Constant {
sp 1 1 00.002300000e-14
sp 3 1 00.000001200e-14
}
analyze 1
remove loadPattern 999999
