package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.nees.illinois.uisimcor.fem_executor.response.ProcessResponse;
/**
 * Interface to wrap some management threads around the {@link ProcessBuilder
 * ProcessBuilder} and {@link Process Process}.
* @author Michael Bletzinger
 */

public interface ProcessManagmentI {

	/**
	 * Cleanup after the command has finished executing.
	 */
	void abort();

	/**
	 * Add an argument to the command.
	 * @param arg
	 *            Argument string.
	 */
	void addArg(String arg);

	/**
	 * Add a variable to the process environment.
	 * @param name
	 *            Name of the variable.
	 * @param value
	 *            Value string.
	 */
	void addEnv(String name, String value);

	/**
	 * @return the command arguments.
	 */
	List<String> getArgs();

	/**
	 * @return the command.
	 */
	String getCmd();

	/**
	 * @return the command environment.
	 */
	Map<String, String> getEnv();

	/**
	 * @return the STDERR response.
	 */
	ProcessResponse getErrPr();

	/**
	 * @return the STDOUT response.
	 */
	ProcessResponse getStoutPr();

	/**
	 * @return the workDir
	 */
	String getWorkDir();

	/**
	 * @return True if the process has stopped running.
	 */
	boolean hasExited();

	/**
	 * @param workDir
	 *            the workDir to set
	 */
	void setWorkDir(String workDir);

	/**
	 * Start the execution of the command.
	 * @throws IOException
	 *             if the command fails to start.
	 */
	void startExecute() throws IOException;

}
