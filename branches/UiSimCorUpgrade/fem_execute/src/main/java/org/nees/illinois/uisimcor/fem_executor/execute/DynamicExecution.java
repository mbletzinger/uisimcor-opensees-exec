package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.config.dao.SubstructureDao;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageT;
import org.nees.illinois.uisimcor.fem_executor.process.QMessageType;
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
public class DynamicExecution extends SubstructureExecutor {

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
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(DynamicExecution.class);

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
		super(progCfg, scfg, configDir, workDir, true);
	}

	/**
	 * Abort the execution.
	 */
	@Override
	public final void abort() {
		getFem().abort();
		dispListener.setQuit(true);
		forceListener.setQuit(true);
		dispListener.interrupt();
		forceListener.interrupt();
		if(dispReader == null) {
			return; // We were not running dynamic.
		}
		dispReader.setQuit(true);
		dispReader.interrupt();
		forceReader.setQuit(true);
		forceReader.interrupt();
	}

	@Override
	protected final void checkDisplacementResponse() {
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
		setRawDisp(rawDisp);
		statuses.setDisplacementsAreHere(true);
	}

	/**
	 * Check the forces queue and set the status.
	 */
	@Override
	protected final void checkForceResponse() {
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
		setRawForce(rawForce);
		statuses.setForcesAreHere(true);
	}

	/**
	 * Send the initial command to the FEM program.
	 */
	private void init() {
		String init = getScriptGen().generateInit();
		BlockingQueue<QMessageT<String>> stdinQ = getFem().getProcess()
				.getStdinQ();
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
		SubstructureDao scfg = getScfg();
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
		getFem().start();
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
		String stepCmnd = getScriptGen().generateStep(step, displacements);
		BlockingQueue<QMessageT<String>> stdinQ = getFem().getProcess()
				.getStdinQ();
		stdinQ.add(new QMessageT<String>(QMessageType.Command, stepCmnd));
		getStatuses().newStep();
	}
}
