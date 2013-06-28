package org.nees.illinois.uisimcor.fem_executor.config.dao;

/**
 * Class which contains the file names to look for when scanning the
 * configuration directory for templates.
 * @author Michael Bletzinger
 */
public class TemplateDao {
	/**
	 * @return the runTemplateFile
	 */
	public final String getRunTemplateFile() {
		return runTemplateFile;
	}

	/**
	 * Template file for initializing an FEM analysis.
	 */
	private final String initTemplateFile;
	/**
	 * Template file to execute a step in the FEM process.
	 */
	private final String stepTemplateFile;

	/**
	 * Template file to run a one step analysis.
	 */
	private final String runTemplateFile;

	/**
	 * @param stepTemplateFile
	 *            Template file to execute a step in the FEM process.
	 * @param initTemplateFile
	 *            Template file for initializing an FEM analysis.
	 * @param runTemplateFile
	 *            Template file for doing a one step analysis.
	 */
	public TemplateDao(final String stepTemplateFile,
			final String initTemplateFile, final String runTemplateFile) {
		this.stepTemplateFile = stepTemplateFile;
		this.initTemplateFile = initTemplateFile;
		this.runTemplateFile = runTemplateFile;
	}

	/**
	 * @return the initialization template filename.
	 */
	public final String getInitTemplateFile() {
		return initTemplateFile;
	}

	/**
	 * @return the step execution template filename.
	 */
	public final String getStepTemplateFile() {
		return stepTemplateFile;
	}

}
