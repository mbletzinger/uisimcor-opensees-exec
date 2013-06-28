package org.nees.illinois.uisimcor.fem_executor.execute;

import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.input.ScriptGeneratorI;
import org.nees.illinois.uisimcor.fem_executor.output.DataFormatter;
import org.nees.illinois.uisimcor.fem_executor.utils.LogMessageWithCounter;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Class to get one step executed from an FEM program.
 * @author Michael Bletzinger
 */
public abstract class SubstructureExecutor {

	/**
	 * Reformat the output for UI-SimCor.
	 */
	private final DataFormatter dformat;

	/**
	 * FEM Process execution.
	 */
	private final ProcessExecution fem;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(SubstructureExecutor.class);
	/**
	 * Log with counter to cut down on repetitive messages.
	 */
	private final LogMessageWithCounter logC = new LogMessageWithCounter(10,
			log, Level.INFO);
	/**
	 * Results of a step command.
	 */
	private List<Double> rawDisp;
	/**
	 * Results of a step command.
	 */
	private List<Double> rawForce;
	/**
	 * Substructure configuration.
	 */
	private final SubstructureDao scfg;
	/**
	 * FEM execution script generator.
	 */
	private final ScriptGeneratorI scriptGen;
	/**
	 * Statuses for the substructure execution.
	 */
	private final FemStatus statuses = new FemStatus();
	/**
	 * Wait interval for checking thread for done. Default is 2 seconds
	 */
	private int waitInMillisecs;
	/**
	 * Working directory for FEM execution.
	 */
	private final String workDir;
	/**
	 * @param progCfg
	 *            FEM program configuration parameters.
	 * @param scfg
	 *            Configuration for the substructure.
	 * @param configDir
	 *            Directory containing templates and configuration files..
	 * @param workDir
	 *            Directory to store temporary files.
	 */
	public SubstructureExecutor(final ProgramDao progCfg,
			final SubstructureDao scfg, final String configDir,
			final String workDir) {
		final int halfSecond = 500;
		this.waitInMillisecs = halfSecond;
		this.workDir = workDir;
		this.dformat = new DataFormatter(scfg);
		this.scfg = scfg;
		this.fem = new ProcessExecution(progCfg, workDir, waitInMillisecs);
		this.scriptGen = new OpenSeesSG(configDir, scfg,
				progCfg.getTemplateDao());
	}

	/**
	 * Abort the execution.
	 */
	public abstract void abort();

	/**
	 * Check the displacements queue and set the status.
	 */
	protected abstract void checkDisplacementResponse();

	/**
	 * Check the forces queue and set the status.
	 */
	protected abstract void checkForceResponse();

	/**
	 * @return the data formatter.
	 */
	public final DataFormatter getDformat() {
		return dformat;
	}

	/**
	 * Return the displacements data set.
	 * @return double matrix
	 */
	public final double[] getDisplacements() {
		List<Double> result = dformat.filter(rawDisp);
		log.debug("Filtered Displacements " + MtxUtils.list2String(result));
		return MtxUtils.list2Array(result);
	}

	/**
	 * @return the fem
	 */
	public final ProcessExecution getFem() {
		return fem;
	}

	/**
	 * Return the forces data set.
	 * @return double matrix
	 */
	public final double[] getForces() {
		List<Double> result = dformat.filter(rawForce);
		log.debug("Filtered Forces " + MtxUtils.list2String(result));
		return MtxUtils.list2Array(result);
	}

	/**
	 * @return the rawDisp
	 */
	public final List<Double> getRawDisp() {
		return rawDisp;
	}

	/**
	 * @return the rawForce
	 */
	public final List<Double> getRawForce() {
		return rawForce;
	}

	/**
	 * @return the scfg
	 */
	public final SubstructureDao getScfg() {
		return scfg;
	}

	/**
	 * @return the scriptGen
	 */
	public final ScriptGeneratorI getScriptGen() {
		return scriptGen;
	}

	/**
	 * @return the statuses
	 */
	public final FemStatus getStatuses() {
		return statuses;
	}

	/**
	 * @return the waitInMillisecs
	 */
	public final int getWaitInMillisecs() {
		return waitInMillisecs;
	}

	/**
	 * @return the workDir
	 */
	public final String getWorkDir() {
		return workDir;
	}

	/**
	 * Determines if the execution is still proceeding correctly.
	 * @return True if the execution is broken in some way.
	 */
	public abstract boolean iveGotProblems();

	/**
	 * @param rawDisp
	 *            the rawDisp to set
	 */
	public final void setRawDisp(final List<Double> rawDisp) {
		this.rawDisp = rawDisp;
	}

	/**
	 * @param rawForce
	 *            the rawForce to set
	 */
	public final void setRawForce(final List<Double> rawForce) {
		this.rawForce = rawForce;
	}

	/**
	 * Setup links for the FEM program.
	 * @return True if successful.
	 */
	public abstract boolean setup();

	/**
	 * @param waitInMillisecs
	 *            the waitInMillisecs to set
	 */
	public final void setWaitInMillisecs(final int waitInMillisecs) {
		this.waitInMillisecs = waitInMillisecs;
	}

	/**
	 * Start the FEM program and listen for socket connection requests.
	 * @return True if simulation has started.
	 */
	public abstract boolean startSimulation();

	/**
	 * Send the next step command to the FEM program.
	 * @param step
	 *            Current step.
	 * @param displacements
	 *            Current displacement target.
	 */
	public abstract void startStep(final int step, final double[] displacements);

	/**
	 * Execution Polling function. Use this repeatedly inside a polling loop to
	 * transition the process to new execution states.
	 * @return True if the command has completed.
	 */
	public final boolean stepIsDone() {
		checkDisplacementResponse();
		checkForceResponse();
		fem.checkIfProcessIsAlive(statuses);
		fem.checkStepCompletion(statuses);
		fem.checkForErrors(statuses);
		if (statuses.isChanged()) {
			log.info(scfg.getAddress() + " Is " + statuses.getStatus());
			logC.reset();
		}
		logC.log(scfg.getAddress() + " Is " + statuses.getStatus());
		return statuses.responsesHaveArrived();
	}
}
