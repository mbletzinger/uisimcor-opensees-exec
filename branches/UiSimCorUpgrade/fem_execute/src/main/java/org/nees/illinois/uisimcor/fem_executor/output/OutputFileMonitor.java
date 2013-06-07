package org.nees.illinois.uisimcor.fem_executor.output;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which monitors if OpenSees has gotten around to creating an output file
 * with its node recorders. It then reads the file and deletes it.
 * @author Michael Bletzinger
 */
public class OutputFileMonitor {
	/**
	 * File to monitor.
	 */
	private final String file;

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
		try {
			clean();
		} catch (OutputFileException e) {
			log.warn("File \"" + file + "\" could not be deleted because", e);
		}
	}

	/**
	 * @return the file
	 */
	public final String getFile() {
		return file;
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
	 * @return true if the output was read.
	 */
	public final boolean readOutput() throws OutputFileException {
		File fileF = new File(file);
		newResponse = false;
		// log.debug("Checking \"" + file + "\"");
		if (fileF.exists() == false) {
			return false;
		}
		List<String> contents;
		try {

			contents = FileUtils.readLines(fileF);
		} catch (IOException e) {
			throw new OutputFileException("Could not read file \"" + file
					+ "\" because", e);
		}
		if(contents.isEmpty()) {
			log.debug("\"" + file + "\" s got nuttin'");
			return false;
		}
		if (contents.size() > 1) {
			log.warn("File \"" + file + "\" has to many lines");
		}
		response = contents.get(0);
		log.debug("New output \"" + response + "\"");
		clean();
		return true;
	}

	/**
	 * Attempts to delete the file.
	 * @throws OutputFileException
	 *             If the file cannot be deleted.
	 */
	private void clean() throws OutputFileException {
		File fileF = new File(file);
		if (fileF.exists()) {
			boolean done = fileF.delete();
			if (done == false) {
				throw new OutputFileException("File \"" + file
						+ "\" could not be deleted");
			}
		}

	}

	/**
	 * @return the newResponse
	 */
	public final boolean isNewResponse() {
		return newResponse;
	}
}
