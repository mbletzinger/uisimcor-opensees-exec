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
	 * @param program
	 *            FEM program type.
	 * @param executablePath
	 *            Path to executable.
	 * @param templateDao
	 *            template file names.
	 */
	public ProgramDao(final String executablePath, final FemProgramType program,
			final TemplateDao templateDao) {
		this.executablePath = executablePath;
		this.program = program;
		this.templateDao = templateDao;
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
