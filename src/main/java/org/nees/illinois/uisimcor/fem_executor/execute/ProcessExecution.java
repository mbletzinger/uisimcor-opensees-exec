package org.nees.illinois.uisimcor.fem_executor.execute;

import java.io.IOException;

import org.nees.illinois.uisimcor.fem_executor.config.dao.ProgramDao;
import org.nees.illinois.uisimcor.fem_executor.process.ProcessManagement;
import org.nees.illinois.uisimcor.fem_executor.response.ResponseMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages and monitors the execution of an FEM program.
 * @author Michael Bletzinger
 */
public class ProcessExecution {
	/**
	 * Program configuration to run.
	 */
	private final ProgramDao command;

	/**
	 * Monitors STDERR stream for error messages.
	 */
	private ResponseMonitor errorMonitor;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(ProcessExecution.class);

	/**
	 * {@link ProcessManagement Wrapper} around command line executor.
	 */
	private final ProcessManagement process;

	/**
	 * Monitors STDOUT stream for step is done messages.
	 */
	private ResponseMonitor responseMonitor;

	/**
	 * @param command
	 *            Command to run in the process.
	 * @param workDir
	 *            Directory to run the process in.
	 * @param waitInMillisecs
	 *            Number of milliseconds to wait between response polling.
	 */
	public ProcessExecution(final ProgramDao command, final String workDir,
			final int waitInMillisecs) {
		this.command = command;
		this.process = new ProcessManagement(command.getExecutablePath(), command
				.getProgram().toString(), waitInMillisecs);
		process.setWorkDir(workDir);
	}

	/**
	 * Kill the process.
	 */
	public final void abort() {
		process.abort();
	}

	/**
	 * Determine if the process has sent any errors via SDTERR.
	 * @param statuses
	 *            Status object to be updated
	 */
	public final void checkForErrors(final FemStatus statuses) {
		if (statuses.isFemProcessHasDied()) {
			return;
		}
		String error = errorMonitor.getExtracted().poll();
		if (error != null) {
			statuses.setFemProcessHasErrors(true);
			log.error(error);
		}
	}

	/**
	 * Determine if the process has quit by querying the exit value.
	 * @param statuses
	 *            Status object to be updated
	 */
	public final void checkIfProcessIsAlive(final FemStatus statuses) {
		if (statuses.isFemProcessHasDied()) {
			return;
		}
		boolean result = process.hasExited();
		statuses.setFemProcessHasDied(result);
	}

	/**
	 * Determine if the process has responded via STDOUT that the current step
	 * is finished.
	 * @param statuses
	 *            Status object to be updated
	 */
	public final void checkStepCompletion(final FemStatus statuses) {
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
	 * @return the command
	 */
	public final ProgramDao getCommand() {
		return command;
	}

	/**
	 * @return the process
	 */
	public final ProcessManagement getProcess() {
		return process;
	}

	/**
	 * Start the process.
	 * @return True if successful.
	 */
	public final boolean start() {
		try {
			process.startExecute();
		} catch (IOException e) {
			log.error(process.getCmd() + " failed to start", e);
			return false;
		}
		responseMonitor = new ResponseMonitor();
		process.getStoutPr().addObserver(responseMonitor);
		errorMonitor = new ResponseMonitor();
		process.getErrPr().addObserver(errorMonitor);
		return true;
	}
}
