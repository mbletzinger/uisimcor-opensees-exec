/**
 *
 */
package org.nees.illinois.uisimcor.fem_executor.config.dao;

import org.nees.illinois.uisimcor.fem_executor.config.types.FemProgramType;

/**
 * Configuration parameters for an FEM program.
 * @author Michael Bletzinger
 */
public class ProgramDao {
	/**
	 * @return the stepRecordIndex
	 */
	public final int getStepRecordIndex() {
		return stepRecordIndex;
	}

	/**
	 * Path to executable.
	 */
	private final String executablePath;

	/**
	 * FEM program type.
	 */
	private final FemProgramType program;
	/**
	 * template file names.
	 */
	private final TemplateDao templateDao;
	/**
	 * Record index to use for the response.
	 */
	private final int stepRecordIndex;

	/**
	 * @param executablePath
	 *            Path to executable.
	 * @param program
	 *            FEM program type.
	 * @param templateDao
	 *            template file names.
	 * @param stepRecordIndex
	 *            Record index to use for the response.
	 */
	public ProgramDao(final String executablePath,
			final FemProgramType program, final TemplateDao templateDao,
			final int stepRecordIndex) {
		this.executablePath = executablePath;
		this.program = program;
		this.templateDao = templateDao;
		this.stepRecordIndex = stepRecordIndex;
	}

	/**
	 * @return the executablePath
	 */
	public final String getExecutablePath() {
		return executablePath;
	}

	/**
	 * @return the program
	 */
	public final FemProgramType getProgram() {
		return program;
	}

	/**
	 * @return the templateDao
	 */
	public final TemplateDao getTemplateDao() {
		return templateDao;
	}

}
