package org.nees.illinois.uisimcor.opensees.test_utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nees.illinois.uisimcor.opensees.test_utils.dao.DofLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create an FEM Executor configuration file from the internal Node
 * representation.
 * @author Michael Bletzinger
 */
public class FemExecutorConfigCreate {
	/**
	 * Current contents of the configuration file.
	 */
	private final List<String> contents = new ArrayList<String>();
	/**
	 * List of effective DOFs for UI-SimCor.
	 */
	private final List<DofLabel> controlDofs;

	/**
	 * Current TCP port number for the OpenSees recorders.
	 */
	private int currentTcpPort;
	/**
	 * Header contents for the configuration file.
	 */
	private final String header = "OPENSEES.executable=/usr/bin/OpenSees\n"
			+ "OPENSEES.file.template.step=step_template.tcl\n"
			+ "OPENSEES.file.template.init=init_template.tcl\n"
			+ "OPENSEES.file.template.run=run_template.tcl\n"
			+ "OPENSEES.step.record.index=0\n";
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(FemExecutorConfigCreate.class);

	/**
	 * @param controlDofs
	 *            List of effective DOFs for UI-SimCor.
	 */
	public FemExecutorConfigCreate(final List<DofLabel> controlDofs) {
		this.controlDofs = controlDofs;
		final int tcpPortStart = 4114;
		this.currentTcpPort = tcpPortStart;
	}

	/**
	 * Add a set of nodes associated with a substructure.
	 * @param name
	 *            of the substructure.
	 * @param controlNodes
	 *            Set of control nodes.
	 * @param substructureNodes
	 *            Set of interface nodes.
	 */
	public final void addSubstructure(final String name,
			final List<Integer> controlNodes,
			final List<Integer> substructureNodes) {
		List<Integer> nodes = new ArrayList<Integer>();
		nodes.addAll(substructureNodes);
		nodes.addAll(controlNodes);
		Collections.sort(nodes);
		String cn = name + ".control.nodes=";
		boolean first = true;
		for (Integer i : nodes) {
			cn += (first ? "" : ",") + i;
			first = false;
			String ed = name + ".effective.dofs." + i + "=";
			if (substructureNodes.contains(i)) {
				ed += "Dx, Dy, Dz, Rx, Ry, Rz";
			} else {
				boolean dfirst = true;
				for (DofLabel d : controlDofs) {
					ed += (dfirst ? "" : ",") + d;
					dfirst = false;
				}
			}
			contents.add(ed);
		}
		contents.add(cn);
	}

	/**
	 * Add extra lines for a substructure.
	 * @param name
	 *            of the substructure.
	 * @param sourcefiles
	 *            Tcl files sourced for the substructure.
	 * @param workfiles
	 *            extra files used by the analysis of the substructure.
	 */
	public final void addSuffixes(final String name,
			final List<String> sourcefiles, final List<String> workfiles) {
		String sf = name + ".source.files=";
		boolean first = true;
		for (String s : sourcefiles) {
			sf += (first ? "" : ",") + s;
			first = false;
		}
		if (sourcefiles.isEmpty() == false) {
			contents.add(sf);
		}
		String wf = name + ".work.files=";
		first = true;
		for (String w : workfiles) {
			wf += (first ? "" : ",") + w;
			first = false;
		}
		if (workfiles.isEmpty() == false) {
			contents.add(wf);
		}
		contents.add(name + ".dimension=ThreeD");
		contents.add(name + ".fem.program=OPENSEES");
		contents.add(name + ".tcp.port.disp=" + currentTcpPort);
		currentTcpPort++;
		contents.add(name + ".tcp.port.forc=" + currentTcpPort);
		currentTcpPort++;
	}

	/**
	 * Closes the FileWriter.
	 * @param writer
	 *            FileWriter handle.
	 */
	private void closeFile(final FileWriter writer) {
		try {
			writer.close();
		} catch (IOException e) {
			log.error("Could not close file");
			return;
		}
	}

	/**
	 * Write contents to a file.
	 * @param file
	 *            to write to.
	 */
	@SuppressWarnings("resource")
	public final void createFile(final String file) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(new File(file));
		} catch (IOException e) {
			log.error("File \"" + file + "\" could not be opened for writing");
			closeFile(writer);
			return;
		}
		try {
			writer.write(header);
		} catch (IOException e) {
			log.error("Could not write \"" + header + "\" to file \"" + file
					+ "\"");
			closeFile(writer);
			return;
		}
		for (String l : contents) {
			try {
				writer.write(l + "\n");
			} catch (IOException e) {
				log.error("Could not write \"" + l + "\" to file \"" + file
						+ "\"");
				closeFile(writer);
				return;
			}
		}
		closeFile(writer);
	}
}
