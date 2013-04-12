package org.nees.illinois.uisimcor.fem_executor.process;

/**
 * Class to execute FEM programs
 * 
 * @author Michael Bletzinger
 */
public class FemExecutor {
	/**
	 * Line command
	 */
	private String command;
	/**
	 * Filename as first argument.
	 */
	private String filename;
	/**
	 * {@link ProcessManagement Wrapper} around command line executor.
	 */
	private ProcessManagement pm;
	/**
	 * Wait interval for checking thread for done.
	 */
	private int waitInMillisecs = 2000;

	/**
	 * @param command
	 * Line command
	 * @param filename
	 * Filename as first argument.
	 */
	public FemExecutor(String command, String filename) {
		this.command = command;
		this.filename = filename;
	}

	/**
	 * Create the {@link ProcessManagement ProcessManagement} instance and
	 * execute it.
	 */
	public void execute() {
		pm = new ProcessManagement(command, waitInMillisecs);
		pm.addArg(filename);
		pm.execute();
	}
	public static void main(String[] args) {
		String os = System.getProperty("os.name");
		FemExecutor fem;
		if(os.contains("Window")) {
		fem = new FemExecutor("dir", "/n");
		} else {
			fem = new FemExecutor("ls", "-l");	
		}
		fem.execute();
		System.out.print("Output: \"" + fem.getPm().getOutput() + "\"");
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
	public final void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public final void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @param waitInMillisecs
	 *            the waitInMillisecs to set
	 */
	public final void setWaitInMillisecs(int waitInMillisecs) {
		this.waitInMillisecs = waitInMillisecs;
	}
}
