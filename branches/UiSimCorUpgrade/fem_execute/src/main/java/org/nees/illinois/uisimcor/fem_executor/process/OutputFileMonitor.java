package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which slurps the entire output file and determines if a new response
 * has been added.
 * @author Michael Bletzinger
 */
public class OutputFileMonitor {
	/**
	 * File to monitor.
	 */
	private final String file;

	/**
	 * Number of lines that have been generated so far.
	 */
	private int lineCount = 0;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(OutputFileMonitor.class);

	/**
	 * Flag indicating that there is a new response.
	 */
	private boolean newResponse;

	/**
	 * The new response.
	 */
	private String response;

	/**
	 * @param file
	 *            File to monitor.
	 */
	public OutputFileMonitor(final String file) {
		this.file = file;
	}

	/**
	 * @return the file
	 */
	public final String getFile() {
		return file;
	}

	/**
	 * @return the lineCount
	 */
	public final int getLineCount() {
		return lineCount;
	}

	/**
	 * @return the response
	 */
	public final String getResponse() {
		return response;
	}

	/**
	 * Read the output files by skipping lineCount lines and then reading the
	 * last line for both force and displacement files.
	 * @throws OutputFileException
	 *             thrown when problems reading the output file occurs.
	 */
	public final void readOutput() throws OutputFileException {
		File fileF = new File(file);
		newResponse = false;
//		log.debug("Checking \"" + file + "\"");
		if (fileF.exists() == false) {
			throw new OutputFileException("File \"" + fileF.getAbsolutePath()
					+ "\" is missing.");
		}
		List<String> contents;
		try {
			contents = FileUtils.readLines(fileF);
		} catch (IOException e) {
			throw new OutputFileException("Could not read file \"" + file
					+ "\" because", e);
		}
		log.debug("Read " + contents + " for " + lineCount);
		response = contents.get(lineCount);
		log.debug("New output \"" + response + "\"");
		lineCount++;
	}

	/**
	 * @return the newResponse
	 */
	public final boolean isNewResponse() {
		return newResponse;
	}
}
