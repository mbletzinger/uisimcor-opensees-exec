package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.input.ScriptGeneratorI;
import org.nees.illinois.uisimcor.fem_executor.output.DataFormatter;
import org.nees.illinois.uisimcor.fem_executor.response.ResponseMonitor;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpLinkDto;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpListener;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpParameters;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpReader;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to execute an FEM program to statically analyze a substructure at one
 * step.
 * @author Michael Bletzinger
 */
public class SubstructureExecutor {

	/**
	 * @return the statuses
	 */
	public final FemStatus getStatuses() {
		return statuses;
	}

	/**
	 * Statuses for the substructure execution.
	 */
	private final FemStatus statuses = new FemStatus();
	/**
	 * Program configuration to run.
	 */
	private ProgramDao command;

	/**
	 * default wait.
	 */
	private final int defaultWait = 20000;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(SubstructureExecutor.class);
	/**
	 * {@link ProcessManagement Wrapper} around command line executor.
	 */
	private ProcessManagement pm;
	/**
	 * Wait interval for checking thread for done. Default is 2 seconds
	 */
	private int waitInMillisecs = defaultWait / 10;

	/**
	 * Working directory for FEM execution.
	 */
	private final String workDir;
	/**
	 * FEM execution script generator.
	 */
	private final ScriptGeneratorI scriptGen;
	/**
	 * Reformat the output for UI-SimCor.
	 */
	private final DataFormatter dformat;
	/**
	 * Results of a step command.
	 */
	private List<Double> rawDisp;
	/**
	 * Results of a step command.
	 */
	private List<Double> rawForce;

	/**
	 * Counter for debug messages.
	 */
	private int debugCnt = 0;
	/**
	 * Number of counts until reset.
	 */
	private final int maxCnt = 5;

	/**
	 * Listener for the displacements socket.
	 */
	private TcpListener dispListener;

	/**
	 * Listener for the reaction socket.
	 */
	private TcpListener forceListener;

	/**
	 * Reader for the disp socket.
	 */
	private TcpReader dispReader;

	/**
	 * Reader for the reaction socket.
	 */
	/**
	 * Substructure configuration.
	 */
	private final SubstructureDao scfg;
	/**
	 * Read forces from reaction link.
	 */
	private TcpReader forceReader;
	/**
	 * Monitors STDOUT stream for step is done messages.
	 */
	private ResponseMonitor responseMonitor;
	/**
	 * Monitors STDERR stream for error messages.
	 */
	private ResponseMonitor errorMonitor;

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
		this.workDir = workDir;
		this.command = progCfg;
		this.dformat = new DataFormatter(scfg);
		this.scriptGen = new OpenSeesSG(configDir, scfg,
				progCfg.getTemplateDao());
		this.pm = new ProcessManagement(command.getExecutablePath(), command
				.getProgram().toString(), waitInMillisecs);
		this.scfg = scfg;
	}

	/**
	 * @return the workDir
	 */
	public final String getWorkDir() {
		return workDir;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public final void setCommand(final ProgramDao command) {
		this.command = command;
		pm.setCmd(command.getExecutablePath());
	}

	/**
	 * @param waitInMillisecs
	 *            the waitInMillisecs to set
	 */
	public final void setWaitInMillisecs(final int waitInMillisecs) {
		this.waitInMillisecs = waitInMillisecs;
	}

	/**
	 * Setup links for the FEM program.
	 * @return True if succes.
	 */
	public final boolean setup() {
		try {
			dispListener = new TcpListener(new TcpParameters(null, 0,
					scfg.getDispPort(), defaultWait));
		} catch (IOException e) {
			log.error("Bind to displacements port " + scfg.getDispPort()
					+ " failed because ", e);
			return false;
		}
		dispListener.start();
		try {
			forceListener = new TcpListener(new TcpParameters(null, 0,
					scfg.getForcePort(), defaultWait));
		} catch (IOException e) {
			log.error("Bind to displacements port " + scfg.getDispPort()
					+ " failed because ", e);
			return false;
		}
		forceListener.start();
		return true;
	}

	/**
	 * Start the FEM program and listen for socket connection requests.
	 * @return True if simulation has started.
	 */
	public final boolean startSimulation() {
		pm.setWorkDir(workDir);
		try {
			pm.startExecute();
			String init = scriptGen.generateInit();
			pm.getStdinQ().add(new QMessageT<String>(QMessageType.Setup, init));
		} catch (IOException e) {
			log.error(pm.getCmd() + " failed to start", e);
			return false;
		}
		TcpLinkDto link = null;
		try {
			link = dispListener.getConnections().poll(waitInMillisecs,
					TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason");
		}
		if (link == null) {
			log.error("No link to " + pm.getCmd());
			return false;
		}
		try {
			dispReader = new TcpReader(link);
		} catch (IOException e1) {
			log.error("No displacement link to " + pm.getCmd() + " because ",
					e1);
			return false;
		}
		dispReader.start();
//		dispListener.setQuit(true);
//		dispListener.interrupt();
		try {
			link = forceListener.getConnections().poll(waitInMillisecs,
					TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason");
		}
		if (link == null) {
			log.error("No link to " + pm.getCmd());
			return false;
		}
		try {
			forceReader = new TcpReader(link);
		} catch (IOException e) {
			log.error("No force link to " + pm.getCmd() + " because ", e);
			return false;
		}
		forceReader.start();
//		forceListener.setQuit(true);
//		forceListener.interrupt();
		responseMonitor = new ResponseMonitor();
		pm.getStoutPr().addObserver(responseMonitor);
		errorMonitor = new ResponseMonitor();
		pm.getErrPr().addObserver(errorMonitor);
		return true;
	}

	/**
	 * Send the next step command to the FEM program.
	 * @param step
	 *            Current step.
	 * @param displacements
	 *            Current displacement target.
	 */
	public final void startStep(final int step, final double[] displacements) {
		String stepCmnd = scriptGen.generateStep(step, displacements);
		pm.getStdinQ().add(
				new QMessageT<String>(QMessageType.Command, stepCmnd));
		debugCnt = 0;
		statuses.newStep();
	}

	/**
	 * Execution Polling function. Use this repeatedly inside a polling loop to
	 * transition the process to new execution states.
	 * @return True if the command has completed.
	 */
	public final boolean stepIsDone() {
		checkDisplacementResponse();
		checkForceResponse();
		checkIfProcessIsAlive();
		checkCurrentStep();
		checkForErrors();
		if (statuses.isChanged()) {
			log.info(scfg.getAddress() + " Is " + statuses.getStatus());
			debugCnt = 0;
		}
		if (debugCnt == maxCnt) {
			debugCnt = 0;
			log.info(scfg.getAddress() + " Is " + statuses.getStatus());
		} else {
			debugCnt++;
		}
		return statuses.responsesHaveArrived();
	}

	/**
	 * Abort the execution.
	 */
	public final void abort() {
		pm.abort();
		dispListener.setQuit(true);
		forceListener.setQuit(true);
		dispReader.setQuit(true);
		forceReader.setQuit(true);
		dispListener.interrupt();
		forceListener.interrupt();
		dispReader.interrupt();
		forceReader.interrupt();
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
	 * Return the forces data set.
	 * @return double matrix
	 */
	public final double[] getForces() {
		List<Double> result = dformat.filter(rawForce);
		log.debug("Filtered Forces " + MtxUtils.list2String(result));
		return MtxUtils.list2Array(result);
	}

	/**
	 * Check the displacements queue and set the status.
	 */
	private void checkDisplacementResponse() {
		if (statuses.isDisplacementsAreHere()) {
			return;
		}
		BlockingQueue<List<Double>> responses = dispReader.getDoublesQ();
		rawDisp = responses.poll();
		if (rawDisp == null) {
			return;
		}
		log.debug("Raw Displacements " + MtxUtils.list2String(rawDisp));
		statuses.setDisplacementsAreHere(true);
	}

	/**
	 * Check the forces queue and set the status.
	 */
	private void checkForceResponse() {
		if (statuses.isForcesAreHere()) {
			return;
		}
		BlockingQueue<List<Double>> responses = forceReader.getDoublesQ();
		rawForce = responses.poll();
		if (rawForce == null) {
			return;
		}
		log.debug("Raw Forces " + MtxUtils.list2String(rawForce));
		statuses.setForcesAreHere(true);
	}

	/**
	 * Determine if the process has quit by querying the exit value.
	 */
	private void checkIfProcessIsAlive() {
		if (statuses.isFemProcessHasDied()) {
			return;
		}
		boolean result = pm.hasExited();
		statuses.setFemProcessHasDied(result);
	}

	/**
	 * Determine if the process has responded via STDOUT that the current step
	 * is finished.
	 */
	private void checkCurrentStep() {
		if (statuses.isCurrentStepHasExecuted()) {
			return;
		}
		String step = responseMonitor.getExtracted().poll();
		if (step == null) {
			return;
		}
		statuses.setCurrentStepHasExecuted(true);
		statuses.setLastExecutedStep(step);
	}

	/**
	 * Determine if the process has sent any errors via SDTERR.
	 */
	private void checkForErrors() {
		if(statuses.isFemProcessHasDied()) {
			return;
		}
		String error = errorMonitor.getExtracted().poll();
		if (error != null) {
			statuses.setFemProcessHasErrors(true);
			log.error(error);
		}
	}
}
