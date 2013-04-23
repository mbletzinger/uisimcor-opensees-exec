package org.nees.illinois.uisimcor.fem_executor;

import java.util.HashMap;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.FemProgram;
import org.nees.illinois.uisimcor.fem_executor.config.FemProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.LoadSaveConfig;
import org.nees.illinois.uisimcor.fem_executor.input.FemInputFile;
import org.nees.illinois.uisimcor.fem_executor.process.DoubleMatrix;
import org.nees.illinois.uisimcor.fem_executor.process.SubstructureExecutor;

/**
 * Class to execute FEM programs.
 * @author Michael Bletzinger
 */
public class FemExecutor {

	/**
	 * Test function to see if the executor works in the given OS environment.
	 * @param args
	 *            ignored.
	 */
	// public static void main(final String[] args) {
	// String os = System.getProperty("os.name");
	// FemExecutor fem;
	// if (os.contains("Window")) {
	// fem = new FemExecutor("echo", "Is there anybody out there?", null);
	// } else {
	// fem = new FemExecutor("ls", "-l", null);
	// }
	// fem.startCmd();
	// while (fem.isDone() == false) {
	// try {
	// final int waitTime = 500;
	// Thread.sleep(waitTime);
	// } catch (InterruptedException e) {
	// @SuppressWarnings("unused")
	// int dumb = 0;
	// }
	// }
	// System.out.print("Output: \"" + fem.getPm().getOutput() + "\"");
	// }
	private FemExecutorConfig config;
	/**
	 * Map of substructure FEM executors.
	 */
	private final Map<String, SubstructureExecutor> executors = new HashMap<String, SubstructureExecutor>();
	/**
	 * Map of displacement targets for each substructure.
	 */
	private final Map<String, DoubleMatrix> displacementsMap = new HashMap<String, DoubleMatrix>();
	/**
	 * Current step.
	 */
	private int step;

	/**
	 * Load the configuration parameters for executing the FEM substructures.
	 * @param configFile
	 *            Name of the configuration file.
	 */
	public final void loadConfig(final String configFile) {
		LoadSaveConfig lsc = new LoadSaveConfig();
		lsc.setConfigFilePath(configFile);
		lsc.load();
		config = lsc.getFemConfig();
		FemProgramConfig progCfg = config.getFemProgramParameters().get(
				FemProgram.OPENSEES);
		for (String fsc : config.getSubstructCfgs().keySet()) {
			FemInputFile input = new FemInputFile(progCfg, config
					.getSubstructCfgs().get(fsc), config.getWorkDir());
			SubstructureExecutor exe = new SubstructureExecutor(progCfg, input);
			executors.put(fsc, exe);
		}
	}

	/**
	 * Set the displacement targets for a substructure.
	 * @param address
	 *            Substructure id.
	 * @param displacements
	 *            Matrix of displacements.
	 */
	public final void setDisplacements(final String address,
			final double[][] displacements) {
		DoubleMatrix data = new DoubleMatrix(displacements);
		displacementsMap.put(address, data);
	}

	/**
	 * Start execution of all of the substructures for the current step.
	 */
	public final void execute() {
		for (String mdl : executors.keySet()) {
			SubstructureExecutor exe = executors.get(mdl);
			exe.start(step, displacementsMap.get(mdl));
		}
	}

	/**
	 * Check to see if all of the substructures have finished.
	 * @return True if everything is done.
	 */
	public final boolean isDone() {
		boolean result = true;
		for (String mdl : executors.keySet()) {
			SubstructureExecutor exe = executors.get(mdl);
			result = result && exe.isDone();
		}
		return result;
	}

	/**
	 * Get the displacement response for a substructure.
	 * @param address
	 *            Substructure id.
	 * @return Displacement data.
	 */
	public final double[][] getDisplacements(final String address) {
		return executors.get(address).getDisplacements();
	}

	/**
	 * Get the force response for a substructure.
	 * @param address
	 *            Substructure id.
	 * @return Force data.
	 */
	public final double[][] getForces(final String address) {
		return executors.get(address).getForces();
	}
}
