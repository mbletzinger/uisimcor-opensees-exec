package org.nees.illinois.uisimcor.fem_executor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.config.LoadSaveConfig;
import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;
import org.nees.illinois.uisimcor.fem_executor.execute.DynamicExecution;
import org.nees.illinois.uisimcor.fem_executor.execute.StaticExecution;
import org.nees.illinois.uisimcor.fem_executor.execute.SubstructureExecutorI;
import org.nees.illinois.uisimcor.fem_executor.input.WorkingDir;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
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
	 * Instance containing all of the configuration parameters.
	 */
	private FemExecutorConfig config;

	/**
	 * Path to the root directory of the configurations.
	 */
	private final String configRootDir;

	/**
	 * Map of displacement targets for each substructure.
	 */
	private final Map<String, List<Double>> displacementsMap = new HashMap<String, List<Double>>();

	/**
	 * Flag to indicate what type of execution for the FEM program.
	 */
	private boolean dynamic;
	/**
	 * Map of substructure Dynamic FEM executors.
	 */
	private final Map<String, SubstructureExecutorI> dynamicExecutors = new HashMap<String, SubstructureExecutorI>();

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
	 * Flag indicating that the simulation is running.
	 */
	private boolean running = false;

	/**
	 * Map of substructure Static Evaluation FEM executors.
	 */
	private final Map<String, SubstructureExecutorI> staticExecutors = new HashMap<String, SubstructureExecutorI>();

	/**
	 * Current step.
	 */
	private int step;
	/**
	 * Folder to store temporary files during model execution.
	 */
	private final String workDir;

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

	/**
	 * Start execution of all of the substructures for the current step.
	 */
	public final void execute() {
		log.debug("Execute was called");
		Map<String, SubstructureExecutorI> executors = pickExecutors();
		for (String mdl : executors.keySet()) {
			SubstructureExecutorI exe = executors.get(mdl);
			exe.startStep(getStep(),
					MtxUtils.list2Array(displacementsMap.get(mdl)));
		}
	}

	/**
	 * Abort the execution.
	 * @return True if the abort has completed.
	 */
	public final boolean finish() {
		log.debug("Finish was called");
		boolean result = true;
		Map<String, SubstructureExecutorI> executors = pickExecutors();
		for (String mdl : executors.keySet()) {
			SubstructureExecutorI exe = dynamicExecutors.get(mdl);
			exe.abort();
			exe = staticExecutors.get(mdl);
			exe.abort();
		}
		setRunning(false);
		return result;
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
		Map<String, SubstructureExecutorI> executors = pickExecutors();
		return executors.get(address).getDisplacements();
	}

	/**
	 * @return the displacementsMap
	 */
	public final Map<String, List<Double>> getDisplacementsMap() {
		return displacementsMap;
	}

	/**
	 * @return the executors
	 */
	public final Map<String, SubstructureExecutorI> getDynamicExecutors() {
		return dynamicExecutors;
	}

	/**
	 * Get the force response for a substructure.
	 * @param address
	 *            Substructure id.
	 * @return Force data.
	 */
	public final double[] getForces(final String address) {
		Map<String, SubstructureExecutorI> executors = pickExecutors();
		return executors.get(address).getForces();
	}

	/**
	 * @return the staticExecutors
	 */
	public final Map<String, SubstructureExecutorI> getStaticExecutors() {
		return staticExecutors;
	}

	/**
	 * @return the step
	 */
	public final int getStep() {
		return step;
	}

	/**
	 * @return the workDir
	 */
	public final String getWorkDir() {
		return workDir;
	}

	/**
	 * Check to see if all of the substructures have finished.
	 * @return True if everything is done.
	 */
	public final boolean isDone() {
		boolean result = true;
		Map<String, SubstructureExecutorI> executors = pickExecutors();
		for (String mdl : executors.keySet()) {
			SubstructureExecutorI exe = executors.get(mdl);
			result = result && exe.stepIsDone();
		}
		// final int matlabWait = 200;
		// try {
		// log.debug("Waiting to return");
		// Thread.sleep(matlabWait);
		// } catch (InterruptedException e) {
		// log.debug("HEY who woke me up?");
		// }
		return result;
	}

	/**
	 * @return the dynamic
	 */
	public final boolean isDynamic() {
		return dynamic;
	}

	/**
	 * @return the running
	 */
	public final boolean isRunning() {
		return running;
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
	 * Choose an executor map based on the dynamic flag.
	 * @return the map.
	 */
	private Map<String, SubstructureExecutorI> pickExecutors() {
		if (dynamic) {
			return dynamicExecutors;
		}
		return staticExecutors;
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
	 *            array of displacements. Size of the matrix is number of
	 *            effective DOFs x 1.
	 */
	public final void setDisplacements(final String address,
			final double[] displacements) {
		List<Double> data = double2List(displacements);
		log.debug("Set disp for \"" + address + "\" to " + data);
		displacementsMap.put(address, data);
	}

	/**
	 * @param dynamic
	 *            the dynamic to set
	 */
	public final void setDynamic(final boolean dynamic) {
		this.dynamic = dynamic;
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public final void setRunning(final boolean running) {
		this.running = running;
	}

	/**
	 * @param step
	 *            the step to set
	 */
	public final void setStep(final int step) {
		log.debug("Step was set to " + step);
		this.step = step;
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
			SubstructureExecutorI exe = new DynamicExecution(progCfg, scfg,
					configRootDir, wd.getWorkDir());
			result = result && exe.setup();
			dynamicExecutors.put(fsc, exe);
			exe = new StaticExecution(progCfg, scfg, configRootDir,
					wd.getWorkDir());
			result = result && exe.setup();
			staticExecutors.put(fsc, exe);
		}
		return result;
	}

	/**
	 * Check if all of the substructures are running.
	 * @return True if one of them has died.
	 */
	public final boolean simulationHasProblems() {
		boolean result = false;
		Map<String, SubstructureExecutorI> executors = pickExecutors();
		for (String mdl : executors.keySet()) {
			SubstructureExecutorI exe = executors.get(mdl);
			if (exe.iveGotProblems()) {
				log.error(mdl + " is no longer running");
				result = true;
			}
		}
		return result;
	}

	/**
	 * Start the simulation for all of the substructures for the current step.
	 * @return True if successful.
	 */
	public final boolean startSimulation() {
		boolean result = true;
		Map<String, SubstructureExecutorI> executors = pickExecutors();
		for (String mdl : executors.keySet()) {
			SubstructureExecutorI exe = executors.get(mdl);
			result = result && exe.startSimulation();
		}
		setRunning(true);
		return result;
	}
}
