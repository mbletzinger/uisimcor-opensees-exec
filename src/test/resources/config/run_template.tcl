source ${ModelFile}
source ${StaticAnalysisFile}
recorder Node -file tmp_disp.out -node -dof ${ResponseDofs} disp
recorder Node -file tmp_forc.out -node -dof ${ResponseDofs} reaction
pattern Plain ${StepNumber}	Constant {
${LoadPattern}
}
analyze 1
remove loadPattern ${StepNumber}
