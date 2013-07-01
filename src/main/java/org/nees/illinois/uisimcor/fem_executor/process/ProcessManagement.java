package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.response.OpenSeesErrorFilter;
import org.nees.illinois.uisimcor.fem_executor.response.ProcessResponse;
import org.nees.illinois.uisimcor.fem_executor.response.StepFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
/**
 * Class to wrap some management threads around the {@link ProcessBuilder
 * ProcessBuilder} and {@link Process Process}.
 * @author Michael Bletzinger
 */
public class ProcessManagement implements ProcessManagmentI {

	/**
	 * @return the errThrd
	 */
	public final Thread getErrThrd() {
		return errThrd;
	}

	/**
	 * @return the process
	 */
	public final Process getProcess() {
		return process;
	}

	/**
	 * @return the processName
	 */
	public final String getProcessName() {
		return processName;
	}

	/**
	 * @return the stoutThrd
	 */
	public final Thread getStoutThrd() {
		return stoutThrd;
	}

	/**
	 * Argument list for the command.
	 */
	private final List<String> args = new ArrayList<String>();
	/**
	 * Line command to execute.
	 */
	private String cmd;
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
	 * Interval for the {@link ProcessResponse ProcessResponse} threads to wait
	 * before reading content.
	 */
	private final int listenerWaitInterval = 100;
	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory
			.getLogger(ProcessManagementWithStdin.class);
	/**
	 * {@link Process Process} associated with the executing command.
	 */
	private Process process = null;
	/**
	 * Name used for log messages.
	 */
	private final String processName;
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
	 */
	public ProcessManagement(final String cmd, final String processName,
			final int waitInMillSecs) {
		this.cmd = checkWindowsCommand(cmd);
		this.processName = processName;
		this.waitInMillSecs = waitInMillSecs;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#abort()
	 */
	@Override
	public final void abort() {
		if (process == null) { // Obviously we are not running.
			return;
		}
		log.debug("Ending threads");
		errPr.setQuit(true);
		errThrd.interrupt();
		stoutPr.setQuit(true);
		stoutThrd.interrupt();
		process.destroy();
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#addArg(java.lang.String)
	 */
	@Override
	public final void addArg(final String arg) {
		args.add(arg);
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#addEnv(java.lang.String, java.lang.String)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#getArgs()
	 */
	@Override
	public final List<String> getArgs() {
		return args;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#getCmd()
	 */
	@Override
	public final String getCmd() {
		return cmd;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#getEnv()
	 */
	@Override
	public final Map<String, String> getEnv() {
		return env;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#getErrPr()
	 */
	@Override
	public final ProcessResponse getErrPr() {
		return errPr;
	}

	/**
	 * @return the listenerWaitInterval
	 */
	public final int getListenerWaitInterval() {
		return listenerWaitInterval;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#getStoutPr()
	 */
	@Override
	public final ProcessResponse getStoutPr() {
		return stoutPr;
	}

	/**
	 * @return the waitInMillSecs
	 */
	public final int getWaitInMillSecs() {
		return waitInMillSecs;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#getWorkDir()
	 */
	@Override
	public final String getWorkDir() {
		return workDir;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#hasExited()
	 */
	@Override
	public final boolean hasExited() {
		if (process == null) {
			return true;
		}
		try {
			process.exitValue();
		} catch (IllegalThreadStateException e) {
			log.debug("Still running.");
			return false;
		}
		return true;
	}

	/**
	 * @param cmd
	 *            the command to set
	 */
	public final void setCmd(final String cmd) {
		this.cmd = checkWindowsCommand(cmd);
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#setWorkDir(java.lang.String)
	 */
	@Override
	public final void setWorkDir(final String workDir) {
		this.workDir = workDir;
	}

	/* (non-Javadoc)
	 * @see org.nees.illinois.uisimcor.fem_executor.process.ProcessManagmentI#startExecute()
	 */
	@Override
	public final void startExecute() throws IOException {
		String[] executeLine = assemble();
		log.debug("Start execution in " + workDir);
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
				listenerWaitInterval, processName, new OpenSeesErrorFilter());
		stoutPr = new ProcessResponse(Level.DEBUG, process.getInputStream(),
				listenerWaitInterval, processName, new StepFilter());
		errThrd = new Thread(errPr);
		stoutThrd = new Thread(stoutPr);
		log.debug("Starting threads");
		errThrd.start();
		stoutThrd.start();
	}

}
