package org.nees.illinois.uisimcor.fem_executor.response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Observable;

import org.nees.illinois.uisimcor.fem_executor.process.AbortableI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Listener thread to listen to output from a Process execution.
 * @author Michael Bletzinger
 */
public class ProcessResponse extends Observable implements AbortableI {
	/**
	 * @param output
	 *            the output to set
	 */
	public final synchronized void appendOutput(final String output) {
		this.output += output;
	}

	/**
	 * Quit flag for {@link AbortableI abort} interface.
	 */
	private volatile boolean quit = false;
	/**
	 * Debugging level that is used to print output.
	 */
	private final Level level;
	/**
	 * Logger.
	 */
	private final Logger log = LoggerFactory.getLogger(ProcessResponse.class);
	/**
	 * Interval to wait between read requests.
	 */
	private final int millSecWait;
	/**
	 * Output accumulator.
	 */
	private volatile String output = "";
	/**
	 * Name of the process. Used as a label for logging messages.
	 */
	private final String processName;
	/**
	 * Stream that we are listening to.
	 */
	private final InputStream strm;
	/**
	 * Filter for observations.
	 */
	private final ResponseFilterI filter;

	/**
	 * Constructor.
	 * @param level
	 *            Debugging level that is used to print output.
	 * @param strm
	 *            Stream that we are listening to.
	 * @param millSecWait
	 *            Interval to wait between read requests.
	 * @param processName
	 *            Name of the process. Used as a label for logging messages.
	 * @param filter
	 *            Filter for observations.  Use null if no filter is needed.
	 */
	public ProcessResponse(final Level level, final InputStream strm,
			final int millSecWait, final String processName,
			final ResponseFilterI filter) {
		super();
		this.level = level;
		this.strm = strm;
		this.millSecWait = millSecWait;
		this.processName = processName;
		this.filter = filter;
	}

	/**
	 * @return the level
	 */
	public final Level getLevel() {
		return level;
	}

	/**
	 * @return the output
	 */
	public final synchronized String getOutput() {
		return output;
	}

	@Override
	public final void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
		log.info("Starting response monitor");
		while (isQuit() == false) {
			try {
				String cbuf;
				if (reader.ready()) {
					cbuf = reader.readLine();
					// log.debug("read \"" + cbuf + "\"");
					if (filter != null && filter.filter(cbuf)) {
						log.debug("Notifying about \"" +filter.get() + "\" for line \"" + cbuf + "\"" );
						setChanged();
						notifyObservers(filter.get());
					}
					writeLog(cbuf);
					appendOutput(cbuf + "\n");
				}
			} catch (IOException e) {
				log.debug("Stream for \"" + processName
						+ "\" has closed because ", e);
				setQuit(true);
			}
			try {
				Thread.sleep(millSecWait);
			} catch (InterruptedException e) {
				log.debug("Checking quit flag");
			}
		}
		try {
			reader.close();
		} catch (IOException e) {
			log.debug("Could not close stream for \"" + processName
					+ "\" because ", e);
		}
		log.info("Closing down response monitor");
	}

	/**
	 * Writes line to the log specified by the logging level.
	 * @param line
	 *            Content to write.
	 */
	private void writeLog(final String line) {
		if (level.equals(Level.ERROR)) {
			log.error("[" + processName + "] " + line);
		}
		if (level.equals(Level.INFO)) {
			log.info("[" + processName + "] " + line);
		}
		if (level.equals(Level.DEBUG)) {
			log.debug("[" + processName + "] " + line);
		}
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
