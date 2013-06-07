package org.nees.illinois.uisimcor.fem_executor.config.dao;

/**
 * Class which contains the file names to look for when scanning the
 * configuration directory for templates.
 * @author Michael Bletzinger
 */
public class TemplateDao {
	/**
	 * Template file for initializing an FEM analysis.
	 */
	private final String initTemplateFile;
	/**
	 * Template file to execute a step in the FEM process.
	 */
	private final String stepTemplateFile;

	/**
	 * @param stepTemplateFile
	 *            Template file to execute a step in the FEM process.
	 * @param initTemplateFile
	 *            Template file for initializing an FEM analysis.
	 */
	public TemplateDao(final String stepTemplateFile, final String initTemplateFile) {
		this.stepTemplateFile = stepTemplateFile;
		this.initTemplateFile = initTemplateFile;
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
