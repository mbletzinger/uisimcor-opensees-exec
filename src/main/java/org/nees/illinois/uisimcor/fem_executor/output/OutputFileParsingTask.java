/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.output;

import java.util.List;


/**
 * @author Michael Bletzinger
 */
public class OutputFileParsingTask implements Runnable {
	/**
	 * Control flag which tells the thread to finish.
	 */
	private boolean done;

	/**
	 * The parser.
	 */
	private final OutputFileParser parser;

	/**
	 * File to be parsed.
	 */
	private final String textFile;

	/**
	 * Constructor.
	 * @param textFile
	 *            File to parse.
	 */
	public OutputFileParsingTask(final String textFile) {
		this.textFile = textFile;
		this.parser = new OutputFileParser();
	}

	/**
	 * Get the data from the parsing task.
	 * @return Returns the data or null if the task was not completed.
	 */
	public final List<Double> getData() {
		if (parser.isEmpty()) {
			return null;
		}
		return parser.getArchive();
	}

	/**
	 * @return the done
	 */
	public final synchronized boolean isDone() {
		return done;
	}

	@Override
	public final void run() {
		setDone(false);
		parser.parseDataFile(textFile);
		setDone(true);
	}

	/**
	 * @param done
	 *            the done to set
	 */
	public final synchronized void setDone(final boolean done) {
		this.done = done;
	}
}
