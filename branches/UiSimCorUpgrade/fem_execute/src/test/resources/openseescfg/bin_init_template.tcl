${SourcedFiles}
recorder Node -binary tmp_disp.bin -node ${NodeList} -dof ${ResponseDofs} disp
recorder Node -binary tmp_forc.bin -node ${NodeList} -dof ${ResponseDofs} reaction
