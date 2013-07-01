package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.nees.illinois.uisimcor.fem_executor.response.ProcessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class adds a queue to STDIN.
 * @author Michael Bletzinger
 */
public class ProcessManagementWithStdin implements ProcessManagmentI {
	/**
	 * Sends commands to the process and receive responses from the process.
	 */
	private StdInExchange exchange;

	/**
	 * STDIN management for the process.
	 */
	private Thread exchangeThrd;

	/**
	 * The process.
	 */
	private final ProcessManagement pm;
	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory
			.getLogger(ProcessManagementWithStdin.class);

	/**
	 * @param cmd
	 *            Line command to execute.
	 * @param processName
	 *            Name of the command.
	 * @param waitInMillSecs
	 *            Argument list for the command.
	 */
	public ProcessManagementWithStdin(final String cmd,
			final String processName, final int waitInMillSecs) {
		pm = new ProcessManagement(cmd, processName, waitInMillSecs);
	}

	/**
	 * Cleanup after the command has finished executing.
	 */
	@Override
	public final void abort() {
		if (pm.getProcess() == null) { // Obviously we are not running.
			return;
		}
		exchange.setQuit(true);
		exchangeThrd.interrupt();
		pm.abort();
	}

	@Override
	public final void addArg(final String arg) {
		pm.addArg(arg);
	}

	@Override
	public final void addEnv(final String name, final String value) {
		pm.addEnv(name, value);
	}

	@Override
	public final List<String> getArgs() {
		return pm.getArgs();
	}

	@Override
	public final String getCmd() {
		return pm.getCmd();
	}

	@Override
	public final Map<String, String> getEnv() {
		return pm.getEnv();
	}

	@Override
	public final ProcessResponse getErrPr() {
		return pm.getErrPr();
	}

	/**
	 * @return the STDIN queue
	 */
	public final BlockingQueue<QMessageT<String>> getStdinQ() {
		return exchange.getStdinQ();
	}

	@Override
	public final ProcessResponse getStoutPr() {
		return pm.getStoutPr();
	}

	@Override
	public final String getWorkDir() {
		return pm.getWorkDir();
	}

	@Override
	public final boolean hasExited() {
		return pm.hasExited();
	}

	@Override
	public final void setWorkDir(final String workDir) {
		pm.setWorkDir(workDir);
	}

	/**
	 * Start the execution of the command.
	 * @throws IOException
	 *             if the command fails to start.
	 */
	public final void startExecute() throws IOException {
		pm.startExecute();
		log.debug("Starting STDIN exchange in " + pm.getWorkDir());
		exchange = new StdInExchange(pm.getWaitInMillSecs(), pm.getProcess()
				.getOutputStream());
		exchangeThrd = new Thread(exchange);
		exchangeThrd.start();
	}
}
