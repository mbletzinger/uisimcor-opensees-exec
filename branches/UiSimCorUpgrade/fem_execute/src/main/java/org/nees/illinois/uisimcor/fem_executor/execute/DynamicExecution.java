package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.File;
import java.util.concurrent.BlockingQueue;

import org.nees.illinois.uisimcor.fem_executor.archiving.DataArchive;
import org.nees.illinois.uisimcor.fem_executor.archiving.HeaderArchive;
import org.nees.illinois.uisimcor.fem_executor.archiving.TextArchive;
import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.input.ScriptGeneratorI;
import org.nees.illinois.uisimcor.fem_executor.output.RecordCollector;
import org.nees.illinois.uisimcor.fem_executor.process.ProcessManagementWithStdin;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageT;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageType;
import org.nees.illinois.uisimcor.fem_executor.utils.LogMessageWithCounter;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Class to execute an FEM program to statically analyze a substructure at one
 * step.
 * @author Michael Bletzinger
 */
public class DynamicExecution implements SubstructureExecutorI {

	/**
	 * FEM execution management.
	 */
	private final ProcessExecution exec;
	/**
	 * FEM execution script generator.
	 */
	private final ScriptGeneratorI scriptGen;

	/**
	 * Configuration of the substructure.
	 */
	private final SubstructureDao scfg;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(DynamicExecution.class);
	/**
	 * Logger filtered by a counter.
	 */
	private final LogMessageWithCounter logC = new LogMessageWithCounter(20,
			log, Level.INFO);
	/**
	 * Forces archive.
	 */
	private final DataArchive farch;
	/**
	 * Displacements archive.
	 */
	private final DataArchive darch;
	/**
	 * Input string archive.
	 */
	private final TextArchive iarch;
	/**
	 * Current step.
	 */
	private int currentStep;

	/**
	 * Collects the responses for an iteration step.
	 */
	private final RecordCollector responses;

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
	public DynamicExecution(final ProgramDao progCfg,
			final SubstructureDao scfg, final String configDir,
			final String workDir) {
		final int quarterSecond = 250;
		WorkingDir wd = new WorkingDir(workDir, scfg, configDir);
		wd.createWorkDir();
		this.exec = new ProcessExecution(progCfg, wd.getWorkDir(),
				quarterSecond, true);
		this.scriptGen = new OpenSeesSG(configDir, scfg,
				progCfg.getTemplateDao());
		this.scfg = scfg;
		SubstructureDir logDir = new SubstructureDir(workDir, scfg, "logDir");
		logDir.createSubstructDir();
		String darchPath = PathUtils.append(logDir.getSubstructDir(),
				"Displacements");
		String farchPath = PathUtils.append(logDir.getSubstructDir(), "Forces");
		HeaderArchive hd = new HeaderArchive(darchPath, scfg, false);
		hd.write();
		hd = new HeaderArchive(farchPath, scfg, true);
		hd.write();
		this.darch = new DataArchive(darchPath);
		this.farch = new DataArchive(farchPath);
		String ipath = PathUtils.append(logDir.getSubstructDir(), "Inputs");
		this.iarch = new TextArchive(new File(ipath));
		this.responses = new RecordCollector(scfg, progCfg);
	}

	/**
	 * Abort the execution.
	 */
	@Override
	public final void abort() {
		exec.abort();
		responses.abort();
	}

	/**
	 * Check the displacements queue and set the status.
	 */
	private void checkResponses() {
		responses.checkResponses(getStatuses());
	}

	/**
	 * Send the initial command to the FEM program.
	 */
	private void init() {
		String init = scriptGen.generateInit();
		iarch.write(init);
		ProcessManagementWithStdin execWStdin = (ProcessManagementWithStdin) exec
				.getProcess();
		BlockingQueue<QMessageT<String>> stdinQ = execWStdin.getStdinQ();
		stdinQ.add(new QMessageT<String>(QMessageType.Command, init));
		getStatuses().newStep();
	}

	@Override
	public final boolean iveGotProblems() {
		FemStatus statuses = getStatuses();
		return statuses.isFemProcessHasErrors();
	}

	@Override
	public final boolean setup() {
		return responses.setup(scfg);
	}

	/**
	 * Start the FEM program and listen for socket connection requests.
	 * @return True if simulation has started.
	 */
	@Override
	public final boolean startSimulation() {
		exec.start();
		init();
		return responses.connect();
	}

	/**
	 * Send the next step command to the FEM program.
	 * @param step
	 *            Current step.
	 * @param displacements
	 *            Current displacement target.
	 */
	public final void startStep(final int step, final double[] displacements) {
		currentStep = step;
		String stepCmnd = scriptGen.generateStep(step, displacements);
		iarch.write(stepCmnd);
		responses.start();
		ProcessManagementWithStdin execWStdin = (ProcessManagementWithStdin) exec
				.getProcess();
		BlockingQueue<QMessageT<String>> stdinQ = execWStdin.getStdinQ();
		stdinQ.add(new QMessageT<String>(QMessageType.Command, stepCmnd));
		getStatuses().newStep();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.nees.illinois.uisimcor.fem_executor.execute.SubstructurerExecutorI
	 * #stepIsDone()
	 */
	@Override
	public final boolean stepIsDone() {
		FemStatus statuses = exec.getStatuses();
		checkResponses();
		exec.checkIfProcessIsAlive(statuses);
		exec.checkStepCompletion(statuses);
		exec.checkForErrors(statuses);
		if (statuses.isChanged()) {
			log.info(scfg.getAddress() + " Is " + statuses.getStatus());
			logC.reset();
		}
		logC.log(scfg.getAddress() + " Is " + statuses.getStatus());
		boolean result = statuses.responsesHaveArrived();
		if (result && (statuses.isFemProcessHasDied() == false)) {
			responses.finish();
			darch.write(currentStep, responses.getResponseVals()
					.getDisplacements());
			farch.write(currentStep, responses.getResponseVals().getForces());
		}
		return result;
	}

	@Override
	public final double[] getDisplacements() {
		return responses.getResponseVals().getDisplacements();
	}

	@Override
	public final double[] getForces() {
		return responses.getResponseVals().getForces();
	}

	@Override
	public final FemStatus getStatuses() {
		return exec.getStatuses();
	}

}
