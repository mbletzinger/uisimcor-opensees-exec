${SourcedFiles}
recorder Node -tcp 127.0.0.1 ${DispPort} -node ${NodeList} -dof ${ResponseDofs} disp
recorder Node -tcp 127.0.0.1 ${ForcePort} -node ${NodeList} -dof ${ResponseDofs} reaction
