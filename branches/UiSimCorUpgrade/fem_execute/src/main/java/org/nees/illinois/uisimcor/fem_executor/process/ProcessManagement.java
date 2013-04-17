package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Class to wrap some management threads around the {@link ProcessBuilder
 * ProcessBuilder} and {@link Process Process}.
 * 
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
	private final String cmd;

	/**
	 * Environment variables for the command.
	 */
	private final Map<String, String> env = new HashMap<String, String>();

	/**
	 * Listener for error messages.
	 */
	private ProcessResponse errPr;

	/**
	 * The value returned by the executed command.
	 */
	private int exitValue;
	/**
	 * Interval for the {@link ProcessResponse ProcessResponse} threads to wait
	 * before reading content.
	 */
	private final int listenerWaitInterval = 100;
	/**
	 * Logger
	 */
	private final Logger log = LoggerFactory.getLogger(ProcessManagement.class);
	/**
	 * {@link Process Process} associated with the executing command.
	 */
	private Process process = null;
	/**
	 * Listener for output.
	 */
	private ProcessResponse stoutPr;
	/**
	 * Interval to wait between thread checks.
	 */
	private final int waitInMillSecs;

	/**
	 * Constructor
	 * 
	 * @param cmd
	 *            Line command to execute.
	 * @param waitInMilliSec
	 *            Argument list for the command.
	 */
	public ProcessManagement(final String cmd, final int waitInMilliSec) {
		super();
		this.cmd = cmd;
		this.waitInMillSecs = waitInMilliSec;
	}

	/**
	 * Add an argument to the command.
	 * 
	 * @param arg
	 *            Argument string.
	 */
	public final void addArg(final String arg) {
		args.add(arg);
	}

	/**
	 * Add a variable to the process environment.
	 * 
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
	 * 
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
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			log.debug("I was Interrupted");
		}
	}

	/**
	 * Cleanup after the command has finished executing.
	 */
	public final void finish() {
		log.debug("Waiting for threads");
		try {
			Thread.sleep(waitInMillSecs);
		} catch (InterruptedException e) {
		}
		log.debug("Ending threads");
		errPr.setDone(true);
		stoutPr.setDone(true);
	}

	/**
	 * @return the cmd
	 */
	public final String getCmd() {
		return cmd;
	}

	/**
	 * Get the error from the command execution.
	 * 
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
	 * Get standard output from the command execution.
	 * 
	 * @return The current standard output.
	 */
	public final String getOutput() {
		return stoutPr.getOutput();
	}

	/**
	 * @return the waitInMillSecs
	 */
	public final int getWaitInMillSecs() {
		return waitInMillSecs;
	}

	/**
	 * Check to see if the command has finished executing.
	 * 
	 * @return True if the command has finished.
	 */
	public final boolean isDone() {
		if (process == null) {
			log.error("Command \"" + cmd + "\" has not started.");
			return true;
		}
		try {
			exitValue = process.exitValue();
		} catch (IllegalThreadStateException e) {
			log.debug("Command \"" + cmd + "\" is still executing");
			return false;
		}
		return true;
	}

	/**
	 * Start the execution of the command.
	 * 
	 * @throws IOException
	 *             if the command fails to start.
	 */
	public final void startExecute() throws IOException {
		String[] executeLine = assemble();
		ProcessBuilder pb = new ProcessBuilder(executeLine);
		pb.environment().putAll(env);

		log.debug("Starting process");
		process = pb.start();
		log.debug("Creating threads");
		errPr = new ProcessResponse(Level.ERROR, process.getErrorStream(),
				listenerWaitInterval, cmd);
		stoutPr = new ProcessResponse(Level.DEBUG, process.getInputStream(),
				listenerWaitInterval, cmd);
		Thread errThrd = new Thread(errPr);
		Thread stoutThrd = new Thread(stoutPr);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();

	}
}
