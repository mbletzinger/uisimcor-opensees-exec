package org.nees.illinois.uisimcor.fem_executor.output;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import name.pachler.nio.file.WatchEvent;

import org.apache.commons.io.FileUtils;
import org.nees.illinois.uisimcor.fem_executor.process.AbortableI;
import org.nees.illinois.uisimcor.fem_executor.utils.OutputFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which monitors if OpenSees has gotten around to creating an output file
 * with its node recorders. It then reads the file and deletes it.
 * @author Michael Bletzinger
 */
public class OutputFileMonitor implements AbortableI {
	/**
	 * Quit flag.
	 */
	private volatile boolean quit;

	/**
	 * @return the eventQ
	 */
	public final BlockingQueue<WatchEvent<?>> getEventQ() {
		return eventQ;
	}

	/**
	 * Operational states for the file monitor.
	 * @author Michael Bletzinger
	 */
	public enum OfmStates {
		/**
		 * Waiting to start monitoring.
		 */
		Idle,
		/**
		 * Start monitoring.
		 */
		Starting,
		/**
		 * Waiting the heuristic delay.
		 */
		DelayWait,
		/**
		 * Wait for File creation.
		 */
		CheckFileCreation,
		/**
		 * Wait for no more modifications.
		 */
		CheckingForMods,
		/**
		 * Read the file.
		 */
		ReadingOutput,
		/**
		 * Return the output.
		 */
		SendOutput
	};

	/**
	 * Current state of monitor.
	 */
	private OfmStates state;
	/**
	 * File to monitor.
	 */
	private final String file;

	/**
	 * Logger.
	 **/
	private final Logger log = LoggerFactory.getLogger(OutputFileMonitor.class);

	/**
	 * The new response.
	 */
	private String response;
	/**
	 * Queue for the watcher service to send events.
	 */
	private final BlockingQueue<WatchEvent<?>> eventQ = new LinkedBlockingQueue<WatchEvent<?>>();

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
	 * Read the output files by skipping lineCount lines and then reading the
	 * last line for both force and displacement files.
	 * @throws OutputFileException
	 *             thrown when problems reading the output file occurs.
	 * @return true if the output was read.
	 */
	public final boolean readOutput() throws OutputFileException {
		File fileF = new File(file);
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
		if (contents.isEmpty()) {
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

	@Override
	public void run() {
	}

	@Override
	public final synchronized boolean isQuit() {
		return quit;
	}

	@Override
	public final synchronized void setQuit(final boolean quit) {
		this.quit = quit;

	}

}
