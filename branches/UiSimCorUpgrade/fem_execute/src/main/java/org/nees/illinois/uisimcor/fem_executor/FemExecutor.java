package org.nees.illinois.uisimcor.fem_executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.LoadSaveConfig;
import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.input.WorkingDir;
import org.nees.illinois.uisimcor.fem_executor.process.SubstructureExecutor;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

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
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(FemExecutor.class);
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
	private final Map<String, List<Double>> displacementsMap = new HashMap<String, List<Double>>();

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
	 * @return True if successful.
	 */
	public final boolean setup() {
		ProgramDao progCfg = config.getFemProgramParameters().get(
				FemProgramType.OPENSEES);
		WorkingDir wd = new WorkingDir(workDir, configRootDir);
		boolean result = true;
		for (String fsc : config.getSubstructCfgs().keySet()) {
			SubstructureDao scfg = config.getSubstructCfgs().get(fsc);
			wd.setSubstructureCfg(scfg);
			wd.createWorkDir();
			SubstructureExecutor exe = new SubstructureExecutor(progCfg, scfg,
					configRootDir, wd.getWorkDir());
			result = result && exe.setup();
			executors.put(fsc, exe);
		}
		return result;
	}

	/**
	 * Start the simulation for all of the substructures for the current step.
	 * @return True if successful.
	 */
	public final boolean startSimulation() {
		boolean result = true;
		for (String mdl : executors.keySet()) {
			SubstructureExecutor exe = executors.get(mdl);
			result = result && exe.startSimulation();
		}
		return result;
	}
	/**
	 * Start execution of all of the substructures for the current step.
	 */
	public final void execute() {

		for (String mdl : executors.keySet()) {
			SubstructureExecutor exe = executors.get(mdl);
			exe.startStep(getStep(),
					MtxUtils.list2Array(displacementsMap.get(mdl)));
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
	public final double[] getDisplacements(final String address) {
		return executors.get(address).getDisplacements();
	}

	/**
	 * Get the force response for a substructure.
	 * @param address
	 *            Substructure id.
	 * @return Force data.
	 */
	public final double[] getForces(final String address) {
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
			try {
				// log.debug("Checking " + mdl);
				result = result && exe.stepIsDone();
			} catch (OutputFileException e) {
				log.error("Step command for " + mdl + " failed because", e);
			}
		}
		return result;
	}

	/**
	 * Abort the execution.
	 * @return True if the abort has completed.
	 */
	public final boolean finish() {
		boolean result = true;
		for (String mdl : executors.keySet()) {
			SubstructureExecutor exe = executors.get(mdl);
			exe.abort();
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
	 * <em>addEffectiveDofs</em> {@link SubstructureDao here}).
	 * @param address
	 *            Substructure id.
	 * @param displacements
	 *            Matrix of displacements. Size of the matrix is (# of control
	 *            nodes) x 6.
	 */
	public final void setDisplacements(final String address,
			final double[] displacements) {
		List<Double> data = double2List(displacements);
		log.debug("Set disp for \"" + address + "\" to " + data);
		displacementsMap.put(address, data);
	}

	/**
	 * @param step
	 *            the step to set
	 */
	public final void setStep(final int step) {
		this.step = step;
	}

	/**
	 * I'm alive function for debugging.
	 * @return Config Directory
	 */
	public final String ping() {
		log.debug("I'm here \"" + workDir + "\"");
		return configRootDir;
	}

	/**
	 * Reloads the logback.xml file for logging.
	 * @param logfile
	 *            Path to logback configuration file.
	 */
	public static void configureLog(final String logfile) {
		LoggerContext context = (LoggerContext) LoggerFactory
				.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g.
			// default
			// configuration. For multi-step configuration, omit calling
			// context.reset().
			context.reset();
			configurator.doConfigure(logfile);
		} catch (JoranException je) {
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}
	}

	/**
	 * Converts array into list.
	 * @param array
	 *            double array
	 * @return Double List
	 */
	private List<Double> double2List(final double[] array) {
		List<Double> result = new ArrayList<Double>();
		for (double n : array) {
			result.add(new Double(n));
		}
		return result;
	}
}
