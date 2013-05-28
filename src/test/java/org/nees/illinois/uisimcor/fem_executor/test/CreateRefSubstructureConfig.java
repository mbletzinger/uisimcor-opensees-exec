package org.nees.illinois.uisimcor.fem_executor.test;

import java.util.ArrayList;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.DimensionType;
import org.nees.illinois.uisimcor.fem_executor.config.DispDof;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;

/**
 * Creates a reference substructure config that is used for unit tests.
 * @author Michael Bletzinger
 */
public class CreateRefSubstructureConfig {
	/**
	 * @return the configuration.
	 */
	public final SubstructureConfig getConfig() {
		return config;
	}

	/**
	 * Created configuration.
	 */
	private final SubstructureConfig config;

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

		DimensionType dim = DimensionType.TwoD;
		List<Integer> nodes = new ArrayList<Integer>();
		String modelFilename;
		if (address.equals("MDL-01")) {
			nodes.add(node1);
			modelFilename = "LeftCol.tcl";
		} else if (address.equals("MDL-02")) {
			nodes.add(node1);
			nodes.add(node2);
			nodes.add(node3);
			modelFilename = "Middle.tcl";
		} else {
			nodes.add(node2);
			modelFilename = "RightCol.tcl";
		}
		List<String> workFiles = new ArrayList<String>();
		workFiles.add(workfile1);
		workFiles.add(workfile2);
		FemProgramType program = FemProgramType.OPENSEES;
		config = new SubstructureConfig(address, dim, program, modelFilename,
				nodes, workFiles);
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
