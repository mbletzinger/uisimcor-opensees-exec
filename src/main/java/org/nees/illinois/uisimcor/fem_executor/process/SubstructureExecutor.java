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
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpLinkDto;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpListener;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpParameters;
import org.nees.illinois.uisimcor.fem_executor.tcp.TcpReader;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to execute an FEM program to statically analyze a substructure at one
 * step.
 * @author Michael Bletzinger
 */
public class SubstructureExecutor {
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
	private int waitInMillisecs = defaultWait;

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
	private final int maxCnt = 20;

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
	}

	/**
	 * Execution Polling function. Use this repeatedly inside a polling loop to
	 * transition the process to new execution states.
	 * @return True if the command has completed.
	 * @throws OutputFileException
	 *             If force values are missing,
	 */
	public final boolean stepIsDone() throws OutputFileException {
		BlockingQueue<List<Double>> responses = dispReader.getDoublesQ();
		rawDisp = responses.poll();
		if (rawDisp == null) {
			if (debugCnt == maxCnt) {
				debugCnt = 0;
				log.debug("Still waiting for " + pm.getCmd());
			} else {
				debugCnt++;
			}
			return false;
		}
		log.debug("Raw Displacements " + MtxUtils.list2String(rawDisp));
		responses = forceReader.getDoublesQ();
		rawForce = responses.poll();
		if (rawForce == null) {
			throw new OutputFileException("Force values are missing");
		}
		log.debug("Raw Forces " + MtxUtils.list2String(rawForce));
		return true;
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
		return MtxUtils.list2Array(result);
	}

	/**
	 * Return the forces data set.
	 * @return double matrix
	 */
	public final double[] getForces() {
		List<Double> result = dformat.filter(rawForce);
		return MtxUtils.list2Array(result);
	}
}
