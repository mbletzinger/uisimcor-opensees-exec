package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.nees.illinois.uisimcor.fem_executor.config.ProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.input.OpenSeesSG;
import org.nees.illinois.uisimcor.fem_executor.input.ScriptGeneratorI;
import org.nees.illinois.uisimcor.fem_executor.output.DataFormatter;
import org.nees.illinois.uisimcor.fem_executor.process.QMessage.MessageType;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.nees.illinois.uisimcor.fem_executor.utils.PathUtils;
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
	private ProgramConfig command;

	/**
	 * default wait.
	 */
	private final int defaultWait = 2000;

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
	 * @param progCfg
	 *            FEM program configuration parameters.
	 * @param scfg
	 *            Configuration for the substructure.
	 * @param workDir
	 *            Directory to store temporary files.
	 * @param configDir
	 *            Directory containing templates and configuration files..
	 */
	public SubstructureExecutor(final ProgramConfig progCfg,
			final SubstructureConfig scfg, final String configDir,
			final String workDir) {
		this.workDir = workDir;
		this.command = progCfg;
		String dispF = PathUtils.append(workDir, "tmp_disp.out");
		String forceF = PathUtils.append(workDir, "tmp_forc.out");
		this.dformat = new DataFormatter(scfg);
		this.scriptGen = new OpenSeesSG(configDir, scfg);
		this.pm = new ProcessManagement(command.getExecutablePath(), command
				.getProgram().toString(), waitInMillisecs, dispF, forceF);

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
	public final void setCommand(final ProgramConfig command) {
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
	 * 	 */
	public final void startSimulation() {
		pm.setWorkDir(workDir);
		try {
			pm.startExecute();
			String init = scriptGen.generateInit();
			pm.getCommandQ().add(new QMessage(MessageType.Setup, init));
		} catch (IOException e) {
			log.debug(pm.getCmd() + " failed to start", e);
		}
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
		pm.getCommandQ().add(new QMessage(MessageType.Command, stepCmnd));
	}

	/**
	 * Execution Polling function. Use this repeatedly inside a polling loop to
	 * transition the process to new execution states.
	 * @return True if the command has completed.
	 * @throws OutputFileException If force values are missing,
	 */
	public final boolean stepIsDone() throws OutputFileException {
		BlockingQueue<QMessage> responses = pm.getResponseQ();
		QMessage rspStr = responses.poll();
		if (rspStr == null) {
			return false;
		}
		if(rspStr.getType().equals(MessageType.Exit)) {
			abort();
		}
		rawDisp = dformat.tokenString2Double(rspStr.getContent());
		rspStr = responses.poll();
		if (rspStr == null) {
			throw new OutputFileException("Force values are missing");
		}
		rawForce = dformat.tokenString2Double(rspStr.getContent());
		return true;
	}

	/**
	 * Abort the execution.
	 */
	public final void abort() {
			pm.abort();
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
