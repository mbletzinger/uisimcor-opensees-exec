source Examples/MOST/02_Middle_OpenSees/Middle.tcl
source /Example/MOST/01_Left_OpenSees/StaticAnalysisEnv.tcl
recorder Node -file tmp_disp.out -node -dof 1 2 3 disp
recorder Node -file tmp_forc.out -node -dof 1 2 3 reaction
pattern Plain Step3	Constant {
sp 2 1 130.20300000000000000E-009
sp 2 6 34.000120000000000000E-012
sp 3 1 120.03450000000000000E-009
sp 4 1 150.11000000000000000E-009

}
analyze 1
remove loadPattern Step3
