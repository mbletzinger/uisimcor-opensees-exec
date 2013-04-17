package org.nees.illinois.uisimcor.fem_executor.process;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to execute FEM programs
 * 
 * @author Michael Bletzinger
 */
public class FemExecutor {
	/**
	 * Test function to see if the executor works in the given OS environment.
	 * 
	 * @param args
	 *            ignored.
	 */
	public static void main(final String[] args) {
		String os = System.getProperty("os.name");
		FemExecutor fem;
		if (os.contains("Window")) {
			fem = new FemExecutor("echo", "Is there anybody out there?");
		} else {
			fem = new FemExecutor("ls", "-l");
		}
		fem.startCmd();
		while(fem.getPm().isDone() == false) {
			try {
				final int waitTime = 500;
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				@SuppressWarnings("unused")
				int dumb = 0;
			}
		}
		System.out.print("Output: \"" + fem.getPm().getOutput() + "\"");
	}

	/**
	 * Line command
	 */
	private String command;
	/**
	 * default wait.
	 */
	private final int defaultWait = 2000;
	/**
	 * Filename as first argument.
	 */
	private String filename;
	/**
	 * Logger
	 **/
	private final Logger log = LoggerFactory.getLogger(FemExecutor.class);
	/**
	 * {@link ProcessManagement Wrapper} around command line executor.
	 */
	private ProcessManagement pm;

	/**
	 * Wait interval for checking thread for done. Default is 2 seconds
	 */
	private int waitInMillisecs = defaultWait;

	/**
	 * @param command
	 *            Line command
	 * @param filename
	 *            Filename as first argument.
	 */
	public FemExecutor(final String command, final String filename) {
		this.command = command;
		this.filename = filename;
	}

	/**
	 * @return the command
	 */
	public final String getCommand() {
		return command;
	}

	/**
	 * @return the filename
	 */
	public final String getFilename() {
		return filename;
	}

	/**
	 * @return the pm
	 */
	public final ProcessManagement getPm() {
		return pm;
	}

	/**
	 * @return the waitInMillisecs
	 */
	public final int getWaitInMillisecs() {
		return waitInMillisecs;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public final void setCommand(final String command) {
		this.command = command;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public final void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * @param waitInMillisecs
	 *            the waitInMillisecs to set
	 */
	public final void setWaitInMillisecs(final int waitInMillisecs) {
		this.waitInMillisecs = waitInMillisecs;
	}

	/**
	 * Create the {@link ProcessManagement ProcessManagement} instance and start
	 * it.
	 * @return the {@link ProcessManagement ProcessManagement} instance
	 */
	public final ProcessManagement startCmd() {
		pm = new ProcessManagement(command, waitInMillisecs);
		pm.addArg(filename);
		try {
			pm.startExecute();
			return pm;
		} catch (IOException e) {
			log.debug(command + " falied to start", e);
		}
		return null;
	}
}
