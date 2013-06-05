package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Class to wrap some management threads around the {@link ProcessBuilder
 * ProcessBuilder} and {@link Process Process}.
 * @author Michael Bletzinger
 */
public class ProcessManagement {
	/**
	 * Argument list for the command.
	 */
	private final List<String> args = new ArrayList<String>();

	/**
	 * Line command to execute.
	 */
	private String cmd;

	/**
	 * Command queue.
	 */
	private final BlockingQueue<QMessage> commandQ = new LinkedBlockingQueue<QMessage>();

	/**
	 * File which gets the response displacements.
	 */
	private final String dispResponseFile;

	/**
	 * Environment variables for the command.
	 */
	private final Map<String, String> env = new HashMap<String, String>();

	/**
	 * Listener for error messages.
	 */
	private ProcessResponse errPr;

	/**
	 * Error reading thread.
	 */
	private Thread errThrd;

	/**
	 * Sends commands to the process and receive responses from the process.
	 */
	private StdInExchange exchange;

	/**
	 * STDIN management for the process.
	 */
	private Thread exchangeThrd;

	/**
	 * The value returned by the executed command.
	 */
	private int exitValue;

	/**
	 * File which gets the force displacements.
	 */
	private final String forceResponseFile;

	/**
	 * Interval for the {@link ProcessResponse ProcessResponse} threads to wait
	 * before reading content.
	 */
	private final int listenerWaitInterval = 100;

	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory.getLogger(ProcessManagement.class);
	/**
	 * {@link Process Process} associated with the executing command.
	 */
	private Process process = null;
	/**
	 * Name used for log messages.
	 */
	private final String processName;
	/**
	 * Response queue.
	 */
	private final BlockingQueue<QMessage> responseQ = new LinkedBlockingQueue<QMessage>();
	/**
	 * Listener for output.
	 */
	private ProcessResponse stoutPr;
	/**
	 * Output reading thread.
	 */
	private Thread stoutThrd;
	/**
	 * Interval to wait between thread checks.
	 */
	private final int waitInMillSecs;
	/**
	 * Working directory for the execution.
	 */
	private String workDir = null;
	/**
	 * @param cmd
	 *            Line command to execute.
	 * @param processName
	 *            Name of the command.
	 * @param waitInMillSecs
	 *            Argument list for the command.
	 * @param dispResponseFile
	 *            File which gets the response displacements.
	 * @param forceResponseFile
	 *            File which gets the force displacements.
	 */
	public ProcessManagement(final String cmd, final String processName,
			final int waitInMillSecs, final String dispResponseFile, final String forceResponseFile) {
		this.cmd = checkWindowsCommand(cmd);
		this.processName = processName;
		this.waitInMillSecs = waitInMillSecs;
		this.dispResponseFile = dispResponseFile;
		this.forceResponseFile = forceResponseFile;
	}
	/**
	 * Stop execution immediately.
	 */
	public final void abort() {
		log.debug("Aborting");
		process.destroy();
		log.debug("Ending threads");
		errPr.setQuit(true);
		errThrd.interrupt();
		stoutPr.setQuit(true);
		stoutThrd.interrupt();
		exchange.setQuit(true);
		exchangeThrd.interrupt();
	}
	/**
	 * Add an argument to the command.
	 * @param arg
	 *            Argument string.
	 */
	public final void addArg(final String arg) {
		args.add(arg);
	}

	/**
	 * Add a variable to the process environment.
	 * @param name
	 *            Name of the variable.
	 * @param value
	 *            Value string.
	 */
	public final void addEnv(final String name, final String value) {
		env.put(name, value);
	}

	/**
	 * Assemble the command and its arguments.
	 * @return The full command string.
	 */
	private String[] assemble() {
		String[] result = new String[args.size() + 1];
		result[0] = cmd;
		int i = 1;
		for (String a : args) {
			result[i] = a;
			i++;
		}
		return result;
	}

	/**
	 * Starts the command and does not return until the command has finished
	 * executing.
	 */
	public final void blockingExecute() {
		try {
			startExecute();
		} catch (IOException e) {
			log.error(cmd + " failed to start because", e);
			return;
		}

		boolean done = false;
		while (done == false) {
			try {
				process.waitFor();
				done = true;
				finish();
			} catch (InterruptedException e) {
				log.debug("I was Interrupted");
			}
		}
	}

	/**
	 * Wraps a command in quotes to protect spaces if the OS is Windows.
	 * @param cmdIn
	 *            Original command.
	 * @return Wrapped command.
	 */
	private String checkWindowsCommand(final String cmdIn) {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win") == false) {
			return cmdIn;
		}
		String result = "\"" + cmdIn + "\"";
		return result;
	}

	/**
	 * Cleanup after the command has finished executing.
	 */
	public final void finish() {
		process.destroy();

		log.debug("Waiting for threads");
		try {
			Thread.sleep(waitInMillSecs);
		} catch (InterruptedException e) {
			log.debug("Sleeping...");
		}
		log.debug("Ending threads");
		errPr.setQuit(true);
		errThrd.interrupt();
		stoutPr.setQuit(true);
		stoutThrd.interrupt();
		exchange.setQuit(true);
		exchangeThrd.interrupt();
	}

	/**
	 * @return the args
	 */
	public final List<String> getArgs() {
		return args;
	}

	/**
	 * @return the cmd
	 */
	public final String getCmd() {
		return cmd;
	}

	/**
	 * @return the commandQ
	 */
	public final BlockingQueue<QMessage> getCommandQ() {
		return commandQ;
	}

	/**
	 * @return the env
	 */
	public final Map<String, String> getEnv() {
		return env;
	}

	/**
	 * Get the error from the command execution.
	 * @return The current error.
	 */
	public final String getError() {
		return errPr.getOutput();
	}

	/**
	 * @return the exitValue
	 */
	public final int getExitValue() {
		return exitValue;
	}

	/**
	 * @return the listenerWaitInterval
	 */
	public final int getListenerWaitInterval() {
		return listenerWaitInterval;
	}

	/**
	 * Get standard output from the command execution.
	 * @return The current standard output.
	 */
	public final String getOutput() {
		return stoutPr.getOutput();
	}

	/**
	 * @return the responseQ
	 */
	public final BlockingQueue<QMessage> getResponseQ() {
		return responseQ;
	}

	/**
	 * @return the waitInMillSecs
	 */
	public final int getWaitInMillSecs() {
		return waitInMillSecs;
	}

	/**
	 * @return the workDir
	 */
	public final String getWorkDir() {
		return workDir;
	}

	/**
	 * @param cmd the command to set
	 */
	public final void setCmd(final String cmd) {
		this.cmd = checkWindowsCommand(cmd);
	}

	/**
	 * @param workDir
	 *            the workDir to set
	 */
	public final void setWorkDir(final String workDir) {
		this.workDir = workDir;
	}

	/**
	 * Start the execution of the command.
	 * @throws IOException
	 *             if the command fails to start.
	 */
	public final void startExecute() throws IOException {
		String[] executeLine = assemble();
		ProcessBuilder pb = new ProcessBuilder(executeLine);
		if (workDir != null) {
			File workDirF = new File(workDir);
			if (workDirF.exists() == false) {
				throw new IOException("Directory \"" + workDir
						+ "\" does not exist");
			}
			if (workDirF.isDirectory() == false) {
				throw new IOException("Directory \"" + workDir
						+ "\" is not a directory");
			}
			log.debug("Setting the working directory to \""
					+ workDirF.getAbsolutePath() + "\"");
			pb.directory(workDirF);
		}
		pb.environment().putAll(env);

		log.debug("Starting process");
		process = pb.start();
		log.debug("Creating threads");
		errPr = new ProcessResponse(Level.ERROR, process.getErrorStream(),
				listenerWaitInterval, processName);
		stoutPr = new ProcessResponse(Level.DEBUG, process.getInputStream(),
				listenerWaitInterval, processName);
		exchange = new StdInExchange(commandQ, dispResponseFile,
				forceResponseFile, responseQ, process.getOutputStream(),
				waitInMillSecs);
		stoutPr.addObserver(exchange);
		errThrd = new Thread(errPr);
		stoutThrd = new Thread(stoutPr);
		exchangeThrd = new Thread(exchange);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();
		exchangeThrd.start();
	}
}
