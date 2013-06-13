${SourcedFiles}
recorder Node -file tmp_disp.txt -node ${NodeList} -dof ${ResponseDofs} disp
recorder Node -file tmp_forc.txt -node ${NodeList} -dof ${ResponseDofs} reaction
