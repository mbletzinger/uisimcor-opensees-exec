package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nees.illinois.uisimcor.fem_executor.archiving.DataArchive;
import org.nees.illinois.uisimcor.fem_executor.archiving.HeaderArchive;
import org.nees.illinois.uisimcor.fem_executor.archiving.TextArchive;
import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.input.ScriptGeneratorI;
import org.nees.illinois.uisimcor.fem_executor.process.ProcessManagementWithStdin;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageT;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageType;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpLinkDto;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpListener;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpParameters;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpReader;
import org.nees.illinois.uisimcor.fem_executor.utils.LogMessageWithCounter;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
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
	 * Response values.
	 */
	private final ResponseValues responseVals;

	/**
	 * FEM execution management.
	 */
	private final ProcessExecution exec;
	/**
	 * FEM execution script generator.
	 */
	private final ScriptGeneratorI scriptGen;
	/**
	 * Listener for the displacements socket.
	 */
	private TcpListener dispListener;

	/**
	 * Reader for the disp socket.
	 */
	private TcpReader dispReader;

	/**
	 * Listener for the reaction socket.
	 */
	private TcpListener forceListener;

	/**
	 * Read forces from reaction link.
	 */
	private TcpReader forceReader;

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
		this.exec = new ProcessExecution(progCfg, wd.getWorkDir(), quarterSecond, true);
		this.scriptGen = new OpenSeesSG(configDir, scfg,
				progCfg.getTemplateDao());
		this.responseVals = new ResponseValues(scfg);
		this.scfg = scfg;
		SubstructureDir logDir = new SubstructureDir(workDir, scfg, "logDir");
		logDir.createSubstructDir();
		String darchPath = PathUtils.append(logDir.getSubstructDir(), "Displacements");
		String farchPath = PathUtils.append(logDir.getSubstructDir(), "Forces");
		HeaderArchive hd = new HeaderArchive(darchPath, scfg, false);
		hd.write();
		hd = new HeaderArchive(farchPath, scfg, true);
		hd.write();
		this.darch = new DataArchive(darchPath);
		this.farch = new DataArchive(farchPath);
		String ipath = PathUtils.append(logDir.getSubstructDir(), "Inputs");
		this.iarch = new TextArchive(new File(ipath));
	}

	/**
	 * Abort the execution.
	 */
	@Override
	public final void abort() {
		exec.abort();
		dispListener.setQuit(true);
		forceListener.setQuit(true);
		dispListener.interrupt();
		forceListener.interrupt();
		if (dispReader == null) {
			return; // We were not running dynamic.
		}
		dispReader.setQuit(true);
		dispReader.interrupt();
		forceReader.setQuit(true);
		forceReader.interrupt();
	}

	/**
	 * Check the displacements queue and set the status.
	 */
	private void checkDisplacementResponse() {
		FemStatus statuses = getStatuses();
		if (statuses.isDisplacementsAreHere()) {
			return;
		}
		BlockingQueue<List<Double>> responses = dispReader.getDoublesQ();
		List<Double> rawDisp = responses.poll();
		if (rawDisp == null) {
			return;
		}

		log.debug("Raw Displacements " + MtxUtils.list2String(rawDisp));
		responseVals.setRawDisp(rawDisp);
		statuses.setDisplacementsAreHere(true);
	}

	/**
	 * Check the forces queue and set the status.
	 */
	private void checkForceResponse() {
		FemStatus statuses = getStatuses();
		if (statuses.isForcesAreHere()) {
			return;
		}
		BlockingQueue<List<Double>> responses = forceReader.getDoublesQ();
		List<Double> rawForce = responses.poll();
		if (rawForce == null) {
			return;
		}
		log.debug("Raw Forces " + MtxUtils.list2String(rawForce));
		responseVals.setRawForce(rawForce);
		statuses.setForcesAreHere(true);
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
		final int fiveSeconds = 5000;
		try {
			dispListener = new TcpListener(new TcpParameters(null, 0,
					scfg.getDispPort(), fiveSeconds));
		} catch (IOException e) {
			log.error("Bind to displacements port " + scfg.getDispPort()
					+ " failed because ", e);
			return false;
		}
		dispListener.start();
		try {
			forceListener = new TcpListener(new TcpParameters(null, 0,
					scfg.getForcePort(), fiveSeconds));
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
	@Override
	public final boolean startSimulation() {
		exec.start();
		init();
		final int tenSeconds = 10;
		TcpLinkDto link = null;
		try {
			link = dispListener.getConnections().poll(tenSeconds,
					TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason");
		}
		if (link == null) {
			log.error("No link available on "
					+ dispListener.getParams().getLocalPort());
			return false;
		}
		try {
			dispReader = new TcpReader(link);
		} catch (IOException e1) {
			log.error("No link available on "
					+ dispListener.getParams().getLocalPort() + " because ", e1);
			return false;
		}
		dispReader.start();
		// dispListener.setQuit(true);
		// dispListener.interrupt();
		try {
			link = forceListener.getConnections().poll(tenSeconds,
					TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.debug("Interrupted for some reason");
		}
		if (link == null) {
			log.error("No link available on "
					+ forceListener.getParams().getLocalPort());
			return false;
		}
		try {
			forceReader = new TcpReader(link);
		} catch (IOException e) {
			log.error("No link available on "
					+ forceListener.getParams().getLocalPort() + " because ", e);
			return false;
		}
		forceReader.start();
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
		currentStep = step;
		String stepCmnd = scriptGen.generateStep(step, displacements);
		iarch.write(stepCmnd);
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
		checkDisplacementResponse();
		checkForceResponse();
		exec.checkIfProcessIsAlive(statuses);
		exec.checkStepCompletion(statuses);
		exec.checkForErrors(statuses);
		if (statuses.isChanged()) {
			log.info(scfg.getAddress() + " Is " + statuses.getStatus());
			logC.reset();
		}
		logC.log(scfg.getAddress() + " Is " + statuses.getStatus());
		boolean result = statuses.responsesHaveArrived();
		if(result && (statuses.isFemProcessHasDied() == false)) {
			darch.write(currentStep, getDisplacements());
			farch.write(currentStep, getForces());
		}
		return result;
	}

	@Override
	public final double[] getDisplacements() {
		return responseVals.getDisplacements();
	}

	@Override
	public final double[] getForces() {
		return responseVals.getForces();
	}

	@Override
	public final FemStatus getStatuses() {
		return exec.getStatuses();
	}

}
