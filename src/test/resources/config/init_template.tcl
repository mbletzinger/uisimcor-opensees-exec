${SourcedFiles}
recorder Node -file tmp_disp.out -node ${NodeList} -dof ${ResponseDofs} disp
recorder Node -file tmp_forc.out -node ${NodeList} -dof ${ResponseDofs} reaction
