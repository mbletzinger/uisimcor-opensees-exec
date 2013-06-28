package org.nees.illinois.uisimcor.fem_executor.test.utils;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.types.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;

/**
 * Creates a reference substructure config that is used for unit tests.
 * @author Michael Bletzinger
 */
public class CreateRefSubstructureConfig {
	/**
	 * @return the configuration.
	 */
	public final SubstructureDao getConfig() {
		return config;
	}

	/**
	 * Created configuration.
	 */
	private final SubstructureDao config;

	/**
	 * Constructor.
	 * @param address
	 *            Address of the substructure.
	 */
	public CreateRefSubstructureConfig(final String address) {
		final int node1 = 2;
		final int node2 = 3;
		final int node3 = 4;
		final String workfile1 = "Wsection.tcl";
		final String workfile2 = "acc475C.dat";
		final int initialPort = 4114;
		int dport;

		DimensionType dim = DimensionType.TwoD;
		List<Integer> nodes = new ArrayList<Integer>();
		String modelFilename;
		if (address.equals("MDL-01")) {
			nodes.add(node1);
			modelFilename = "LeftCol.tcl";
			dport = initialPort;
		} else if (address.equals("MDL-02")) {
			nodes.add(node1);
			nodes.add(node2);
			nodes.add(node3);
			modelFilename = "Middle.tcl";
			dport = initialPort + 2;
		} else {
			nodes.add(node2);
			modelFilename = "RightCol.tcl";
			final int number4 = 4;
			dport = initialPort + number4;
		}
		List<String> workFiles = new ArrayList<String>();
		workFiles.add(workfile1);
		workFiles.add(workfile2);
		List<String> sourceFiles = new ArrayList<String>();
		sourceFiles.add(modelFilename);
		sourceFiles.add("StaticAnalysisEnv.tcl");
		FemProgramType program = FemProgramType.OPENSEES;
		config = new SubstructureDao(address, dim, program, sourceFiles,
				nodes, workFiles, dport, dport + 1);
		for (Integer n : nodes) {
			List<DispDof> edof = new ArrayList<DispDof>();
			if (n == node1) {
				edof.add(DispDof.DX);
				edof.add(DispDof.RZ);
			} else {
				edof.add(DispDof.DX);
			}
			config.addEffectiveDofs(n, edof);
		}
	}

}
