${SourcedFiles}
recorder Node -tcp 127.0.0.1 4114 -node ${NodeList} -dof ${ResponseDofs} disp
recorder Node -tcp 127.0.0.1 4115 -node ${NodeList} -dof ${ResponseDofs} reaction
