/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.process;

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
	private final OutputFileParser parser = new OutputFileParser();

	/**
	 * File to be parsed;
	 */
	private final String textFile;

	/**
	 * Constructor
	 * 
	 * @param textFile
	 *            File to parse.
	 */
	public OutputFileParsingTask(final String textFile) {
		this.textFile = textFile;
	}

	/**
	 * Get the data from the parsing task
	 * 
	 * @return Returns the data or null if the task was not completed.
	 */
	public final double[][] getData() {
		if (parser.isEmpty()) {
			return null;
		}
		return parser.getArchive().getData();
	}

	/**
	 * @return the done
	 */
	public final synchronized boolean isDone() {
		return done;
	}

	@Override
	public final void run() {
		done = false;
		parser.parseDataFile(textFile);
		done = true;
	}

	/**
	 * @param done
	 *            the done to set
	 */
	public synchronized final void setDone(boolean done) {
		this.done = done;
	}
}
