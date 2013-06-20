${SourcedFiles}
recorder Node -binary tmp_disp.out -node ${NodeList} -dof ${ResponseDofs} disp
recorder Node -binary tmp_forc.out -node ${NodeList} -dof ${ResponseDofs} reaction
