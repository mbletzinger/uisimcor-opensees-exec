package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * Listener thread to listen to output from a Process execution.
 * @author Michael Bletzinger
 */
public class ProcessResponse extends Observable implements AbortableI {
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
	private String output = "";
	/**
	 * Name of the process. Used as a label for logging messages.
	 */
	private final String processName;
	/**
	 * Stream that we are listening to.
	 */
	private final InputStream strm;

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
	 */
	public ProcessResponse(final Level level, final InputStream strm,
			final int millSecWait, final String processName) {
		super();
		this.level = level;
		this.strm = strm;
		this.millSecWait = millSecWait;
		this.processName = processName;
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
	public final String getOutput() {
		return output;
	}

	@Override
	public final void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
		while (isQuit() == false) {
			try {
				String cbuf;
				if (reader.ready()) {
					cbuf = reader.readLine();
//					log.debug("read \"" + cbuf + "\"");
					if(cbuf.contains("#:")) {
//						log.debug("Time to notify");
						setChanged();
						notifyObservers();
					}
					writeLog(cbuf);
					output += cbuf + "\n";
				}
			} catch (IOException e) {
				log.debug("Stream for \"" + processName + "\" has closed because ",e);
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
			log.debug("Could not close stream for \"" + processName + "\" because ",e);
		}
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
