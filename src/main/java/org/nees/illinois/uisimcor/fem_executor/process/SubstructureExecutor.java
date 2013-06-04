package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.IOException;
import java.util.List;

import org.nees.illinois.uisimcor.fem_executor.config.ProgramConfig;
import org.nees.illinois.uisimcor.fem_executor.config.SubstructureConfig;
import org.nees.illinois.uisimcor.fem_executor.input.FemInputFile;
import org.nees.illinois.uisimcor.fem_executor.output.DataPad;
import org.nees.illinois.uisimcor.fem_executor.output.OutputFileParsingTask;
import org.nees.illinois.uisimcor.fem_executor.utils.MtxUtils;
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
	 * Current execution state.
	 */
	private ExecutionState current = ExecutionState.NotStarted;

	/**
	 * default wait.
	 */
	private final int defaultWait = 2000;

	/**
	 * Filename as first argument.
	 */
	private String filename;
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
	 * Flag indicating that output files need to be parsed after execution.
	 */
	private boolean processOutputFiles = true;
	/**
	 * Wait interval for checking thread for done. Default is 2 seconds
	 */
	private int waitInMillisecs = defaultWait;

	/**
	 * Working directory for FEM execution.
	 */
	private final String workDir;
	/**
	 * Output file {@link OutputFileParsingTask parser} for displacements.
	 */
	private final OutputFileParsingTask ofptDisp;
	/**
	 * Output file {@link OutputFileParsingTask parser} for forces.
	 */
	private final OutputFileParsingTask ofptForce;
	/**
	 * FEM execution input.
	 */
	private final FemInputFile input;
	/**
	 * Reformat the output for UI-SimCor.
	 */
	private final DataPad pad;

	/**
	 * @param progCfg
	 *            FEM program configuration parameters.
	 * @param input
	 *            FEM program input.
	 * @param scfg
	 *            Configuration for the substructure.
	 */
	public SubstructureExecutor(final ProgramConfig progCfg,
			final FemInputFile input, final SubstructureConfig scfg) {
		this.workDir = input.getWorkDir();
		this.command = progCfg;
		this.input = input;
		this.ofptDisp = new OutputFileParsingTask(PathUtils.append(workDir,
				"tmp_disp.out"));
		this.ofptForce = new OutputFileParsingTask(PathUtils.append(workDir,
				"tmp_forc.out"));
		this.pad = new DataPad(scfg);

	}

	/**
	 * @return the command
	 */
	public final ProgramConfig getCommand() {
		return command;
	}

	/**
	 * @return the current
	 */
	public final ExecutionState getCurrent() {
		return current;
	}

	/**
	 * @return the defaultWait
	 */
	public final int getDefaultWait() {
		return defaultWait;
	}

	/**
	 * @return the filename
	 */
	public final String getFilename() {
		return filename;
	}

	/**
	 * @return the pm
	 */
	public final ProcessManagement getPm() {
		return pm;
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
	 * @return the processOutputFiles
	 */
	public final boolean isProcessOutputFiles() {
		return processOutputFiles;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public final void setCommand(final ProgramConfig command) {
		this.command = command;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public final void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * @param processOutputFiles
	 *            the processOutputFiles to set
	 */
	public final void setProcessOutputFiles(final boolean processOutputFiles) {
		this.processOutputFiles = processOutputFiles;
	}

	/**
	 * @param waitInMillisecs
	 *            the waitInMillisecs to set
	 */
	public final void setWaitInMillisecs(final int waitInMillisecs) {
		this.waitInMillisecs = waitInMillisecs;
	}

	/**
	 * Create the {@link ProcessManagement ProcessManagement} instance and start
	 * it.
	 * @param step
	 *            Current step.
	 * @param displacements
	 *            Current displacement target.
	 * @return the {@link ProcessManagement ProcessManagement} instance
	 */
	public final ProcessManagement start(final int step,
			final double[] displacements) {
		input.generate(step, displacements); // this may need to go in its own
												// thread later.
		pm = new ProcessManagement(command.getExecutablePath(), input
				.getSubstructureCfg().getAddress(), waitInMillisecs, null, null);
		filename = input.getInputFileName();
		pm.addArg(filename);
		pm.setWorkDir(workDir);
		try {
			pm.startExecute();
			current = ExecutionState.Executing;
			return pm;
		} catch (IOException e) {
			log.debug(pm.getCmd() + " failed to start", e);
		}
		return null;
	}

	/**
	 * Execution Polling function. Use this repeatedly inside a polling loop to
	 * transition the process to new execution states.
	 * @return True if the command has completed.
	 */
	public final boolean isDone() {
		boolean result = false;
		if (current.equals(ExecutionState.Executing)) {
			boolean done = pm.isDone();
			if (done) {
				current = ExecutionState.ExecutionFinished;
			}
		}
		if (current.equals(ExecutionState.ExecutionFinished)) {
			if (processOutputFiles) {
				Thread thrd1 = new Thread(ofptDisp);
				Thread thrd2 = new Thread(ofptForce);
				log.debug("Starting parsing threads");
				thrd1.start();
				thrd2.start();
				current = ExecutionState.ProcessingOutputFiles;
			} else {
				current = ExecutionState.Finished;
			}
		}
		if (current.equals(ExecutionState.ProcessingOutputFiles)) {
			boolean done = ofptDisp.isDone() && ofptForce.isDone();
			if (done) {
				current = ExecutionState.Finished;
			}
		}

		if (current.equals(ExecutionState.Finished)
				|| current.equals(ExecutionState.NotStarted)) {
			result = true;
		}
		log.debug("Current state is " + current);
		return result;
	}

	/**
	 * Abort the execution.
	 * @return True if the abort has completed.
	 */
	public final boolean abort() {
		boolean result = false;
		if (current.equals(ExecutionState.Executing)) {
			pm.abort();
			result = true;
		}
		if (current.equals(ExecutionState.ProcessingOutputFiles)) {
			boolean done = ofptDisp.isDone() && ofptForce.isDone();
			if (done) {
				current = ExecutionState.Finished;
				result = true;
			}
		}
		if (current.equals(ExecutionState.Finished)) {
			result = true;
		}
		log.debug("Current state is " + current);
		return result;
	}

	/**
	 * Return the displacements data set.
	 * @return double matrix
	 */
	public final double[] getDisplacements() {
		List<Double> result = pad.filter(ofptDisp.getData());
		return MtxUtils.list2Array(result);
	}

	/**
	 * Return the forces data set.
	 * @return double matrix
	 */
	public final double[] getForces() {
		List<Double> result = pad.filter(ofptForce.getData());
		return MtxUtils.list2Array(result);
	}
}
