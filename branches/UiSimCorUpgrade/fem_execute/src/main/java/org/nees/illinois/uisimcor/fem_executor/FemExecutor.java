package org.nees.illinois.uisimcor.fem_executor;

import java.util.HashMap;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.FemProgramType;
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

	// /**
	// * Test function to see if the executor works in the given OS environment.
	// * @param args
	// * ignored.
	// */
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
	/**
	 * Instance containing all of the configuration parameters.
	 */
	private FemExecutorConfig config;

	/**
	 * Path to the root directory of the configurations.
	 */
	private final String configRootDir;

	/**
	 * Folder to store temporary files during model execution.
	 */
	private final String workDir;

	/**
	 * Map of displacement targets for each substructure.
	 */
	private final Map<String, DoubleMatrix> displacementsMap = new HashMap<String, DoubleMatrix>();

	/**
	 * Map of substructure FEM executors.
	 */
	private final Map<String, SubstructureExecutor> executors = new HashMap<String, SubstructureExecutor>();

	/**
	 * Current step.
	 */
	private int step;

	/**
	 * @param configDir
	 *            Root directory containing the model files.
	 * @param workDir
	 *            Folder to store temporary files during model execution.
	 */
	public FemExecutor(final String configDir, final String workDir) {
		this.configRootDir = configDir;
		this.workDir = workDir;
	}

	/**
	 * Load the input file parameters into each executor.
	 */
	public void setup() {
		FemProgramConfig progCfg = config.getFemProgramParameters().get(
				FemProgramType.OPENSEES);
		for (String fsc : config.getSubstructCfgs().keySet()) {
			FemInputFile input = new FemInputFile(progCfg, config
					.getSubstructCfgs().get(fsc), workDir,
					configRootDir);
			SubstructureExecutor exe = new SubstructureExecutor(progCfg, input);
			executors.put(fsc, exe);
		}
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
	 * @return the config
	 */
	public final FemExecutorConfig getConfig() {
		return config;
	}

	/**
	 * @return the configRootDir
	 */
	public final String getConfigRootDir() {
		return configRootDir;
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

	/**
	 * @return the step
	 */
	public final int getStep() {
		return step;
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
	 * Abort the execution.
	 * @return True if the abort has completed.
	 */
	public final boolean abort() {
		boolean result = true;
		for (String mdl : executors.keySet()) {
			SubstructureExecutor exe = executors.get(mdl);
			result = result && exe.abort();
		}
		return result;
	}

	/**
	 * Load the configuration parameters for executing the FEM substructures.
	 * @param configFile
	 *            Name of the configuration file.
	 */
	public final void loadConfig(final String configFile) {
		LoadSaveConfig lsc = new LoadSaveConfig();
		lsc.setConfigFilePath(configFile);
		lsc.load(configRootDir);
		config = lsc.getFemConfig();
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public final void setConfig(final FemExecutorConfig config) {
		this.config = config;
	}

	/**
	 * Set the displacement targets for a substructure. Note that the matrix
	 * includes all 6 displacements DOFs regardless of which of them are
	 * effective. The executor uses the effective DOFs configuration parameter
	 * to select which values to use from the matrix. ( See the function
	 * <em>addEffectiveDofs</em> {@link SubstructureConfig here}).
	 * @param address
	 *            Substructure id.
	 * @param displacements
	 *            Matrix of displacements. Size of the matrix is (# of control
	 *            nodes) x 6.
	 */
	public final void setDisplacements(final String address,
			final double[][] displacements) {
		DoubleMatrix data = new DoubleMatrix(displacements);
		displacementsMap.put(address, data);
	}

	/**
	 * @param step
	 *            the step to set
	 */
	public final void setStep(final int step) {
		this.step = step;
	}
}
